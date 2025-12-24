package dev.hossain.mathtutor.ui.stats.accuracydetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.domain.model.DailyAccuracy
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import timber.log.Timber

/**
 * Presenter for [AccuracyDetailsScreen].
 *
 * Manages the state and business logic for displaying daily accuracy data.
 * Collects data from the session repository and formats it for display.
 */
@AssistedInject
class AccuracyDetailsPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val sessionRepository: SessionRepository,
        private val analyticsService: AnalyticsService,
    ) : Presenter<AccuracyDetailsScreen.State> {
        @CircuitInject(AccuracyDetailsScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): AccuracyDetailsPresenter
        }

        @Composable
        override fun present(): AccuracyDetailsScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "AccuracyDetails",
                    screenClass = AccuracyDetailsScreen::class.java.name,
                )
            }

            // Collect daily accuracy data
            val dailyAccuracyList by produceRetainedState<List<DailyAccuracy>>(initialValue = emptyList()) {
                sessionRepository.getDailyAccuracy().collect { data ->
                    Timber.d("AccuracyDetailsPresenter: Loaded ${data.size} days of accuracy data")
                    value = data
                }
            }

            return AccuracyDetailsScreen.State(
                dailyAccuracyList = dailyAccuracyList,
                isLoading = dailyAccuracyList.isEmpty(),
            ) { event ->
                when (event) {
                    is AccuracyDetailsScreen.Event.BackPressed -> {
                        Timber.d("AccuracyDetailsScreen: Back pressed")
                        navigator.pop()
                    }
                }
            }
        }
    }
