package dev.hossain.mathtutor.ui.onboarding

import dev.hossain.mathtutor.domain.model.GradeLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [GradeSelectionScreen].
 *
 * Tests the screen state and events.
 */
class GradeSelectionScreenTest {
    @Test
    fun state_withNoSelection_hasCorrectProperties() {
        // Given
        val eventSink: (GradeSelectionScreen.Event) -> Unit = { }

        // When
        val state =
            GradeSelectionScreen.State(
                selectedGrade = null,
                eventSink = eventSink,
            )

        // Then
        assertNull(state.selectedGrade)
        assertNotNull(state.eventSink)
    }

    @Test
    fun state_withKindergartenSelected_hasCorrectProperties() {
        // Given
        val eventSink: (GradeSelectionScreen.Event) -> Unit = { }

        // When
        val state =
            GradeSelectionScreen.State(
                selectedGrade = GradeLevel.KINDERGARTEN,
                eventSink = eventSink,
            )

        // Then
        assertEquals(GradeLevel.KINDERGARTEN, state.selectedGrade)
    }

    @Test
    fun state_withGrade1Selected_hasCorrectProperties() {
        // Given
        val eventSink: (GradeSelectionScreen.Event) -> Unit = { }

        // When
        val state =
            GradeSelectionScreen.State(
                selectedGrade = GradeLevel.GRADE_1,
                eventSink = eventSink,
            )

        // Then
        assertEquals(GradeLevel.GRADE_1, state.selectedGrade)
    }

    @Test
    fun state_withGrade2Selected_hasCorrectProperties() {
        // Given
        val eventSink: (GradeSelectionScreen.Event) -> Unit = { }

        // When
        val state =
            GradeSelectionScreen.State(
                selectedGrade = GradeLevel.GRADE_2,
                eventSink = eventSink,
            )

        // Then
        assertEquals(GradeLevel.GRADE_2, state.selectedGrade)
    }

    @Test
    fun event_gradeSelected_createsCorrectEvent() {
        // When
        val event = GradeSelectionScreen.Event.GradeSelected(GradeLevel.KINDERGARTEN)

        // Then
        assertTrue(event is GradeSelectionScreen.Event.GradeSelected)
        assertEquals(GradeLevel.KINDERGARTEN, event.grade)
    }

    @Test
    fun event_continueClicked_createsCorrectEvent() {
        // When
        val event = GradeSelectionScreen.Event.ContinueClicked

        // Then
        assertTrue(event is GradeSelectionScreen.Event.ContinueClicked)
    }

    @Test
    fun eventSink_gradeSelected_receivesEvent() {
        // Given
        var receivedEvent: GradeSelectionScreen.Event? = null
        val state =
            GradeSelectionScreen.State(
                selectedGrade = null,
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(GradeSelectionScreen.Event.GradeSelected(GradeLevel.GRADE_1))

        // Then
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is GradeSelectionScreen.Event.GradeSelected)
        assertEquals(
            GradeLevel.GRADE_1,
            (receivedEvent as GradeSelectionScreen.Event.GradeSelected).grade,
        )
    }

    @Test
    fun eventSink_continueClicked_receivesEvent() {
        // Given
        var receivedEvent: GradeSelectionScreen.Event? = null
        val state =
            GradeSelectionScreen.State(
                selectedGrade = GradeLevel.GRADE_2,
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(GradeSelectionScreen.Event.ContinueClicked)

        // Then
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is GradeSelectionScreen.Event.ContinueClicked)
    }
}
