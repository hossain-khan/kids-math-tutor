package dev.hossain.mathtutor.ui.goals.dialog

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.ui.goals.progress.GoalProgressScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import timber.log.Timber

/**
 * Presenter for [GoalActiveDialogScreen].
 *
 * Manages the state and navigation for the goal lock dialog.
 * When user clicks "Continue Goal", navigates to GoalProgressScreen.
 * When user clicks "Dismiss", pops the dialog to return to previous screen.
 */
@AssistedInject
class GoalActiveDialogPresenter(
    @Assisted private val screen: GoalActiveDialogScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<GoalActiveDialogScreen.State> {
    @CircuitInject(GoalActiveDialogScreen::class, AppScope::class)
    @AssistedFactory
    interface Factory {
        fun create(
            screen: GoalActiveDialogScreen,
            navigator: Navigator,
        ): GoalActiveDialogPresenter
    }

    @Composable
    override fun present(): GoalActiveDialogScreen.State =
        GoalActiveDialogScreen.State(
            activeGoal = screen.activeGoal,
            eventSink = { event ->
                when (event) {
                    GoalActiveDialogScreen.Event.ContinueGoalClicked -> {
                        Timber.d("GoalActiveDialog: User continuing with goal: ${screen.activeGoal.goal.id}")
                        navigator.goTo(GoalProgressScreen)
                    }

                    GoalActiveDialogScreen.Event.DismissClicked -> {
                        Timber.d("GoalActiveDialog: User dismissed dialog")
                        navigator.pop()
                    }
                }
            },
        )
}
