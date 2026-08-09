package dev.hossain.mathtutor.ui.memorymatch

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.zacsweers.metro.AppScope
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for Memory Match game.
 *
 * Memory Match is a card-matching game where players flip cards to find pairs
 * of math problems and their corresponding answers. Features include:
 * - 4×4 grid of cards (16 cards = 8 pairs)
 * - Flip two cards at a time
 * - Match problems with answers
 * - Track moves and time
 *
 * @property isTrialMode Whether this game is being played in trial mode (locked game).
 *                       When true, no badges will be awarded and progress may not be saved.
 */
@Parcelize
@CircuitSerializable(AppScope::class)
data class MemoryMatchScreen(
    val isTrialMode: Boolean = false,
) : Screen {
    /**
     * Represents a card in the memory match game.
     *
     * @property id Unique identifier for the card
     * @property content The text to display on the card (problem or answer)
     * @property pairId Identifier linking this card to its match
     * @property isFlipped Whether the card is currently face-up
     * @property isMatched Whether this card has been successfully matched
     */
    data class Card(
        val id: Int,
        val content: String,
        val pairId: Int,
        val isFlipped: Boolean = false,
        val isMatched: Boolean = false,
    )

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
         * @property moves Total number of moves made
         * @property timeElapsed Time taken to complete in seconds
         * @property isNewRecord Whether this is a new personal best (fastest time)
         * @property accuracy Percentage of first-try matches (0-100)
         * @property unlockedBadges List of badges unlocked during this game
         */
        data class Finished(
            val moves: Int,
            val timeElapsed: Int,
            val isNewRecord: Boolean,
            val accuracy: Float,
            val unlockedBadges: List<Badge> = emptyList(),
        ) : GameState
    }

    /**
     * State for [MemoryMatchScreen].
     */
    data class State(
        val gameState: GameState = GameState.NotStarted,
        val cards: List<Card> = emptyList(),
        val moves: Int = 0,
        val timeElapsed: Int = 0,
        val matchesFound: Int = 0,
        val totalPairs: Int = 8,
        val personalBestTime: Int = 0, // In seconds, 0 means no record
        val firstFlippedCard: Card? = null,
        val secondFlippedCard: Card? = null,
        val userName: String? = null,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState {
        /**
         * Whether the game is complete (all pairs matched).
         */
        val isComplete: Boolean get() = matchesFound == totalPairs
    }

    /**
     * Events for [MemoryMatchScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * Player pressed the Start Game button.
         */
        data object StartGame : Event

        /**
         * Player flipped a card.
         * @property cardId The ID of the card to flip
         */
        data class CardFlipped(
            val cardId: Int,
        ) : Event

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
