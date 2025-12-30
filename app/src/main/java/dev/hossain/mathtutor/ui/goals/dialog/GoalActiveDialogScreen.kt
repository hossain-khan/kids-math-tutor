package dev.hossain.mathtutor.ui.goals.dialog

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import kotlinx.parcelize.Parcelize

/**
 * Dialog screen shown when a child tries to access a locked game while an active goal is in progress.
 *
 * This screen informs the user that they have an active goal to complete and provides options to:
 * - Continue working on the active goal
 * - Dismiss the dialog and return to previous screen
 */
@Parcelize
data class GoalActiveDialogScreen(
    val activeGoal: ActiveGoal,
) : Screen {
    /**
     * State for [GoalActiveDialogScreen].
     */
    data class State(
        val activeGoal: ActiveGoal,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [GoalActiveDialogScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /** User tapped "Continue Goal" button */
        data object ContinueGoalClicked : Event

        /** User tapped "Dismiss" or back to close the dialog */
        data object DismissClicked : Event
    }
}
