package dev.hossain.mathtutor.data.repository

import dev.hossain.mathtutor.data.local.dao.SessionDao
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionAnswer
import dev.hossain.mathtutor.domain.model.SessionStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

class SessionRepositoryImplTest {
    private lateinit var fakeDao: FakeSessionDao
    private lateinit var repository: SessionRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeSessionDao()
        repository = SessionRepositoryImpl(fakeDao)
    }

    @Test
    fun `saveSession inserts entity correctly`() =
        runTest {
            val problems = listOf(MathProblem(1, 2, MathOperation.ADDITION, 3))
            val session =
                PracticeSession(
                    totalProblems = 1,
                    problems = problems,
                    answers = mutableMapOf(problems[0].id to SessionAnswer(3, true, 1)),
                )

            val id = repository.saveSession(session, MathOperation.ADDITION, 60L, 1)

            assertEquals(1L, id)
            assertEquals(1, fakeDao.insertedSessions.size)
            val inserted = fakeDao.insertedSessions[0]
            assertEquals(MathOperation.ADDITION, inserted.operation)
            assertEquals(1, inserted.totalProblems)
            assertEquals(1, inserted.correctAnswers)
            assertEquals(0, inserted.incorrectAnswers)
            assertEquals(100f, inserted.accuracy)
            assertEquals(60L, inserted.durationSeconds)
            assertEquals(1, inserted.gradeLevel)
        }

    @Test
    fun `getAllSessions returns all sessions from DAO`() =
        runTest {
            val session1 = createSessionEntity(1, MathOperation.ADDITION, 10, 8)
            val session2 = createSessionEntity(2, MathOperation.SUBTRACTION, 10, 7)
            fakeDao.allSessions.value = listOf(session1, session2)

            val sessions = repository.getAllSessions().first()
            assertEquals(2, sessions.size)
            assertEquals(session1, sessions[0])
            assertEquals(session2, sessions[1])
        }

    @Test
    fun `getRecentSessions returns limited sessions`() =
        runTest {
            val session1 = createSessionEntity(1, MathOperation.ADDITION, 10, 9)
            val session2 = createSessionEntity(2, MathOperation.ADDITION, 10, 8)
            fakeDao.recentSessions.value = listOf(session1, session2)

            val sessions = repository.getRecentSessions(5).first()
            assertEquals(2, sessions.size)
        }

    @Test
    fun `getSessionsByOperation returns filtered sessions`() =
        runTest {
            val additionSession = createSessionEntity(1, MathOperation.ADDITION, 10, 9)
            fakeDao.sessionsByOperation[MathOperation.ADDITION] =
                MutableStateFlow(
                    listOf(additionSession),
                )

            val sessions = repository.getSessionsByOperation(MathOperation.ADDITION).first()
            assertEquals(1, sessions.size)
            assertEquals(MathOperation.ADDITION, sessions[0].operation)
        }

    @Test
    fun `getOverallStats calculates correct statistics`() =
        runTest {
            fakeDao.totalProblems.value = 30
            fakeDao.totalCorrect.value = 24
            fakeDao.sessionCount.value = 3

            val stats = repository.getOverallStats().first()
            assertEquals(30, stats.totalProblems)
            assertEquals(24, stats.correctCount)
            assertEquals(80f, stats.accuracy)
            assertEquals(3, stats.sessionCount)
        }

    @Test
    fun `getOverallStats returns EMPTY when no sessions exist`() =
        runTest {
            fakeDao.totalProblems.value = null
            fakeDao.totalCorrect.value = null
            fakeDao.sessionCount.value = 0

            val stats = repository.getOverallStats().first()
            assertEquals(SessionStats.EMPTY, stats)
        }

    @Test
    fun `getOverallStats handles partial null values`() =
        runTest {
            fakeDao.totalProblems.value = 10
            fakeDao.totalCorrect.value = null
            fakeDao.sessionCount.value = 1

            val stats = repository.getOverallStats().first()
            assertEquals(SessionStats.EMPTY, stats)
        }

    @Test
    fun `getStatsByOperation calculates stats for specific operation`() =
        runTest {
            val session1 = createSessionEntity(1, MathOperation.ADDITION, 10, 9)
            val session2 = createSessionEntity(2, MathOperation.ADDITION, 10, 8)
            fakeDao.sessionsByOperation[MathOperation.ADDITION] =
                MutableStateFlow(
                    listOf(session1, session2),
                )
            fakeDao.sessionCount.value = 2

            val stats = repository.getStatsByOperation(MathOperation.ADDITION).first()
            assertEquals(20, stats.totalProblems)
            assertEquals(17, stats.correctCount)
            assertEquals(85f, stats.accuracy)
            assertEquals(2, stats.sessionCount)
        }

    @Test
    fun `getStatsByOperation returns EMPTY when no sessions for operation`() =
        runTest {
            fakeDao.sessionsByOperation[MathOperation.SUBTRACTION] = MutableStateFlow(emptyList())
            fakeDao.sessionCount.value = 0

            val stats = repository.getStatsByOperation(MathOperation.SUBTRACTION).first()
            assertEquals(SessionStats.EMPTY, stats)
        }

    @Test
    fun `clearAllSessions calls DAO delete method`() =
        runTest {
            repository.clearAllSessions()
            assertEquals(true, fakeDao.deleteAllCalled)
        }

    @Test
    fun `saveSession with different operations`() =
        runTest {
            val problems = listOf(MathProblem(5, 3, MathOperation.SUBTRACTION, 2))
            val session =
                PracticeSession(
                    totalProblems = 1,
                    problems = problems,
                    answers = mutableMapOf(problems[0].id to SessionAnswer(2, true, 1)),
                )

            repository.saveSession(session, MathOperation.SUBTRACTION, 45L, 2)

            val inserted = fakeDao.insertedSessions[0]
            assertEquals(MathOperation.SUBTRACTION, inserted.operation)
        }

    @Test
    fun `saveSession increments ID on multiple inserts`() =
        runTest {
            val problems = listOf(MathProblem(1, 2, MathOperation.ADDITION, 3))
            val session =
                PracticeSession(
                    totalProblems = 1,
                    problems = problems,
                    answers = mutableMapOf(problems[0].id to SessionAnswer(3, true, 1)),
                )

            val id1 = repository.saveSession(session, MathOperation.ADDITION, 60L)
            val id2 = repository.saveSession(session, MathOperation.ADDITION, 70L)

            assertNotEquals(id1, id2)
            assertEquals(2, fakeDao.insertedSessions.size)
        }

    @Test
    fun `getOverallStats accuracy calculation is correct`() =
        runTest {
            fakeDao.totalProblems.value = 100
            fakeDao.totalCorrect.value = 75
            fakeDao.sessionCount.value = 10

            val stats = repository.getOverallStats().first()
            assertEquals(75f, stats.accuracy)
        }

    @Test
    fun `getStatsByOperation groups multiple sessions correctly`() =
        runTest {
            val session1 = createSessionEntity(1, MathOperation.ADDITION, 10, 10)
            val session2 = createSessionEntity(2, MathOperation.ADDITION, 20, 15)
            val session3 = createSessionEntity(3, MathOperation.ADDITION, 30, 20)
            fakeDao.sessionsByOperation[MathOperation.ADDITION] =
                MutableStateFlow(
                    listOf(session1, session2, session3),
                )
            fakeDao.sessionCount.value = 3

            val stats = repository.getStatsByOperation(MathOperation.ADDITION).first()
            assertEquals(60, stats.totalProblems) // 10 + 20 + 30
            assertEquals(45, stats.correctCount) // 10 + 15 + 20
            assertEquals(75f, stats.accuracy) // (45/60) * 100
            assertEquals(3, stats.sessionCount)
        }

    private fun createSessionEntity(
        id: Long,
        operation: MathOperation,
        totalProblems: Int,
        correctAnswers: Int,
    ): PracticeSessionEntity =
        PracticeSessionEntity(
            id = id,
            operation = operation,
            totalProblems = totalProblems,
            correctAnswers = correctAnswers,
            incorrectAnswers = totalProblems - correctAnswers,
            accuracy = (correctAnswers.toFloat() / totalProblems) * 100f,
            durationSeconds = 60L,
            timestamp = Instant.now(),
            gradeLevel = 1,
        )
}

/**
 * Fake implementation of SessionDao for testing.
 */
class FakeSessionDao : SessionDao {
    val insertedSessions = mutableListOf<PracticeSessionEntity>()
    val allSessions = MutableStateFlow<List<PracticeSessionEntity>>(emptyList())
    val recentSessions = MutableStateFlow<List<PracticeSessionEntity>>(emptyList())
    val sessionsByOperation = mutableMapOf<MathOperation, MutableStateFlow<List<PracticeSessionEntity>>>()
    val totalProblems = MutableStateFlow<Int?>(null)
    val totalCorrect = MutableStateFlow<Int?>(null)
    val sessionCount = MutableStateFlow(0)
    var deleteAllCalled = false

    private var nextId = 1L

    override suspend fun insertSession(session: PracticeSessionEntity): Long {
        val id = nextId++
        insertedSessions.add(session.copy(id = id))
        return id
    }

    override fun getAllSessions(): Flow<List<PracticeSessionEntity>> = allSessions

    override fun getRecentSessions(limit: Int): Flow<List<PracticeSessionEntity>> = recentSessions

    override fun getSessionsByOperation(operation: MathOperation): Flow<List<PracticeSessionEntity>> =
        sessionsByOperation.getOrPut(operation) { MutableStateFlow(emptyList()) }

    override fun getTotalProblemsCount(): Flow<Int?> = totalProblems

    override fun getTotalCorrectCount(): Flow<Int?> = totalCorrect

    override fun getSessionCount(): Flow<Int> = sessionCount

    override suspend fun deleteAllSessions() {
        deleteAllCalled = true
        insertedSessions.clear()
    }

    override fun getTodaySessions(
        startOfDayMillis: Long,
        endOfDayMillis: Long,
    ): Flow<List<PracticeSessionEntity>> = MutableStateFlow(emptyList())
}
