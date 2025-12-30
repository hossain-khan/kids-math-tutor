package dev.hossain.mathtutor.ui.goals.creator

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import kotlinx.parcelize.Parcelize

/**
 * Screen for creating a new goal through a multi-step wizard.
 * Guides parents through:
 * 1. Setting goal title and description
 * 2. Selecting/adding goal components (math operations or custom challenges)
 * 3. Reviewing the complete goal before saving
 */
@Parcelize
data object GoalCreatorScreen : Screen {
    /**
     * Steps in the goal creation wizard.
     */
    enum class Step {
        /** Step 1: Enter goal title and description */
        Title,
        /** Step 2: Select or add goal components (milestones) */
        SelectComponents,
        /** Step 3: Review all settings and confirm creation */
        Review,
    }

    /**
     * State for the goal creator screen.
     *
     * @property currentStep The current step in the wizard
     * @property goalTitle The title entered for the goal
     * @property goalDescription Optional description of the goal
     * @property components List of selected goal components
     * @property canAdvance Whether advancing to the next step is allowed
     * @property isLoading Whether the goal is being created
     * @property error Error message if goal creation failed
     * @property eventSink Lambda to handle UI events
     */
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

    /**
     * Events that can be emitted from the goal creator UI.
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User entered a goal title.
         * @property title The goal title
         */
        data class SetTitle(
            val title: String,
        ) : Event

        /**
         * User entered a goal description.
         * @property description The goal description
         */
        data class SetDescription(
            val description: String,
        ) : Event

        /**
         * User added a component to the goal.
         * @property component The component to add
         */
        data class AddComponent(
            val component: GoalComponent,
        ) : Event

        /**
         * User removed a component from the goal.
         * @property index The index of the component to remove
         */
        data class RemoveComponent(
            val index: Int,
        ) : Event

        /** User advanced to the next step */
        object NextStep : Event

        /** User went back to the previous step */
        object PreviousStep : Event

        /** User confirmed and saved the goal */
        object SaveGoal : Event

        /** User cancelled goal creation */
        object Cancel : Event

        /** User dismissed an error message */
        object DismissError : Event
    }
}
