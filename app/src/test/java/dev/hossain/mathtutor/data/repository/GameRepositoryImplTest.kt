package dev.hossain.mathtutor.data.repository

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GameRepositoryImplTest {
    private lateinit var fakeGameSessionDao: FakeGameSessionDao
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var repository: GameRepositoryImpl

    @Before
    fun setup() {
        fakeGameSessionDao = FakeGameSessionDao()
        fakeSessionRepository = FakeSessionRepository()
        repository = GameRepositoryImpl(fakeGameSessionDao, fakeSessionRepository)
    }

    // saveGameSession tests
    @Test
    fun `saveGameSession inserts entity correctly`() =
        runTest {
            val session = createGameSession(Game.MATH_RACE, score = 15)

            val id = repository.saveGameSession(session)

            assertEquals(1L, id)
            assertEquals(1, fakeGameSessionDao.insertedSessions.size)
            val inserted = fakeGameSessionDao.insertedSessions[0]
            assertEquals("MATH_RACE", inserted.gameId)
            assertEquals(15, inserted.score)
        }

    // getPersonalBest tests
    @Test
    fun `getPersonalBest returns highest score`() =
        runTest {
            fakeGameSessionDao.personalBests[Game.MATH_RACE.name] = MutableStateFlow(25)

            val personalBest = repository.getPersonalBest(Game.MATH_RACE).first()

            assertEquals(25, personalBest)
        }

    @Test
    fun `getPersonalBest returns 0 when no sessions exist`() =
        runTest {
            fakeGameSessionDao.personalBests[Game.MATH_RACE.name] = MutableStateFlow(null)

            val personalBest = repository.getPersonalBest(Game.MATH_RACE).first()

            assertEquals(0, personalBest)
        }

    // getBestSession tests
    @Test
    fun `getBestSession returns session with highest score`() =
        runTest {
            val entity = createGameSessionEntity(id = 1, score = 30)
            fakeGameSessionDao.bestSessions[Game.MATH_RACE.name] = MutableStateFlow(entity)

            val bestSession = repository.getBestSession(Game.MATH_RACE).first()

            assertEquals(30, bestSession?.score)
            assertTrue(bestSession?.isNewRecord == true)
        }

    @Test
    fun `getBestSession returns null when no sessions exist`() =
        runTest {
            fakeGameSessionDao.bestSessions[Game.MATH_RACE.name] = MutableStateFlow(null)

            val bestSession = repository.getBestSession(Game.MATH_RACE).first()

            assertNull(bestSession)
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

            assertEquals(Game.MATH_RACE, stats.game)
            assertEquals(25, stats.personalBest)
            assertEquals(5, stats.totalGamesPlayed)
            assertEquals(18f, stats.averageScore)
            assertEquals(95f, stats.bestAccuracy)
            assertEquals(80, stats.totalCorrectAnswers)
            assertEquals(100, stats.totalAttempts)
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

            assertEquals(0, stats.personalBest)
            assertEquals(0, stats.totalGamesPlayed)
            assertEquals(0f, stats.averageScore)
            assertEquals(0f, stats.bestAccuracy)
            assertNull(stats.lastPlayedAt)
        }

    // getTotalGamesPlayed tests
    @Test
    fun `getTotalGamesPlayed returns correct count`() =
        runTest {
            fakeGameSessionDao.totalGamesPlayed[Game.MATH_RACE.name] = MutableStateFlow(10)

            val count = repository.getTotalGamesPlayed(Game.MATH_RACE).first()

            assertEquals(10, count)
        }

    // isGameUnlocked tests
    @Test
    fun `isGameUnlocked returns true when enough problems solved`() =
        runTest {
            fakeSessionRepository.setTotalProblems(100) // Math Race needs 50

            val isUnlocked = repository.isGameUnlocked(Game.MATH_RACE).first()

            assertTrue(isUnlocked)
        }

    @Test
    fun `isGameUnlocked returns false when not enough problems solved`() =
        runTest {
            fakeSessionRepository.setTotalProblems(25) // Math Race needs 50

            val isUnlocked = repository.isGameUnlocked(Game.MATH_RACE).first()

            assertFalse(isUnlocked)
        }

    @Test
    fun `isGameUnlocked with edge case - exactly at requirement`() =
        runTest {
            fakeSessionRepository.setTotalProblems(50) // Math Race needs exactly 50

            val isUnlocked = repository.isGameUnlocked(Game.MATH_RACE).first()

            assertTrue(isUnlocked)
        }

    @Test
    fun `isGameUnlocked with edge case - one below requirement`() =
        runTest {
            fakeSessionRepository.setTotalProblems(49) // Math Race needs 50

            val isUnlocked = repository.isGameUnlocked(Game.MATH_RACE).first()

            assertFalse(isUnlocked)
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

            assertEquals(2, sessions.size)
            assertEquals(15, sessions[0].score)
            assertEquals(20, sessions[1].score)
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

            assertEquals(2, sessions.size)
        }

    // getPerfectGameCount tests
    @Test
    fun `getPerfectGameCount returns count of perfect games`() =
        runTest {
            fakeGameSessionDao.perfectGameCounts[Game.MATH_RACE.name] = MutableStateFlow(3)

            val count = repository.getPerfectGameCount(Game.MATH_RACE).first()

            assertEquals(3, count)
        }

    // clearAllSessions tests
    @Test
    fun `clearAllSessions calls DAO delete method`() =
        runTest {
            repository.clearAllSessions()

            assertTrue(fakeGameSessionDao.deleteAllCalled)
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

    private var nextId = 1L

    override suspend fun insertSession(session: GameSessionEntity): Long {
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
}
