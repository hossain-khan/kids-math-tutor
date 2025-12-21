package dev.hossain.mathtutor.data.repository

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsParam
import dev.hossain.mathtutor.analytics.FakeAnalyticsService
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
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

class SessionRepositoryImplTest {
    private lateinit var fakeDao: FakeSessionDao
    private lateinit var fakeAnalytics: FakeAnalyticsService
    private lateinit var repository: SessionRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeSessionDao()
        fakeAnalytics = FakeAnalyticsService()
        repository = SessionRepositoryImpl(fakeDao, fakeAnalytics)
    }

    @Test
    fun `saveSession inserts entity correctly`() =
        runTest {
            val problems = listOf(MathProblem(num1 = 1, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 3))
            val session =
                PracticeSession(
                    totalProblems = 1,
                    problems = problems,
                    answers =
                        mutableMapOf(
                            problems[0].id to SessionAnswer(problemId = problems[0].id, userAnswer = 3, isCorrect = true, attemptCount = 1),
                        ),
                )

            val id = repository.saveSession(session, MathOperation.ADDITION, 60L, 1)

            assertThat(id).isEqualTo(1L)
            assertThat(fakeDao.insertedSessions.size).isEqualTo(1)
            val inserted = fakeDao.insertedSessions[0]
            assertThat(inserted.operation).isEqualTo(MathOperation.ADDITION)
            assertThat(inserted.totalProblems).isEqualTo(1)
            assertThat(inserted.correctAnswers).isEqualTo(1)
            assertThat(inserted.incorrectAnswers).isEqualTo(0)
            assertThat(inserted.accuracy).isEqualTo(100f)
            assertThat(inserted.durationSeconds).isEqualTo(60L)
            assertThat(inserted.gradeLevel).isEqualTo(1)
        }

    @Test
    fun `getAllSessions returns all sessions from DAO`() =
        runTest {
            val session1 = createSessionEntity(1, MathOperation.ADDITION, 10, 8)
            val session2 = createSessionEntity(2, MathOperation.SUBTRACTION, 10, 7)
            fakeDao.allSessions.value = listOf(session1, session2)

            val sessions = repository.getAllSessions().first()
            assertThat(sessions.size).isEqualTo(2)
            assertThat(sessions[0]).isEqualTo(session1)
            assertThat(sessions[1]).isEqualTo(session2)
        }

    @Test
    fun `getRecentSessions returns limited sessions`() =
        runTest {
            val session1 = createSessionEntity(1, MathOperation.ADDITION, 10, 9)
            val session2 = createSessionEntity(2, MathOperation.ADDITION, 10, 8)
            fakeDao.recentSessions.value = listOf(session1, session2)

            val sessions = repository.getRecentSessions(5).first()
            assertThat(sessions.size).isEqualTo(2)
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
            assertThat(sessions.size).isEqualTo(1)
            assertThat(sessions[0].operation).isEqualTo(MathOperation.ADDITION)
        }

    @Test
    fun `getOverallStats calculates correct statistics`() =
        runTest {
            fakeDao.totalProblems.value = 30
            fakeDao.totalCorrect.value = 24
            fakeDao.sessionCount.value = 3

            val stats = repository.getOverallStats().first()
            assertThat(stats.totalProblems).isEqualTo(30)
            assertThat(stats.correctCount).isEqualTo(24)
            assertThat(stats.accuracy).isEqualTo(80f)
            assertThat(stats.sessionCount).isEqualTo(3)
        }

    @Test
    fun `getOverallStats returns EMPTY when no sessions exist`() =
        runTest {
            fakeDao.totalProblems.value = null
            fakeDao.totalCorrect.value = null
            fakeDao.sessionCount.value = 0

            val stats = repository.getOverallStats().first()
            assertThat(stats).isEqualTo(SessionStats.EMPTY)
        }

    @Test
    fun `getOverallStats handles partial null values`() =
        runTest {
            fakeDao.totalProblems.value = 10
            fakeDao.totalCorrect.value = null
            fakeDao.sessionCount.value = 1

            val stats = repository.getOverallStats().first()
            assertThat(stats).isEqualTo(SessionStats.EMPTY)
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
            assertThat(stats.totalProblems).isEqualTo(20)
            assertThat(stats.correctCount).isEqualTo(17)
            assertThat(stats.accuracy).isEqualTo(85f)
            assertThat(stats.sessionCount).isEqualTo(2)
        }

    @Test
    fun `getStatsByOperation returns EMPTY when no sessions for operation`() =
        runTest {
            fakeDao.sessionsByOperation[MathOperation.SUBTRACTION] = MutableStateFlow(emptyList())
            fakeDao.sessionCount.value = 0

            val stats = repository.getStatsByOperation(MathOperation.SUBTRACTION).first()
            assertThat(stats).isEqualTo(SessionStats.EMPTY)
        }

    @Test
    fun `clearAllSessions calls DAO delete method`() =
        runTest {
            repository.clearAllSessions()
            assertThat(fakeDao.deleteAllCalled).isEqualTo(true)
        }

    @Test
    fun `saveSession with different operations`() =
        runTest {
            val problems = listOf(MathProblem(num1 = 5, num2 = 3, operation = MathOperation.SUBTRACTION, correctAnswer = 2))
            val session =
                PracticeSession(
                    totalProblems = 1,
                    problems = problems,
                    answers =
                        mutableMapOf(
                            problems[0].id to SessionAnswer(problemId = problems[0].id, userAnswer = 2, isCorrect = true, attemptCount = 1),
                        ),
                )

            repository.saveSession(session, MathOperation.SUBTRACTION, 45L, 2)

            val inserted = fakeDao.insertedSessions[0]
            assertThat(inserted.operation).isEqualTo(MathOperation.SUBTRACTION)
        }

    @Test
    fun `saveSession increments ID on multiple inserts`() =
        runTest {
            val problems = listOf(MathProblem(num1 = 1, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 3))
            val session =
                PracticeSession(
                    totalProblems = 1,
                    problems = problems,
                    answers =
                        mutableMapOf(
                            problems[0].id to SessionAnswer(problemId = problems[0].id, userAnswer = 3, isCorrect = true, attemptCount = 1),
                        ),
                )

            val id1 = repository.saveSession(session, MathOperation.ADDITION, 60L)
            val id2 = repository.saveSession(session, MathOperation.ADDITION, 70L)

            assertNotEquals(id1, id2)
            assertThat(fakeDao.insertedSessions.size).isEqualTo(2)
        }

    @Test
    fun `getOverallStats accuracy calculation is correct`() =
        runTest {
            fakeDao.totalProblems.value = 100
            fakeDao.totalCorrect.value = 75
            fakeDao.sessionCount.value = 10

            val stats = repository.getOverallStats().first()
            assertThat(stats.accuracy).isEqualTo(75f)
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
            assertThat(stats.totalProblems).isEqualTo(60) // 10 + 20 + 30
            assertThat(stats.correctCount).isEqualTo(45) // 10 + 15 + 20
            assertThat(stats.accuracy).isEqualTo(75f) // (45/60) * 100
            assertThat(stats.sessionCount).isEqualTo(3)
        }

    @Test
    fun `saveSession logs analytics event with session details`() =
        runTest {
            val problems = listOf(MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8))
            val session =
                PracticeSession(
                    totalProblems = 10,
                    problems = problems,
                    answers =
                        mutableMapOf(
                            problems[0].id to SessionAnswer(problemId = problems[0].id, userAnswer = 8, isCorrect = true, attemptCount = 1),
                        ),
                )

            repository.saveSession(session, MathOperation.ADDITION, 120L, 1)

            // Verify analytics event logged
            val events = fakeAnalytics.getEventsWithName(AnalyticsEvent.PRACTICE_SESSION_COMPLETED)
            assertThat(events).hasSize(1)
            assertThat(events.first().parameters[AnalyticsParam.OPERATION_TYPE]).isEqualTo(MathOperation.ADDITION.name)
            assertThat(events.first().parameters[AnalyticsParam.PROBLEM_COUNT]).isEqualTo(10)
            assertThat(events.first().parameters[AnalyticsParam.CORRECT_ANSWERS]).isEqualTo(1)
            assertThat(events.first().parameters[AnalyticsParam.SESSION_DURATION]).isEqualTo(120L)
        }

    @Test
    fun `saveSession logs error on failure`() =
        runTest {
            val problems = listOf(MathProblem(num1 = 1, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 3))
            val session =
                PracticeSession(
                    totalProblems = 1,
                    problems = problems,
                    answers =
                        mutableMapOf(
                            problems[0].id to SessionAnswer(problemId = problems[0].id, userAnswer = 3, isCorrect = true, attemptCount = 1),
                        ),
                )
            fakeDao.shouldThrowOnInsert = true

            try {
                repository.saveSession(session, MathOperation.ADDITION, 60L, 1)
            } catch (e: Exception) {
                // Expected exception
            }

            // Verify error logged
            assertThat(fakeAnalytics.errors).hasSize(1)
            assertThat(fakeAnalytics.errors.first().context).isEqualTo("Session save failed")
            assertThat(fakeAnalytics.errors.first().isFatal).isFalse()
        }

    @Test
    fun `saveSession updates total problems solved user property`() =
        runTest {
            val problems = listOf(MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8))
            val session =
                PracticeSession(
                    totalProblems = 10,
                    problems = problems,
                    answers =
                        mutableMapOf(
                            problems[0].id to SessionAnswer(problemId = problems[0].id, userAnswer = 8, isCorrect = true, attemptCount = 1),
                        ),
                )
            // Set up fake DAO to return stats with 25 total problems
            fakeDao.totalProblems.value = 25
            fakeDao.totalCorrect.value = 20
            fakeDao.sessionCount.value = 3

            repository.saveSession(session, MathOperation.ADDITION, 120L, 1)

            // Verify user property updated
            assertThat(fakeAnalytics.userProperties).isNotEmpty()
            val totalProblemsProperty =
                fakeAnalytics.userProperties.find { it.propertyName == "total_problems_solved" }
            assertThat(totalProblemsProperty).isNotNull()
            assertThat(totalProblemsProperty?.value).isEqualTo("25")
        }

    @Test
    fun `clearAllSessions logs error on failure`() =
        runTest {
            fakeDao.shouldThrowOnDelete = true

            try {
                repository.clearAllSessions()
            } catch (e: Exception) {
                // Expected exception
            }

            // Verify error logged
            assertThat(fakeAnalytics.errors).hasSize(1)
            assertThat(fakeAnalytics.errors.first().context).isEqualTo("Session clear failed")
            assertThat(fakeAnalytics.errors.first().isFatal).isFalse()
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
    var shouldThrowOnInsert = false
    var shouldThrowOnDelete = false

    private var nextId = 1L

    override suspend fun insertSession(session: PracticeSessionEntity): Long {
        if (shouldThrowOnInsert) {
            throw RuntimeException("Failed to insert session")
        }
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
        if (shouldThrowOnDelete) {
            throw RuntimeException("Failed to delete sessions")
        }
        deleteAllCalled = true
        insertedSessions.clear()
    }

    override fun getTodaySessions(
        startOfDayMillis: Long,
        endOfDayMillis: Long,
    ): Flow<List<PracticeSessionEntity>> = MutableStateFlow(emptyList())
}
