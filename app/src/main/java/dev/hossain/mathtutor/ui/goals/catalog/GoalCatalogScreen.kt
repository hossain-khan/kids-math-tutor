package dev.hossain.mathtutor.ui.goals.catalog

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.Goal
import kotlinx.parcelize.Parcelize

/**
 * Screen for displaying and managing the goal catalog.
 * Allows parents to view all available goals and activate them for their children.
 * This is the parent-facing goal management dashboard.
 */
@Parcelize
data object GoalCatalogScreen : Screen {
    /**
     * State for the goal catalog screen.
     *
     * @property goals List of all available goals in the catalog
     * @property activeGoalId ID of the currently active goal for the child (null if none)
     * @property isLoading Whether the catalog is being loaded
     * @property error Error message if loading failed
     * @property eventSink Lambda to handle UI events
     */
    data class State(
        val goals: List<Goal> = emptyList(),
        val activeGoalId: String? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    /**
     * Events that can be emitted from the goal catalog UI.
     */
    sealed interface Event : CircuitUiEvent {
        /** User initiated goal creation */
        object CreateNewGoal : Event

        /**
         * User activated a goal for the child.
         * @property goalId The ID of the goal to activate
         */
        data class ActivateGoal(
            val goalId: String,
        ) : Event

        /**
         * User deleted a goal from the catalog.
         * @property goalId The ID of the goal to delete
         */
        data class DeleteGoal(
            val goalId: String,
        ) : Event

        data class ViewHistory(
            val goalId: String,
        ) : Event

        data class ArchiveGoal(
            val goalId: String,
        ) : Event

        object DismissError : Event
    }
}
