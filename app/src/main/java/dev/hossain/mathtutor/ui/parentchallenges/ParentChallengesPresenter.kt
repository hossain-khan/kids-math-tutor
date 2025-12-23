package dev.hossain.mathtutor.ui.parentchallenges

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.rememberAnsweringNavigator
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsParam
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.service.CustomChallengeService
import dev.hossain.mathtutor.ui.importchallenge.ImportChallengeScreen
import dev.hossain.mathtutor.ui.mathpractice.MathPracticeScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Presenter for [ParentChallengesScreen].
 *
 * Manages the state and business logic for displaying and managing custom challenges.
 * Handles challenge filtering (active vs archived), deletion with confirmation,
 * and navigation to import and practice screens.
 */
@AssistedInject
class ParentChallengesPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val challengeService: CustomChallengeService,
        private val analyticsService: AnalyticsService,
    ) : Presenter<ParentChallengesScreen.State> {
        @CircuitInject(ParentChallengesScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): ParentChallengesPresenter
        }

        @Composable
        override fun present(): ParentChallengesScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Parent Challenges",
                    screenClass = ParentChallengesScreen::class.java.name,
                )
            }

            val coroutineScope = rememberCoroutineScope()
            var showArchived by rememberSaveable { mutableStateOf(false) }
            var showDeleteConfirmation by remember { mutableStateOf(false) }
            var challengeToDelete by remember { mutableStateOf<CustomChallenge?>(null) }
            var importSuccessMessage by remember { mutableStateOf<String?>(null) }

            // Navigator that handles import results using Circuit's PopResult pattern
            val importNavigator =
                rememberAnsweringNavigator<ImportChallengeScreen.ImportResult>(navigator) { result ->
                    Timber.d("ParentChallenges: PopResult callback! challengeTitle=${result.challengeTitle}")
                    importSuccessMessage = "Challenge \"${result.challengeTitle}\" imported successfully!"
                }

            // Observe active challenges from service
            val activeChallenges by challengeService
                .observeActiveChallenges()
                .collectAsState(initial = emptyList())

            // Filter challenges based on showArchived flag
            val displayedChallenges =
                if (showArchived) {
                    activeChallenges
                } else {
                    activeChallenges.filter { !it.isArchived }
                }

            return ParentChallengesScreen.State(
                challenges = displayedChallenges,
                isLoading = false,
                showArchived = showArchived,
                showDeleteConfirmation = showDeleteConfirmation,
                challengeToDelete = challengeToDelete,
                importSuccessMessage = importSuccessMessage,
            ) { event ->
                when (event) {
                    is ParentChallengesScreen.Event.ImportNewChallenge -> {
                        analyticsService.logEvent(
                            eventName = AnalyticsEvent.CUSTOM_CHALLENGE_IMPORT_STARTED,
                            parameters = mapOf(AnalyticsParam.SOURCE to "parent_challenges_screen"),
                        )
                        // Use importNavigator to receive the result when import completes
                        importNavigator.goTo(ImportChallengeScreen())
                    }

                    is ParentChallengesScreen.Event.ChallengeSelected -> {
                        analyticsService.logEvent(
                            eventName = AnalyticsEvent.CUSTOM_CHALLENGE_STARTED,
                            parameters =
                                mapOf(
                                    AnalyticsParam.CHALLENGE_ID to event.challenge.id,
                                    AnalyticsParam.PROBLEM_COUNT to
                                        event.challenge.problems.size
                                            .toString(),
                                ),
                        )
                        // Navigate to practice screen with custom challenge ID
                        val operation =
                            event.challenge.problems
                                .firstOrNull()
                                ?.operation
                                ?: dev.hossain.mathtutor.domain.model.MathOperation.ADDITION
                        navigator.goTo(
                            MathPracticeScreen(
                                operation = operation,
                                problemCount = event.challenge.problems.size,
                                customChallengeId = event.challenge.id,
                            ),
                        )
                    }

                    is ParentChallengesScreen.Event.ArchiveChallenge -> {
                        analyticsService.logEvent(
                            eventName = AnalyticsEvent.CUSTOM_CHALLENGE_ARCHIVED,
                            parameters = mapOf(AnalyticsParam.CHALLENGE_ID to event.challengeId),
                        )
                        coroutineScope.launch {
                            try {
                                challengeService.archiveChallenge(event.challengeId)
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to archive challenge: ${event.challengeId}")
                            }
                        }
                    }

                    is ParentChallengesScreen.Event.DeleteChallengeRequested -> {
                        challengeToDelete = event.challenge
                        showDeleteConfirmation = true
                    }

                    is ParentChallengesScreen.Event.ConfirmDelete -> {
                        Timber.d("ParentChallenges: Delete confirmed - ${event.challengeId}")
                        analyticsService.logEvent(
                            eventName = AnalyticsEvent.CUSTOM_CHALLENGE_DELETED,
                            parameters = mapOf(AnalyticsParam.CHALLENGE_ID to event.challengeId),
                        )
                        coroutineScope.launch {
                            try {
                                challengeService.deleteChallenge(event.challengeId)
                                showDeleteConfirmation = false
                                challengeToDelete = null
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to delete challenge: ${event.challengeId}")
                            }
                        }
                    }

                    is ParentChallengesScreen.Event.CancelDelete -> {
                        Timber.d("ParentChallenges: Delete cancelled")
                        showDeleteConfirmation = false
                        challengeToDelete = null
                    }

                    is ParentChallengesScreen.Event.ToggleArchived -> {
                        Timber.d("ParentChallenges: Toggle archived - ${event.show}")
                        showArchived = event.show
                    }

                    is ParentChallengesScreen.Event.NavigateBack -> {
                        Timber.d("ParentChallenges: Navigate back")
                        navigator.pop()
                    }

                    is ParentChallengesScreen.Event.DismissImportSuccess -> {
                        importSuccessMessage = null
                    }
                }
            }
        }
    }
