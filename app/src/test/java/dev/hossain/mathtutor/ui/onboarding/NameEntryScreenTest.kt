package dev.hossain.mathtutor.ui.onboarding

import dev.hossain.mathtutor.domain.model.GradeLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [NameEntryScreen].
 *
 * Tests the screen state and events.
 */
class NameEntryScreenTest {
    @Test
    fun screen_hasCorrectGradeLevel() {
        // When
        val screen = NameEntryScreen(gradeLevel = GradeLevel.GRADE_1)

        // Then
        assertEquals(GradeLevel.GRADE_1, screen.gradeLevel)
    }

    @Test
    fun state_withEmptyName_hasCorrectProperties() {
        // Given
        val eventSink: (NameEntryScreen.Event) -> Unit = { }

        // When
        val state =
            NameEntryScreen.State(
                name = "",
                eventSink = eventSink,
            )

        // Then
        assertEquals("", state.name)
        assertNotNull(state.eventSink)
    }

    @Test
    fun state_withName_hasCorrectProperties() {
        // Given
        val eventSink: (NameEntryScreen.Event) -> Unit = { }

        // When
        val state =
            NameEntryScreen.State(
                name = "Alex",
                eventSink = eventSink,
            )

        // Then
        assertEquals("Alex", state.name)
    }

    @Test
    fun event_nameChanged_createsCorrectEvent() {
        // When
        val event = NameEntryScreen.Event.NameChanged("John")

        // Then
        assertTrue(event is NameEntryScreen.Event.NameChanged)
        assertEquals("John", event.name)
    }

    @Test
    fun event_skipClicked_createsCorrectEvent() {
        // When
        val event = NameEntryScreen.Event.SkipClicked

        // Then
        assertTrue(event is NameEntryScreen.Event.SkipClicked)
    }

    @Test
    fun event_continueClicked_createsCorrectEvent() {
        // When
        val event = NameEntryScreen.Event.ContinueClicked

        // Then
        assertTrue(event is NameEntryScreen.Event.ContinueClicked)
    }

    @Test
    fun eventSink_nameChanged_receivesEvent() {
        // Given
        var receivedEvent: NameEntryScreen.Event? = null
        val state =
            NameEntryScreen.State(
                name = "",
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(NameEntryScreen.Event.NameChanged("Sarah"))

        // Then
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is NameEntryScreen.Event.NameChanged)
        assertEquals(
            "Sarah",
            (receivedEvent as NameEntryScreen.Event.NameChanged).name,
        )
    }

    @Test
    fun eventSink_skipClicked_receivesEvent() {
        // Given
        var receivedEvent: NameEntryScreen.Event? = null
        val state =
            NameEntryScreen.State(
                name = "",
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(NameEntryScreen.Event.SkipClicked)

        // Then
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is NameEntryScreen.Event.SkipClicked)
    }

    @Test
    fun eventSink_continueClicked_receivesEvent() {
        // Given
        var receivedEvent: NameEntryScreen.Event? = null
        val state =
            NameEntryScreen.State(
                name = "Alex",
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(NameEntryScreen.Event.ContinueClicked)

        // Then
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is NameEntryScreen.Event.ContinueClicked)
    }
}
