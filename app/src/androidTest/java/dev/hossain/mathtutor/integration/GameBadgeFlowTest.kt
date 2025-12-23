package dev.hossain.mathtutor.integration

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.data.local.MathDatabase
import dev.hossain.mathtutor.data.repository.BadgeRepositoryImpl
import dev.hossain.mathtutor.data.repository.SessionRepositoryImpl
import dev.hossain.mathtutor.data.repository.StreakRepositoryImpl
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GameSession
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.repository.BadgeRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.repository.StreakRepository
import dev.hossain.mathtutor.domain.usecase.CheckBadgeUnlocksUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Integration test for the Game Completion → Badge unlock flow.
 *
 * This test verifies the end-to-end flow for game-specific badges:
 * 1. Completing a game (e.g., Math Race)
 * 2. Saving game session to database
 * 3. Triggering badge unlock check
 * 4. Verifying game-specific badges are unlocked
 *
 * Uses real Room database (in-memory) and fake GameRepository
 * to test game badge unlock logic.
 */
@RunWith(AndroidJUnit4::class)
class GameBadgeFlowTest {
    private lateinit var database: MathDatabase
    private lateinit var sessionRepository: SessionRepository
    private lateinit var badgeRepository: BadgeRepository
    private lateinit var streakRepository: StreakRepository
    private lateinit var gameRepository: FakeGameRepository
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

        gameRepository = FakeGameRepository()

        // Initialize use case with game repository
        checkBadgeUnlocksUseCase =
            CheckBadgeUnlocksUseCase(
                badgeRepository = badgeRepository,
                sessionRepository = sessionRepository,
                streakRepository = streakRepository,
                gameRepository = gameRepository,
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun completingFirstGame_unlocksGameExplorerBadge() =
        runTest {
            // Given - Initialize badges
            badgeRepository.initializeBadges()

            // Verify "Game Explorer" badge is locked initially
            val allBadges = badgeRepository.getAllBadges().first()
            val gameExplorerBadge = allBadges.find { it.id == "game_explorer" }
            assertThat(gameExplorerBadge).isNotNull()
            assertThat(gameExplorerBadge?.isUnlocked()).isFalse()

            // When - Complete first Math Race game
            val gameSession =
                GameSession(
                    game = Game.MATH_RACE,
                    startTime = Instant.now().minusSeconds(60),
                    endTime = Instant.now(),
                    score = 15,
                    correctAnswers = 15,
                    totalAttempts = 20,
                    durationSeconds = 60,
                    gradeLevel = GradeLevel.GRADE_1,
                )

            gameRepository.saveGameSession(gameSession)

            // Then - Check for badge unlocks
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Verify "Game Explorer" badge was unlocked (requires 1 game)
            val unlockedGameExplorer = unlockedBadges.find { it.id == "game_explorer" }
            assertThat(unlockedGameExplorer).isNotNull()
            assertThat(unlockedGameExplorer?.isUnlocked()).isTrue()
        }

    @Test
    fun achievingHighScoreInMathRace_unlocksSpeedDemonBadge() =
        runTest {
            // Given - Initialize badges
            badgeRepository.initializeBadges()

            // When - Complete Math Race with score of 20+ (for "Speed Demon" badge)
            val highScoreSession =
                GameSession(
                    game = Game.MATH_RACE,
                    startTime = Instant.now().minusSeconds(60),
                    endTime = Instant.now(),
                    score = 25,
                    correctAnswers = 25,
                    totalAttempts = 30,
                    durationSeconds = 60,
                    gradeLevel = GradeLevel.GRADE_1,
                )

            gameRepository.saveGameSession(highScoreSession)

            // Then - Check for badge unlocks
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Verify "Speed Demon" badge was unlocked (Math Race score >= 20)
            val speedDemonBadge = unlockedBadges.find { it.id == "speed_demon" }
            assertThat(speedDemonBadge).isNotNull()
            assertThat(speedDemonBadge?.isUnlocked()).isTrue()
        }

    @Test
    fun perfectGameAccuracy_unlocksPerfectionistBadge() =
        runTest {
            // Given - Initialize badges
            badgeRepository.initializeBadges()

            // When - Complete Math Race with 100% accuracy
            val perfectSession =
                GameSession(
                    game = Game.MATH_RACE,
                    startTime = Instant.now().minusSeconds(60),
                    endTime = Instant.now(),
                    score = 15,
                    correctAnswers = 15,
                    totalAttempts = 15, // 100% accuracy
                    durationSeconds = 60,
                    gradeLevel = GradeLevel.GRADE_1,
                )

            gameRepository.saveGameSession(perfectSession)

            // Then - Check for badge unlocks
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Verify "Perfectionist" badge was unlocked (perfect game accuracy)
            val perfectionistBadge = unlockedBadges.find { it.id == "perfectionist" }
            assertThat(perfectionistBadge).isNotNull()
            assertThat(perfectionistBadge?.isUnlocked()).isTrue()
        }

    @Test
    fun completingMemoryMatchQuickly_unlocksMemoryMasterBadge() =
        runTest {
            // Given - Initialize badges
            badgeRepository.initializeBadges()

            // When - Complete Memory Match with 8 moves (perfect score)
            val perfectMemorySession =
                GameSession(
                    game = Game.MEMORY_MATCH,
                    startTime = Instant.now().minusSeconds(60),
                    endTime = Instant.now(),
                    score = 100, // Perfect score
                    correctAnswers = 8,
                    totalAttempts = 8, // Minimum moves (8 pairs)
                    durationSeconds = 60,
                    gradeLevel = GradeLevel.GRADE_1,
                )

            gameRepository.saveGameSession(perfectMemorySession)

            // Then - Check for badge unlocks
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Verify "Memory Master" badge was unlocked (perfect Memory Match)
            val memoryMasterBadge = unlockedBadges.find { it.id == "memory_master" }
            assertThat(memoryMasterBadge).isNotNull()
            assertThat(memoryMasterBadge?.isUnlocked()).isTrue()
        }

    @Test
    fun completingMultipleGames_unlocksGameMasterBadge() =
        runTest {
            // Given - Initialize badges
            badgeRepository.initializeBadges()

            // When - Complete 10 games across different types
            repeat(4) {
                gameRepository.saveGameSession(
                    createGameSession(Game.MATH_RACE, score = 15),
                )
            }
            repeat(3) {
                gameRepository.saveGameSession(
                    createGameSession(Game.MEMORY_MATCH, score = 80),
                )
            }
            repeat(3) {
                gameRepository.saveGameSession(
                    createGameSession(Game.NUMBER_SEQUENCE, score = 10),
                )
            }

            // Then - Check for badge unlocks
            val unlockedBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

            // Verify "Game Master" badge was unlocked (10+ total games)
            val gameMasterBadge = unlockedBadges.find { it.id == "game_master" }
            assertThat(gameMasterBadge).isNotNull()
            assertThat(gameMasterBadge?.isUnlocked()).isTrue()
        }

    @Test
    fun gameSessionIsSaved_canBeRetrievedFromRepository() =
        runTest {
            // Given - A game session
            val gameSession =
                GameSession(
                    game = Game.MATH_RACE,
                    startTime = Instant.now().minusSeconds(60),
                    endTime = Instant.now(),
                    score = 20,
                    correctAnswers = 20,
                    totalAttempts = 25,
                    durationSeconds = 60,
                    gradeLevel = GradeLevel.GRADE_2,
                )

            // When - Save the game session
            val sessionId = gameRepository.saveGameSession(gameSession)

            // Then - Session is saved and can be retrieved
            assertThat(sessionId).isGreaterThan(0)

            val sessions = gameRepository.getSessionsByGame(Game.MATH_RACE).first()
            assertThat(sessions).hasSize(1)
            assertThat(sessions[0].game).isEqualTo(Game.MATH_RACE)
            assertThat(sessions[0].score).isEqualTo(20)
            assertThat(sessions[0].correctAnswers).isEqualTo(20)
            assertThat(sessions[0].totalAttempts).isEqualTo(25)
        }

    @Test
    fun multipleGameSessions_personalBestIsTracked() =
        runTest {
            // Given - Multiple game sessions with different scores
            gameRepository.saveGameSession(createGameSession(Game.MATH_RACE, score = 15))
            gameRepository.saveGameSession(createGameSession(Game.MATH_RACE, score = 25))
            gameRepository.saveGameSession(createGameSession(Game.MATH_RACE, score = 20))

            // When - Get personal best
            val personalBest = gameRepository.getPersonalBest(Game.MATH_RACE).first()

            // Then - Personal best is the highest score
            assertThat(personalBest).isEqualTo(25)
        }

    // Helper function to create a game session
    private fun createGameSession(
        game: Game,
        score: Int,
    ): GameSession =
        GameSession(
            game = game,
            startTime = Instant.now().minusSeconds(60),
            endTime = Instant.now(),
            score = score,
            correctAnswers = score,
            totalAttempts = score + 5,
            durationSeconds = 60,
            gradeLevel = GradeLevel.GRADE_1,
        )
}
