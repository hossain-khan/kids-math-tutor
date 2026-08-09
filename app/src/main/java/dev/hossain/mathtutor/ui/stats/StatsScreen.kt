package dev.hossain.mathtutor.ui.stats

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.SessionStats
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for displaying practice session statistics and history.
 *
 * Shows overall statistics, per-operation breakdown, and recent session history.
 */
import com.slack.circuit.serialization.CircuitSerializable
import dev.zacsweers.metro.AppScope

@Parcelize
@CircuitSerializable(AppScope::class)
data object StatsScreen : Screen {
    /**
     * State for [StatsScreen].
     *
     * @property userName Optional user name for personalized title
     * @property overallStats Overall statistics across all sessions
     * @property operationStats Statistics grouped by math operation
     * @property recentSessions List of recent practice sessions (up to 10)
     * @property eventSink Handler for screen events
     */
    data class State(
        val userName: String?,
        val overallStats: SessionStats,
        val operationStats: Map<MathOperation, SessionStats>,
        val recentSessions: List<PracticeSessionEntity>,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [StatsScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User pressed the back button.
         */
        data object BackPressed : Event

        /**
         * User clicked on the accuracy card.
         */
        data object AccuracyClicked : Event
    }
}
