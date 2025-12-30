package dev.hossain.mathtutor.ui.parentsettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.ui.goals.catalog.GoalCatalogScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Presenter for [ParentSettingsScreen].
 *
 * Manages the state and business logic for parent-specific settings including:
 * - PIN setup, reset, and verification
 * - Forgot PIN recovery through math challenges
 * - Grade limit configuration with PIN protection
 *
 * ## Business Logic Rationale
 * - PIN is stored as SHA-256 hash in DataStore for security
 * - Grade limit requires PIN verification to prevent children from changing it
 * - Forgot PIN uses Grade 10+ math problems that children cannot easily solve
 * - All sensitive operations are logged for troubleshooting
 */
@AssistedInject
class ParentSettingsPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val preferencesRepository: UserPreferencesRepository,
        private val userProfileRepository: UserProfileRepository,
        private val analyticsService: AnalyticsService,
    ) : Presenter<ParentSettingsScreen.State> {
        @CircuitInject(ParentSettingsScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): ParentSettingsPresenter
        }

        @Composable
        override fun present(): ParentSettingsScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Parent Settings",
                    screenClass = ParentSettingsScreen::class.java.name,
                )
            }

            val coroutineScope = rememberCoroutineScope()

            // Observe PIN and grade limit from repository
            val pinHash by preferencesRepository.parentPinHash.collectAsState(initial = null)
            val maxGradeLevel by preferencesRepository.maxGradeLevel.collectAsState(initial = null)

            // Dialog states
            var showPinSetup by remember { mutableStateOf(false) }
            var showPinVerification by remember { mutableStateOf(false) }
            var showPinReset by remember { mutableStateOf(false) }
            var showForgotPin by remember { mutableStateOf(false) }
            var showGradeLimit by remember { mutableStateOf(false) }
            var showResetForgotOptions by remember { mutableStateOf(false) }
            var pinVerificationMode by remember {
                mutableStateOf(ParentSettingsScreen.PinVerificationMode.NONE)
            }

            return ParentSettingsScreen.State(
                hasPinSet = pinHash != null,
                maxGradeLevel = maxGradeLevel,
                showPinSetup = showPinSetup,
                showPinVerification = showPinVerification,
                showPinReset = showPinReset,
                showForgotPin = showForgotPin,
                showGradeLimit = showGradeLimit,
                showResetForgotOptions = showResetForgotOptions,
                pinVerificationMode = pinVerificationMode,
            ) { event ->
                when (event) {
                    is ParentSettingsScreen.Event.SetupPinClicked -> {
                        Timber.d("ParentSettings: Setup PIN clicked")
                        analyticsService.logEvent(
                            eventName = AnalyticsEvent.PARENT_PIN_SETUP_STARTED,
                            parameters = emptyMap(),
                        )
                        showPinSetup = true
                    }

                    is ParentSettingsScreen.Event.PinSetupCompleted -> {
                        Timber.d("ParentSettings: PIN setup completed")
                        if (event.pin != event.confirmPin) {
                            Timber.w("ParentSettings: PINs do not match")
                            // TODO: Show error message (implement in UI)
                            return@State
                        }

                        if (event.pin.length != 4 || !event.pin.all { it.isDigit() }) {
                            Timber.w("ParentSettings: Invalid PIN format")
                            // TODO: Show error message (implement in UI)
                            return@State
                        }

                        coroutineScope.launch {
                            try {
                                preferencesRepository.setParentPin(event.pin)
                                analyticsService.logEvent(
                                    eventName = AnalyticsEvent.PARENT_PIN_SETUP_COMPLETED,
                                    parameters = emptyMap(),
                                )
                                showPinSetup = false
                                Timber.i("ParentSettings: PIN successfully set")
                            } catch (e: Exception) {
                                Timber.e(e, "ParentSettings: Failed to set PIN")
                            }
                        }
                    }

                    is ParentSettingsScreen.Event.PinSetupCancelled -> {
                        Timber.d("ParentSettings: PIN setup cancelled")
                        showPinSetup = false
                    }

                    is ParentSettingsScreen.Event.ToggleResetForgotOptions -> {
                        Timber.d("ParentSettings: Toggle reset/forgot options")
                        showResetForgotOptions = !showResetForgotOptions
                    }

                    is ParentSettingsScreen.Event.ResetPinClicked -> {
                        Timber.d("ParentSettings: Reset PIN clicked")
                        // First verify old PIN
                        pinVerificationMode = ParentSettingsScreen.PinVerificationMode.RESET_PIN
                        showPinVerification = true
                    }

                    is ParentSettingsScreen.Event.PinResetCompleted -> {
                        Timber.d("ParentSettings: PIN reset completed")
                        if (event.newPin != event.confirmNewPin) {
                            Timber.w("ParentSettings: New PINs do not match")
                            // TODO: Show error message (implement in UI)
                            return@State
                        }

                        if (event.newPin.length != 4 || !event.newPin.all { it.isDigit() }) {
                            Timber.w("ParentSettings: Invalid new PIN format")
                            // TODO: Show error message (implement in UI)
                            return@State
                        }

                        coroutineScope.launch {
                            try {
                                preferencesRepository.setParentPin(event.newPin)
                                analyticsService.logEvent(
                                    eventName = "parent_pin_reset_completed",
                                    parameters = emptyMap(),
                                )
                                showPinReset = false
                                Timber.i("ParentSettings: PIN successfully reset")
                            } catch (e: Exception) {
                                Timber.e(e, "ParentSettings: Failed to reset PIN")
                            }
                        }
                    }

                    is ParentSettingsScreen.Event.PinResetCancelled -> {
                        Timber.d("ParentSettings: PIN reset cancelled")
                        showPinReset = false
                    }

                    is ParentSettingsScreen.Event.ForgotPinClicked -> {
                        Timber.d("ParentSettings: Forgot PIN clicked")
                        analyticsService.logEvent(
                            eventName = "parent_pin_forgot_started",
                            parameters = emptyMap(),
                        )
                        showForgotPin = true
                    }

                    is ParentSettingsScreen.Event.ForgotPinChallengeCompleted -> {
                        Timber.d("ParentSettings: Forgot PIN challenge completed")
                        // The dialog already verified the answer, just clear PIN
                        Timber.i("ParentSettings: Forgot PIN challenge passed - clearing PIN")
                        coroutineScope.launch {
                            try {
                                preferencesRepository.clearParentPin()
                                analyticsService.logEvent(
                                    eventName = "parent_pin_forgot_completed",
                                    parameters = emptyMap(),
                                )
                                showForgotPin = false
                            } catch (e: Exception) {
                                Timber.e(e, "ParentSettings: Failed to clear PIN")
                            }
                        }
                    }

                    is ParentSettingsScreen.Event.ForgotPinChallengeCancelled -> {
                        Timber.d("ParentSettings: Forgot PIN challenge cancelled")
                        showForgotPin = false
                    }

                    is ParentSettingsScreen.Event.ChangeGradeLimitClicked -> {
                        Timber.d("ParentSettings: Change grade limit clicked")
                        // If PIN is set, require verification first
                        if (pinHash != null) {
                            pinVerificationMode =
                                ParentSettingsScreen.PinVerificationMode.CHANGE_GRADE_LIMIT
                            showPinVerification = true
                        } else {
                            // No PIN set, go directly to grade limit dialog
                            showGradeLimit = true
                        }
                    }

                    is ParentSettingsScreen.Event.GradeLimitChanged -> {
                        Timber.d("ParentSettings: Grade limit changed to ${event.gradeLevel?.displayName}")
                        coroutineScope.launch {
                            try {
                                preferencesRepository.setMaxGradeLevel(event.gradeLevel)

                                // If new limit is set, check if current profile grade exceeds it
                                if (event.gradeLevel != null) {
                                    val currentProfile = userProfileRepository.getProfile().first()
                                    if (currentProfile != null && currentProfile.gradeLevel > event.gradeLevel) {
                                        Timber.w(
                                            "ParentSettings: Current profile grade ${currentProfile.gradeLevel.displayName} " +
                                                "exceeds new limit ${event.gradeLevel.displayName}. Downgrading.",
                                        )
                                        userProfileRepository.updateGradeLevel(event.gradeLevel)
                                    }
                                }

                                analyticsService.logEvent(
                                    eventName = "parent_grade_limit_changed",
                                    parameters =
                                        mapOf(
                                            "grade_level" to
                                                (event.gradeLevel?.name ?: "unlimited"),
                                        ),
                                )
                                showGradeLimit = false
                            } catch (e: Exception) {
                                Timber.e(e, "ParentSettings: Failed to set grade limit")
                            }
                        }
                    }

                    is ParentSettingsScreen.Event.GradeLimitCancelled -> {
                        Timber.d("ParentSettings: Grade limit cancelled")
                        showGradeLimit = false
                    }

                    is ParentSettingsScreen.Event.PinSubmitted -> {
                        Timber.d("ParentSettings: PIN submitted for verification")
                        coroutineScope.launch {
                            try {
                                val isValid = preferencesRepository.verifyParentPin(event.pin)
                                if (isValid) {
                                    Timber.i("ParentSettings: PIN verification successful")
                                    showPinVerification = false

                                    // Perform action based on verification mode
                                    when (pinVerificationMode) {
                                        ParentSettingsScreen.PinVerificationMode.CHANGE_GRADE_LIMIT -> {
                                            showGradeLimit = true
                                        }

                                        ParentSettingsScreen.PinVerificationMode.RESET_PIN -> {
                                            showPinReset = true
                                        }

                                        ParentSettingsScreen.PinVerificationMode.NONE -> {
                                            // Should not happen
                                            Timber.w("ParentSettings: PIN verified with no mode set")
                                        }
                                    }

                                    pinVerificationMode = ParentSettingsScreen.PinVerificationMode.NONE
                                } else {
                                    Timber.w("ParentSettings: PIN verification failed - incorrect PIN")
                                    // TODO: Show error message (implement in UI)
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "ParentSettings: Error during PIN verification")
                            }
                        }
                    }

                    is ParentSettingsScreen.Event.PinVerificationCancelled -> {
                        Timber.d("ParentSettings: PIN verification cancelled")
                        showPinVerification = false
                        pinVerificationMode = ParentSettingsScreen.PinVerificationMode.NONE
                    }

                    is ParentSettingsScreen.Event.NavigateBack -> {
                        Timber.d("ParentSettings: Navigate back")
                        navigator.pop()
                    }

                    is ParentSettingsScreen.Event.ManageGoalsClicked -> {
                        Timber.d("ParentSettings: Navigate to manage goals")
                        navigator.goTo(GoalCatalogScreen)
                    }
