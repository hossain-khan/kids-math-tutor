package dev.hossain.mathtutor.ui.goals.completion

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import kotlinx.parcelize.Parcelize

@Parcelize
data class GoalCompletionScreen(
    val goalId: String,
) : Screen {
    data class State(
        val goal: Goal? = null,
        val goalHistory: GoalHistory? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        object ReturnHome : Event

        object DismissError : Event
    }
}
