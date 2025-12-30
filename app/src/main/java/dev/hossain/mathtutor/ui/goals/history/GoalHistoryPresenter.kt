package dev.hossain.mathtutor.ui.goals.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.repository.GoalRepository
import dev.hossain.mathtutor.ui.goals.history.GoalHistoryScreen.Event
import dev.hossain.mathtutor.ui.goals.history.GoalHistoryScreen.State
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class GoalHistoryPresenter(
    @Assisted private val screen: GoalHistoryScreen,
    @Assisted private val navigator: Navigator,
    private val goalRepository: GoalRepository,
) : Presenter<State> {
    @CircuitInject(GoalHistoryScreen::class, AppScope::class)
    @AssistedFactory
    interface Factory {
        fun create(
            screen: GoalHistoryScreen,
            navigator: Navigator,
        ): GoalHistoryPresenter
    }

    @Composable
    override fun present(): State {
        val goal by goalRepository.getGoalById(screen.goalId).collectAsState(null)
        val allHistories by goalRepository.getGoalHistory().collectAsState(emptyList())
        // Filter histories for this specific goal
        val histories = allHistories.filter { it.goal.id == screen.goalId }
        var selectedHistory by remember { mutableStateOf<dev.hossain.mathtutor.domain.model.goals.GoalHistory?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        // Calculate analytics
        val totalCompleted = histories.size
        val averageAccuracy = if (histories.isNotEmpty()) {
            histories.map { it.overallAccuracy }.average().toFloat()
        } else {
            0f
        }
        val totalTimeMins = histories.sumOf { it.totalTimeSeconds / 60 }

        // Update loading state once data is loaded
        if (goal != null && !isLoading) {
            // Already loaded
        } else if (goal != null) {
            isLoading = false
        }

        fun handleEvent(event: Event) {
            when (event) {
                is Event.SelectHistory -> selectedHistory = event.history
                Event.ClearSelection -> selectedHistory = null
                Event.Back -> navigator.pop()
                Event.DismissError -> error = null
            }
        }

        return State(
            goal = goal,
            histories = histories.sortedByDescending { it.completedAt },
            selectedHistory = selectedHistory,
            totalCompleted = totalCompleted,
            averageAccuracy = averageAccuracy,
            totalTimeMins = totalTimeMins,
            isLoading = isLoading,
            error = error,
            eventSink = ::handleEvent,
        )
    }
}
