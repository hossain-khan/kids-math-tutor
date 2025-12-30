package dev.hossain.mathtutor.ui.goals.history

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import kotlinx.parcelize.Parcelize

/**
 * Screen for displaying goal completion history and analytics.
 * Shows statistics for a specific goal including completion counts, accuracy trends, and time spent.
 *
 * @property goalId The ID of the goal to show history for
 */
@Parcelize
data class GoalHistoryScreen(
    val goalId: String,
) : Screen {
    /**
     * State for the goal history and analytics screen.
     *
     * @property goal The goal being analyzed
     * @property histories List of all past completions for this goal
     * @property selectedHistory Currently selected completion history entry (for detailed view)
     * @property totalCompleted Total number of times this goal was completed
     * @property averageAccuracy Average accuracy across all completions (0-100)
     * @property totalTimeMins Total time spent on this goal in minutes
     * @property isLoading Whether history data is being loaded
     * @property error Error message if loading failed
     * @property eventSink Lambda to handle UI events
     */
    data class State(
        val goal: Goal? = null,
        val histories: List<GoalHistory> = emptyList(),
        val selectedHistory: GoalHistory? = null,
        val totalCompleted: Int = 0,
        val averageAccuracy: Float = 0f,
        val totalTimeMins: Int = 0,
        val isLoading: Boolean = true,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    /**
     * Events that can be emitted from the goal history UI.
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User selected a specific completion history to view details.
         * @property history The history entry that was selected
         */
        data class SelectHistory(
            val history: GoalHistory,
        ) : Event

        /** User cleared the selected history entry */
        object ClearSelection : Event

        /** User navigated back from the history screen */
        object Back : Event

        /** User dismissed an error message */
        object DismissError : Event
    }
}
