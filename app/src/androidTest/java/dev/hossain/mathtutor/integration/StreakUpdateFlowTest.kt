package dev.hossain.mathtutor.integration

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.data.local.MathDatabase
import dev.hossain.mathtutor.data.repository.BadgeRepositoryImpl
import dev.hossain.mathtutor.data.repository.SessionRepositoryImpl
import dev.hossain.mathtutor.data.repository.StreakRepositoryImpl
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionAnswer
import dev.hossain.mathtutor.domain.repository.BadgeRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.repository.StreakRepository
import dev.hossain.mathtutor.domain.usecase.CheckBadgeUnlocksUseCase
import dev.hossain.mathtutor.domain.usecase.UpdateStreakUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

/**
 * Integration test for the Streak Update flow.
 *
 * This test verifies the end-to-end flow:
 * 1. Completing a practice session
 * 2. Updating streak based on practice date
 * 3. Triggering badge unlock check for streak badges
 * 4. Verifying streak badges are unlocked at appropriate milestones
 *
 * Uses real Room database (in-memory) and real repository implementations
 * to test streak tracking and badge unlock logic.
 */
@RunWith(AndroidJUnit4::class)
class StreakUpdateFlowTest {
    private lateinit var database: MathDatabase
    private lateinit var sessionRepository: SessionRepository
    private lateinit var badgeRepository: BadgeRepository
    private lateinit var streakRepository: StreakRepository
    private lateinit var updateStreakUseCase: UpdateStreakUseCase
    private lateinit var checkBadgeUnlocksUseCase: CheckBadgeUnlocksUseCase

    @Before
    fun setup() {
        // Create in-memory database for testing
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    MathDatabase::class.java,
                ).allowMainThreadQueries()
                .build()

        // Create fake analytics service
        val fakeAnalyticsService = FakeAnalyticsService()

        // Initialize repositories
        sessionRepository =
            SessionRepositoryImpl(
                sessionDao = database.sessionDao(),
                analyticsService = fakeAnalyticsService,
            )

        badgeRepository =
            BadgeRepositoryImpl(
                badgeDao = database.badgeDao(),
                analyticsService = fakeAnalyticsService,
            )

        streakRepository =
            StreakRepositoryImpl(
                streakDao = database.streakDao(),
            )

        // Initialize use cases
        updateStreakUseCase = UpdateStreakUseCase(streakRepository)

        val fakeGameRepository = FakeGameRepository()
        checkBadgeUnlocksUseCase =
            CheckBadgeUnlocksUseCase(
                badgeRepository = badgeRepository,
                sessionRepository = sessionRepository,
                streakRepository = streakRepository,
                gameRepository = fakeGameRepository,
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun firstPracticeSession_initializesStreakToOne() =
        runTest {
            // Given - No previous practice sessions (empty streak)
            val currentStreak = streakRepository.getStreak().first()
            assertThat(currentStreak).isNull()

            // When - Complete first practice session and update streak
            val today = LocalDate.now()
            val updatedStreak = updateStreakUseCase.updateStreak(today)

            // Then - Streak is initialized to 1
            assertThat(updatedStreak.currentStreak).isEqualTo(1)
            assertThat(updatedStreak.longestStreak).isEqualTo(1)
            assertThat(updatedStreak.lastPracticeDate).isEqualTo(today)
            assertThat(updatedStreak.totalDaysPracticed).isEqualTo(1)

            // Verify streak is saved to database
            val savedStreak = streakRepository.getStreak().first()
            assertThat(savedStreak).isNotNull()
            assertThat(savedStreak?.currentStreak).isEqualTo(1)
        }

    @Test
    fun consecutiveDayPractice_incrementsStreak() =
        runTest {
            // Given - Practice on Day 1
            val day1 = LocalDate.of(2024, 1, 1)
            updateStreakUseCase.updateStreak(day1)

            // When - Practice on Day 2 (consecutive day)
            val day2 = LocalDate.of(2024, 1, 2)
            val updatedStreak = updateStreakUseCase.updateStreak(day2)

            // Then - Streak increments to 2
            assertThat(updatedStreak.currentStreak).isEqualTo(2)
            assertThat(updatedStreak.longestStreak).isEqualTo(2)
            assertThat(updatedStreak.lastPracticeDate).isEqualTo(day2)
            assertThat(updatedStreak.totalDaysPracticed).isEqualTo(2)
        }

    @Test
    fun multiplePracticesSameDay_doesNotChangeStreak() =
        runTest {
            // Given - Practice on Day 1
            val day1 = LocalDate.of(2024, 1, 1)
            val firstStreak = updateStreakUseCase.updateStreak(day1)

            // When - Practice again on Day 1 (same day)
            val secondStreak = updateStreakUseCase.updateStreak(day1)

            // Then - Streak remains the same
            assertThat(secondStreak.currentStreak).isEqualTo(firstStreak.currentStreak)
            assertThat(secondStreak.longestStreak).isEqualTo(firstStreak.longestStreak)
            assertThat(secondStreak.totalDaysPracticed).isEqualTo(firstStreak.totalDaysPracticed)
        }

    @Test
    fun missedDay_resetsStreakToOne() =
        runTest {
            // Given - Build a 3-day streak
            updateStreakUseCase.updateStreak(LocalDate.of(2024, 1, 1))
            updateStreakUseCase.updateStreak(LocalDate.of(2024, 1, 2))
            updateStreakUseCase.updateStreak(LocalDate.of(2024, 1, 3))

            // When - Miss Day 4 and practice on Day 5
            val day5 = LocalDate.of(2024, 1, 5)
            val updatedStreak = updateStreakUseCase.updateStreak(day5)

            // Then - Streak resets to 1 but longest streak is preserved
            assertThat(updatedStreak.currentStreak).isEqualTo(1)
            assertThat(updatedStreak.longestStreak).isEqualTo(3) // Preserves previous longest
            assertThat(updatedStreak.totalDaysPracticed).isEqualTo(4) // Still counts total days
        }

    @Test
    fun sevenDayStreak_unlocksWeekWarriorBadge() =
        runTest {
            // Given - Initialize badges
            badgeRepository.initializeBadges()

            // Build a 7-day streak
            val startDate = LocalDate.of(2024, 1, 1)
            repeat(7) { dayOffset ->
                updateStreakUseCase.updateStreak(startDate.plusDays(dayOffset.toLong()))
            }

            // Verify streak is at 7 days
            val currentStreak = streakRepository.getStreak().first()
            assertThat(currentStreak?.currentStreak).isEqualTo(7)

            // When - Check for badge unlocks
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Then - "Week Warrior" badge is unlocked (7-day streak)
            val weekWarriorBadge = unlockedBadges.find { it.id == "week_warrior" }
            assertThat(weekWarriorBadge).isNotNull()
            assertThat(weekWarriorBadge?.isUnlocked()).isTrue()
        }

    @Test
    fun thirtyDayStreak_unlocksMonthlyMasterBadge() =
        runTest {
            // Given - Initialize badges
            badgeRepository.initializeBadges()

            // Build a 30-day streak
            val startDate = LocalDate.of(2024, 1, 1)
            repeat(30) { dayOffset ->
                updateStreakUseCase.updateStreak(startDate.plusDays(dayOffset.toLong()))
            }

            // Verify streak is at 30 days
            val currentStreak = streakRepository.getStreak().first()
            assertThat(currentStreak?.currentStreak).isEqualTo(30)

            // When - Check for badge unlocks
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Then - Both "Week Warrior" and "Monthly Master" badges are unlocked
            val weekWarriorBadge = unlockedBadges.find { it.id == "week_warrior" }
            assertThat(weekWarriorBadge).isNotNull()

            val monthlyMasterBadge = unlockedBadges.find { it.id == "monthly_master" }
            assertThat(monthlyMasterBadge).isNotNull()
            assertThat(monthlyMasterBadge?.isUnlocked()).isTrue()
        }

    @Test
    fun streakBadgesUnlockAtCorrectMilestones() =
        runTest {
            // Given - Initialize badges
            badgeRepository.initializeBadges()

            // When - Build up streak day by day, checking badges at milestones
            val startDate = LocalDate.of(2024, 1, 1)

            // Day 3: Should unlock "First Streak" (3-day streak)
            repeat(3) { dayOffset ->
                updateStreakUseCase.updateStreak(startDate.plusDays(dayOffset.toLong()))
            }
            var unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()
            var firstStreakBadge = unlockedBadges.find { it.id == "first_streak" }
            assertThat(firstStreakBadge).isNotNull()

            // Continue to Day 7: Should unlock "Week Warrior"
            repeat(4) { dayOffset ->
                updateStreakUseCase.updateStreak(startDate.plusDays(dayOffset.toLong() + 3))
            }
            unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()
            var weekWarriorBadge = unlockedBadges.find { it.id == "week_warrior" }
            assertThat(weekWarriorBadge).isNotNull()
        }

    @Test
    fun practiceSessionWithStreakUpdate_triggersStreakBadgeCheck() =
        runTest {
            // Given - Initialize badges and build a 3-day streak
            badgeRepository.initializeBadges()
            val startDate = LocalDate.of(2024, 1, 1)
            repeat(3) { dayOffset ->
                updateStreakUseCase.updateStreak(startDate.plusDays(dayOffset.toLong()))
            }

            // When - Complete a practice session on day 3
            val problems = createTestProblems(count = 10)
            val userAnswers = createPerfectAnswers(problems)
            val practiceSession =
                PracticeSession(
                    totalProblems = problems.size,
                    problems = problems,
                    answers = userAnswers.toMutableMap(),
                    operation = MathOperation.ADDITION,
                    durationSeconds = 60,
                    completedAt = Instant.now(),
                )

            sessionRepository.saveSession(
                session = practiceSession,
                operation = MathOperation.ADDITION,
                durationSeconds = 60,
            )

            // Then - Check badges (should include streak badges)
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Should unlock both practice badges and streak badges
            val firstStepsBadge = unlockedBadges.find { it.id == "first_steps" }
            val firstStreakBadge = unlockedBadges.find { it.id == "first_streak" }

            assertThat(firstStepsBadge).isNotNull() // From practice session
            assertThat(firstStreakBadge).isNotNull() // From 3-day streak
        }

    @Test
    fun longestStreakIsPreserved_whenCurrentStreakIsReset() =
        runTest {
            // Given - Build a 10-day streak
            val startDate = LocalDate.of(2024, 1, 1)
            repeat(10) { dayOffset ->
                updateStreakUseCase.updateStreak(startDate.plusDays(dayOffset.toLong()))
            }

            var streak = streakRepository.getStreak().first()
            assertThat(streak?.currentStreak).isEqualTo(10)
            assertThat(streak?.longestStreak).isEqualTo(10)

            // When - Miss days and start a new streak
            updateStreakUseCase.updateStreak(LocalDate.of(2024, 1, 15)) // Missed 4 days
            updateStreakUseCase.updateStreak(LocalDate.of(2024, 1, 16))
            updateStreakUseCase.updateStreak(LocalDate.of(2024, 1, 17))

            // Then - Current streak is 3, but longest streak remains 10
            streak = streakRepository.getStreak().first()
            assertThat(streak?.currentStreak).isEqualTo(3)
            assertThat(streak?.longestStreak).isEqualTo(10) // Preserved
        }

    // Helper function to create test problems
    private fun createTestProblems(count: Int): List<MathProblem> =
        List(count) { index ->
            MathProblem(
                id = "problem_$index",
                num1 = index + 1,
                num2 = 1,
                operation = MathOperation.ADDITION,
                correctAnswer = index + 2,
            )
        }

    // Helper function to create perfect answers
    private fun createPerfectAnswers(problems: List<MathProblem>): Map<String, SessionAnswer> =
        problems.associate { problem ->
            problem.id to
                SessionAnswer(
                    problemId = problem.id,
                    userAnswer = problem.correctAnswer,
                    isCorrect = true,
                    timeSpentSeconds = 5,
                )
        }
}
