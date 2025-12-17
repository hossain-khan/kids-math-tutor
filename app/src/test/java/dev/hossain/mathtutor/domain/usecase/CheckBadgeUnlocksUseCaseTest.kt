package dev.hossain.mathtutor.domain.usecase

import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.repository.BadgeProgress
import dev.hossain.mathtutor.domain.repository.BadgeRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class CheckBadgeUnlocksUseCaseTest {
    private lateinit var fakeBadgeRepository: FakeBadgeRepository
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var fakeStreakRepository: FakeStreakRepository
    private lateinit var useCase: CheckBadgeUnlocksUseCase

    @Before
    fun setup() {
        fakeBadgeRepository = FakeBadgeRepository()
        fakeSessionRepository = FakeSessionRepository()
        fakeStreakRepository = FakeStreakRepository()
        useCase = CheckBadgeUnlocksUseCase(fakeBadgeRepository, fakeSessionRepository, fakeStreakRepository)
    }

    @Test
    fun `checkAndUnlockBadges returns empty list when all badges are unlocked`() =
        runTest {
            val unlockedBadge = createBadge("badge1", unlockedAt = Instant.now())
            fakeBadgeRepository.allBadges = listOf(unlockedBadge)

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertTrue(newlyUnlocked.isEmpty())
            assertEquals(0, fakeBadgeRepository.unlockCalls.size)
        }

    @Test
    fun `checkAndUnlockBadges returns empty list when no badges meet requirements`() =
        runTest {
            val lockedBadge = createBadge("badge1", requirement = BadgeRequirement.ProblemCount(100))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)
            fakeSessionRepository.overallStats = SessionStats(totalProblems = 10, correctCount = 8, accuracy = 80f, sessionCount = 1)

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertTrue(newlyUnlocked.isEmpty())
            assertEquals(0, fakeBadgeRepository.unlockCalls.size)
        }

    @Test
    fun `checkAndUnlockBadges unlocks badge when ProblemCount requirement is met`() =
        runTest {
            val lockedBadge = createBadge("badge1", requirement = BadgeRequirement.ProblemCount(25))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)
            fakeSessionRepository.overallStats = SessionStats(totalProblems = 30, correctCount = 24, accuracy = 80f, sessionCount = 3)

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertEquals(1, newlyUnlocked.size)
            assertEquals("badge1", newlyUnlocked[0].id)
            assertEquals(1, fakeBadgeRepository.unlockCalls.size)
            assertEquals("badge1", fakeBadgeRepository.unlockCalls[0])
        }

    @Test
    fun `checkAndUnlockBadges unlocks badge when OperationCount requirement is met`() =
        runTest {
            val lockedBadge =
                createBadge(
                    "addition_badge",
                    requirement = BadgeRequirement.OperationCount(MathOperation.ADDITION, 50),
                )
            fakeBadgeRepository.allBadges = listOf(lockedBadge)
            fakeSessionRepository.statsByOperation[MathOperation.ADDITION] =
                SessionStats(totalProblems = 60, correctCount = 50, accuracy = 83.3f, sessionCount = 6)

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertEquals(1, newlyUnlocked.size)
            assertEquals("addition_badge", newlyUnlocked[0].id)
        }

    @Test
    fun `checkAndUnlockBadges does not unlock when OperationCount requirement is not met`() =
        runTest {
            val lockedBadge =
                createBadge(
                    "subtraction_badge",
                    requirement = BadgeRequirement.OperationCount(MathOperation.SUBTRACTION, 50),
                )
            fakeBadgeRepository.allBadges = listOf(lockedBadge)
            fakeSessionRepository.statsByOperation[MathOperation.SUBTRACTION] =
                SessionStats(totalProblems = 30, correctCount = 25, accuracy = 83.3f, sessionCount = 3)

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertTrue(newlyUnlocked.isEmpty())
        }

    @Test
    fun `checkAndUnlockBadges does not unlock ConsecutiveCorrect badges (not implemented)`() =
        runTest {
            val lockedBadge = createBadge("streak_badge", requirement = BadgeRequirement.ConsecutiveCorrect(5))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertTrue(newlyUnlocked.isEmpty())
        }

    @Test
    fun `checkAndUnlockBadges unlocks badge when SessionAccuracy requirement is met`() =
        runTest {
            val lockedBadge = createBadge("accuracy_badge", requirement = BadgeRequirement.SessionAccuracy(90f, 2))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)

            val session1 = createSessionEntity(1, accuracy = 95f)
            val session2 = createSessionEntity(2, accuracy = 92f)
            fakeSessionRepository.recentSessions = listOf(session1, session2)

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertEquals(1, newlyUnlocked.size)
            assertEquals("accuracy_badge", newlyUnlocked[0].id)
        }

    @Test
    fun `checkAndUnlockBadges does not unlock when SessionAccuracy requirement is not met`() =
        runTest {
            val lockedBadge = createBadge("accuracy_badge", requirement = BadgeRequirement.SessionAccuracy(90f, 2))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)

            val session1 = createSessionEntity(1, accuracy = 85f)
            val session2 = createSessionEntity(2, accuracy = 88f)
            fakeSessionRepository.recentSessions = listOf(session1, session2)

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertTrue(newlyUnlocked.isEmpty())
        }

    @Test
    fun `checkAndUnlockBadges does not unlock when not enough sessions exist`() =
        runTest {
            val lockedBadge = createBadge("accuracy_badge", requirement = BadgeRequirement.SessionAccuracy(90f, 3))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)

            val session1 = createSessionEntity(1, accuracy = 95f)
            val session2 = createSessionEntity(2, accuracy = 92f)
            fakeSessionRepository.recentSessions = listOf(session1, session2) // Only 2 sessions, need 3

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertTrue(newlyUnlocked.isEmpty())
        }

    @Test
    fun `checkAndUnlockBadges does not unlock DailyStreak badges (not implemented)`() =
        runTest {
            val lockedBadge = createBadge("streak_badge", requirement = BadgeRequirement.DailyStreak(7))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertTrue(newlyUnlocked.isEmpty())
        }

    @Test
    fun `checkAndUnlockBadges does not unlock ProblemSpeed badges (not implemented)`() =
        runTest {
            val lockedBadge = createBadge("speed_badge", requirement = BadgeRequirement.ProblemSpeed(3))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertTrue(newlyUnlocked.isEmpty())
        }

    @Test
    fun `checkAndUnlockBadges unlocks badge when MixedSessions requirement is met`() =
        runTest {
            val lockedBadge = createBadge("mixed_badge", requirement = BadgeRequirement.MixedSessions(10))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)

            val mixedSessions = List(12) { createSessionEntity(it.toLong(), operation = MathOperation.MIXED) }
            fakeSessionRepository.sessionsByOperation[MathOperation.MIXED] = mixedSessions

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertEquals(1, newlyUnlocked.size)
            assertEquals("mixed_badge", newlyUnlocked[0].id)
        }

    @Test
    fun `checkAndUnlockBadges unlocks badge when DailyStreak requirement is met`() =
        runTest {
            val lockedBadge = createBadge("streak_badge", requirement = BadgeRequirement.DailyStreak(7))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)
            fakeStreakRepository.currentStreak =
                DailyStreak(
                    currentStreak = 7,
                    longestStreak = 7,
                    lastPracticeDate = java.time.LocalDate.now(),
                    totalDaysPracticed = 7,
                )

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertEquals(1, newlyUnlocked.size)
            assertEquals("streak_badge", newlyUnlocked[0].id)
        }

    @Test
    fun `checkAndUnlockBadges does not unlock badge when DailyStreak requirement not met`() =
        runTest {
            val lockedBadge = createBadge("streak_badge", requirement = BadgeRequirement.DailyStreak(7))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)
            fakeStreakRepository.currentStreak =
                DailyStreak(
                    currentStreak = 5,
                    longestStreak = 10,
                    lastPracticeDate = java.time.LocalDate.now(),
                    totalDaysPracticed = 15,
                )

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertTrue(newlyUnlocked.isEmpty())
        }

    @Test
    fun `checkAndUnlockBadges does not unlock badge when no streak data exists`() =
        runTest {
            val lockedBadge = createBadge("streak_badge", requirement = BadgeRequirement.DailyStreak(3))
            fakeBadgeRepository.allBadges = listOf(lockedBadge)
            fakeStreakRepository.currentStreak = null

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertTrue(newlyUnlocked.isEmpty())
        }

    @Test
    fun `checkAndUnlockBadges unlocks multiple badges when multiple requirements are met`() =
        runTest {
            val badge1 = createBadge("badge1", requirement = BadgeRequirement.ProblemCount(10))
            val badge2 = createBadge("badge2", requirement = BadgeRequirement.ProblemCount(25))
            val badge3 = createBadge("badge3", requirement = BadgeRequirement.ProblemCount(100)) // Not met
            fakeBadgeRepository.allBadges = listOf(badge1, badge2, badge3)
            fakeSessionRepository.overallStats = SessionStats(totalProblems = 30, correctCount = 24, accuracy = 80f, sessionCount = 3)

            val newlyUnlocked = useCase.checkAndUnlockBadges()

            assertEquals(2, newlyUnlocked.size)
            assertTrue(newlyUnlocked.any { it.id == "badge1" })
            assertTrue(newlyUnlocked.any { it.id == "badge2" })
            assertEquals(2, fakeBadgeRepository.unlockCalls.size)
        }

    private fun createBadge(
        id: String,
        requirement: BadgeRequirement = BadgeRequirement.ProblemCount(10),
        unlockedAt: Instant? = null,
    ): Badge =
        Badge(
            id = id,
            name = "Test Badge",
            description = "Test description",
            icon = "🎯",
            category = BadgeCategory.GETTING_STARTED,
            requirement = requirement,
            unlockedAt = unlockedAt,
        )

    private fun createSessionEntity(
        id: Long,
        operation: MathOperation = MathOperation.ADDITION,
        accuracy: Float = 80f,
    ): PracticeSessionEntity =
        PracticeSessionEntity(
            id = id,
            operation = operation,
            totalProblems = 10,
            correctAnswers = (10 * accuracy / 100).toInt(),
            incorrectAnswers = 10 - (10 * accuracy / 100).toInt(),
            accuracy = accuracy,
            durationSeconds = 60,
            timestamp = Instant.now(),
            gradeLevel = 1,
        )

    /**
     * Fake implementation of BadgeRepository for testing.
     */
    private class FakeBadgeRepository : BadgeRepository {
        var allBadges: List<Badge> = emptyList()
        val unlockCalls = mutableListOf<String>()

        override fun getAllBadges(): Flow<List<Badge>> = flowOf(allBadges)

        override fun getRecentlyUnlockedBadges(limit: Int): Flow<List<Badge>> = flowOf(emptyList())

        override fun getBadgesByCategory(category: BadgeCategory): Flow<List<Badge>> = flowOf(emptyList())

        override fun getUnlockedBadges(): Flow<List<Badge>> = flowOf(emptyList())

        override fun getProgressSummary(): Flow<BadgeProgress> = flowOf(BadgeProgress(0, 0))

        override suspend fun unlockBadge(
            badgeId: String,
            unlockedAt: Instant,
        ) {
            unlockCalls.add(badgeId)
        }

        override suspend fun initializeBadges() {
            // Not needed for these tests
        }
    }

    /**
     * Fake implementation of SessionRepository for testing.
     */
    private class FakeSessionRepository : SessionRepository {
        var overallStats = SessionStats.EMPTY
        var statsByOperation = mutableMapOf<MathOperation, SessionStats>()
        var recentSessions = emptyList<PracticeSessionEntity>()
        var sessionsByOperation = mutableMapOf<MathOperation, List<PracticeSessionEntity>>()

        override suspend fun saveSession(
            session: PracticeSession,
            operation: MathOperation,
            durationSeconds: Long,
            gradeLevel: Int?,
        ): Long = 0

        override fun getAllSessions(): Flow<List<PracticeSessionEntity>> = flowOf(emptyList())

        override fun getRecentSessions(limit: Int): Flow<List<PracticeSessionEntity>> = flowOf(recentSessions)

        override fun getSessionsByOperation(operation: MathOperation): Flow<List<PracticeSessionEntity>> =
            flowOf(sessionsByOperation.getOrDefault(operation, emptyList()))

        override fun getOverallStats(): Flow<SessionStats> = flowOf(overallStats)

        override fun getStatsByOperation(operation: MathOperation): Flow<SessionStats> =
            flowOf(statsByOperation.getOrDefault(operation, SessionStats.EMPTY))

        override suspend fun clearAllSessions() {
            // Not needed for these tests
        }
    }

    /**
     * Fake implementation of StreakRepository for testing.
     */
    private class FakeStreakRepository : StreakRepository {
        var currentStreak: DailyStreak? = null

        override fun getStreak(): Flow<DailyStreak?> = flowOf(currentStreak)

        override suspend fun saveStreak(streak: DailyStreak) {
            currentStreak = streak
        }
    }
}
