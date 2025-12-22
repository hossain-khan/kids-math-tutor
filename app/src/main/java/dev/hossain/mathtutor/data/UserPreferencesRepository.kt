package dev.hossain.mathtutor.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import dev.hossain.mathtutor.data.local.userPreferencesDataStore
import dev.hossain.mathtutor.di.ApplicationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

interface UserPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)

    val isHapticsEnabled: Flow<Boolean>

    suspend fun setHapticsEnabled(enabled: Boolean)

    val isSoundEffectsEnabled: Flow<Boolean>

    suspend fun setSoundEffectsEnabled(enabled: Boolean)

    val isBackgroundMusicEnabled: Flow<Boolean>

    suspend fun setBackgroundMusicEnabled(enabled: Boolean)

    val volume: Flow<Float>

    suspend fun setVolume(volume: Float)

    val isHighContrastEnabled: Flow<Boolean>

    suspend fun setHighContrastEnabled(enabled: Boolean)

    val isLargeTextEnabled: Flow<Boolean>

    suspend fun setLargeTextEnabled(enabled: Boolean)

    val isAnalyticsEnabled: Flow<Boolean>

    suspend fun setAnalyticsEnabled(enabled: Boolean)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class UserPreferencesRepositoryImpl
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : UserPreferencesRepository {
        private object PreferencesKeys {
            val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
            val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
            val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
            val BACKGROUND_MUSIC_ENABLED = booleanPreferencesKey("background_music_enabled")
            val VOLUME = floatPreferencesKey("volume")
            val HIGH_CONTRAST_ENABLED = booleanPreferencesKey("high_contrast_enabled")
            val LARGE_TEXT_ENABLED = booleanPreferencesKey("large_text_enabled")
            val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        }

        override val isOnboardingCompleted: Flow<Boolean> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
            }

        override suspend fun setOnboardingCompleted(completed: Boolean) {
            Timber.d("UserPreferencesRepository: Setting onboarding completed = $completed")
            context.userPreferencesDataStore.edit { preferences ->
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
            }
        }

        override val isHapticsEnabled: Flow<Boolean> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true
            }

        override suspend fun setHapticsEnabled(enabled: Boolean) {
            Timber.d("UserPreferencesRepository: Setting haptics enabled = $enabled")
            context.userPreferencesDataStore.edit { preferences ->
                preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
            }
        }

        override val isSoundEffectsEnabled: Flow<Boolean> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED] ?: true
            }

        override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
            Timber.d("UserPreferencesRepository: Setting sound effects enabled = $enabled")
            context.userPreferencesDataStore.edit { preferences ->
                preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED] = enabled
            }
        }

        override val isBackgroundMusicEnabled: Flow<Boolean> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[PreferencesKeys.BACKGROUND_MUSIC_ENABLED] ?: false
            }

        override suspend fun setBackgroundMusicEnabled(enabled: Boolean) {
            Timber.d("UserPreferencesRepository: Setting background music enabled = $enabled")
            context.userPreferencesDataStore.edit { preferences ->
                preferences[PreferencesKeys.BACKGROUND_MUSIC_ENABLED] = enabled
            }
        }

        override val volume: Flow<Float> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[PreferencesKeys.VOLUME] ?: 0.5f
            }

        override suspend fun setVolume(volume: Float) {
            Timber.d("UserPreferencesRepository: Setting volume = $volume")
            context.userPreferencesDataStore.edit { preferences ->
                preferences[PreferencesKeys.VOLUME] = volume.coerceIn(0f, 1f)
            }
        }

        override val isHighContrastEnabled: Flow<Boolean> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[PreferencesKeys.HIGH_CONTRAST_ENABLED] ?: false
            }

        override suspend fun setHighContrastEnabled(enabled: Boolean) {
            Timber.d("UserPreferencesRepository: Setting high contrast enabled = $enabled")
            context.userPreferencesDataStore.edit { preferences ->
                preferences[PreferencesKeys.HIGH_CONTRAST_ENABLED] = enabled
            }
        }

        override val isLargeTextEnabled: Flow<Boolean> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[PreferencesKeys.LARGE_TEXT_ENABLED] ?: false
            }

        override suspend fun setLargeTextEnabled(enabled: Boolean) {
            Timber.d("UserPreferencesRepository: Setting large text enabled = $enabled")
            context.userPreferencesDataStore.edit { preferences ->
                preferences[PreferencesKeys.LARGE_TEXT_ENABLED] = enabled
            }
        }

        override val isAnalyticsEnabled: Flow<Boolean> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[PreferencesKeys.ANALYTICS_ENABLED] ?: true
            }

        override suspend fun setAnalyticsEnabled(enabled: Boolean) {
            Timber.d("UserPreferencesRepository: Setting analytics enabled = $enabled")
            context.userPreferencesDataStore.edit { preferences ->
                preferences[PreferencesKeys.ANALYTICS_ENABLED] = enabled
            }
        }
    }
