package dev.hossain.mathtutor.ui.goals.progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.repository.GoalRepository
import dev.hossain.mathtutor.ui.mathpractice.MathPracticeScreen
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class GoalProgressPresenter(
    @Assisted private val navigator: Navigator,
    private val goalRepository: GoalRepository,
) : Presenter<GoalProgressScreen.State> {
    @Composable
    override fun present(): GoalProgressScreen.State {
        var activeGoal by remember { mutableStateOf<ActiveGoal?>(null) }
        var currentComponentIndex by remember { mutableIntStateOf(0) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            goalRepository
                .getActiveGoal()
                .catch { exception ->
                    error = exception.message ?: "Failed to load active goal"
                    isLoading = false
                }
                .collectLatest { result ->
                    result
                        .onSuccess { goal ->
                            activeGoal = goal
                            currentComponentIndex = 0
                            isLoading = false
                        }
                        .onFailure { exception ->
                            error = exception.message ?: "Failed to load active goal"
                            isLoading = false
                        }
                }
        }

        return GoalProgressScreen.State(
            activeGoal = activeGoal,
            currentComponentIndex = currentComponentIndex,
            componentProgress = activeGoal?.componentProgress ?: emptyList(),
            overallProgress = calculateOverallProgress(activeGoal),
            isLoading = isLoading,
            error = error,
            eventSink = { event ->
                when (event) {
                    is GoalProgressScreen.Event.StartComponent -> {
                        currentComponentIndex = event.componentIndex
                        // Navigate to MathPracticeScreen to start the component
                        navigator.goTo(MathPracticeScreen())
                    }

                    GoalProgressScreen.Event.ResumeCurrentComponent -> {
                        // Resume current component by navigating to practice
                        navigator.goTo(MathPracticeScreen())
                    }

                    GoalProgressScreen.Event.Back -> {
                        navigator.pop()
                    }

                    GoalProgressScreen.Event.DismissError -> {
                        error = null
                    }
                }
            },
        )
    }

    private fun calculateOverallProgress(goal: ActiveGoal?): Float {
        if (goal == null) return 0f
        if (goal.componentProgress.isEmpty()) return 0f

        val totalSessions = goal.goal.components.sumOf { it.sessionCount }
        val completedSessions = goal.componentProgress.sumOf { it.completedSessions }

        return if (totalSessions > 0) {
            completedSessions.toFloat() / totalSessions.toFloat()
        } else {
            0f
        }
    }
}
