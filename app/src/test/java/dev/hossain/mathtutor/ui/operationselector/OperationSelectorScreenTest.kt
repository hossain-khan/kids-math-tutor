package dev.hossain.mathtutor.ui.operationselector

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Test

/**
 * Unit tests for [OperationSelectorScreen].
 *
 * Tests the screen state and events.
 */
class OperationSelectorScreenTest {
    @Test
    fun state_hasCorrectProperties() {
        // Given
        val hasHistory = true
        var eventReceived: OperationSelectorScreen.Event? = null
        val eventSink: (OperationSelectorScreen.Event) -> Unit = { event ->
            eventReceived = event
        }

        // When
        val state =
            OperationSelectorScreen.State(
                hasSessionHistory = hasHistory,
                eventSink = eventSink,
            )

        // Then
        assertThat(state.hasSessionHistory).isTrue()
        assertThat(state.eventSink).isNotNull()
    }

    @Test
    fun state_withNoHistory_hasCorrectProperties() {
        // Given
        val hasHistory = false
        val eventSink: (OperationSelectorScreen.Event) -> Unit = { }

        // When
        val state =
            OperationSelectorScreen.State(
                hasSessionHistory = hasHistory,
                eventSink = eventSink,
            )

        // Then
        assertThat(state.hasSessionHistory).isFalse()
    }

    @Test
    fun event_operationSelected_createsCorrectEvent() {
        // When
        val event =
            OperationSelectorScreen.Event.OperationSelected(
                MathOperation.ADDITION,
            )

        // Then
        assertThat(event.operation).isEqualTo(MathOperation.ADDITION)
    }

    @Test
    fun event_viewStatsClicked_createsCorrectEvent() {
        // When
        val event = OperationSelectorScreen.Event.ViewStatsClicked

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(OperationSelectorScreen.Event.ViewStatsClicked)
    }

    @Test
    fun eventSink_operationSelected_receivesEvent() {
        // Given
        var receivedEvent: OperationSelectorScreen.Event? = null
        val state =
            OperationSelectorScreen.State(
                hasSessionHistory = false,
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(
            OperationSelectorScreen.Event.OperationSelected(
                MathOperation.SUBTRACTION,
            ),
        )

        // Then
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is OperationSelectorScreen.Event.OperationSelected).isTrue()
        assertThat((receivedEvent as OperationSelectorScreen.Event.OperationSelected).operation).isEqualTo(MathOperation.SUBTRACTION)
    }

    @Test
    fun eventSink_viewStatsClicked_receivesEvent() {
        // Given
        var receivedEvent: OperationSelectorScreen.Event? = null
        val state =
            OperationSelectorScreen.State(
                hasSessionHistory = true,
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(OperationSelectorScreen.Event.ViewStatsClicked)

        // Then
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is OperationSelectorScreen.Event.ViewStatsClicked).isTrue()
    }
}
