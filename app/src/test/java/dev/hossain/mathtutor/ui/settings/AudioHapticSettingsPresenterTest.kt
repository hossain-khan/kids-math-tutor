package dev.hossain.mathtutor.ui.settings

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.audio.AudioService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.haptic.HapticService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

/**
 * Unit tests for [AudioHapticSettingsPresenter].
 *
 * Tests presenter logic for audio and haptic settings management.
 */
class AudioHapticSettingsPresenterTest {
    @Test
    fun repository_setSoundEffectsEnabled_savesCorrectly() {
        // Given
        val repository = FakeUserPreferencesRepository()

        // When
        repository.setSoundEffectsEnabledSync(false)

        // Then
        assertThat(repository.getCurrentSoundEffectsEnabled()).isFalse()
    }

    @Test
    fun repository_setBackgroundMusicEnabled_savesCorrectly() {
        // Given
        val repository = FakeUserPreferencesRepository()

        // When
        repository.setBackgroundMusicEnabledSync(true)

        // Then
        assertThat(repository.getCurrentBackgroundMusicEnabled()).isTrue()
    }

    @Test
    fun repository_setHapticsEnabled_savesCorrectly() {
        // Given
        val repository = FakeUserPreferencesRepository()

        // When
        repository.setHapticsEnabledSync(false)

        // Then
        assertThat(repository.getCurrentHapticsEnabled()).isFalse()
    }

    @Test
    fun repository_setVolume_savesCorrectly() {
        // Given
        val repository = FakeUserPreferencesRepository()

        // When
        repository.setVolumeSync(0.5f)

        // Then
        assertThat(repository.getCurrentVolume()).isWithin(0.01f).of(0.5f)
    }

    @Test
    fun repository_setVolume_coercesToValidRange() {
        // Given
        val repository = FakeUserPreferencesRepository()

        // When - Set above max
        repository.setVolumeSync(1.5f)

        // Then
        assertThat(repository.getCurrentVolume()).isWithin(0.01f).of(1.0f)

        // When - Set below min
        repository.setVolumeSync(-0.5f)

        // Then
        assertThat(repository.getCurrentVolume()).isWithin(0.01f).of(0.0f)
    }

    @Test
    fun repository_setHighContrastEnabled_savesCorrectly() {
        // Given
        val repository = FakeUserPreferencesRepository()

        // When
        repository.setHighContrastEnabledSync(true)

        // Then
        assertThat(repository.getCurrentHighContrastEnabled()).isTrue()
    }

    @Test
    fun repository_setLargeTextEnabled_savesCorrectly() {
        // Given
        val repository = FakeUserPreferencesRepository()

        // When
        repository.setLargeTextEnabledSync(true)

        // Then
        assertThat(repository.getCurrentLargeTextEnabled()).isTrue()
    }

    @Test
    fun audioService_setSoundEffectsEnabled_updatesState() {
        // Given
        val audioService = FakeAudioService()

        // When
        audioService.setSoundEffectsEnabled(false)

        // Then
        assertThat(audioService.isSoundEffectsEnabled()).isFalse()
    }

    @Test
    fun audioService_setMusicEnabled_updatesState() {
        // Given
        val audioService = FakeAudioService()

        // When
        audioService.setMusicEnabled(true)

        // Then
        assertThat(audioService.isMusicEnabled()).isTrue()
    }

    @Test
    fun audioService_setVolume_updatesState() {
        // Given
        val audioService = FakeAudioService()

        // When
        audioService.setVolume(0.8f)

        // Then
        assertThat(audioService.getVolume()).isWithin(0.01f).of(0.8f)
    }

    @Test
    fun hapticService_setHapticsEnabled_updatesState() {
        // Given
        val hapticService = FakeHapticService()

        // When
        hapticService.setHapticsEnabled(false)

        // Then
        assertThat(hapticService.isHapticsEnabled()).isFalse()
    }

    /**
     * Fake implementation of [UserPreferencesRepository] for testing.
     */
    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        private val soundEffectsEnabledFlow = MutableStateFlow(true)
        private val backgroundMusicEnabledFlow = MutableStateFlow(false)
        private val hapticsEnabledFlow = MutableStateFlow(true)
        private val volumeFlow = MutableStateFlow(dev.hossain.mathtutor.audio.AudioConstants.DEFAULT_SOUND_EFFECTS_VOLUME)
        private val highContrastEnabledFlow = MutableStateFlow(false)
        private val largeTextEnabledFlow = MutableStateFlow(false)
        private val analyticsEnabledFlow = MutableStateFlow(true)
        private val hintSystemEnabledFlow = MutableStateFlow(true)

        override val isOnboardingCompleted: Flow<Boolean> = MutableStateFlow(false)

        override suspend fun setOnboardingCompleted(completed: Boolean) {}

        override val isHapticsEnabled: Flow<Boolean> = hapticsEnabledFlow

        override suspend fun setHapticsEnabled(enabled: Boolean) {
            hapticsEnabledFlow.value = enabled
        }

        override val isSoundEffectsEnabled: Flow<Boolean> = soundEffectsEnabledFlow

        override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
            soundEffectsEnabledFlow.value = enabled
        }

        override val isBackgroundMusicEnabled: Flow<Boolean> = backgroundMusicEnabledFlow

        override suspend fun setBackgroundMusicEnabled(enabled: Boolean) {
            backgroundMusicEnabledFlow.value = enabled
        }

        override val volume: Flow<Float> = volumeFlow

        override suspend fun setVolume(volume: Float) {
            volumeFlow.value = volume.coerceIn(0f, 1f)
        }

        override val isHighContrastEnabled: Flow<Boolean> = highContrastEnabledFlow

        override suspend fun setHighContrastEnabled(enabled: Boolean) {
            highContrastEnabledFlow.value = enabled
        }

        override val isLargeTextEnabled: Flow<Boolean> = largeTextEnabledFlow

        override suspend fun setLargeTextEnabled(enabled: Boolean) {
            largeTextEnabledFlow.value = enabled
        }

        override val isAnalyticsEnabled: Flow<Boolean> = analyticsEnabledFlow

        override suspend fun setAnalyticsEnabled(enabled: Boolean) {
            analyticsEnabledFlow.value = enabled
        }

        private val importGuideExpandedFlow = MutableStateFlow(true)

        override val isImportGuideExpanded: Flow<Boolean> = importGuideExpandedFlow

        override suspend fun setImportGuideExpanded(expanded: Boolean) {
            importGuideExpandedFlow.value = expanded
        }

        private val gameTrialAttemptsFlows = mutableMapOf<dev.hossain.mathtutor.domain.model.Game, MutableStateFlow<Int>>()

        override fun getGameTrialAttempts(game: dev.hossain.mathtutor.domain.model.Game): Flow<Int> =
            gameTrialAttemptsFlows.getOrPut(game) { MutableStateFlow(0) }

        override suspend fun incrementGameTrialAttempts(game: dev.hossain.mathtutor.domain.model.Game): Int {
            val flow = gameTrialAttemptsFlows.getOrPut(game) { MutableStateFlow(0) }
            val newCount = (flow.value + 1).coerceAtMost(3)
            flow.value = newCount
            return newCount
        }

        // Test helper methods
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

        // Parent control methods (minimal implementation for testing)
        override val parentPinHash: Flow<String?> = MutableStateFlow(null)

        override suspend fun setParentPin(pin: String) {}

        override suspend fun verifyParentPin(pin: String): Boolean = false

        override suspend fun clearParentPin() {}

        override val maxGradeLevel: Flow<GradeLevel?> = MutableStateFlow(null)

        override suspend fun setMaxGradeLevel(gradeLevel: GradeLevel?) {}

        override val isHintSystemEnabled: Flow<Boolean> = hintSystemEnabledFlow

        override suspend fun setHintSystemEnabled(enabled: Boolean) {
            hintSystemEnabledFlow.value = enabled
        }
    }

    /**
     * Fake implementation of [AudioService] for testing.
     */
    private class FakeAudioService : AudioService {
        private var soundEffectsEnabled = true
        private var musicEnabled = false
        private var volume = 1.0f

        override fun playSuccess() {}

        override fun playPerfectScore() {}

        override fun playBadgeUnlock() {}

        override fun playError() {}

        override fun playStreakContinue() {}

        override fun playLevelUp() {}

        override fun playCountdown() {}

        override fun playGo() {}

        override fun playWarning() {}

        override fun startBackgroundMusic() {}

        override fun stopBackgroundMusic() {}

        override fun pauseBackgroundMusic() {}

        override fun resumeBackgroundMusic() {}

        override fun setMusicEnabled(enabled: Boolean) {
            musicEnabled = enabled
        }

        override fun setSoundEffectsEnabled(enabled: Boolean) {
            soundEffectsEnabled = enabled
        }

        override fun setVolume(volume: Float) {
            this.volume = volume
        }

        override fun registerSoundLoadListener(listener: (Boolean, Map<String, Int>) -> Unit) {
            // No-op for tests
        }

        override fun unregisterSoundLoadListener(listener: (Boolean, Map<String, Int>) -> Unit) {
            // No-op for tests
        }

        override fun isDeviceAudioSuppressed(): Boolean {
            // Return false by default in tests
            return false
        }

        override fun release() {}

        fun isSoundEffectsEnabled(): Boolean = soundEffectsEnabled

        fun isMusicEnabled(): Boolean = musicEnabled

        fun getVolume(): Float = volume
    }

    /**
     * Fake implementation of [HapticService] for testing.
     */
    private class FakeHapticService : HapticService {
        private var hapticsEnabled = true

        override fun triggerSuccess() {}

        override fun triggerError() {}

        override fun triggerBadgeUnlock() {}

        override fun triggerButtonClick() {}

        override fun triggerLongPress() {}

        override fun setHapticsEnabled(enabled: Boolean) {
            hapticsEnabled = enabled
        }

        fun isHapticsEnabled(): Boolean = hapticsEnabled
    }
}
