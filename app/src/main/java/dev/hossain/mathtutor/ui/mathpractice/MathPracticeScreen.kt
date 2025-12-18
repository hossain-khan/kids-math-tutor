package dev.hossain.mathtutor.ui.mathpractice

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for math practice session.
 *
 * This screen presents a series of math problems for the user to solve,
 * tracking progress and providing immediate feedback.
 *
 * @param operation The math operation type for this practice session
 * @param problemCount Number of problems in this practice session
 */
@Parcelize
data class MathPracticeScreen(
    val operation: MathOperation = MathOperation.ADDITION,
    val problemCount: Int = 10,
) : Screen {
    /**
     * State for [MathPracticeScreen].
     */
    data class State(
        val currentProblem: MathProblem?,
        val currentAnswer: String,
        val currentProblemIndex: Int,
        val totalProblems: Int,
        val isCorrect: Boolean?,
        val unlockedBadges: List<Badge> = emptyList(),
        val showBadgeUnlock: Boolean = false,
        val currentBadgeIndex: Int = 0,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [MathPracticeScreen].
     */
    sealed interface Event : CircuitUiEvent {
        data class NumberClicked(
            val number: Int,
        ) : Event

        data object ClearAnswer : Event

        data object CheckAnswer : Event

        data object NextProblem : Event

        data object NavigateBack : Event

        data object DismissBadgeDialog : Event
    }
}
