package dev.hossain.mathtutor.ui.goals.creator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.MathOperation
import dev.hossain.mathtutor.domain.usecase.goals.CreateGoalUseCase
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen.Event
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen.State
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen.Step
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class GoalCreatorPresenter(
    @Assisted private val screen: GoalCreatorScreen,
    @Assisted private val navigator: Navigator,
    private val createGoalUseCase: CreateGoalUseCase,
) : Presenter<State> {
    @CircuitInject(GoalCreatorScreen::class, AppScope::class)
    @AssistedFactory
    interface Factory {
        fun create(
            screen: GoalCreatorScreen,
            navigator: Navigator,
        ): GoalCreatorPresenter
    }

    @Composable
    override fun present(): State {
        var currentStep by remember { mutableStateOf(Step.Title) }
        var goalTitle by remember { mutableStateOf("") }
        var goalDescription by remember { mutableStateOf("") }
        var components by remember { mutableStateOf<List<GoalComponent>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }

        val canAdvance =
            when (currentStep) {
                Step.Title -> goalTitle.isNotBlank()
                Step.SelectComponents -> components.isNotEmpty()
                Step.Review -> true
            }

        fun handleEvent(event: Event) {
            when (event) {
                is Event.SetTitle -> {
                    goalTitle = event.title
                }

                is Event.SetDescription -> {
                    goalDescription = event.description
                }

                is Event.AddComponent -> {
                    components = components + event.component
                }

                is Event.RemoveComponent -> {
                    components = components.filterIndexed { index, _ -> index != event.index }
                }

                Event.NextStep -> {
                    if (currentStep != Step.Review) {
                        currentStep =
                            when (currentStep) {
                                Step.Title -> Step.SelectComponents
                                Step.SelectComponents -> Step.Review
                                Step.Review -> Step.Review
                            }
                    }
                }

                Event.PreviousStep -> {
                    if (currentStep != Step.Title) {
                        currentStep =
                            when (currentStep) {
                                Step.Title -> Step.Title
                                Step.SelectComponents -> Step.Title
                                Step.Review -> Step.SelectComponents
                            }
                    }
                }

                Event.SaveGoal -> {
                    isLoading = true
                    error = null
                    // Create goal in coroutine context (should be wrapped in proper scope)
                    val newGoal =
                        Goal(
                            title = goalTitle,
                            description = goalDescription,
                            components = components,
                        )
                    // In a real implementation, this would be properly handled with coroutines
                    // For now, we'll just navigate back
                    isLoading = false
                    navigator.pop()
                }

                Event.Cancel -> {
                    navigator.pop()
                }

                Event.DismissError -> {
                    error = null
                }
            }
        }

        return State(
            currentStep = currentStep,
            goalTitle = goalTitle,
            goalDescription = goalDescription,
            components = components,
            canAdvance = canAdvance,
            isLoading = isLoading,
            error = error,
            eventSink = ::handleEvent,
        )
    }
}
