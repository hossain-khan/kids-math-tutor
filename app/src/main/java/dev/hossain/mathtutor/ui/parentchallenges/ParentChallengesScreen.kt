package dev.hossain.mathtutor.ui.parentchallenges

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.CustomChallenge
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for managing parent custom challenges.
 *
 * This screen displays all custom challenges (active and archived),
 * allows parents to manage them (archive/delete), and navigate to
 * import new challenges or start practice sessions.
 *
 * Parents can create custom challenges using the Math Pup Worksheet Creator web app:
 * `https://math-worksheet.gohk.xyz/`
 *
 * @see ImportChallengeScreen for importing challenges
 * @see CustomChallenge for the challenge data model
 */
@Parcelize
data object ParentChallengesScreen : Screen {
    /**
     * State for [ParentChallengesScreen].
     *
     * @property challenges List of challenges to display (filtered by showArchived)
     * @property isLoading Whether data is being loaded
     * @property showArchived Whether to show archived challenges
     * @property showDeleteConfirmation Whether to show delete confirmation dialog
     * @property challengeToDelete Challenge pending deletion (for confirmation)
     * @property eventSink Handler for screen events
     */
    data class State(
        val challenges: List<CustomChallenge>,
        val isLoading: Boolean,
        val showArchived: Boolean,
        val showDeleteConfirmation: Boolean = false,
        val challengeToDelete: CustomChallenge? = null,
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
         * User requested to archive a challenge.
         */
        data class ArchiveChallenge(
            val challengeId: String,
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
         * User toggled archived challenges visibility.
         */
        data class ToggleArchived(
            val show: Boolean,
        ) : Event

        /**
         * User requested to navigate back.
         */
        data object NavigateBack : Event
    }
}
