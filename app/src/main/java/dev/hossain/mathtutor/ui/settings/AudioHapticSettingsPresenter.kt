package dev.hossain.mathtutor.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsParam
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.audio.AudioService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.haptic.HapticService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Presenter for [AudioHapticSettingsScreen].
 *
 * Manages the state and business logic for audio and haptic feedback settings.
 * Handles preference loading, audio/haptic service integration, and settings persistence.
 */
@AssistedInject
class AudioHapticSettingsPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val audioService: AudioService,
        private val hapticService: HapticService,
        private val analyticsService: AnalyticsService,
    ) : Presenter<AudioHapticSettingsScreen.State> {
        @CircuitInject(AudioHapticSettingsScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(navigator: Navigator): AudioHapticSettingsPresenter
        }

        @Composable
        override fun present(): AudioHapticSettingsScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Audio & Haptic Settings",
                    screenClass = AudioHapticSettingsScreen::class.java.name,
                )
            }

            val scope = rememberCoroutineScope()

            // Collect all preferences
            val soundEffectsEnabled by userPreferencesRepository.isSoundEffectsEnabled.collectAsState(initial = true)
            val backgroundMusicEnabled by userPreferencesRepository.isBackgroundMusicEnabled.collectAsState(initial = false)
            val hapticsEnabled by userPreferencesRepository.isHapticsEnabled.collectAsState(initial = true)
            val volume by userPreferencesRepository.volume.collectAsState(initial = 0.7f)
            val highContrastEnabled by userPreferencesRepository.isHighContrastEnabled.collectAsState(initial = false)
            val largeTextEnabled by userPreferencesRepository.isLargeTextEnabled.collectAsState(initial = false)

            // Log state changes in LaunchedEffect to avoid recomposition spam
            LaunchedEffect(soundEffectsEnabled, backgroundMusicEnabled, hapticsEnabled, volume, highContrastEnabled, largeTextEnabled) {
                Timber.d(
                    "[AudioHapticSettings] State - soundEffects=$soundEffectsEnabled, music=$backgroundMusicEnabled, " +
                        "haptics=$hapticsEnabled, volume=$volume, highContrast=$highContrastEnabled, largeText=$largeTextEnabled",
                )
            }

            return AudioHapticSettingsScreen.State(
                soundEffectsEnabled = soundEffectsEnabled,
                backgroundMusicEnabled = backgroundMusicEnabled,
                hapticsEnabled = hapticsEnabled,
                volume = volume,
                highContrastEnabled = highContrastEnabled,
                largeTextEnabled = largeTextEnabled,
            ) { event ->
                when (event) {
                    is AudioHapticSettingsScreen.Event.ToggleSoundEffects -> {
                        Timber.d("[AudioHapticSettings] Toggle sound effects - enabled=${event.enabled}")
                        analyticsService.logEvent(
                            eventName = AnalyticsEvent.AUDIO_TOGGLED,
                            parameters =
                                mapOf(
                                    AnalyticsParam.SETTING_NAME to "sound_effects",
                                    AnalyticsParam.SETTING_VALUE to event.enabled.toString(),
                                ),
                        )
                        audioService.setSoundEffectsEnabled(event.enabled)
                        scope.launch {
                            userPreferencesRepository.setSoundEffectsEnabled(event.enabled)
                        }
                    }

                    is AudioHapticSettingsScreen.Event.ToggleBackgroundMusic -> {
                        Timber.d("[AudioHapticSettings] Toggle background music - enabled=${event.enabled}")
                        analyticsService.logEvent(
                            eventName = AnalyticsEvent.AUDIO_TOGGLED,
                            parameters =
                                mapOf(
                                    AnalyticsParam.SETTING_NAME to "background_music",
                                    AnalyticsParam.SETTING_VALUE to event.enabled.toString(),
                                ),
                        )
                        audioService.setMusicEnabled(event.enabled)
                        if (event.enabled) {
                            audioService.startBackgroundMusic()
                        } else {
                            audioService.stopBackgroundMusic()
                        }
                        scope.launch {
                            userPreferencesRepository.setBackgroundMusicEnabled(event.enabled)
                        }
                    }

                    is AudioHapticSettingsScreen.Event.ToggleHaptics -> {
                        Timber.d("[AudioHapticSettings] Toggle haptics - enabled=${event.enabled}")
                        analyticsService.logEvent(
                            eventName = AnalyticsEvent.HAPTICS_TOGGLED,
                            parameters =
                                mapOf(
                                    AnalyticsParam.SETTING_NAME to "haptics",
                                    AnalyticsParam.SETTING_VALUE to event.enabled.toString(),
                                ),
                        )
                        hapticService.setHapticsEnabled(event.enabled)
                        scope.launch {
                            userPreferencesRepository.setHapticsEnabled(event.enabled)
                        }
                    }

                    is AudioHapticSettingsScreen.Event.SetVolume -> {
                        Timber.d("[AudioHapticSettings] Set volume - volume=${event.volume}")
                        audioService.setVolume(event.volume)
                        scope.launch {
                            userPreferencesRepository.setVolume(event.volume)
                        }
                    }

                    is AudioHapticSettingsScreen.Event.ToggleHighContrast -> {
                        Timber.d("[AudioHapticSettings] Toggle high contrast - enabled=${event.enabled}")
                        scope.launch {
                            userPreferencesRepository.setHighContrastEnabled(event.enabled)
                        }
                    }

                    is AudioHapticSettingsScreen.Event.ToggleLargeText -> {
                        Timber.d("[AudioHapticSettings] Toggle large text - enabled=${event.enabled}")
                        scope.launch {
                            userPreferencesRepository.setLargeTextEnabled(event.enabled)
                        }
                    }

                    is AudioHapticSettingsScreen.Event.BackClicked -> {
                        Timber.d("[AudioHapticSettings] Back clicked")
                        navigator.pop()
                    }
                }
            }
        }
    }
