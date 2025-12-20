package dev.hossain.mathtutor.ui.games

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.Game
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for the Game Selection Hub.
 *
 * Displays available mini-games with their unlock status, personal bests,
 * and provides navigation to launch games.
 */
@Parcelize
data object GameSelectionScreen : Screen {
    /**
     * Information about a game's unlock status and statistics.
     *
     * @property game The game type
     * @property isUnlocked Whether the game is unlocked based on total problems solved
     * @property personalBest The highest score achieved in this game
     * @property totalPlays Total number of times the game has been played
     */
    data class GameInfo(
        val game: Game,
        val isUnlocked: Boolean,
        val personalBest: Int,
        val totalPlays: Int,
    )

    /**
     * State for [GameSelectionScreen].
     *
     * @property gameInfoList List of all games with their unlock status and stats
     * @property totalProblemsSolved Total problems solved (for showing progress toward unlocks)
     * @property eventSink Handler for screen events
     */
    data class State(
        val gameInfoList: List<GameInfo> = emptyList(),
        val totalProblemsSolved: Int = 0,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [GameSelectionScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User wants to play a specific game.
         * @property game The game to launch
         */
        data class PlayGame(
            val game: Game,
        ) : Event

        /**
         * User wants to navigate back to the previous screen.
         */
        data object NavigateBack : Event
    }
}
