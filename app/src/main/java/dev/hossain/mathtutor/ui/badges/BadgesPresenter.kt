package dev.hossain.mathtutor.ui.badges

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsParam
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.repository.BadgeProgress
import dev.hossain.mathtutor.domain.repository.BadgeRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import timber.log.Timber

/**
 * Presenter for [BadgesScreen].
 *
 * Manages the state and business logic for displaying badges organized by category.
 * Collects badge data from the repository and handles user interactions.
 */
@AssistedInject
class BadgesPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val badgeRepository: BadgeRepository,
        private val analyticsService: AnalyticsService,
    ) : Presenter<BadgesScreen.State> {
        @CircuitInject(BadgesScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): BadgesPresenter
        }

        @Composable
        override fun present(): BadgesScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Badges",
                    screenClass = BadgesScreen::class.java.name,
                )
            }

            // Track selected badge for detail dialog
            var selectedBadge by remember { mutableStateOf<Badge?>(null) }

            // Use produceRetainedState to batch badge collection, progress summary, and grouping
            // This reduces overhead by:
            // 1. Combining multiple flow collections into a single producer
            // 2. Caching the expensive groupBy computation
            // 3. Retaining state across configuration changes without reprocessing
            data class BadgeData(
                val badgesByCategory: Map<BadgeCategory, List<Badge>>,
                val progressSummary: BadgeProgress,
            )

            val badgeData by produceRetainedState(
                initialValue =
                    BadgeData(
                        badgesByCategory = emptyMap(),
                        progressSummary = BadgeProgress(unlockedCount = 0, totalCount = 0),
                    ),
            ) {
                // Collect all badges
                badgeRepository.getAllBadges().collect { badges ->
                    // Also collect progress summary
                    badgeRepository.getProgressSummary().collect { progress ->
                        // Group badges by category and update state
                        val grouped = badges.groupBy { it.category }
                        Timber.d("BadgesPresenter: Loaded ${badges.size} badges in ${grouped.size} categories")
                        value =
                            BadgeData(
                                badgesByCategory = grouped,
                                progressSummary = progress,
                            )
                    }
                }
            }

            return BadgesScreen.State(
                badgesByCategory = badgeData.badgesByCategory,
                progressSummary = badgeData.progressSummary,
                selectedBadge = selectedBadge,
            ) { event ->
                when (event) {
                    is BadgesScreen.Event.BadgeClicked -> {
                        Timber.d("Badge clicked: ${event.badge.name} (${event.badge.id})")
                        analyticsService.logEvent(
                            eventName = AnalyticsEvent.BADGES_VIEWED,
                            parameters =
                                mapOf(
                                    AnalyticsParam.BADGE_ID to event.badge.id,
                                    AnalyticsParam.BADGE_NAME to event.badge.name,
                                ),
                        )
                        selectedBadge = event.badge
                    }

                    is BadgesScreen.Event.CloseDialog -> {
                        Timber.d("Badge detail dialog closed")
                        selectedBadge = null
                    }

                    is BadgesScreen.Event.BackPressed -> {
                        Timber.d("Back pressed from badges screen")
                        navigator.pop()
                    }
                }
            }
        }
    }
