package dev.hossain.mathtutor.ui.practiceresults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.usecase.CheckBadgeUnlocksUseCase
import dev.hossain.mathtutor.domain.usecase.UpdateStreakUseCase
import dev.hossain.mathtutor.ui.home.HomeScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

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
        private val checkBadgeUnlocksUseCase: CheckBadgeUnlocksUseCase,
        private val updateStreakUseCase: UpdateStreakUseCase,
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
            val coroutineScope = rememberCoroutineScope()
            var unlockedBadges by remember { mutableStateOf<List<Badge>>(emptyList()) }
            var showBadgeUnlock by remember { mutableStateOf(false) }
            var currentBadgeIndex by remember { mutableStateOf(0) }

            // Backup: Check for badges and update streak on results screen load
            // This serves as a fallback if the practice screen doesn't handle it
            // Skip if badges were already checked to avoid duplicate processing
            LaunchedEffect(Unit) {
                if (!screen.badgesAlreadyChecked) {
                    Timber.d("[ResultsPresenter] Running backup badge/streak check")
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            // Update streak as backup
                            Timber.d("[ResultsPresenter] Updating streak as backup...")
                            val updatedStreak = updateStreakUseCase.updateStreak()
                            Timber.d("[ResultsPresenter] Streak updated: current=${updatedStreak.currentStreak}")

                            // Check for badge unlocks as backup
                            Timber.d("[ResultsPresenter] Checking for badge unlocks as backup...")
                            val newlyUnlocked = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

                            // Switch to Main dispatcher for state updates
                            withContext(Dispatchers.Main) {
                                if (newlyUnlocked.isNotEmpty()) {
                                    Timber.d("[ResultsPresenter] Unlocked ${newlyUnlocked.size} badges in results screen")
                                    unlockedBadges = newlyUnlocked
                                    showBadgeUnlock = true
                                    currentBadgeIndex = 0
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "[ResultsPresenter] Failed to check achievements")
                        }
                    }
                } else {
                    Timber.d("[ResultsPresenter] Skipping backup check - badges already processed in practice screen")
                }
            }

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
                unlockedBadges = unlockedBadges,
                showBadgeUnlock = showBadgeUnlock,
                currentBadgeIndex = currentBadgeIndex,
            ) { event ->
                when (event) {
                    is ResultsScreen.Event.TryAgain -> {
                        // Navigate back to home screen
                        navigator.resetRoot(HomeScreen)
                    }

                    is ResultsScreen.Event.NavigateBack -> {
                        navigator.pop()
                    }

                    is ResultsScreen.Event.DismissBadgeDialog -> {
                        // Check if there are more badges to show
                        if (currentBadgeIndex < unlockedBadges.size - 1) {
                            currentBadgeIndex++
                        } else {
                            // All badges shown, hide dialog
                            showBadgeUnlock = false
                        }
                    }
                }
            }
        }
    }
