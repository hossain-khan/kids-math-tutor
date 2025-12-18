package dev.hossain.mathtutor.ui.home

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.model.SessionStats
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for the home dashboard.
 *
 * This is the main entry point of the app after onboarding, displaying:
 * - Welcome message
 * - Current streak with calendar
 * - Quick stats overview
 * - Recently unlocked badges
 * - Primary action button to start practice
 */
@Parcelize
data object HomeScreen : Screen {
    /**
     * State for [HomeScreen].
     *
     * @property userName Optional user name for personalized greeting (null = generic greeting)
     * @property streakData Current streak data, null if no practice history
     * @property overallStats Overall session statistics
     * @property recentBadges List of 3 most recently unlocked badges
     * @property eventSink Handler for screen events
     */
    data class State(
        val userName: String?,
        val streakData: DailyStreak?,
        val overallStats: SessionStats,
        val recentBadges: List<Badge>,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [HomeScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User tapped the Start Practice button.
         */
        data object StartPracticeClicked : Event

        /**
         * User tapped the View Full Stats link.
         */
        data object ViewStatsClicked : Event

        /**
         * User tapped the View All Badges link.
         */
        data object ViewBadgesClicked : Event
    }
}
