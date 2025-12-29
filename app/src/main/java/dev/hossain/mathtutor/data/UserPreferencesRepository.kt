package dev.hossain.mathtutor.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.hossain.mathtutor.audio.AudioConstants
import dev.hossain.mathtutor.data.local.userPreferencesDataStore
import dev.hossain.mathtutor.di.ApplicationContext
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.security.MessageDigest

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

    /**
     * Gets the number of trial attempts used for a specific game.
     * Trial attempts allow kids to try locked games up to 3 times before unlocking.
     *
     * @param game The game to get trial attempts for
     * @return Flow of trial attempts count (0-3)
     */
    fun getGameTrialAttempts(game: Game): Flow<Int>

    /**
     * Increments the trial attempt count for a specific game.
     *
     * @param game The game to increment trial attempts for
     * @return The new trial attempt count after incrementing
     */
    suspend fun incrementGameTrialAttempts(game: Game): Int

    /**
     * Gets the hashed parent PIN for verification.
     * Returns null if no PIN has been set.
     *
     * @return Flow of hashed PIN string or null
     */
    val parentPinHash: Flow<String?>

    /**
     * Sets the parent PIN for accessing protected features.
     * The PIN is stored as a SHA-256 hash for security.
     *
     * @param pin The 4-digit PIN to set
     */
    suspend fun setParentPin(pin: String)

    /**
     * Verifies if the provided PIN matches the stored PIN.
     *
     * @param pin The PIN to verify
     * @return true if PIN matches, false otherwise
     */
    suspend fun verifyParentPin(pin: String): Boolean

    /**
     * Clears the parent PIN (used for testing or reset scenarios).
     */
    suspend fun clearParentPin()

    /**
     * Gets the maximum grade level that the child can select.
     * Returns null if no limit has been set (unlimited).
     *
     * @return Flow of maximum GradeLevel or null
     */
    val maxGradeLevel: Flow<GradeLevel?>

    /**
     * Sets the maximum grade level that the child can select.
     * This prevents children from accessing problems that are too difficult.
     *
     * @param gradeLevel The maximum grade level to set, or null to remove limit
     */
    suspend fun setMaxGradeLevel(gradeLevel: GradeLevel?)
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

            // Game trial attempts keys (max 3 trials per game)
            fun gameTrialAttempts(game: Game) = intPreferencesKey("game_trial_${game.name}")

            // Parent controls keys
            val PARENT_PIN_HASH = stringPreferencesKey("parent_pin_hash")
            val MAX_GRADE_LEVEL = stringPreferencesKey("max_grade_level")
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
                preferences[PreferencesKeys.VOLUME] ?: AudioConstants.DEFAULT_SOUND_EFFECTS_VOLUME
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

        override fun getGameTrialAttempts(game: Game): Flow<Int> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[PreferencesKeys.gameTrialAttempts(game)] ?: 0
            }

        override suspend fun incrementGameTrialAttempts(game: Game): Int {
            var newCount = 0
            context.userPreferencesDataStore.edit { preferences ->
                val currentCount = preferences[PreferencesKeys.gameTrialAttempts(game)] ?: 0
                newCount = (currentCount + 1).coerceAtMost(3) // Max 3 trials
                preferences[PreferencesKeys.gameTrialAttempts(game)] = newCount
                Timber.d("UserPreferencesRepository: Incremented trial attempts for ${game.name} to $newCount")
            }
            return newCount
        }

        override val parentPinHash: Flow<String?> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[PreferencesKeys.PARENT_PIN_HASH]
            }

        override suspend fun setParentPin(pin: String) {
            require(pin.length == 4 && pin.all { it.isDigit() }) {
                "PIN must be exactly 4 digits"
            }
            val hashedPin = hashPin(pin)
            Timber.d("UserPreferencesRepository: Setting parent PIN (hashed)")
            context.userPreferencesDataStore.edit { preferences ->
                preferences[PreferencesKeys.PARENT_PIN_HASH] = hashedPin
            }
        }

        override suspend fun verifyParentPin(pin: String): Boolean {
            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                Timber.w("UserPreferencesRepository: Invalid PIN format")
                return false
            }

            val storedHash =
                context.userPreferencesDataStore.data
                    .map { it[PreferencesKeys.PARENT_PIN_HASH] }
                    .first()

            if (storedHash == null) {
                Timber.w("UserPreferencesRepository: No PIN set, verification failed")
                return false
            }

            val inputHash = hashPin(pin)
            val isValid = inputHash == storedHash
            Timber.d("UserPreferencesRepository: PIN verification result = $isValid")
            return isValid
        }

        override suspend fun clearParentPin() {
            Timber.d("UserPreferencesRepository: Clearing parent PIN")
            context.userPreferencesDataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.PARENT_PIN_HASH)
            }
        }

        override val maxGradeLevel: Flow<GradeLevel?> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[PreferencesKeys.MAX_GRADE_LEVEL]?.let { levelName ->
                    try {
                        GradeLevel.valueOf(levelName)
                    } catch (e: IllegalArgumentException) {
                        Timber.e(e, "Invalid grade level stored: $levelName")
                        null
                    }
                }
            }

        override suspend fun setMaxGradeLevel(gradeLevel: GradeLevel?) {
            Timber.d("UserPreferencesRepository: Setting max grade level = ${gradeLevel?.displayName}")
            context.userPreferencesDataStore.edit { preferences ->
                if (gradeLevel != null) {
                    preferences[PreferencesKeys.MAX_GRADE_LEVEL] = gradeLevel.name
                } else {
                    preferences.remove(PreferencesKeys.MAX_GRADE_LEVEL)
                }
            }
        }

        /**
         * Hashes a PIN using SHA-256 for secure storage.
         * We use a simple hash here as PINs are short (4 digits).
         * For production, consider using a stronger algorithm like PBKDF2 or bcrypt.
         *
         * @param pin The PIN to hash
         * @return The hex-encoded hash string
         */
        private fun hashPin(pin: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
