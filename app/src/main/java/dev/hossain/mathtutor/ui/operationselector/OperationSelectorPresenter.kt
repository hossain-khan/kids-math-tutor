package dev.hossain.mathtutor.ui.operationselector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsParam
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.ui.mathpractice.MathPracticeScreen
import dev.hossain.mathtutor.ui.stats.StatsScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import timber.log.Timber

/**
 * Presenter for [OperationSelectorScreen].
 *
 * Manages the state and business logic for operation selection.
 * - Fetches the user's current grade level to determine available operations
 * - Checks for session history to enable/disable stats button
 */
@AssistedInject
class OperationSelectorPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val sessionRepository: SessionRepository,
        private val userProfileRepository: UserProfileRepository,
        private val analyticsService: AnalyticsService,
    ) : Presenter<OperationSelectorScreen.State> {
        @CircuitInject(OperationSelectorScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): OperationSelectorPresenter
        }

        @Composable
        override fun present(): OperationSelectorScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Operation Selector",
                    screenClass = OperationSelectorScreen::class.java.name,
                )
            }

            // Fetch user profile to get grade level
            val userProfile by userProfileRepository.getProfile().collectAsState(initial = null)
            val gradeLevel = userProfile?.gradeLevel ?: GradeLevel.GRADE_1

            // Check if session history exists
            val overallStats by sessionRepository.getOverallStats().collectAsState(
                initial = dev.hossain.mathtutor.domain.model.SessionStats.EMPTY,
            )
            val hasSessionHistory = overallStats.sessionCount > 0

            // Log only when grade or stats change (not on every recomposition)
            LaunchedEffect(gradeLevel, overallStats.sessionCount) {
                val availableOps = gradeLevel.getAvailableOperations().map { it.displayName }
                Timber.d(
                    "OperationSelector: Grade level = ${gradeLevel.displayName}, " +
                        "available operations = $availableOps",
                )
                Timber.d(
                    "OperationSelector: Session history exists = $hasSessionHistory " +
                        "(sessionCount=${overallStats.sessionCount})",
                )
            }

            return OperationSelectorScreen.State(
                gradeLevel = gradeLevel,
                hasSessionHistory = hasSessionHistory,
            ) { event ->
                when (event) {
                    is OperationSelectorScreen.Event.OperationSelected -> {
                        Timber.d("OperationSelector: Operation selected - ${event.operation}")
                        analyticsService.logEvent(
                            eventName = AnalyticsEvent.OPERATION_SELECTED,
                            parameters =
                                mapOf(
                                    AnalyticsParam.OPERATION_TYPE to event.operation.name.lowercase(),
                                ),
                        )
                        // Navigate to MathPracticeScreen with selected operation
                        navigator.goTo(
                            MathPracticeScreen(
                                operation = event.operation,
                                problemCount = 10,
                            ),
                        )
                    }

                    is OperationSelectorScreen.Event.ViewStatsClicked -> {
                        Timber.d("OperationSelector: View stats clicked")
                        navigator.goTo(StatsScreen)
                    }

                    is OperationSelectorScreen.Event.NavigateBack -> {
                        Timber.d("OperationSelector: Navigate back")
                        navigator.pop()
                    }
                }
            }
        }
    }
