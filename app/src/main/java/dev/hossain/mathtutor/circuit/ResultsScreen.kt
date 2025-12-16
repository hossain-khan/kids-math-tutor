package dev.hossain.mathtutor.circuit

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.MathProblem
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for displaying practice session results.
 *
 * Shows summary statistics, problem list with user answers, and navigation options.
 */
@Parcelize
data class ResultsScreen(
    val problems: List<MathProblem>,
    val userAnswers: List<Int?>,
) : Screen {
    /**
     * State for the Results screen.
     *
     * @property totalProblems Total number of problems attempted
     * @property correctCount Number of correct answers
     * @property accuracyPercentage Accuracy as a percentage (0-100)
     * @property problemResults List of problems with their user answers
     * @property eventSink Handler for user events
     */
    data class State(
        val totalProblems: Int,
        val correctCount: Int,
        val accuracyPercentage: Float,
        val problemResults: List<ProblemResult>,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Represents a single problem result with user's answer.
     */
    data class ProblemResult(
        val problem: MathProblem,
        val userAnswer: Int?,
        val isCorrect: Boolean,
    )

    /**
     * Events for the Results screen.
     */
    sealed interface Event : CircuitUiEvent {
        /** User wants to try another practice session */
        data object TryAgain : Event

        /** User wants to navigate back */
        data object NavigateBack : Event
    }
}
