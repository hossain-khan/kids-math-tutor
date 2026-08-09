package dev.hossain.mathtutor.ui.mathrace

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.zacsweers.metro.AppScope
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for the Math Race game.
 *
 * A fast-paced 60-second challenge where users solve as many math problems as possible.
 * Features:
 * - 3-2-1 countdown before game starts
 * - 60-second timer with 10-second warning
 * - Instant feedback on answers
 * - Score tracking and personal best
 *
 * @property isTrialMode Whether this game is being played in trial mode (locked game).
 *                       When true, no badges will be awarded and progress may not be saved.
 */
@Parcelize
@CircuitSerializable(AppScope::class)
data class MathRaceScreen(
    val isTrialMode: Boolean = false,
) : Screen {
    /**
     * Represents the current state of the game.
     */
    sealed interface GameState {
        /**
         * Game hasn't started yet. Player can see instructions and press Start.
         */
        data object NotStarted : GameState

        /**
         * Countdown is in progress (3-2-1-GO!).
         * @property countdownValue Current countdown number (3, 2, 1, or 0 for GO)
         */
        data class Countdown(
            val countdownValue: Int,
        ) : GameState

        /**
         * Game is actively being played.
         */
        data object Playing : GameState

        /**
         * Game has finished. Shows final results.
         * @property finalScore Number of correct answers
         * @property totalAttempts Total problems attempted
         * @property isNewRecord Whether this is a new personal best
         * @property accuracy Percentage of correct answers (0-100)
         * @property averageTimePerProblem Average seconds per problem
         * @property unlockedBadges List of badges unlocked during this game
         */
        data class Finished(
            val finalScore: Int,
            val totalAttempts: Int,
            val isNewRecord: Boolean,
            val accuracy: Float,
            val averageTimePerProblem: Float,
            val unlockedBadges: List<Badge> = emptyList(),
        ) : GameState
    }

    /**
     * State for [MathRaceScreen].
     */
    data class State(
        val gameState: GameState = GameState.NotStarted,
        val currentProblem: MathProblem? = null,
        val currentAnswer: String = "",
        val score: Int = 0,
        val timeRemaining: Int = 60,
        val personalBest: Int = 0,
        val totalAttempts: Int = 0,
        val correctAnswers: Int = 0,
        val lastAnswerCorrect: Boolean? = null,
        val userName: String? = null,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [MathRaceScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * Player pressed the Start Game button.
         */
        data object StartGame : Event

        /**
         * Player entered a digit.
         * @property digit The digit (0-9) entered
         */
        data class NumberEntered(
            val digit: Int,
        ) : Event

        /**
         * Player pressed backspace to delete last digit.
         */
        data object Backspace : Event

        /**
         * Player submitted their answer.
         */
        data object CheckAnswer : Event

        /**
         * Player wants to play again after game ends.
         */
        data object PlayAgain : Event

        /**
         * Player wants to return to home/game selection.
         */
        data object NavigateHome : Event
    }
}
