package dev.hossain.mathtutor.ui.onboarding

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.GradeLevel
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
        assertThat(state.selectedGrade).isNull()
        assertThat(state.eventSink).isNotNull()
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
        assertThat(state.selectedGrade).isEqualTo(GradeLevel.KINDERGARTEN)
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
        assertThat(state.selectedGrade).isEqualTo(GradeLevel.GRADE_1)
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
        assertThat(state.selectedGrade).isEqualTo(GradeLevel.GRADE_2)
    }

    @Test
    fun event_gradeSelected_createsCorrectEvent() {
        // When
        val event = GradeSelectionScreen.Event.GradeSelected(GradeLevel.KINDERGARTEN)

        // Then
        assertThat(event.grade).isEqualTo(GradeLevel.KINDERGARTEN)
    }

    @Test
    fun event_continueClicked_createsCorrectEvent() {
        // When
        val event = GradeSelectionScreen.Event.ContinueClicked

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(GradeSelectionScreen.Event.ContinueClicked)
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
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is GradeSelectionScreen.Event.GradeSelected).isTrue()
        assertThat((receivedEvent as GradeSelectionScreen.Event.GradeSelected).grade).isEqualTo(GradeLevel.GRADE_1)
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
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is GradeSelectionScreen.Event.ContinueClicked).isTrue()
    }
}
