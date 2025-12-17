package dev.hossain.mathtutor.ui.practiceresults

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.ui.operationselector.OperationSelectorScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

/**
 * Presenter for [ResultsScreen].
 *
 * Calculates session statistics and handles navigation events.
 */
@AssistedInject
class ResultsPresenter
    constructor(
        @Assisted private val screen: ResultsScreen,
        @Assisted private val navigator: Navigator,
    ) : Presenter<ResultsScreen.State> {
        @CircuitInject(ResultsScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(
                screen: ResultsScreen,
                navigator: Navigator,
            ): ResultsPresenter
        }

        @Composable
        override fun present(): ResultsScreen.State {
            // Calculate problem results
            val problemResults =
                screen.problems.mapIndexed { index, problem ->
                    val userAnswer = screen.userAnswers.getOrNull(index)
                    val isCorrect = userAnswer?.let { problem.checkAnswer(it) } ?: false

                    ResultsScreen.ProblemResult(
                        problem = problem,
                        userAnswer = userAnswer,
                        isCorrect = isCorrect,
                    )
                }

            val totalProblems = problemResults.size
            val correctCount = problemResults.count { it.isCorrect }
            val accuracyPercentage =
                if (totalProblems > 0) {
                    (correctCount.toFloat() / totalProblems) * 100f
                } else {
                    0f
                }

            return ResultsScreen.State(
                totalProblems = totalProblems,
                correctCount = correctCount,
                accuracyPercentage = accuracyPercentage,
                problemResults = problemResults,
            ) { event ->
                when (event) {
                    is ResultsScreen.Event.TryAgain -> {
                        // Navigate back to operation selector to choose a new practice session
                        navigator.resetRoot(OperationSelectorScreen)
                    }

                    is ResultsScreen.Event.NavigateBack -> {
                        navigator.pop()
                    }
                }
            }
        }
    }
