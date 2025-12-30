package dev.hossain.mathtutor.ui.goals.history

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import kotlinx.parcelize.Parcelize

@Parcelize
data class GoalHistoryScreen(
    val goalId: String,
) : Screen {
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

    sealed interface Event : CircuitUiEvent {
        data class SelectHistory(
            val history: GoalHistory,
        ) : Event

        object ClearSelection : Event

        object Back : Event

        object DismissError : Event
    }
}
