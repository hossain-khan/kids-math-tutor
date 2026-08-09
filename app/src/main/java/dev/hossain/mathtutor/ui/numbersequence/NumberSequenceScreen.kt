package dev.hossain.mathtutor.ui.numbersequence

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import dev.hossain.mathtutor.domain.generator.SequenceQuestion
import dev.hossain.mathtutor.domain.model.Badge
import dev.zacsweers.metro.AppScope
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for Number Sequence game.
 *
 * A pattern recognition game where players identify missing numbers in a sequence.
 * Features:
 * - 10 rounds per game
 * - Grade-appropriate sequence patterns (+1, +2, doubles, etc.)
 * - Score 1 point for each correct answer
 * - Personal best tracking
 * - Badge unlocks for achievements
 *
 * @property isTrialMode Whether this game is being played in trial mode (locked game).
 *                       When true, no badges will be awarded and progress may not be saved.
 */
@Parcelize
@CircuitSerializable(AppScope::class)
data class NumberSequenceScreen(
    val isTrialMode: Boolean = false,
) : Screen {
    /**
     * Represents the different phases of the game.
     */
    sealed interface GameState {
        /**
         * Initial state before the game begins.
         * Shows start screen with instructions and personal best.
         */
        data object NotStarted : GameState

        /**
         * Countdown phase (3-2-1-GO!).
         * @property countdownValue The current countdown number (3, 2, 1, 0 for GO)
         */
        data class Countdown(
            val countdownValue: Int,
        ) : GameState

        /**
         * Active gameplay phase.
         * Player is solving sequence puzzles against the timer.
         */
        data object Playing : GameState

        /**
         * Game over phase showing results.
         *
         * @property finalScore Total correct answers
         * @property totalAttempts Total sequences attempted
         * @property isNewRecord Whether this is a new personal best
         * @property accuracy Percentage of correct answers
         * @property averageTimePerSequence Average seconds per sequence
         * @property unlockedBadges List of badges unlocked in this session
         */
        data class Finished(
            val finalScore: Int,
            val totalAttempts: Int,
            val isNewRecord: Boolean,
            val accuracy: Float,
            val averageTimePerSequence: Float,
            val unlockedBadges: List<Badge> = emptyList(),
        ) : GameState
    }

    /**
     * UI state for the Number Sequence game.
     *
     * @property gameState Current phase of the game
     * @property currentSequence The current sequence puzzle to solve
     * @property currentAnswer User's current typed answer
     * @property score Number of correct answers so far
     * @property timeRemaining Seconds left in the game
     * @property personalBest User's highest score for this game
     * @property totalAttempts Number of sequences attempted this session
     * @property correctAnswers Number of correct answers this session
     * @property lastAnswerCorrect Whether the last answer was correct (null if no answer yet)
     * @property userName The user's display name
     * @property eventSink Callback to handle user events
     */
    data class State(
        val gameState: GameState,
        val currentSequence: SequenceQuestion?,
        val currentAnswer: String,
        val score: Int,
        val timeRemaining: Int,
        val personalBest: Int,
        val totalAttempts: Int,
        val correctAnswers: Int,
        val lastAnswerCorrect: Boolean?,
        val userName: String?,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events that can occur in the Number Sequence game.
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User pressed the start game button.
         */
        data object StartGame : Event

        /**
         * User entered a digit on the number pad.
         * @property digit The digit entered (0-9)
         */
        data class NumberEntered(
            val digit: Int,
        ) : Event

        /**
         * User pressed the backspace button.
         */
        data object Backspace : Event

        /**
         * User submitted their answer.
         */
        data object CheckAnswer : Event

        /**
         * User wants to play again after game over.
         */
        data object PlayAgain : Event

        /**
         * User wants to navigate back to game selection.
         */
        data object NavigateHome : Event
    }
}
