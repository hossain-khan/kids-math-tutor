package dev.hossain.mathtutor.fakes

import dev.hossain.mathtutor.audio.AudioConstants
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GradeLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake implementation of [UserPreferencesRepository] for testing.
 *
 * Provides in-memory storage for all user preferences with Flow support.
 * This shared fake is used across multiple test files to avoid duplication
 * of test infrastructure.
 */
class FakeUserPreferencesRepository : UserPreferencesRepository {
    private val analyticsEnabledFlow = MutableStateFlow(true)
    private val onboardingCompletedFlow = MutableStateFlow(false)
    private val hapticsEnabledFlow = MutableStateFlow(true)
    private val soundEffectsEnabledFlow = MutableStateFlow(true)
    private val backgroundMusicEnabledFlow = MutableStateFlow(false)
    private val volumeFlow = MutableStateFlow(AudioConstants.DEFAULT_SOUND_EFFECTS_VOLUME)
    private val highContrastEnabledFlow = MutableStateFlow(false)
    private val largeTextEnabledFlow = MutableStateFlow(false)
    private val hintSystemEnabledFlow = MutableStateFlow(true)
    private val importGuideExpandedFlow = MutableStateFlow(true)
    private val parentPinHashFlow = MutableStateFlow<String?>(null)
    private val maxGradeLevelFlow = MutableStateFlow<GradeLevel?>(null)
    private val gameTrialAttemptsFlows = mutableMapOf<Game, MutableStateFlow<Int>>()

    // Analytics
    override val isAnalyticsEnabled: Flow<Boolean> = analyticsEnabledFlow

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        analyticsEnabledFlow.value = enabled
    }

    // Onboarding
    override val isOnboardingCompleted: Flow<Boolean> = onboardingCompletedFlow

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        onboardingCompletedFlow.value = completed
    }

    // Haptics
    override val isHapticsEnabled: Flow<Boolean> = hapticsEnabledFlow

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        hapticsEnabledFlow.value = enabled
    }

    // Sound Effects
    override val isSoundEffectsEnabled: Flow<Boolean> = soundEffectsEnabledFlow

    override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        soundEffectsEnabledFlow.value = enabled
    }

    // Background Music
    override val isBackgroundMusicEnabled: Flow<Boolean> = backgroundMusicEnabledFlow

    override suspend fun setBackgroundMusicEnabled(enabled: Boolean) {
        backgroundMusicEnabledFlow.value = enabled
    }

    // Volume
    override val volume: Flow<Float> = volumeFlow

    override suspend fun setVolume(volume: Float) {
        volumeFlow.value = volume
    }

    // High Contrast
    override val isHighContrastEnabled: Flow<Boolean> = highContrastEnabledFlow

    override suspend fun setHighContrastEnabled(enabled: Boolean) {
        highContrastEnabledFlow.value = enabled
    }

    // Large Text
    override val isLargeTextEnabled: Flow<Boolean> = largeTextEnabledFlow

    override suspend fun setLargeTextEnabled(enabled: Boolean) {
        largeTextEnabledFlow.value = enabled
    }

    // Import Guide
    override val isImportGuideExpanded: Flow<Boolean> = importGuideExpandedFlow

    override suspend fun setImportGuideExpanded(expanded: Boolean) {
        importGuideExpandedFlow.value = expanded
    }

    // Parent Control - PIN
    override val parentPinHash: Flow<String?> = parentPinHashFlow

    override suspend fun setParentPin(pin: String) {
        parentPinHashFlow.value = "hashed_$pin"
    }

    override suspend fun verifyParentPin(pin: String): Boolean = false

    override suspend fun clearParentPin() {
        parentPinHashFlow.value = null
    }

    // Parent Control - Grade Level
    override val maxGradeLevel: Flow<GradeLevel?> = maxGradeLevelFlow

    override suspend fun setMaxGradeLevel(gradeLevel: GradeLevel?) {
        maxGradeLevelFlow.value = gradeLevel
    }

    // Hint System
    override val isHintSystemEnabled: Flow<Boolean> = hintSystemEnabledFlow

    override suspend fun setHintSystemEnabled(enabled: Boolean) {
        hintSystemEnabledFlow.value = enabled
    }

    // Game Trial Attempts
    override fun getGameTrialAttempts(game: Game): Flow<Int> = gameTrialAttemptsFlows.getOrPut(game) { MutableStateFlow(0) }

    override suspend fun incrementGameTrialAttempts(game: Game): Int {
        val flow = gameTrialAttemptsFlows.getOrPut(game) { MutableStateFlow(0) }
        val newCount = (flow.value + 1).coerceAtMost(3)
        flow.value = newCount
        return newCount
    }

    // Test helper methods for synchronous access
    fun getAnalyticsEnabled(): Boolean = analyticsEnabledFlow.value

    fun getOnboardingCompleted(): Boolean = onboardingCompletedFlow.value

    fun getHapticsEnabled(): Boolean = hapticsEnabledFlow.value

    fun getSoundEffectsEnabled(): Boolean = soundEffectsEnabledFlow.value

    fun getVolume(): Float = volumeFlow.value

    fun setMaxGradeLevelSync(gradeLevel: GradeLevel?) {
        maxGradeLevelFlow.value = gradeLevel
    }

    fun setSoundEffectsEnabledSync(enabled: Boolean) {
        soundEffectsEnabledFlow.value = enabled
    }

    fun getCurrentSoundEffectsEnabled(): Boolean = soundEffectsEnabledFlow.value

    fun setBackgroundMusicEnabledSync(enabled: Boolean) {
        backgroundMusicEnabledFlow.value = enabled
    }

    fun getCurrentBackgroundMusicEnabled(): Boolean = backgroundMusicEnabledFlow.value

    fun setHapticsEnabledSync(enabled: Boolean) {
        hapticsEnabledFlow.value = enabled
    }

    fun getCurrentHapticsEnabled(): Boolean = hapticsEnabledFlow.value

    fun setVolumeSync(volume: Float) {
        volumeFlow.value = volume.coerceIn(0f, 1f)
    }

    fun getCurrentVolume(): Float = volumeFlow.value

    fun setHighContrastEnabledSync(enabled: Boolean) {
        highContrastEnabledFlow.value = enabled
    }

    fun getCurrentHighContrastEnabled(): Boolean = highContrastEnabledFlow.value

    fun setLargeTextEnabledSync(enabled: Boolean) {
        largeTextEnabledFlow.value = enabled
    }

    fun getCurrentLargeTextEnabled(): Boolean = largeTextEnabledFlow.value
}
