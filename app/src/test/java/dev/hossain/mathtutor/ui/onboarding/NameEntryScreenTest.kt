package dev.hossain.mathtutor.ui.onboarding

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.GradeLevel
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
        assertThat(screen.gradeLevel).isEqualTo(GradeLevel.GRADE_1)
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
        assertThat(state.name).isEqualTo("")
        assertThat(state.eventSink).isNotNull()
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
        assertThat(state.name).isEqualTo("Alex")
    }

    @Test
    fun event_nameChanged_createsCorrectEvent() {
        // When
        val event = NameEntryScreen.Event.NameChanged("John")

        // Then
        assertThat(event.name).isEqualTo("John")
    }

    @Test
    fun event_skipClicked_createsCorrectEvent() {
        // When
        val event = NameEntryScreen.Event.SkipClicked

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(NameEntryScreen.Event.SkipClicked)
    }

    @Test
    fun event_continueClicked_createsCorrectEvent() {
        // When
        val event = NameEntryScreen.Event.ContinueClicked

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(NameEntryScreen.Event.ContinueClicked)
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
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is NameEntryScreen.Event.NameChanged).isTrue()
        assertThat((receivedEvent as NameEntryScreen.Event.NameChanged).name).isEqualTo("Sarah")
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
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is NameEntryScreen.Event.SkipClicked).isTrue()
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
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is NameEntryScreen.Event.ContinueClicked).isTrue()
    }
}
