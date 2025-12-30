package dev.hossain.mathtutor.ui.goals.completion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import dev.hossain.mathtutor.domain.repository.GoalRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class GoalCompletionPresenter(
    @Assisted private val screen: GoalCompletionScreen,
    @Assisted private val navigator: Navigator,
    private val goalRepository: GoalRepository,
) : Presenter<GoalCompletionScreen.State> {
    @Composable
    override fun present(): GoalCompletionScreen.State {
        var goal by remember { mutableStateOf<Goal?>(null) }
        var goalHistory by remember { mutableStateOf<GoalHistory?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(screen.goalId) {
            try {
                // Load goal
                goalRepository
                    .getGoalById(screen.goalId)
                    .catch { exception ->
                        error = exception.message ?: "Failed to load goal"
                        isLoading = false
                    }.collectLatest { result ->
                        result
                            .onSuccess { loadedGoal ->
                                goal = loadedGoal
                                isLoading = false
                            }.onFailure { exception ->
                                error = exception.message ?: "Failed to load goal"
                                isLoading = false
                            }
                    }
            } catch (e: Exception) {
                error = e.message ?: "An error occurred"
                isLoading = false
            }
        }

        return GoalCompletionScreen.State(
            goal = goal,
            goalHistory = goalHistory,
            isLoading = isLoading,
            error = error,
            eventSink = { event ->
                when (event) {
                    GoalCompletionScreen.Event.ReturnHome -> {
                        navigator.resetRoot(
                            dev.hossain.mathtutor.ui.home
                                .HomeScreen(),
                        )
                    }

                    GoalCompletionScreen.Event.DismissError -> {
                        error = null
                    }
                }
            },
        )
    }
}
