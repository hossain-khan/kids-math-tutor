package dev.hossain.mathtutor.ui.mathpractice

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.DifficultyAdjustment
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.ui.component.WorkBreakdownStep
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for math practice session.
 *
 * This screen presents a series of math problems for the user to solve,
 * tracking progress and providing immediate feedback.
 *
 * @param operation The math operation type for this practice session
 * @param problemCount Number of problems in this practice session
 * @param customChallengeId Optional ID of custom challenge for parent-created challenges
 * @param goalComponentIndex Optional index of the goal component (for tracking goal progress)
 * @param goalId Optional ID of the goal being worked on (for tracking goal progress)
 */
@Parcelize
data class MathPracticeScreen(
    val operation: MathOperation = MathOperation.ADDITION,
    val problemCount: Int = 10,
    val customChallengeId: String? = null,
    val goalComponentIndex: Int = 0,
    val goalId: String? = null,
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
        val isLoading: Boolean = false,
        val userName: String? = null,
        val unlockedBadges: List<Badge> = emptyList(),
        val showBadgeUnlock: Boolean = false,
        val currentBadgeIndex: Int = 0,
        val difficultyAdjustment: DifficultyAdjustment? = null,
        val actualGradeLevel: GradeLevel? = null,
        val showDifficultyChangeNotice: Boolean = false,
        val customChallengeTitle: String? = null,
        val wrongAttempts: Int = 0,
        val showHintButton: Boolean = false,
        val currentHintText: String? = null,
        val hintButtonClicked: Boolean = false,
        val showVisualHint: Boolean = false,
        val isVisualHintFeasible: Boolean = false,
        val showWorkBreakdown: Boolean = false,
        val workBreakdownSteps: List<WorkBreakdownStep> = emptyList(),
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

        data object DismissDifficultyNotice : Event

        data object RequestHint : Event

        data object DismissHint : Event

        data object ShowVisualHint : Event

        data object DismissVisualHint : Event

        data object ShowWork : Event

        data object DismissWork : Event
    }
}
