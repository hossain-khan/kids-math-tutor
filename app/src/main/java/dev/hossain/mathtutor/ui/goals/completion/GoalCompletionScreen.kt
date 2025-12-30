package dev.hossain.mathtutor.ui.goals.completion

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import kotlinx.parcelize.Parcelize

/**
 * Screen displayed when a child completes a goal successfully.
 * Shows celebration animations, achievements earned, and completion summary.
 *
 * @property goalId The ID of the completed goal
 */
@Parcelize
data class GoalCompletionScreen(
    val goalId: String,
) : Screen {
    /**
     * State for the goal completion celebration screen.
     *
     * @property goal The goal that was completed
     * @property goalHistory Historical completion data and statistics
     * @property isLoading Whether completion data is being loaded
     * @property error Error message if loading failed
     * @property eventSink Lambda to handle UI events
     */
    data class State(
        val goal: Goal? = null,
        val goalHistory: GoalHistory? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    /**
     * Events that can be emitted from the goal completion UI.
     */
    sealed interface Event : CircuitUiEvent {
        /** User returned to home screen after viewing completion celebration */
        object ReturnHome : Event

        /** User dismissed an error message */
        object DismissError : Event
    }
}
