package dev.hossain.mathtutor.ui.goals.creator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.repository.CustomChallengeRepository
import dev.hossain.mathtutor.domain.usecase.goals.CreateGoalUseCase
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen.Event
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen.State
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen.Step
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import timber.log.Timber

@AssistedInject
class GoalCreatorPresenter(
    @Assisted private val screen: GoalCreatorScreen,
    @Assisted private val navigator: Navigator,
    private val createGoalUseCase: CreateGoalUseCase,
    private val customChallengeRepository: CustomChallengeRepository,
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
        val coroutineScope = rememberCoroutineScope()

        var currentStep by remember { mutableStateOf(Step.Title) }
        var goalTitle by remember { mutableStateOf("") }
        var goalDescription by remember { mutableStateOf("") }
        var components by remember { mutableStateOf<List<GoalComponent>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var availableChallenges by remember { mutableStateOf<List<CustomChallenge>>(emptyList()) }

        // Load available custom challenges on composition
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    val challenges = customChallengeRepository.getAllChallenges()
                    availableChallenges = challenges
                    Timber.d("GoalCreator: Loaded ${challenges.size} custom challenges")
                } catch (e: Exception) {
                    Timber.w(e, "GoalCreator: Failed to load custom challenges")
                }
            }
        }

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
                    if (isLoading) return

                    coroutineScope.launch {
                        isLoading = true
                        error = null

                        Timber.d(
                            "GoalCreator: Save goal requested (titleLength=%d, componentCount=%d)",
                            goalTitle.length,
                            components.size,
                        )

                        val result = createGoalUseCase(goalTitle, goalDescription, components)
                        if (result.isSuccess) {
                            val goalId = result.getOrNull()?.id
                            Timber.d("GoalCreator: Goal saved successfully (goalId=%s)", goalId)
                            navigator.pop()
                        } else {
                            val message = result.exceptionOrNull()?.message ?: "Failed to save goal"
                            Timber.w(result.exceptionOrNull(), "GoalCreator: Save goal failed")
                            error = message
                        }

                        isLoading = false
                    }
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
            availableChallenges = availableChallenges,
            canAdvance = canAdvance,
            isLoading = isLoading,
            error = error,
            eventSink = ::handleEvent,
        )
    }
}
