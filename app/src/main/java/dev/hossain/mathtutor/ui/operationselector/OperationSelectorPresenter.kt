package dev.hossain.mathtutor.ui.operationselector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.ui.mathpractice.MathPracticeScreen
import dev.hossain.mathtutor.ui.stats.StatsScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

/**
 * Presenter for [OperationSelectorScreen].
 *
 * Manages the state and business logic for operation selection.
 * Checks for session history to enable/disable stats button.
 */
@AssistedInject
class OperationSelectorPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val sessionRepository: SessionRepository,
    ) : Presenter<OperationSelectorScreen.State> {
        @CircuitInject(OperationSelectorScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): OperationSelectorPresenter
        }

        @Composable
        override fun present(): OperationSelectorScreen.State {
            // Check if session history exists
            val overallStats by sessionRepository.getOverallStats().collectAsState(
                initial = dev.hossain.mathtutor.domain.model.SessionStats.EMPTY,
            )
            val hasSessionHistory = overallStats.sessionCount > 0

            return OperationSelectorScreen.State(
                hasSessionHistory = hasSessionHistory,
            ) { event ->
                when (event) {
                    is OperationSelectorScreen.Event.OperationSelected -> {
                        // Navigate to MathPracticeScreen with selected operation
                        navigator.goTo(
                            MathPracticeScreen(
                                operation = event.operation,
                                problemCount = 10,
                            ),
                        )
                    }

                    is OperationSelectorScreen.Event.ViewStatsClicked -> {
                        navigator.goTo(StatsScreen)
                    }
                }
            }
        }
    }
