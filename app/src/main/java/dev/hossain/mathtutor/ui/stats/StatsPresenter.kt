package dev.hossain.mathtutor.ui.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
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

            // Collect overall statistics
            val overallStats by sessionRepository.getOverallStats().collectAsState(
                initial = SessionStats.EMPTY,
            )

            // Collect recent sessions (last 10)
            val recentSessions by sessionRepository.getRecentSessions(limit = 10).collectAsState(
                initial = emptyList(),
            )

            // Collect statistics for each operation
            val additionStats by sessionRepository.getStatsByOperation(MathOperation.ADDITION).collectAsState(
                initial = SessionStats.EMPTY,
            )
            val subtractionStats by sessionRepository.getStatsByOperation(MathOperation.SUBTRACTION).collectAsState(
                initial = SessionStats.EMPTY,
            )

            // Log stats only when they change (not on every recomposition)
            LaunchedEffect(overallStats) {
                Timber.d(
                    "StatsScreen: Overall stats loaded - sessionCount=${overallStats.sessionCount}, " +
                        "totalProblems=${overallStats.totalProblems}, accuracy=${overallStats.accuracy}",
                )
            }
            LaunchedEffect(recentSessions.size) {
                Timber.d("StatsScreen: Loaded ${recentSessions.size} recent sessions")
            }
            LaunchedEffect(additionStats, subtractionStats) {
                Timber.d(
                    "StatsScreen: Operation stats - Addition(sessions=${additionStats.sessionCount}), " +
                        "Subtraction(sessions=${subtractionStats.sessionCount})",
                )
            }

            // Build operation stats map, only including operations with sessions
            val operationStats =
                buildMap {
                    if (additionStats.sessionCount > 0) {
                        put(MathOperation.ADDITION, additionStats)
                    }
                    if (subtractionStats.sessionCount > 0) {
                        put(MathOperation.SUBTRACTION, subtractionStats)
                    }
                }

            return StatsScreen.State(
                overallStats = overallStats,
                operationStats = operationStats,
                recentSessions = recentSessions,
            ) { event ->
                when (event) {
                    is StatsScreen.Event.BackPressed -> {
                        Timber.d("StatsScreen: Back pressed")
                        navigator.pop()
                    }
                }
            }
        }
    }
