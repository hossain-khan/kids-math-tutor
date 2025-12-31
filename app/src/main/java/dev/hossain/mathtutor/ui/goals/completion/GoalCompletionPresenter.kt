package dev.hossain.mathtutor.ui.goals.completion

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
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import dev.hossain.mathtutor.domain.repository.GoalRepository
import dev.hossain.mathtutor.ui.home.HomeScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class GoalCompletionPresenter(
    @Assisted private val screen: GoalCompletionScreen,
    @Assisted private val navigator: Navigator,
    private val goalRepository: GoalRepository,
) : Presenter<GoalCompletionScreen.State> {
    @CircuitInject(GoalCompletionScreen::class, AppScope::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): GoalCompletionPresenter
    }

    @Composable
    override fun present(): GoalCompletionScreen.State {
        val goal by goalRepository.getGoalById(screen.goalId).collectAsState(null)
        var error by remember { mutableStateOf<String?>(null) }

        return GoalCompletionScreen.State(
            goal = goal,
            goalHistory = null,
            isLoading = goal == null,
            error = error,
            eventSink = { event ->
                when (event) {
                    GoalCompletionScreen.Event.ReturnHome -> {
                        navigator.resetRoot(HomeScreen)
                    }

                    GoalCompletionScreen.Event.DismissError -> {
                        error = null
                    }
                }
            },
        )
    }
}
