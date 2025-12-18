package dev.hossain.mathtutor.ui.practiceresults

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.Badge
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
     * @property unlockedBadges List of badges unlocked during this session
     * @property showBadgeUnlock Whether to show badge unlock dialog
     * @property eventSink Handler for user events
     */
    data class State(
        val totalProblems: Int,
        val correctCount: Int,
        val accuracyPercentage: Float,
        val problemResults: List<ProblemResult>,
        val unlockedBadges: List<Badge> = emptyList(),
        val showBadgeUnlock: Boolean = false,
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

        /** User dismissed the badge unlock dialog */
        data object DismissBadgeDialog : Event
    }
}
