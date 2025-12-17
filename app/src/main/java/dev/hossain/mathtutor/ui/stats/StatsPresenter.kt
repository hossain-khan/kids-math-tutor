package dev.hossain.mathtutor.ui.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

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
    ) : Presenter<StatsScreen.State> {
        @CircuitInject(StatsScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): StatsPresenter
        }

        @Composable
        override fun present(): StatsScreen.State {
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
                        navigator.pop()
                    }
                }
            }
        }
    }
