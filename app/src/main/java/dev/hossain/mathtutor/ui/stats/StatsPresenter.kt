package dev.hossain.mathtutor.ui.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.model.UserProfile
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.combine
import timber.log.Timber

/**
 * Presenter for [StatsScreen].
 *
 * Manages the state and business logic for displaying practice statistics.
 * Collects data from the session repository and formats it for display.
 */
@AssistedInject
class StatsPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val sessionRepository: SessionRepository,
        private val userProfileRepository: UserProfileRepository,
        private val analyticsService: AnalyticsService,
    ) : Presenter<StatsScreen.State> {
        @CircuitInject(StatsScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): StatsPresenter
        }

        @Composable
        override fun present(): StatsScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Stats",
                    screenClass = StatsScreen::class.java.name,
                )
            }

            // Use produceRetainedState to batch all statistics collection
            // This reduces overhead by:
            // 1. Combining 4 separate flow collections into a single producer
            // 2. Building operationStats map only once per data update
            // 3. Retaining state across configuration changes without reprocessing
            data class StatsData(
                val overallStats: SessionStats,
                val recentSessions: List<PracticeSessionEntity>,
                val operationStats: Map<MathOperation, SessionStats>,
            )

            val statsData by produceRetainedState(
                initialValue =
                    StatsData(
                        overallStats = SessionStats.EMPTY,
                        recentSessions = emptyList(),
                        operationStats = emptyMap(),
                    ),
            ) {
                // Combine all flows - emits whenever any flow updates
                combine(
                    sessionRepository.getOverallStats(),
                    sessionRepository.getRecentSessions(limit = 10),
                    sessionRepository.getStatsByOperation(MathOperation.ADDITION),
                    sessionRepository.getStatsByOperation(MathOperation.SUBTRACTION),
                ) { overall, sessions, addition, subtraction ->
                    // Build operation stats map
                    val operationStatsMap =
                        buildMap {
                            if (addition.sessionCount > 0) {
                                put(MathOperation.ADDITION, addition)
                            }
                            if (subtraction.sessionCount > 0) {
                                put(MathOperation.SUBTRACTION, subtraction)
                            }
                        }

                    Timber.d(
                        "StatsPresenter: Loaded stats - sessions=${overall.sessionCount}, " +
                            "totalProblems=${overall.totalProblems}, accuracy=${overall.accuracy}, " +
                            "recentSessions=${sessions.size}, operations=${operationStatsMap.size}",
                    )

                    StatsData(
                        overallStats = overall,
                        recentSessions = sessions,
                        operationStats = operationStatsMap,
                    )
                }.collect { data ->
                    value = data
                }
            }

            // Collect user profile
            val userProfile by produceRetainedState<UserProfile?>(initialValue = null) {
                userProfileRepository.getProfile().collect { profile ->
                    value = profile
                }
            }

            return StatsScreen.State(
                userName = userProfile?.name,
                overallStats = statsData.overallStats,
                operationStats = statsData.operationStats,
                recentSessions = statsData.recentSessions,
            ) { event ->
                when (event) {
                    is StatsScreen.Event.BackPressed -> {
                        Timber.d("StatsScreen: Back pressed")
                        navigator.pop()
                    }

                    is StatsScreen.Event.AccuracyClicked -> {
                        Timber.d("StatsScreen: Accuracy clicked, navigating to details")
                        navigator.goTo(dev.hossain.mathtutor.ui.stats.accuracydetails.AccuracyDetailsScreen)
                    }
                }
            }
        }
    }
