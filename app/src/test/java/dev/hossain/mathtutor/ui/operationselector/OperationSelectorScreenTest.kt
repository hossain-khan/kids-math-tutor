package dev.hossain.mathtutor.ui.operationselector

import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
        assertTrue(state.hasSessionHistory)
        assertNotNull(state.eventSink)
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
        assertFalse(state.hasSessionHistory)
    }

    @Test
    fun event_operationSelected_createsCorrectEvent() {
        // When
        val event =
            OperationSelectorScreen.Event.OperationSelected(
                MathOperation.ADDITION,
            )

        // Then
        assertEquals(MathOperation.ADDITION, event.operation)
    }

    @Test
    fun event_viewStatsClicked_createsCorrectEvent() {
        // When
        val event = OperationSelectorScreen.Event.ViewStatsClicked

        // Then - verify it's the singleton object
        assertEquals(OperationSelectorScreen.Event.ViewStatsClicked, event)
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
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is OperationSelectorScreen.Event.OperationSelected)
        assertEquals(
            MathOperation.SUBTRACTION,
            (receivedEvent as OperationSelectorScreen.Event.OperationSelected).operation,
        )
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
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is OperationSelectorScreen.Event.ViewStatsClicked)
    }
}
