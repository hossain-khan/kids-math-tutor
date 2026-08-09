package dev.hossain.mathtutor.ui.parentchallenges

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.CustomChallenge
import kotlinx.parcelize.Parcelize
import com.slack.circuit.serialization.CircuitSerializable
import dev.zacsweers.metro.AppScope

/**
 * Circuit screen for managing parent-created custom challenges.
 *
 * Displays a list of custom challenges created by parents/teachers,
 * allowing them to view, manage, filter, and launch practice sessions.
 *
 * Parents can create custom challenges using the Math Pup Worksheet Creator web app:
 * `https://math-worksheet.gohk.xyz/`
 *
 * @see ImportChallengeScreen for importing challenges
 * @see CustomChallenge for the challenge data model
 */
@Parcelize
@CircuitSerializable(AppScope::class)
data object ParentChallengesScreen : Screen {
    /**
     * State for [ParentChallengesScreen].
     *
     * @property challenges List of challenges to display (filtered by showArchived)
     * @property isLoading Whether data is being loaded
     * @property showArchived Whether to show archived challenges
     * @property showDeleteConfirmation Whether to show delete confirmation dialog
     * @property challengeToDelete Challenge pending deletion (for confirmation)
     * @property showClearSessionsConfirmation Whether to show clear sessions confirmation dialog
     * @property challengeToClearSessions Challenge pending session clearing (for confirmation)
     * @property importSuccessMessage Success message to show after importing a challenge
     * @property eventSink Handler for screen events
     */
    data class State(
        val challenges: List<CustomChallenge>,
        val isLoading: Boolean,
        val showArchived: Boolean,
        val showDeleteConfirmation: Boolean = false,
        val challengeToDelete: CustomChallenge? = null,
        val showClearSessionsConfirmation: Boolean = false,
        val challengeToClearSessions: CustomChallenge? = null,
        val importSuccessMessage: String? = null,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [ParentChallengesScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User requested to import a new challenge.
         */
        data object ImportNewChallenge : Event

        /**
         * User selected a challenge to practice.
         */
        data class ChallengeSelected(
            val challenge: CustomChallenge,
        ) : Event

        /**
         * User requested to toggle archive state of a challenge.
         * If the challenge is archived, it will be unarchived.
         * If the challenge is active, it will be archived.
         */
        data class ArchiveChallenge(
            val challenge: CustomChallenge,
        ) : Event

        /**
         * User requested to delete a challenge (shows confirmation).
         */
        data class DeleteChallengeRequested(
            val challenge: CustomChallenge,
        ) : Event

        /**
         * User confirmed deletion of a challenge.
         */
        data class ConfirmDelete(
            val challengeId: String,
        ) : Event

        /**
         * User cancelled delete operation.
         */
        data object CancelDelete : Event

        /**
         * User requested to clear sessions for a challenge (shows confirmation).
         */
        data class ClearSessionsRequested(
            val challenge: CustomChallenge,
        ) : Event

        /**
         * User confirmed clearing sessions for a challenge.
         */
        data class ConfirmClearSessions(
            val challengeId: String,
        ) : Event

        /**
         * User cancelled clear sessions operation.
         */
        data object CancelClearSessions : Event

        /**
         * User toggled archived challenges visibility.
         */
        data class ToggleArchived(
            val show: Boolean,
        ) : Event

        /**
         * Dismiss the import success message.
         */
        data object DismissImportSuccess : Event

        /**
         * User requested to navigate back.
         */
        data object NavigateBack : Event

        /**
         * User tapped the parent settings button.
         */
        data object ParentSettingsClicked : Event
    }
}
