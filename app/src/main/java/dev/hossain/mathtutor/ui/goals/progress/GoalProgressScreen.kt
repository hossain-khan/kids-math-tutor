package dev.hossain.mathtutor.ui.goals.progress

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.ComponentProgress
import kotlinx.parcelize.Parcelize

@Parcelize
data object GoalProgressScreen : Screen {
    data class State(
        val activeGoal: ActiveGoal? = null,
        val currentComponentIndex: Int = 0,
        val componentProgress: List<ComponentProgress> = emptyList(),
        val overallProgress: Float = 0f, // 0-1.0
        val isLoading: Boolean = false,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class StartComponent(val componentIndex: Int) : Event

        object ResumeCurrentComponent : Event

        object Back : Event

        object DismissError : Event
    }
}
