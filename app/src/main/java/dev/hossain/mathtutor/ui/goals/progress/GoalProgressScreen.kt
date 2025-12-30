package dev.hossain.mathtutor.ui.goals.progress

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.ComponentProgress
import kotlinx.parcelize.Parcelize

/**
 * Screen for displaying goal progress to the child.
 * Shows the current active goal with milestone-based progress tracking.
 * Child can see how many sessions are completed and overall progress percentage.
 */
@Parcelize
data object GoalProgressScreen : Screen {
    /**
     * State for the goal progress screen.
     *
     * @property activeGoal The currently active goal being tracked
     * @property currentComponentIndex Index of the component currently in progress (0-based)
     * @property componentProgress Progress details for each component
     * @property overallProgress Overall goal completion percentage (0.0-1.0)
     * @property isLoading Whether progress data is being loaded
     * @property error Error message if loading failed
     * @property eventSink Lambda to handle UI events
     */
    data class State(
        val activeGoal: ActiveGoal? = null,
        val currentComponentIndex: Int = 0,
        val componentProgress: List<ComponentProgress> = emptyList(),
        val overallProgress: Float = 0f, // 0-1.0
        val isLoading: Boolean = false,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    /**
     * Events that can be emitted from the goal progress UI.
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User initiated starting a new component/milestone.
         * @property componentIndex The index of the component to start
         */
        data class StartComponent(
            val componentIndex: Int,
        ) : Event

        /** User resumed the current component after pausing */
        object ResumeCurrentComponent : Event

        /** User navigated back from the progress screen */
        object Back : Event

        /** User dismissed an error message */
        object DismissError : Event
    }
}
