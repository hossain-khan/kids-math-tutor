package dev.hossain.mathtutor.ui.goals.progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.repository.GoalRepository
import dev.hossain.mathtutor.ui.mathpractice.MathPracticeScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class GoalProgressPresenter(
    @Assisted private val navigator: Navigator,
    private val goalRepository: GoalRepository,
) : Presenter<GoalProgressScreen.State> {
    @CircuitInject(GoalProgressScreen::class, AppScope::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): GoalProgressPresenter
    }

    @Composable
    override fun present(): GoalProgressScreen.State {
        val activeGoal by goalRepository.getActiveGoal().collectAsState(null)
        var currentComponentIndex by remember { mutableIntStateOf(0) }
        var error by remember { mutableStateOf<String?>(null) }

        return GoalProgressScreen.State(
            activeGoal = activeGoal,
            currentComponentIndex = currentComponentIndex,
            componentProgress = activeGoal?.componentProgress ?: emptyList(),
            overallProgress = calculateOverallProgress(activeGoal),
            isLoading = activeGoal == null,
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
