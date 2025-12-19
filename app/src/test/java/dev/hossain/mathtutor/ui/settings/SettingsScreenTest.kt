package dev.hossain.mathtutor.ui.settings

import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        assertEquals(profile, state.profile)
        assertFalse(state.showNameDialog)
        assertFalse(state.showGradeDialog)
        assertNotNull(state.eventSink)
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
        assertNull(state.profile)
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
        assertTrue(state.showNameDialog)
        assertFalse(state.showGradeDialog)
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
        assertFalse(state.showNameDialog)
        assertTrue(state.showGradeDialog)
    }

    @Test
    fun event_editNameClicked_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.EditNameClicked

        // Then
        assertTrue(event is SettingsScreen.Event.EditNameClicked)
    }

    @Test
    fun event_changeGradeClicked_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.ChangeGradeClicked

        // Then
        assertTrue(event is SettingsScreen.Event.ChangeGradeClicked)
    }

    @Test
    fun event_toggleAdaptiveDifficulty_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.ToggleAdaptiveDifficulty(true)

        // Then
        assertTrue(event is SettingsScreen.Event.ToggleAdaptiveDifficulty)
        assertTrue((event as SettingsScreen.Event.ToggleAdaptiveDifficulty).enabled)
    }

    @Test
    fun event_saveName_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.SaveName("John")

        // Then
        assertTrue(event is SettingsScreen.Event.SaveName)
        assertEquals("John", (event as SettingsScreen.Event.SaveName).name)
    }

    @Test
    fun event_saveName_withNullName_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.SaveName(null)

        // Then
        assertTrue(event is SettingsScreen.Event.SaveName)
        assertNull((event as SettingsScreen.Event.SaveName).name)
    }

    @Test
    fun event_cancelNameEdit_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.CancelNameEdit

        // Then
        assertTrue(event is SettingsScreen.Event.CancelNameEdit)
    }

    @Test
    fun event_saveGrade_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.SaveGrade(GradeLevel.GRADE_1)

        // Then
        assertTrue(event is SettingsScreen.Event.SaveGrade)
        assertEquals(GradeLevel.GRADE_1, (event as SettingsScreen.Event.SaveGrade).gradeLevel)
    }

    @Test
    fun event_cancelGradeChange_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.CancelGradeChange

        // Then
        assertTrue(event is SettingsScreen.Event.CancelGradeChange)
    }

    @Test
    fun event_backClicked_createsCorrectEvent() {
        // When
        val event = SettingsScreen.Event.BackClicked

        // Then
        assertTrue(event is SettingsScreen.Event.BackClicked)
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
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is SettingsScreen.Event.EditNameClicked)
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
        assertNotNull(receivedEvent)
        assertTrue(receivedEvent is SettingsScreen.Event.ToggleAdaptiveDifficulty)
        assertFalse((receivedEvent as SettingsScreen.Event.ToggleAdaptiveDifficulty).enabled)
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
        assertTrue(state.profile?.adaptiveDifficultyEnabled ?: false)
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
        assertFalse(state.profile?.adaptiveDifficultyEnabled ?: true)
    }
}
