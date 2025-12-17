package dev.hossain.mathtutor.ui.stats

import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.SessionStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for [StatsScreen].
 *
 * Tests the screen state and events.
 */
class StatsScreenTest {
    @Test
    fun state_hasCorrectProperties() {
        // Given
        val overallStats =
            SessionStats(
                totalProblems = 50,
                correctCount = 45,
                accuracy = 90f,
                sessionCount = 5,
            )
        val operationStats =
            mapOf(
                MathOperation.ADDITION to
                    SessionStats(
                        totalProblems = 30,
                        correctCount = 27,
                        accuracy = 90f,
                        sessionCount = 3,
                    ),
            )
        val recentSessions =
            listOf(
                PracticeSessionEntity(
                    id = 1,
                    operation = MathOperation.ADDITION,
                    totalProblems = 10,
                    correctAnswers = 9,
                    incorrectAnswers = 1,
                    accuracy = 90f,
                    durationSeconds = 120,
                    timestamp = Instant.now(),
                ),
            )
        var eventReceived: StatsScreen.Event? = null
        val eventSink: (StatsScreen.Event) -> Unit = { event ->
            eventReceived = event
        }

        // When
        val state =
            StatsScreen.State(
                overallStats = overallStats,
                operationStats = operationStats,
                recentSessions = recentSessions,
                eventSink = eventSink,
            )

        // Then
        assertEquals(overallStats, state.overallStats)
        assertEquals(operationStats, state.operationStats)
        assertEquals(recentSessions, state.recentSessions)
        assertNotNull(state.eventSink)
    }

    @Test
    fun state_withEmptyStats_hasCorrectProperties() {
        // Given
        val overallStats = SessionStats.EMPTY
        val operationStats = emptyMap<MathOperation, SessionStats>()
        val recentSessions = emptyList<PracticeSessionEntity>()
        val eventSink: (StatsScreen.Event) -> Unit = { }

        // When
        val state =
            StatsScreen.State(
                overallStats = overallStats,
                operationStats = operationStats,
                recentSessions = recentSessions,
                eventSink = eventSink,
            )

        // Then
        assertEquals(SessionStats.EMPTY, state.overallStats)
        assertTrue(state.operationStats.isEmpty())
        assertTrue(state.recentSessions.isEmpty())
    }

    @Test
    fun event_backPressed_createsCorrectEvent() {
        // When
        val event = StatsScreen.Event.BackPressed

        // Then
        assertTrue(event is StatsScreen.Event.BackPressed)
    }

    @Test
    fun eventSink_backPressed_receivesEvent() {
        // Given
        var receivedEvent: StatsScreen.Event? = null
        val state =
            StatsScreen.State(
                overallStats = SessionStats.EMPTY,
                operationStats = emptyMap(),
                recentSessions = emptyList(),
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(StatsScreen.Event.BackPressed)

        // Then
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is StatsScreen.Event.BackPressed)
    }

    @Test
    fun state_withMultipleOperations_hasCorrectStats() {
        // Given
        val additionStats =
            SessionStats(
                totalProblems = 30,
                correctCount = 27,
                accuracy = 90f,
                sessionCount = 3,
            )
        val subtractionStats =
            SessionStats(
                totalProblems = 20,
                correctCount = 18,
                accuracy = 90f,
                sessionCount = 2,
            )
        val operationStats =
            mapOf(
                MathOperation.ADDITION to additionStats,
                MathOperation.SUBTRACTION to subtractionStats,
            )

        // When
        val state =
            StatsScreen.State(
                overallStats = SessionStats.EMPTY,
                operationStats = operationStats,
                recentSessions = emptyList(),
                eventSink = {},
            )

        // Then
        assertEquals(2, state.operationStats.size)
        assertEquals(additionStats, state.operationStats[MathOperation.ADDITION])
        assertEquals(subtractionStats, state.operationStats[MathOperation.SUBTRACTION])
    }

    @Test
    fun state_withRecentSessions_maintainsOrder() {
        // Given
        val session1 =
            PracticeSessionEntity(
                id = 1,
                operation = MathOperation.ADDITION,
                totalProblems = 10,
                correctAnswers = 9,
                incorrectAnswers = 1,
                accuracy = 90f,
                durationSeconds = 120,
                timestamp = Instant.now(),
            )
        val session2 =
            PracticeSessionEntity(
                id = 2,
                operation = MathOperation.SUBTRACTION,
                totalProblems = 10,
                correctAnswers = 8,
                incorrectAnswers = 2,
                accuracy = 80f,
                durationSeconds = 150,
                timestamp = Instant.now().minusSeconds(86400),
            )
        val recentSessions = listOf(session1, session2)

        // When
        val state =
            StatsScreen.State(
                overallStats = SessionStats.EMPTY,
                operationStats = emptyMap(),
                recentSessions = recentSessions,
                eventSink = {},
            )

        // Then
        assertEquals(2, state.recentSessions.size)
        assertEquals(session1, state.recentSessions[0])
        assertEquals(session2, state.recentSessions[1])
    }
}
