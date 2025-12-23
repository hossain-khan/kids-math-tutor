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

/**
 * Integration test for the complete Practice → Results → Badge unlock flow.
 *
 * This test verifies the end-to-end flow:
 * 1. Starting a practice session with problems
 * 2. Completing the session with 100% accuracy
 * 3. Saving session to database
 * 4. Triggering badge unlock check
 * 5. Verifying newly unlocked badge appears in database
 *
 * Uses real Room database (in-memory) and real repository implementations
 * to ensure all layers work together correctly.
 */
@RunWith(AndroidJUnit4::class)
class PracticeToResultsFlowTest {
    private lateinit var database: MathDatabase
    private lateinit var sessionRepository: SessionRepository
    private lateinit var badgeRepository: BadgeRepository
    private lateinit var streakRepository: StreakRepository
    private lateinit var checkBadgeUnlocksUseCase: CheckBadgeUnlocksUseCase
    private lateinit var updateStreakUseCase: UpdateStreakUseCase

    @Before
    fun setup() {
        // Create in-memory database for testing
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    MathDatabase::class.java,
                ).allowMainThreadQueries() // For testing only
                .build()

        // Create fake analytics service
        val fakeAnalyticsService = FakeAnalyticsService()

        // Initialize repositories with real implementations
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

        // We need a fake GameRepository for the CheckBadgeUnlocksUseCase
        // For now, we'll create the use case without game repository (testing only practice badges)
        val fakeGameRepository = FakeGameRepository()

        // Initialize use cases
        checkBadgeUnlocksUseCase =
            CheckBadgeUnlocksUseCase(
                badgeRepository = badgeRepository,
                sessionRepository = sessionRepository,
                streakRepository = streakRepository,
                gameRepository = fakeGameRepository,
            )

        updateStreakUseCase = UpdateStreakUseCase(streakRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun practiceSessionWithPerfectAccuracy_savesSessionAndUnlocksBadge() =
        runTest {
            // Given - Initialize badges in database (simulating first app launch)
            badgeRepository.initializeBadges()

            // Verify badges are initialized
            val allBadges = badgeRepository.getAllBadges().first()
            assertThat(allBadges).isNotEmpty()

            // Find the "First Steps" badge (first practice session badge)
            val firstStepsBadge = allBadges.find { it.id == "first_steps" }
            assertThat(firstStepsBadge).isNotNull()
            assertThat(firstStepsBadge?.isUnlocked()).isFalse()

            // Create a practice session with 10 problems (all correct)
            val problems = createTestProblems(count = 10, operation = MathOperation.ADDITION)
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

            // When - Save the practice session (simulating session completion)
            val sessionId =
                sessionRepository.saveSession(
                    session = practiceSession,
                    operation = MathOperation.ADDITION,
                    durationSeconds = 60,
                )

            // Then - Verify session was saved
            assertThat(sessionId).isGreaterThan(0)

            // Verify session exists in database
            val savedSessions = sessionRepository.getAllSessions().first()
            assertThat(savedSessions).hasSize(1)
            assertThat(savedSessions[0].operation).isEqualTo(MathOperation.ADDITION)
            assertThat(savedSessions[0].totalProblems).isEqualTo(10)
            assertThat(savedSessions[0].correctAnswers).isEqualTo(10)
            assertThat(savedSessions[0].accuracy).isEqualTo(100.0f)

            // When - Check for badge unlocks (simulating post-session badge check)
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Then - Verify "First Steps" badge was unlocked
            assertThat(unlockedBadges).isNotEmpty()
            val unlockedFirstSteps = unlockedBadges.find { it.id == "first_steps" }
            assertThat(unlockedFirstSteps).isNotNull()
            assertThat(unlockedFirstSteps?.isUnlocked()).isTrue()

            // Verify badge is marked as unlocked in database
            val updatedBadges = badgeRepository.getAllBadges().first()
            val updatedFirstSteps = updatedBadges.find { it.id == "first_steps" }
            assertThat(updatedFirstSteps?.isUnlocked()).isTrue()
        }

    @Test
    fun multipleSessionsWithHighAccuracy_unlocksAccuracyBadge() =
        runTest {
            // Given - Initialize badges
            badgeRepository.initializeBadges()

            // Complete 3 sessions with 90%+ accuracy to unlock "Perfect Practice" badge
            repeat(3) { sessionNumber ->
                val problems = createTestProblems(count = 10, operation = MathOperation.ADDITION)
                // Create 9 correct answers (90% accuracy)
                val userAnswers =
                    problems
                        .mapIndexed { index, problem ->
                            problem.id to
                                SessionAnswer(
                                    problemId = problem.id,
                                    userAnswer = if (index < 9) problem.correctAnswer else problem.correctAnswer + 1,
                                    isCorrect = index < 9,
                                    timeSpentSeconds = 5,
                                )
                        }.toMap()

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
            }

            // When - Check for badge unlocks
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Then - Verify "Perfect Practice" badge (3 sessions with 90%+ accuracy) is unlocked
            val perfectPracticeBadge = unlockedBadges.find { it.id == "perfect_practice" }
            assertThat(perfectPracticeBadge).isNotNull()
            assertThat(perfectPracticeBadge?.isUnlocked()).isTrue()
        }

    @Test
    fun completingManyProblems_unlocksVolumeBadges() =
        runTest {
            // Given - Initialize badges
            badgeRepository.initializeBadges()

            // Complete multiple sessions to reach 25 total problems (for "Practice Pro" badge)
            // Session 1: 10 problems
            saveTestSession(problemCount = 10, operation = MathOperation.ADDITION)

            // Session 2: 10 problems
            saveTestSession(problemCount = 10, operation = MathOperation.ADDITION)

            // Session 3: 5 problems (total = 25)
            saveTestSession(problemCount = 5, operation = MathOperation.ADDITION)

            // When - Check for badge unlocks
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Then - Verify volume-based badges are unlocked
            // "First Steps" (10 problems) should be unlocked
            val firstSteps = unlockedBadges.find { it.id == "first_steps" }
            assertThat(firstSteps).isNotNull()

            // "Practice Pro" (25 problems) should be unlocked
            val practicePro = unlockedBadges.find { it.id == "practice_pro" }
            assertThat(practicePro).isNotNull()
        }

    @Test
    fun operationSpecificBadges_unlockForSpecificOperations() =
        runTest {
            // Given - Initialize badges
            badgeRepository.initializeBadges()

            // Complete 25 addition problems to unlock "Addition Expert"
            repeat(3) {
                saveTestSession(problemCount = 10, operation = MathOperation.ADDITION)
            }

            // When - Check for badge unlocks
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Then - Verify "Addition Expert" badge is unlocked
            val additionExpert = unlockedBadges.find { it.id == "addition_expert" }
            assertThat(additionExpert).isNotNull()
            assertThat(additionExpert?.isUnlocked()).isTrue()

            // But subtraction-specific badges should NOT be unlocked
            val subtractionExpert = unlockedBadges.find { it.id == "subtraction_expert" }
            assertThat(subtractionExpert).isNull()
        }

    // Helper function to create test problems
    private fun createTestProblems(
        count: Int,
        operation: MathOperation,
    ): List<MathProblem> =
        List(count) { index ->
            MathProblem(
                id = "problem_$index",
                num1 = index + 1,
                num2 = 1,
                operation = operation,
                correctAnswer = if (operation == MathOperation.ADDITION) index + 2 else index,
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

    // Helper function to save a test session
    private suspend fun saveTestSession(
        problemCount: Int,
        operation: MathOperation,
    ) {
        val problems = createTestProblems(count = problemCount, operation = operation)
        val userAnswers = createPerfectAnswers(problems)

        val practiceSession =
            PracticeSession(
                totalProblems = problems.size,
                problems = problems,
                answers = userAnswers.toMutableMap(),
                operation = operation,
                durationSeconds = 60,
                completedAt = Instant.now(),
            )

        sessionRepository.saveSession(
            session = practiceSession,
            operation = operation,
            durationSeconds = 60,
        )
    }
}
