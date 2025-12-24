package dev.hossain.mathtutor.data.repository

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.analytics.FakeAnalyticsService
import dev.hossain.mathtutor.data.local.dao.GameSessionDao
import dev.hossain.mathtutor.data.local.entity.GameSessionEntity
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GameSession
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GameRepositoryImplTest {
    private lateinit var fakeGameSessionDao: FakeGameSessionDao
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var fakeAnalytics: FakeAnalyticsService
    private lateinit var repository: GameRepositoryImpl

    @Before
    fun setup() {
        fakeGameSessionDao = FakeGameSessionDao()
        fakeSessionRepository = FakeSessionRepository()
        fakeAnalytics = FakeAnalyticsService()
        repository = GameRepositoryImpl(fakeGameSessionDao, fakeSessionRepository, fakeAnalytics)
    }

    // saveGameSession tests
    @Test
    fun `saveGameSession inserts entity correctly`() =
        runTest {
            val session = createGameSession(Game.MATH_RACE, score = 15)

            val id = repository.saveGameSession(session)

            assertThat(id).isEqualTo(1L)
            assertThat(fakeGameSessionDao.insertedSessions.size).isEqualTo(1)
            val inserted = fakeGameSessionDao.insertedSessions[0]
            assertThat(inserted.gameId).isEqualTo("MATH_RACE")
            assertThat(inserted.score).isEqualTo(15)
        }

    // getPersonalBest tests
    @Test
    fun `getPersonalBest returns highest score`() =
        runTest {
            fakeGameSessionDao.personalBests[Game.MATH_RACE.name] = MutableStateFlow(25)

            val personalBest = repository.getPersonalBest(Game.MATH_RACE).first()

            assertThat(personalBest).isEqualTo(25)
        }

    @Test
    fun `getPersonalBest returns 0 when no sessions exist`() =
        runTest {
            fakeGameSessionDao.personalBests[Game.MATH_RACE.name] = MutableStateFlow(null)

            val personalBest = repository.getPersonalBest(Game.MATH_RACE).first()

            assertThat(personalBest).isEqualTo(0)
        }

    // getBestSession tests
    @Test
    fun `getBestSession returns session with highest score`() =
        runTest {
            val entity = createGameSessionEntity(id = 1, score = 30)
            fakeGameSessionDao.bestSessions[Game.MATH_RACE.name] = MutableStateFlow(entity)

            val bestSession = repository.getBestSession(Game.MATH_RACE).first()

            assertThat(bestSession?.score).isEqualTo(30)
            assertThat(bestSession?.isNewRecord == true).isTrue()
        }

    @Test
    fun `getBestSession returns null when no sessions exist`() =
        runTest {
            fakeGameSessionDao.bestSessions[Game.MATH_RACE.name] = MutableStateFlow(null)

            val bestSession = repository.getBestSession(Game.MATH_RACE).first()

            assertThat(bestSession).isNull()
        }

    // getGameStats tests
    @Test
    fun `getGameStats returns aggregated statistics`() =
        runTest {
            val gameName = Game.MATH_RACE.name
            fakeGameSessionDao.personalBests[gameName] = MutableStateFlow(25)
            fakeGameSessionDao.totalGamesPlayed[gameName] = MutableStateFlow(5)
            fakeGameSessionDao.averageScores[gameName] = MutableStateFlow(18f)
            fakeGameSessionDao.bestAccuracies[gameName] = MutableStateFlow(95f)
            fakeGameSessionDao.lastPlayedTimestamps[gameName] = MutableStateFlow(1000L)
            fakeGameSessionDao.totalCorrectAnswers[gameName] = MutableStateFlow(80)
            fakeGameSessionDao.totalAttempts[gameName] = MutableStateFlow(100)

            val stats = repository.getGameStats(Game.MATH_RACE).first()

            assertThat(stats.game).isEqualTo(Game.MATH_RACE)
            assertThat(stats.personalBest).isEqualTo(25)
            assertThat(stats.totalGamesPlayed).isEqualTo(5)
            assertThat(stats.averageScore).isEqualTo(18f)
            assertThat(stats.bestAccuracy).isEqualTo(95f)
            assertThat(stats.totalCorrectAnswers).isEqualTo(80)
            assertThat(stats.totalAttempts).isEqualTo(100)
        }

    @Test
    fun `getGameStats returns default values when no data`() =
        runTest {
            val gameName = Game.MATH_RACE.name
            fakeGameSessionDao.personalBests[gameName] = MutableStateFlow(null)
            fakeGameSessionDao.totalGamesPlayed[gameName] = MutableStateFlow(0)
            fakeGameSessionDao.averageScores[gameName] = MutableStateFlow(null)
            fakeGameSessionDao.bestAccuracies[gameName] = MutableStateFlow(null)
            fakeGameSessionDao.lastPlayedTimestamps[gameName] = MutableStateFlow(null)
            fakeGameSessionDao.totalCorrectAnswers[gameName] = MutableStateFlow(null)
            fakeGameSessionDao.totalAttempts[gameName] = MutableStateFlow(null)

            val stats = repository.getGameStats(Game.MATH_RACE).first()

            assertThat(stats.personalBest).isEqualTo(0)
            assertThat(stats.totalGamesPlayed).isEqualTo(0)
            assertThat(stats.averageScore).isEqualTo(0f)
            assertThat(stats.bestAccuracy).isEqualTo(0f)
            assertThat(stats.lastPlayedAt).isNull()
        }

    // getTotalGamesPlayed tests
    @Test
    fun `getTotalGamesPlayed returns correct count`() =
        runTest {
            fakeGameSessionDao.totalGamesPlayed[Game.MATH_RACE.name] = MutableStateFlow(10)

            val count = repository.getTotalGamesPlayed(Game.MATH_RACE).first()

            assertThat(count).isEqualTo(10)
        }

    // isGameUnlocked tests
    @Test
    fun `isGameUnlocked returns true when enough problems solved`() =
        runTest {
            fakeSessionRepository.setTotalProblems(100) // Math Race needs 50

            val isUnlocked = repository.isGameUnlocked(Game.MATH_RACE).first()

            assertThat(isUnlocked).isTrue()
        }

    @Test
    fun `isGameUnlocked returns false when not enough problems solved`() =
        runTest {
            fakeSessionRepository.setTotalProblems(25) // Math Race needs 50

            val isUnlocked = repository.isGameUnlocked(Game.MATH_RACE).first()

            assertThat(isUnlocked).isFalse()
        }

    @Test
    fun `isGameUnlocked with edge case - exactly at requirement`() =
        runTest {
            fakeSessionRepository.setTotalProblems(50) // Math Race needs exactly 50

            val isUnlocked = repository.isGameUnlocked(Game.MATH_RACE).first()

            assertThat(isUnlocked).isTrue()
        }

    @Test
    fun `isGameUnlocked with edge case - one below requirement`() =
        runTest {
            fakeSessionRepository.setTotalProblems(49) // Math Race needs 50

            val isUnlocked = repository.isGameUnlocked(Game.MATH_RACE).first()

            assertThat(isUnlocked).isFalse()
        }

    // getSessionsByGame tests
    @Test
    fun `getSessionsByGame returns sessions for specific game`() =
        runTest {
            val entities =
                listOf(
                    createGameSessionEntity(id = 1, score = 15),
                    createGameSessionEntity(id = 2, score = 20),
                )
            fakeGameSessionDao.sessionsByGame[Game.MATH_RACE.name] = MutableStateFlow(entities)

            val sessions = repository.getSessionsByGame(Game.MATH_RACE).first()

            assertThat(sessions.size).isEqualTo(2)
            assertThat(sessions[0].score).isEqualTo(15)
            assertThat(sessions[1].score).isEqualTo(20)
        }

    // getRecentSessions tests
    @Test
    fun `getRecentSessions returns limited sessions`() =
        runTest {
            val entities =
                listOf(
                    createGameSessionEntity(id = 1, score = 10),
                    createGameSessionEntity(id = 2, score = 15),
                )
            fakeGameSessionDao.recentSessions = MutableStateFlow(entities)

            val sessions = repository.getRecentSessions(10).first()

            assertThat(sessions.size).isEqualTo(2)
        }

    // getPerfectGameCount tests
    @Test
    fun `getPerfectGameCount returns count of perfect games`() =
        runTest {
            fakeGameSessionDao.perfectGameCounts[Game.MATH_RACE.name] = MutableStateFlow(3)

            val count = repository.getPerfectGameCount(Game.MATH_RACE).first()

            assertThat(count).isEqualTo(3)
        }

    // clearAllSessions tests
    @Test
    fun `clearAllSessions calls DAO delete method`() =
        runTest {
            repository.clearAllSessions()

            assertThat(fakeGameSessionDao.deleteAllCalled).isTrue()
        }

    @Test
    fun `saveGameSession logs error on failure`() =
        runTest {
            val session = createGameSession(Game.MATH_RACE, score = 15)
            fakeGameSessionDao.shouldThrowOnInsert = true

            try {
                repository.saveGameSession(session)
            } catch (e: Exception) {
                // Expected exception
            }

            // Verify error logged
            assertThat(fakeAnalytics.errors).hasSize(1)
            assertThat(fakeAnalytics.errors.first().context).isEqualTo("Game session save failed")
            assertThat(fakeAnalytics.errors.first().isFatal).isFalse()
        }

    @Test
    fun `clearAllSessions logs error on failure`() =
        runTest {
            fakeGameSessionDao.shouldThrowOnDelete = true

            try {
                repository.clearAllSessions()
            } catch (e: Exception) {
                // Expected exception
            }

            // Verify error logged
            assertThat(fakeAnalytics.errors).hasSize(1)
            assertThat(fakeAnalytics.errors.first().context).isEqualTo("Game session clear failed")
            assertThat(fakeAnalytics.errors.first().isFatal).isFalse()
        }

    // Helper methods
    private fun createGameSession(
        game: Game = Game.MATH_RACE,
        score: Int = 10,
        correctAnswers: Int = score,
        totalAttempts: Int = score + 2,
    ): GameSession =
        GameSession(
            id = 0,
            game = game,
            startTime = Instant.now(),
            endTime = Instant.now(),
            score = score,
            correctAnswers = correctAnswers,
            totalAttempts = totalAttempts,
            durationSeconds = 60,
            gradeLevel = GradeLevel.GRADE_1,
        )

    private fun createGameSessionEntity(
        id: Long = 1,
        gameId: String = Game.MATH_RACE.name,
        score: Int = 10,
        correctAnswers: Int = score,
        totalAttempts: Int = score + 2,
    ): GameSessionEntity =
        GameSessionEntity(
            id = id,
            gameId = gameId,
            startTime = Instant.now(),
            endTime = Instant.now(),
            score = score,
            correctAnswers = correctAnswers,
            totalAttempts = totalAttempts,
            durationSeconds = 60,
            gradeLevel = GradeLevel.GRADE_1,
        )
}

/**
 * Fake implementation of GameSessionDao for testing.
 */
class FakeGameSessionDao : GameSessionDao {
    val insertedSessions = mutableListOf<GameSessionEntity>()
    var allSessions = MutableStateFlow<List<GameSessionEntity>>(emptyList())
    val sessionsByGame = mutableMapOf<String, MutableStateFlow<List<GameSessionEntity>>>()
    var recentSessions = MutableStateFlow<List<GameSessionEntity>>(emptyList())
    val personalBests = mutableMapOf<String, MutableStateFlow<Int?>>()
    val bestSessions = mutableMapOf<String, MutableStateFlow<GameSessionEntity?>>()
    val totalGamesPlayed = mutableMapOf<String, MutableStateFlow<Int>>()
    val averageScores = mutableMapOf<String, MutableStateFlow<Float?>>()
    val bestAccuracies = mutableMapOf<String, MutableStateFlow<Float?>>()
    val lastPlayedTimestamps = mutableMapOf<String, MutableStateFlow<Long?>>()
    val totalCorrectAnswers = mutableMapOf<String, MutableStateFlow<Int?>>()
    val totalAttempts = mutableMapOf<String, MutableStateFlow<Int?>>()
    val perfectGameCounts = mutableMapOf<String, MutableStateFlow<Int>>()
    var deleteAllCalled = false
    var deletedGameIds = mutableListOf<String>()
    var shouldThrowOnInsert = false
    var shouldThrowOnDelete = false

    private var nextId = 1L

    override suspend fun insertSession(session: GameSessionEntity): Long {
        if (shouldThrowOnInsert) {
            throw RuntimeException("Failed to insert game session")
        }
        val id = nextId++
        insertedSessions.add(session.copy(id = id))
        return id
    }

    override fun getAllSessions(): Flow<List<GameSessionEntity>> = allSessions

    override fun getSessionsByGame(gameId: String): Flow<List<GameSessionEntity>> =
        sessionsByGame.getOrPut(gameId) { MutableStateFlow(emptyList()) }

    override fun getRecentSessions(limit: Int): Flow<List<GameSessionEntity>> = recentSessions

    override fun getPersonalBest(gameId: String): Flow<Int?> = personalBests.getOrPut(gameId) { MutableStateFlow(null) }

    override fun getBestSession(gameId: String): Flow<GameSessionEntity?> = bestSessions.getOrPut(gameId) { MutableStateFlow(null) }

    override fun getTotalGamesPlayed(gameId: String): Flow<Int> = totalGamesPlayed.getOrPut(gameId) { MutableStateFlow(0) }

    override fun getAverageScore(gameId: String): Flow<Float?> = averageScores.getOrPut(gameId) { MutableStateFlow(null) }

    override fun getBestAccuracy(gameId: String): Flow<Float?> = bestAccuracies.getOrPut(gameId) { MutableStateFlow(null) }

    override fun getLastPlayedTimestamp(gameId: String): Flow<Long?> = lastPlayedTimestamps.getOrPut(gameId) { MutableStateFlow(null) }

    override fun getTotalCorrectAnswers(gameId: String): Flow<Int?> = totalCorrectAnswers.getOrPut(gameId) { MutableStateFlow(null) }

    override fun getTotalAttempts(gameId: String): Flow<Int?> = totalAttempts.getOrPut(gameId) { MutableStateFlow(null) }

    override fun getTotalSessionCount(): Flow<Int> = flowOf(insertedSessions.size)

    override fun getPerfectGameCount(gameId: String): Flow<Int> = perfectGameCounts.getOrPut(gameId) { MutableStateFlow(0) }

    override suspend fun deleteAllSessions() {
        if (shouldThrowOnDelete) {
            throw RuntimeException("Failed to delete game sessions")
        }
        deleteAllCalled = true
        insertedSessions.clear()
    }

    override suspend fun deleteSessionsByGame(gameId: String) {
        deletedGameIds.add(gameId)
        insertedSessions.removeAll { it.gameId == gameId }
    }
}

/**
 * Fake implementation of SessionRepository for testing game unlock checks.
 */
class FakeSessionRepository : SessionRepository {
    private val totalProblemsFlow = MutableStateFlow(SessionStats.EMPTY)

    fun setTotalProblems(total: Int) {
        totalProblemsFlow.value =
            SessionStats(
                totalProblems = total,
                correctCount = total,
                accuracy = 100f,
                sessionCount = total / 10,
            )
    }

    override suspend fun saveSession(
        session: dev.hossain.mathtutor.domain.model.PracticeSession,
        operation: dev.hossain.mathtutor.domain.model.MathOperation,
        durationSeconds: Long,
        gradeLevel: Int?,
    ): Long = 1L

    override fun getAllSessions() = flowOf(emptyList<dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity>())

    override fun getRecentSessions(limit: Int) = flowOf(emptyList<dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity>())

    override fun getSessionsByOperation(operation: dev.hossain.mathtutor.domain.model.MathOperation) =
        flowOf(emptyList<dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity>())

    override fun getOverallStats(): Flow<SessionStats> = totalProblemsFlow

    override fun getStatsByOperation(operation: dev.hossain.mathtutor.domain.model.MathOperation) = flowOf(SessionStats.EMPTY)

    override suspend fun clearAllSessions() {}

    override fun getDailyAccuracy(): Flow<List<dev.hossain.mathtutor.domain.model.DailyAccuracy>> = flowOf(emptyList())
}
