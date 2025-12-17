package dev.hossain.mathtutor.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.hossain.mathtutor.data.local.MathDatabase
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.MathOperation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Instrumented tests for SessionDao database operations.
 * Uses in-memory database for testing to ensure isolated test environment.
 */
@RunWith(AndroidJUnit4::class)
class SessionDaoTest {
    private lateinit var database: MathDatabase
    private lateinit var sessionDao: SessionDao

    @Before
    fun setup() {
        // Create in-memory database for testing
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    MathDatabase::class.java,
                ).allowMainThreadQueries() // For testing only - allows synchronous queries on main thread. Never use in production!
                .build()
        sessionDao = database.sessionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertSession_returnsValidId() =
        runTest {
            val session = createTestSession()

            val id = sessionDao.insertSession(session)

            assertTrue("Inserted ID should be greater than 0", id > 0)
        }

    @Test
    fun insertSession_andRetrieve_returnsCorrectData() =
        runTest {
            val session = createTestSession()
            sessionDao.insertSession(session)

            val sessions = sessionDao.getAllSessions().first()

            assertEquals(1, sessions.size)
            assertEquals(MathOperation.ADDITION, sessions[0].operation)
            assertEquals(10, sessions[0].totalProblems)
            assertEquals(8, sessions[0].correctAnswers)
        }

    @Test
    fun getAllSessions_emptyDatabase_returnsEmptyList() =
        runTest {
            val sessions = sessionDao.getAllSessions().first()

            assertTrue("Should return empty list for empty database", sessions.isEmpty())
        }

    @Test
    fun getAllSessions_multipleSessions_orderedByTimestamp() =
        runTest {
            val session1 = createTestSession(timestamp = Instant.ofEpochMilli(1000))
            val session2 = createTestSession(timestamp = Instant.ofEpochMilli(2000))
            val session3 = createTestSession(timestamp = Instant.ofEpochMilli(3000))

            sessionDao.insertSession(session1)
            sessionDao.insertSession(session2)
            sessionDao.insertSession(session3)

            val sessions = sessionDao.getAllSessions().first()

            assertEquals(3, sessions.size)
            // Should be ordered by timestamp DESC (most recent first)
            assertEquals(3000L, sessions[0].timestamp.toEpochMilli())
            assertEquals(2000L, sessions[1].timestamp.toEpochMilli())
            assertEquals(1000L, sessions[2].timestamp.toEpochMilli())
        }

    @Test
    fun getRecentSessions_limitsResults() =
        runTest {
            // Insert 15 sessions
            repeat(15) { i ->
                sessionDao.insertSession(
                    createTestSession(timestamp = Instant.ofEpochMilli(i * 1000L)),
                )
            }

            val sessions = sessionDao.getRecentSessions(limit = 5).first()

            assertEquals("Should return only 5 most recent sessions", 5, sessions.size)
        }

    @Test
    fun getSessionsByOperation_filtersCorrectly() =
        runTest {
            sessionDao.insertSession(createTestSession(operation = MathOperation.ADDITION))
            sessionDao.insertSession(createTestSession(operation = MathOperation.ADDITION))
            sessionDao.insertSession(createTestSession(operation = MathOperation.SUBTRACTION))

            val additionSessions = sessionDao.getSessionsByOperation(MathOperation.ADDITION).first()
            val subtractionSessions = sessionDao.getSessionsByOperation(MathOperation.SUBTRACTION).first()

            assertEquals(2, additionSessions.size)
            assertEquals(1, subtractionSessions.size)
        }

    @Test
    fun getTotalProblemsCount_calculatesCorrectly() =
        runTest {
            sessionDao.insertSession(createTestSession(totalProblems = 10))
            sessionDao.insertSession(createTestSession(totalProblems = 15))
            sessionDao.insertSession(createTestSession(totalProblems = 20))

            val total = sessionDao.getTotalProblemsCount().first()

            assertEquals(45, total)
        }

    @Test
    fun getTotalProblemsCount_emptyDatabase_returnsNull() =
        runTest {
            val total = sessionDao.getTotalProblemsCount().first()

            assertNull("Should return null for empty database", total)
        }

    @Test
    fun getTotalCorrectCount_calculatesCorrectly() =
        runTest {
            sessionDao.insertSession(createTestSession(correctAnswers = 8))
            sessionDao.insertSession(createTestSession(correctAnswers = 7))
            sessionDao.insertSession(createTestSession(correctAnswers = 10))

            val total = sessionDao.getTotalCorrectCount().first()

            assertEquals(25, total)
        }

    @Test
    fun getTotalCorrectCount_emptyDatabase_returnsNull() =
        runTest {
            val total = sessionDao.getTotalCorrectCount().first()

            assertNull("Should return null for empty database", total)
        }

    @Test
    fun getSessionCount_returnsCorrectCount() =
        runTest {
            sessionDao.insertSession(createTestSession())
            sessionDao.insertSession(createTestSession())
            sessionDao.insertSession(createTestSession())

            val count = sessionDao.getSessionCount().first()

            assertEquals(3, count)
        }

    @Test
    fun getSessionCount_emptyDatabase_returnsZero() =
        runTest {
            val count = sessionDao.getSessionCount().first()

            assertEquals(0, count)
        }

    @Test
    fun deleteAllSessions_removesAllData() =
        runTest {
            sessionDao.insertSession(createTestSession())
            sessionDao.insertSession(createTestSession())
            sessionDao.insertSession(createTestSession())

            sessionDao.deleteAllSessions()
            val sessions = sessionDao.getAllSessions().first()

            assertTrue("Should have no sessions after delete", sessions.isEmpty())
        }

    @Test
    fun getTodaySessions_filtersCurrentDay() =
        runTest {
            // Calculate start and end of today in local timezone
            val now = Instant.now()
            val zonedNow = now.atZone(java.time.ZoneId.systemDefault())
            val startOfDay = zonedNow.toLocalDate().atStartOfDay(zonedNow.zone)
            val endOfDay = startOfDay.plusDays(1)
            
            val startOfDayMillis = startOfDay.toInstant().toEpochMilli()
            val endOfDayMillis = endOfDay.toInstant().toEpochMilli()

            // Insert session with today's timestamp
            val today = Instant.now()
            sessionDao.insertSession(createTestSession(timestamp = today))

            // Insert session from yesterday (30 hours ago to be safe across timezone boundaries)
            val yesterday = Instant.now().minusSeconds(30 * 60 * 60)
            sessionDao.insertSession(createTestSession(timestamp = yesterday))

            val todaySessions = sessionDao.getTodaySessions(startOfDayMillis, endOfDayMillis).first()

            // Should only get today's session
            assertEquals(1, todaySessions.size)
            // Verify it's the today session by checking it's not the yesterday one
            assertTrue(
                "Session should be from today",
                todaySessions[0].timestamp.toEpochMilli() >= startOfDayMillis,
            )
        }

    @Test
    fun practiceSessionEntity_storesAllFields() =
        runTest {
            val session =
                PracticeSessionEntity(
                    operation = MathOperation.SUBTRACTION,
                    totalProblems = 15,
                    correctAnswers = 12,
                    incorrectAnswers = 3,
                    accuracy = 80.0f,
                    durationSeconds = 120,
                    timestamp = Instant.ofEpochMilli(5000),
                    gradeLevel = 1,
                )

            val id = sessionDao.insertSession(session)
            val retrieved = sessionDao.getAllSessions().first()[0]

            assertNotNull(retrieved)
            assertEquals(MathOperation.SUBTRACTION, retrieved.operation)
            assertEquals(15, retrieved.totalProblems)
            assertEquals(12, retrieved.correctAnswers)
            assertEquals(3, retrieved.incorrectAnswers)
            assertEquals(80.0f, retrieved.accuracy, 0.01f)
            assertEquals(120, retrieved.durationSeconds)
            assertEquals(5000L, retrieved.timestamp.toEpochMilli())
            assertEquals(1, retrieved.gradeLevel)
        }

    // Helper function to create test session
    private fun createTestSession(
        operation: MathOperation = MathOperation.ADDITION,
        totalProblems: Int = 10,
        correctAnswers: Int = 8,
        incorrectAnswers: Int = 2,
        accuracy: Float = 80.0f,
        durationSeconds: Long = 60,
        timestamp: Instant = Instant.now(),
        gradeLevel: Int? = null,
    ): PracticeSessionEntity =
        PracticeSessionEntity(
            operation = operation,
            totalProblems = totalProblems,
            correctAnswers = correctAnswers,
            incorrectAnswers = incorrectAnswers,
            accuracy = accuracy,
            durationSeconds = durationSeconds,
            timestamp = timestamp,
            gradeLevel = gradeLevel,
        )
}
