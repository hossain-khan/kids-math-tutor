package dev.hossain.mathtutor.ui.goals.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.repository.GoalRepository
import dev.hossain.mathtutor.domain.usecase.goals.ActivateGoalUseCase
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen
import dev.hossain.mathtutor.ui.goals.history.GoalHistoryScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch

@AssistedInject
class GoalCatalogPresenter(
    @Assisted private val screen: GoalCatalogScreen,
    @Assisted private val navigator: Navigator,
    private val goalRepository: GoalRepository,
    private val activateGoalUseCase: ActivateGoalUseCase,
) : Presenter<GoalCatalogScreen.State> {
    @CircuitInject(GoalCatalogScreen::class, AppScope::class)
    @AssistedFactory
    interface Factory {
        fun create(
            screen: GoalCatalogScreen,
            navigator: Navigator,
        ): GoalCatalogPresenter
    }

    @Composable
    override fun present(): GoalCatalogScreen.State {
        var isLoading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }

        // Observe all goals from repository
        val goalsFlow = goalRepository.getAllGoals()
        val goals by goalsFlow.collectAsState(emptyList())

        // Observe active goal ID
        val activeGoalFlow = goalRepository.getActiveGoal()
        val activeGoal by activeGoalFlow.collectAsState(null)
        val activeGoalId = activeGoal?.id

        return GoalCatalogScreen.State(
            goals = goals,
            activeGoalId = activeGoalId,
            isLoading = isLoading,
            error = error,
            eventSink = { event ->
                when (event) {
                    GoalCatalogScreen.Event.CreateNewGoal -> {
                        navigator.goTo(GoalCreatorScreen())
                    }

                    is GoalCatalogScreen.Event.ActivateGoal -> {
                        isLoading = true
                        error = null
                        // Activate the goal
                        // This will be done via coroutine in real implementation
                    }

                    is GoalCatalogScreen.Event.DeleteGoal -> {
                        isLoading = true
                        error = null
                        // Delete the goal from repository
                    }

                    is GoalCatalogScreen.Event.ViewHistory -> {
                        navigator.goTo(GoalHistoryScreen(event.goalId))
                    }

                    is GoalCatalogScreen.Event.ArchiveGoal -> {
                        isLoading = true
                        error = null
                        // Archive the goal
                    }

                    GoalCatalogScreen.Event.DismissError -> {
                        error = null
                    }
                }
            },
        )
    }
}
