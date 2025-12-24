package dev.hossain.mathtutor.ui.stats.accuracydetails

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.DailyAccuracy
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for displaying daily accuracy details.
 *
 * Shows a list of daily accuracy data based on practice sessions completed.
 * Each day shows the date, number of sessions, total problems, and accuracy percentage.
 */
@Parcelize
data object AccuracyDetailsScreen : Screen {
    /**
     * State for [AccuracyDetailsScreen].
     *
     * @property dailyAccuracyList List of daily accuracy data, sorted by date (most recent first)
     * @property isLoading Whether data is being loaded
     * @property eventSink Handler for screen events
     */
    data class State(
        val dailyAccuracyList: List<DailyAccuracy>,
        val isLoading: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [AccuracyDetailsScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User pressed the back button.
         */
        data object BackPressed : Event
    }
}
