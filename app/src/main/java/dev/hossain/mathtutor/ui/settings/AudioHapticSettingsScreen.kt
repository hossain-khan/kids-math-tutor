package dev.hossain.mathtutor.ui.settings

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

/**
 * Circuit screen for audio and haptic feedback settings.
 *
 * Allows users to:
 * - Enable/disable sound effects
 * - Enable/disable background music
 * - Adjust audio volume
 * - Enable/disable haptic feedback
 * - Enable/disable high contrast mode
 * - Enable/disable large text mode
 */
@Parcelize
data object AudioHapticSettingsScreen : Screen {
    /**
     * State for [AudioHapticSettingsScreen].
     *
     * @property soundEffectsEnabled Whether sound effects are enabled
     * @property backgroundMusicEnabled Whether background music is enabled
     * @property hapticsEnabled Whether haptic feedback is enabled
     * @property volume Current volume level (0.0 to 1.0)
     * @property highContrastEnabled Whether high contrast mode is enabled
     * @property largeTextEnabled Whether large text mode is enabled
     * @property isDeviceAudioSuppressed Whether device is in silent or vibrate mode
     * @property eventSink Handler for screen events
     */
    data class State(
        val soundEffectsEnabled: Boolean,
        val backgroundMusicEnabled: Boolean,
        val hapticsEnabled: Boolean,
        val volume: Float,
        val highContrastEnabled: Boolean,
        val largeTextEnabled: Boolean,
        val isDeviceAudioSuppressed: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events for [AudioHapticSettingsScreen].
     */
    sealed interface Event : CircuitUiEvent {
        /**
         * User toggled sound effects switch.
         */
        data class ToggleSoundEffects(
            val enabled: Boolean,
        ) : Event

        /**
         * User toggled background music switch.
         */
        data class ToggleBackgroundMusic(
            val enabled: Boolean,
        ) : Event

        /**
         * User toggled haptic feedback switch.
         */
        data class ToggleHaptics(
            val enabled: Boolean,
        ) : Event

        /**
         * User adjusted the volume slider.
         */
        data class SetVolume(
            val volume: Float,
        ) : Event

        /**
         * User toggled high contrast mode.
         */
        data class ToggleHighContrast(
            val enabled: Boolean,
        ) : Event

        /**
         * User toggled large text mode.
         */
        data class ToggleLargeText(
            val enabled: Boolean,
        ) : Event

        /**
         * User tapped the back button.
         */
        data object BackClicked : Event
    }
}
