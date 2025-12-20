package dev.hossain.mathtutor.ui.stats

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.SessionStats
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
        assertThat(state.overallStats).isEqualTo(overallStats)
        assertThat(state.operationStats).isEqualTo(operationStats)
        assertThat(state.recentSessions).isEqualTo(recentSessions)
        assertThat(state.eventSink).isNotNull()
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
        assertThat(state.overallStats).isEqualTo(SessionStats.EMPTY)
        assertThat(state.operationStats.isEmpty()).isTrue()
        assertThat(state.recentSessions.isEmpty()).isTrue()
    }

    @Test
    fun event_backPressed_createsCorrectEvent() {
        // When
        val event = StatsScreen.Event.BackPressed

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(StatsScreen.Event.BackPressed)
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
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is StatsScreen.Event.BackPressed).isTrue()
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
        assertThat(state.operationStats.size).isEqualTo(2)
        assertThat(state.operationStats[MathOperation.ADDITION]).isEqualTo(additionStats)
        assertThat(state.operationStats[MathOperation.SUBTRACTION]).isEqualTo(subtractionStats)
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
        assertThat(state.recentSessions.size).isEqualTo(2)
        assertThat(state.recentSessions[0]).isEqualTo(session1)
        assertThat(state.recentSessions[1]).isEqualTo(session2)
    }
}
