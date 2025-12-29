package dev.hossain.mathtutor.ui.parentsettings

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.mathtutor.domain.model.GradeLevel
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for parent-specific settings and controls.
 *
 * This screen provides parent-only features that are separate from the main app settings:
 * - **PIN Protection**: Set a 4-digit PIN to lock sensitive parent settings
 * - **Grade Limit**: Restrict the maximum grade level children can select
 *
 * ## Rationale
 * These settings are separated from main app settings because:
 * 1. They require parent authentication (PIN) to prevent children from modifying them
 * 2. They control what children can access in the app, not how the app behaves
 * 3. They implement parental controls for educational content management
 *
 * @see dev.hossain.mathtutor.ui.settings.SettingsScreen for child-accessible app settings
 */
@Parcelize
data object ParentSettingsScreen : Screen {
    /**
     * State for [ParentSettingsScreen].
     *
     * @property hasPinSet Whether a parent PIN has been configured
     * @property maxGradeLevel The maximum grade level children can select (null = unlimited)
     * @property showPinSetup Whether to show the PIN setup dialog
     * @property showPinVerification Whether to show PIN verification dialog
     * @property showPinReset Whether to show PIN reset dialog (requires old PIN)
     * @property showForgotPin Whether to show the forgot PIN recovery dialog (math challenge)
     * @property showGradeLimit Whether to show the grade limit configuration dialog
     * @property showResetForgotOptions Whether to show animated Reset/Forgot PIN options
     * @property pinVerificationMode What action to perform after PIN verification succeeds
     * @property eventSink Handler for screen events
     */
    data class State(
        val hasPinSet: Boolean,
        val maxGradeLevel: GradeLevel?,
        val showPinSetup: Boolean,
        val showPinVerification: Boolean,
        val showPinReset: Boolean,
        val showForgotPin: Boolean,
        val showGradeLimit: Boolean,
        val showResetForgotOptions: Boolean,
        val pinVerificationMode: PinVerificationMode,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Defines what action should be performed after successful PIN verification.
     */
    enum class PinVerificationMode {
        /** No verification in progress */
        NONE,

        /** Verifying to change grade limit settings */
        CHANGE_GRADE_LIMIT,

        /** Verifying old PIN before allowing reset to new PIN */
        RESET_PIN,
    }

    /**
     * Events for [ParentSettingsScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User requested to set up a new PIN (first time).
         */
        data object SetupPinClicked : Event

        /**
         * User completed PIN setup with a new 4-digit PIN.
         *
         * @property pin The 4-digit PIN
         * @property confirmPin The confirmation PIN (must match pin)
         */
        data class PinSetupCompleted(
            val pin: String,
            val confirmPin: String,
        ) : Event

        /**
         * User cancelled PIN setup dialog.
         */
        data object PinSetupCancelled : Event

        /**
         * User toggled the info icon to show/hide Reset and Forgot PIN options.
         */
        data object ToggleResetForgotOptions : Event

        /**
         * User requested to reset their PIN (requires old PIN verification).
         */
        data object ResetPinClicked : Event

        /**
         * User completed PIN reset after verifying old PIN.
         *
         * @property newPin The new 4-digit PIN
         * @property confirmNewPin The confirmation of new PIN (must match newPin)
         */
        data class PinResetCompleted(
            val newPin: String,
            val confirmNewPin: String,
        ) : Event

        /**
         * User cancelled PIN reset dialog.
         */
        data object PinResetCancelled : Event

        /**
         * User clicked "Forgot PIN" to solve a math challenge.
         */
        data object ForgotPinClicked : Event

        /**
         * User completed the forgot PIN math challenge.
         *
         * @property answer User's answer to the math challenge
         * @property correctAnswer The correct answer to verify against
         */
        data class ForgotPinChallengeCompleted(
            val answer: String,
            val correctAnswer: Int,
        ) : Event

        /**
         * User cancelled forgot PIN challenge.
         */
        data object ForgotPinChallengeCancelled : Event

        /**
         * User requested to change the grade limit setting.
         * Requires PIN verification if PIN is set.
         */
        data object ChangeGradeLimitClicked : Event

        /**
         * User completed grade limit selection.
         *
         * @property gradeLevel The new maximum grade level (null = no limit)
         */
        data class GradeLimitChanged(
            val gradeLevel: GradeLevel?,
        ) : Event

        /**
         * User cancelled grade limit dialog.
         */
        data object GradeLimitCancelled : Event

        /**
         * User submitted PIN for verification.
         *
         * @property pin The PIN to verify
         */
        data class PinSubmitted(
            val pin: String,
        ) : Event

        /**
         * User cancelled PIN verification dialog.
         */
        data object PinVerificationCancelled : Event

        /**
         * User tapped the back button.
         */
        data object NavigateBack : Event
    }
}
