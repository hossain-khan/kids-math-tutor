package dev.hossain.mathtutor.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.di.ApplicationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Implementation of [HapticService] using Android's Vibrator/VibratorManager APIs.
 *
 * Supports different Android API levels:
 * - Android S+ (API 31): Uses VibratorManager for system-level vibrator access
 * - Pre-S: Uses deprecated Vibrator service for backwards compatibility
 *
 * Vibration patterns:
 * - Android Q+ (API 29): Uses predefined VibrationEffect constants for standard patterns
 * - Pre-Q: Uses custom timing patterns with vibrate() method
 *
 * The service respects user preferences and only triggers vibrations when haptics are enabled.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class HapticServiceImpl
    constructor(
        @param:ApplicationContext private val context: Context,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : HapticService {
        // Get vibrator based on Android version
        private val vibrator: Vibrator? by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android S+ (API 31): Use VibratorManager
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                // Pre-S: Use deprecated Vibrator service
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }

        // Coroutine scope for observing user preferences
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Track haptics enabled state (loaded from preferences)
        private var hapticsEnabled: Boolean = true

        init {
            // Observe haptics preference from repository
            scope.launch {
                try {
                    hapticsEnabled = userPreferencesRepository.isHapticsEnabled.first()
                    Timber.d("[HapticService] Initial haptics enabled state: $hapticsEnabled")
                } catch (e: Exception) {
                    Timber.e(e, "[HapticService] Error loading haptics preference, defaulting to enabled")
                    hapticsEnabled = true
                }
            }
        }

        /**
         * Trigger vibration if haptics are enabled and vibrator is available.
         */
        private fun vibrate(effect: VibrationEffect) {
            if (!hapticsEnabled) {
                Timber.d("[HapticService] Haptics disabled, skipping vibration")
                return
            }

            vibrator?.let {
                if (it.hasVibrator()) {
                    it.vibrate(effect)
                    Timber.d("[HapticService] Vibration triggered")
                } else {
                    Timber.d("[HapticService] Device does not support vibration")
                }
            } ?: run {
                Timber.w("[HapticService] Vibrator not available")
            }
        }

        /**
         * Trigger vibration with legacy pattern (pre-Q).
         */
        @Suppress("DEPRECATION")
        private fun vibrateLegacy(milliseconds: Long) {
            if (!hapticsEnabled) {
                Timber.d("[HapticService] Haptics disabled, skipping vibration")
                return
            }

            vibrator?.let {
                if (it.hasVibrator()) {
                    it.vibrate(milliseconds)
                    Timber.d("[HapticService] Legacy vibration triggered (${milliseconds}ms)")
                } else {
                    Timber.d("[HapticService] Device does not support vibration")
                }
            } ?: run {
                Timber.w("[HapticService] Vibrator not available")
            }
        }

        /**
         * Trigger vibration with legacy pattern array (pre-Q).
         */
        @Suppress("DEPRECATION")
        private fun vibrateLegacyPattern(pattern: LongArray) {
            if (!hapticsEnabled) {
                Timber.d("[HapticService] Haptics disabled, skipping vibration")
                return
            }

            vibrator?.let {
                if (it.hasVibrator()) {
                    it.vibrate(pattern, -1)
                    Timber.d("[HapticService] Legacy pattern vibration triggered")
                } else {
                    Timber.d("[HapticService] Device does not support vibration")
                }
            } ?: run {
                Timber.w("[HapticService] Vibrator not available")
            }
        }

        // ==================== Haptic Feedback Methods ====================

        override fun triggerSuccess() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android Q+: Use EFFECT_CLICK for short, pleasant feedback
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                vibrate(effect)
            } else {
                // Pre-Q: Single 50ms vibration
                vibrateLegacy(50)
            }
        }

        override fun triggerError() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android Q+: Use EFFECT_DOUBLE_CLICK for distinct feedback
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                vibrate(effect)
            } else {
                // Pre-Q: Pattern [delay, vibrate, delay, vibrate]
                // Pattern: 0ms delay, 50ms vibrate, 100ms delay, 50ms vibrate
                val pattern = longArrayOf(0, 50, 100, 50)
                vibrateLegacyPattern(pattern)
            }
        }

        override fun triggerBadgeUnlock() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android O+: Waveform with increasing amplitudes (crescendo)
                // Pattern: 0ms delay, 50ms@128, 50ms pause, 100ms@192, 50ms pause, 150ms@255
                val timings = longArrayOf(0, 50, 50, 100, 50, 150)
                val amplitudes = intArrayOf(0, 128, 0, 192, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrate(effect)
            } else {
                // Pre-O: Pattern without amplitude control
                // Pattern: 0ms delay, 50ms, 50ms pause, 100ms, 50ms pause, 150ms
                val pattern = longArrayOf(0, 50, 50, 100, 50, 150)
                vibrateLegacyPattern(pattern)
            }
        }

        override fun triggerButtonClick() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android Q+: Use EFFECT_TICK for very subtle feedback
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                vibrate(effect)
            } else {
                // Pre-Q: Single 10ms vibration
                vibrateLegacy(10)
            }
        }

        override fun triggerLongPress() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android Q+: Use EFFECT_HEAVY_CLICK for firm feedback
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                vibrate(effect)
            } else {
                // Pre-Q: Single 100ms vibration
                vibrateLegacy(100)
            }
        }

        // ==================== Settings ====================

        override fun setHapticsEnabled(enabled: Boolean) {
            hapticsEnabled = enabled
            Timber.d("[HapticService] Haptics enabled: $enabled")

            // Persist to user preferences
            scope.launch {
                try {
                    userPreferencesRepository.setHapticsEnabled(enabled)
                    Timber.d("[HapticService] Haptics preference saved")
                } catch (e: Exception) {
                    Timber.e(e, "[HapticService] Error saving haptics preference")
                }
            }
        }

        /**
         * Clean up resources when the service is no longer needed.
         * Call this when the app is being destroyed.
         */
        fun release() {
            scope.cancel()
            Timber.d("[HapticService] Released")
        }
    }
