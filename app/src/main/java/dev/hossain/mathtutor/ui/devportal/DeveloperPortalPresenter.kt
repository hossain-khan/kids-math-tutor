package dev.hossain.mathtutor.ui.devportal

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
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.audio.AudioService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.domain.repository.GameRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.usecase.CheckBadgeUnlocksUseCase
import dev.hossain.mathtutor.haptic.HapticService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Basic scaffold presenter for `DeveloperPortalScreen`.
 * Implements placeholder actions used by dev tools. Concrete implementations of actions
 * will be added later as separate tasks.
 */
@AssistedInject
class DeveloperPortalPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val sessionRepository: SessionRepository,
        private val gameRepository: GameRepository,
        private val checkBadgeUnlocksUseCase: CheckBadgeUnlocksUseCase,
        private val audioService: AudioService,
        private val hapticService: HapticService,
        private val analyticsService: AnalyticsService,
    ) : Presenter<DeveloperPortalScreen.State> {
        @CircuitInject(DeveloperPortalScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): DeveloperPortalPresenter
        }

        @Composable
        override fun present(): DeveloperPortalScreen.State {
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Developer Portal",
                    screenClass = DeveloperPortalScreen::class.java.name,
                )
            }

            val scope = rememberCoroutineScope()
            var showSeedSection by remember { mutableStateOf(true) }
            var showDataOpsSection by remember { mutableStateOf(true) }
            var showDiagnosticsSection by remember { mutableStateOf(true) }
            var showClearConfirm by remember { mutableStateOf(false) }
            var clearInProgress by remember { mutableStateOf(false) }
            var clearResultMessage by remember { mutableStateOf<String?>(null) }

            return DeveloperPortalScreen.State(
                showSeedSection = showSeedSection,
                showDataOpsSection = showDataOpsSection,
                showDiagnosticsSection = showDiagnosticsSection,
                showClearConfirm = showClearConfirm,
                clearInProgress = clearInProgress,
                clearResultMessage = clearResultMessage,
            ) { event ->
                when (event) {
                    is DeveloperPortalScreen.Event.ToggleAnalyticsOverride -> {
                        // Toggle analytics immediately (debug-only)
                        scope.launch(Dispatchers.IO) {
                            val current = userPreferencesRepository.isAnalyticsEnabled.firstOrNull() ?: true
                            userPreferencesRepository.setAnalyticsEnabled(!current)
                            analyticsService.setAnalyticsEnabled(!current)
                            Timber.d("[DevPortal] Toggled analytics to ${!current}")
                        }
                    }

                    is DeveloperPortalScreen.Event.ClearAppDataClicked -> {
                        // Show confirmation dialog instead of clearing immediately
                        showClearConfirm = true
                        clearResultMessage = null
                    }

                    is DeveloperPortalScreen.Event.ConfirmClear -> {
                        // Expect user to type "DELETE" (case-sensitive) to confirm
                        if (event.confirmationText != "DELETE") {
                            clearResultMessage = "Confirmation text does not match 'DELETE'"
                            Timber.d("[DevPortal] Clear confirmation failed - wrong text")
                        } else {
                            // Perform clear
                            clearInProgress = true
                            scope.launch(Dispatchers.IO) {
                                try {
                                    Timber.d("[DevPortal] Clearing app data (DB, prefs, cache) - confirmed")
                                    sessionRepository.clearAllSessions()
                                    gameRepository.clearAllSessions()

                                    // Reset preferences to defaults
                                    userPreferencesRepository.setOnboardingCompleted(false)
                                    userPreferencesRepository.setHapticsEnabled(true)
                                    userPreferencesRepository.setSoundEffectsEnabled(true)
                                    userPreferencesRepository.setBackgroundMusicEnabled(false)
                                    userPreferencesRepository.setVolume(0.7f)
                                    userPreferencesRepository.setHighContrastEnabled(false)
                                    userPreferencesRepository.setLargeTextEnabled(false)
                                    userPreferencesRepository.setAnalyticsEnabled(true)

                                    withContext(Dispatchers.Main) {
                                        clearResultMessage = "Clear complete"
                                        showClearConfirm = false
                                        clearInProgress = false
                                    }
                                    Timber.d("[DevPortal] Clear complete")
                                } catch (e: Exception) {
                                    Timber.e(e, "[DevPortal] Failed to clear data")
                                    withContext(Dispatchers.Main) {
                                        clearResultMessage = "Clear failed: ${e.message}"
                                        clearInProgress = false
                                    }
                                }
                            }
                        }
                    }

                    is DeveloperPortalScreen.Event.CancelClear -> {
                        showClearConfirm = false
                        clearResultMessage = "Clear cancelled"
                    }

                    is DeveloperPortalScreen.Event.SeedSessionsClicked -> {
                        scope.launch(Dispatchers.IO) {
                            Timber.d("[DevPortal] Seeding sample sessions (placeholder)")
                            // Placeholder: Actual seed implementation will be in a follow-up task
                        }
                    }

                    is DeveloperPortalScreen.Event.ForceBadgeCheckClicked -> {
                        scope.launch(Dispatchers.IO) {
                            val unlocked = checkBadgeUnlocksUseCase.checkAndUnlockBadges()
                            Timber.d("[DevPortal] Force badge check unlocked ${unlocked.size} badges")
                        }
                    }

                    is DeveloperPortalScreen.Event.PlaySuccessSound -> {
                        audioService.playSuccess()
                        hapticService.triggerSuccess()
                    }

                    is DeveloperPortalScreen.Event.NavigateBack -> {
                        navigator.pop()
                    }
                }
            }
        }
    }
