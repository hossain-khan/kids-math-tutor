package dev.hossain.mathtutor.ui.settings

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.UserProfile
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for user settings and profile management.
 *
 * Allows users to:
 * - View and edit their name
 * - Change their grade level
 * - Toggle adaptive difficulty
 * - Access About, Privacy, and Help information
 */
@Parcelize
data object SettingsScreen : Screen {
    /**
     * State for [SettingsScreen].
     *
     * @property profile The current user profile, null if loading
     * @property showNameDialog Whether the name edit dialog is visible
     * @property showGradeDialog Whether the grade change dialog is visible
     * @property eventSink Handler for screen events
     */
    data class State(
        val profile: UserProfile?,
        val showNameDialog: Boolean,
        val showGradeDialog: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [SettingsScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User tapped the Edit Name button.
         */
        data object EditNameClicked : Event

        /**
         * User tapped the Change Grade button.
         */
        data object ChangeGradeClicked : Event

        /**
         * User toggled the adaptive difficulty switch.
         */
        data class ToggleAdaptiveDifficulty(
            val enabled: Boolean,
        ) : Event

        /**
         * User saved a new name from the name edit dialog.
         */
        data class SaveName(
            val name: String?,
        ) : Event

        /**
         * User canceled the name edit dialog.
         */
        data object CancelNameEdit : Event

        /**
         * User saved a new grade level from the grade change dialog.
         */
        data class SaveGrade(
            val gradeLevel: GradeLevel,
        ) : Event

        /**
         * User canceled the grade change dialog.
         */
        data object CancelGradeChange : Event

        /**
         * User tapped the back button.
         */
        data object BackClicked : Event

        /**
         * User tapped the Audio & Haptics button.
         */
        data object AudioHapticsClicked : Event
    }
}
