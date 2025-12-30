package dev.hossain.mathtutor.ui.goals.catalog

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.Goal
import kotlinx.parcelize.Parcelize

@Parcelize
data class GoalCatalogScreen : Screen {
    data class State(
        val goals: List<Goal> = emptyList(),
        val activeGoalId: String? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        object CreateNewGoal : Event
        data class ActivateGoal(val goalId: String) : Event
        data class DeleteGoal(val goalId: String) : Event
        data class ViewHistory(val goalId: String) : Event
        data class ArchiveGoal(val goalId: String) : Event
        object DismissError : Event
    }
}
