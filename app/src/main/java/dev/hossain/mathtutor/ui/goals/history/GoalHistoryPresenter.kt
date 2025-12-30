package dev.hossain.mathtutor.ui.goals.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.repository.goals.GoalRepository
import dev.hossain.mathtutor.ui.goals.history.GoalHistoryScreen.Event
import dev.hossain.mathtutor.ui.goals.history.GoalHistoryScreen.State
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class GoalHistoryPresenter(
    @Assisted private val screen: GoalHistoryScreen,
    @Assisted private val navigator: Navigator,
    private val goalRepository: GoalRepository,
) : Presenter<State> {

    @Composable
    override fun present(): State {
        val goal by goalRepository.getGoalById(screen.goalId).collectAsState(null)
        val histories by goalRepository.getGoalHistory(screen.goalId).collectAsState(emptyList())
        var selectedHistory by remember { mutableStateOf<dev.hossain.mathtutor.domain.model.goals.GoalHistory?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        // Calculate analytics
        val totalCompleted = histories.size
        val averageAccuracy = if (histories.isNotEmpty()) {
            histories.mapNotNull { it.accuracy }.average().toFloat()
        } else {
            0f
        }
        val totalTimeMins = histories.sumOf { it.timeTakenSeconds / 60 }

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
