package dev.hossain.mathtutor.ui.badges

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.repository.BadgeProgress
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for displaying all badges organized by category.
 *
 * This screen shows all 15 badges grouped by their categories (Getting Started, Volume,
 * Operation Mastery, Speed & Accuracy, and Streak). Users can view their progress,
 * see which badges are unlocked/locked, and tap badges to see details.
 */
@Parcelize
data object BadgesScreen : Screen {
    /**
     * State for [BadgesScreen].
     *
     * @property badgesByCategory All badges grouped by their category
     * @property progressSummary Summary of unlocked vs total badges
     * @property selectedBadge Currently selected badge for detail dialog, null if no dialog shown
     * @property eventSink Handler for screen events
     */
    data class State(
        val badgesByCategory: Map<BadgeCategory, List<Badge>>,
        val progressSummary: BadgeProgress,
        val selectedBadge: Badge?,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [BadgesScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User tapped a badge to view details.
         */
        data class BadgeClicked(
            val badge: Badge,
        ) : Event

        /**
         * User dismissed the badge detail dialog.
         */
        data object CloseDialog : Event

        /**
         * User pressed the back button.
         */
        data object BackPressed : Event
    }
}
