package dev.hossain.mathtutor.ui.goals.creator

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import kotlinx.parcelize.Parcelize

@Parcelize
data object GoalCreatorScreen : Screen {
    enum class Step {
        Title,
        SelectComponents,
        Review,
    }

    data class State(
        val currentStep: Step = Step.Title,
        val goalTitle: String = "",
        val goalDescription: String = "",
        val components: List<GoalComponent> = emptyList(),
        val canAdvance: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class SetTitle(
            val title: String,
        ) : Event

        data class SetDescription(
            val description: String,
        ) : Event

        data class AddComponent(
            val component: GoalComponent,
        ) : Event

        data class RemoveComponent(
            val index: Int,
        ) : Event

        object NextStep : Event

        object PreviousStep : Event

        object SaveGoal : Event

        object Cancel : Event

        object DismissError : Event
    }
}
