package dev.hossain.mathtutor.ui.settings

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for [SettingsScreen].
 *
 * Tests the screen state and events for settings and profile management.
 */
class SettingsScreenTest {
    @Test
    fun state_withProfile_hasCorrectProperties() {
        // Given
        val profile =
            UserProfile(
                name = "Sarah",
                gradeLevel = GradeLevel.GRADE_1,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        var eventReceived: SettingsScreen.Event? = null
        val eventSink: (SettingsScreen.Event) -> Unit = { event ->
            eventReceived = event
        }

        // When
        val state =
            SettingsScreen.State(
                profile = profile,
                showNameDialog = false,
                showGradeDialog = false,
                eventSink = eventSink,
            )

        // Then
        assertThat(state.profile).isEqualTo(profile)
        assertThat(state.showNameDialog).isFalse()
        assertThat(state.showGradeDialog).isFalse()
        assertThat(state.eventSink).isNotNull()
    }

    @Test
    fun state_withNullProfile_hasNullProfile() {
        // Given
        val eventSink: (SettingsScreen.Event) -> Unit = { }

        // When
        val state =
            SettingsScreen.State(
                profile = null,
                showNameDialog = false,
                showGradeDialog = false,
                eventSink = eventSink,
            )

        // Then
        assertThat(state.profile).isNull()
    }

    @Test
    fun state_withNameDialogVisible_hasCorrectFlag() {
        // Given
        val profile =
            UserProfile(
                name = "Alex",
                gradeLevel = GradeLevel.KINDERGARTEN,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = false,
            )
        val eventSink: (SettingsScreen.Event) -> Unit = { }

        // When
        val state =
            SettingsScreen.State(
                profile = profile,
                showNameDialog = true,
                showGradeDialog = false,
                eventSink = eventSink,
            )

        // Then
        assertThat(state.showNameDialog).isTrue()
        assertThat(state.showGradeDialog).isFalse()
    }

    @Test
    fun state_withGradeDialogVisible_hasCorrectFlag() {
        // Given
        val profile =
            UserProfile(
                name = null,
                gradeLevel = GradeLevel.GRADE_2,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        val eventSink: (SettingsScreen.Event) -> Unit = { }

        // When
        val state =
            SettingsScreen.State(
                profile = profile,
                showNameDialog = false,
                showGradeDialog = true,
                eventSink = eventSink,
            )

        // Then
        assertThat(state.showNameDialog).isFalse()
        assertThat(state.showGradeDialog).isTrue()
    }

    @Test
    fun event_editNameClicked_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.EditNameClicked

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(SettingsScreen.Event.EditNameClicked)
    }

    @Test
    fun event_changeGradeClicked_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.ChangeGradeClicked

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(SettingsScreen.Event.ChangeGradeClicked)
    }

    @Test
    fun event_toggleAdaptiveDifficulty_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.ToggleAdaptiveDifficulty(true)

        // Then
        assertThat(event.enabled).isTrue()
    }

    @Test
    fun event_saveName_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.SaveName("John")

        // Then
        assertThat(event.name).isEqualTo("John")
    }

    @Test
    fun event_saveName_withNullName_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.SaveName(null)

        // Then
        assertThat(event.name).isNull()
    }

    @Test
    fun event_cancelNameEdit_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.CancelNameEdit

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(SettingsScreen.Event.CancelNameEdit)
    }

    @Test
    fun event_saveGrade_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.SaveGrade(GradeLevel.GRADE_1)

        // Then
        assertThat(event.gradeLevel).isEqualTo(GradeLevel.GRADE_1)
    }

    @Test
    fun event_cancelGradeChange_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.CancelGradeChange

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(SettingsScreen.Event.CancelGradeChange)
    }

    @Test
    fun event_backClicked_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.BackClicked

        // Then - verify it's the singleton object
        assertThat(event).isEqualTo(SettingsScreen.Event.BackClicked)
    }

    @Test
    fun eventSink_editNameClicked_receivesEvent() {
        // Given
        var receivedEvent: SettingsScreen.Event? = null
        val profile =
            UserProfile(
                name = "Test",
                gradeLevel = GradeLevel.GRADE_1,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        val state =
            SettingsScreen.State(
                profile = profile,
                showNameDialog = false,
                showGradeDialog = false,
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(SettingsScreen.Event.EditNameClicked)

        // Then
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is SettingsScreen.Event.EditNameClicked).isTrue()
    }

    @Test
    fun eventSink_toggleAdaptiveDifficulty_receivesEvent() {
        // Given
        var receivedEvent: SettingsScreen.Event? = null
        val profile =
            UserProfile(
                name = "Test",
                gradeLevel = GradeLevel.GRADE_1,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        val state =
            SettingsScreen.State(
                profile = profile,
                showNameDialog = false,
                showGradeDialog = false,
                eventSink = { event -> receivedEvent = event },
            )

        // When
        state.eventSink(SettingsScreen.Event.ToggleAdaptiveDifficulty(false))

        // Then
        assertThat(receivedEvent).isNotNull()
        assertThat(receivedEvent is SettingsScreen.Event.ToggleAdaptiveDifficulty).isTrue()
        assertThat((receivedEvent as SettingsScreen.Event.ToggleAdaptiveDifficulty).isFalse().enabled)
    }

    @Test
    fun state_withAdaptiveDifficultyEnabled_hasCorrectValue() {
        // Given
        val profile =
            UserProfile(
                name = "Test",
                gradeLevel = GradeLevel.GRADE_1,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = true,
            )
        val eventSink: (SettingsScreen.Event) -> Unit = { }

        // When
        val state =
            SettingsScreen.State(
                profile = profile,
                showNameDialog = false,
                showGradeDialog = false,
                eventSink = eventSink,
            )

        // Then
        assertThat(state.profile?.adaptiveDifficultyEnabled ?: false).isTrue()
    }

    @Test
    fun state_withAdaptiveDifficultyDisabled_hasCorrectValue() {
        // Given
        val profile =
            UserProfile(
                name = "Test",
                gradeLevel = GradeLevel.GRADE_2,
                createdAt = Instant.now(),
                adaptiveDifficultyEnabled = false,
            )
        val eventSink: (SettingsScreen.Event) -> Unit = { }

        // When
        val state =
            SettingsScreen.State(
                profile = profile,
                showNameDialog = false,
                showGradeDialog = false,
                eventSink = eventSink,
            )

        // Then
        assertThat(state.profile?.adaptiveDifficultyEnabled ?: true).isFalse()
    }
}
