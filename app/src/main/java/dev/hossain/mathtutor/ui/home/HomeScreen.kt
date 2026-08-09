package dev.hossain.mathtutor.ui.home

import com.slack.circuit.serialization.CircuitSerializable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.zacsweers.metro.AppScope
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
@CircuitSerializable(AppScope::class)
data object HomeScreen : Screen {
    /**
     * State for [HomeScreen].
     *
     * @property userName Optional user name for personalized greeting (null = generic greeting)
     * @property gradeLevel User's current grade level (null if no profile)
     * @property streakData Current streak data, null if no practice history
     * @property overallStats Overall session statistics
     * @property recentBadges List of 3 most recently unlocked badges
     * @property isMusicPlaying Whether background music is currently playing
     * @property eventSink Handler for screen events
     */
    data class State(
        val userName: String?,
        val gradeLevel: GradeLevel?,
        val streakData: DailyStreak?,
        val overallStats: SessionStats,
        val recentBadges: List<Badge>,
        val isMusicPlaying: Boolean = false,
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

        /**
         * User tapped the Settings button.
         */
        data object ViewSettingsClicked : Event

        /**
         * User tapped the Music toggle button.
         */
        data object ToggleMusicClicked : Event

        /**
         * User tapped the Games button.
         */
        data object ViewGamesClicked : Event
    }
}
