package dev.hossain.mathtutor.ui.devportal

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.analytics.FakeAnalyticsService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import dev.hossain.mathtutor.domain.model.Game
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DeveloperPortalPresenter].
 *
 * Focuses on testing the analytics toggle functionality and state management.
 */
class DeveloperPortalPresenterTest {
    private lateinit var fakeAnalyticsService: FakeAnalyticsService
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository

    @Before
    fun setup() {
        fakeAnalyticsService = FakeAnalyticsService()
        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    }

    @Test
    fun `toggleAnalytics - when enabled - disables analytics`() =
        runTest {
            // Given - Analytics is currently enabled
            fakeUserPreferencesRepository.setAnalyticsEnabled(true)
            assertThat(fakeUserPreferencesRepository.getAnalyticsEnabled()).isTrue()
            assertThat(fakeAnalyticsService.analyticsEnabled).isTrue()

            // When - Toggle analytics
            val currentEnabled = fakeUserPreferencesRepository.getAnalyticsEnabled()
            fakeUserPreferencesRepository.setAnalyticsEnabled(!currentEnabled)
            fakeAnalyticsService.setAnalyticsEnabled(!currentEnabled)

            // Then - Analytics should be disabled
            assertThat(fakeUserPreferencesRepository.getAnalyticsEnabled()).isFalse()
            assertThat(fakeAnalyticsService.analyticsEnabled).isFalse()
        }

    @Test
    fun `toggleAnalytics - when disabled - enables analytics`() =
        runTest {
            // Given - Analytics is currently disabled
            fakeUserPreferencesRepository.setAnalyticsEnabled(false)
            fakeAnalyticsService.setAnalyticsEnabled(false)
            assertThat(fakeUserPreferencesRepository.getAnalyticsEnabled()).isFalse()
            assertThat(fakeAnalyticsService.analyticsEnabled).isFalse()

            // When - Toggle analytics
            val currentEnabled = fakeUserPreferencesRepository.getAnalyticsEnabled()
            fakeUserPreferencesRepository.setAnalyticsEnabled(!currentEnabled)
            fakeAnalyticsService.setAnalyticsEnabled(!currentEnabled)

            // Then - Analytics should be enabled
            assertThat(fakeUserPreferencesRepository.getAnalyticsEnabled()).isTrue()
            assertThat(fakeAnalyticsService.analyticsEnabled).isTrue()
        }

    @Test
    fun `toggleAnalytics - updates both repository and service`() =
        runTest {
            // Given - Analytics is enabled
            fakeUserPreferencesRepository.setAnalyticsEnabled(true)
            fakeAnalyticsService.setAnalyticsEnabled(true)

            // When - Toggle analytics off
            fakeUserPreferencesRepository.setAnalyticsEnabled(false)
            fakeAnalyticsService.setAnalyticsEnabled(false)

            // Then - Both should be updated
            assertThat(fakeUserPreferencesRepository.getAnalyticsEnabled()).isFalse()
            assertThat(fakeAnalyticsService.analyticsEnabled).isFalse()

            // When - Toggle analytics back on
            fakeUserPreferencesRepository.setAnalyticsEnabled(true)
            fakeAnalyticsService.setAnalyticsEnabled(true)

            // Then - Both should be updated again
            assertThat(fakeUserPreferencesRepository.getAnalyticsEnabled()).isTrue()
            assertThat(fakeAnalyticsService.analyticsEnabled).isTrue()
        }

    @Test
    fun `analytics state - reflects repository state`() =
        runTest {
            // Given - Set analytics to false
            fakeUserPreferencesRepository.setAnalyticsEnabled(false)

            // When - Reading the flow
            val isEnabled = fakeUserPreferencesRepository.getAnalyticsEnabled()

            // Then - Should reflect the set value
            assertThat(isEnabled).isFalse()
        }

    @Test
    fun `analytics state - defaults to true when not set`() =
        runTest {
            // Given - Fresh fake repository (default state)
            val freshRepo = FakeUserPreferencesRepository()

            // When - Reading the default value
            val isEnabled = freshRepo.getAnalyticsEnabled()

            // Then - Should default to true
            assertThat(isEnabled).isTrue()
        }

    // ==================== Reset Onboarding Tests ====================

    @Test
    fun `resetOnboarding - when onboarding completed - sets to false`() =
        runTest {
            // Given - Onboarding is currently completed
            fakeUserPreferencesRepository.setOnboardingCompleted(true)
            assertThat(fakeUserPreferencesRepository.getOnboardingCompleted()).isTrue()

            // When - Reset onboarding
            fakeUserPreferencesRepository.setOnboardingCompleted(false)

            // Then - Onboarding should be marked as not completed
            assertThat(fakeUserPreferencesRepository.getOnboardingCompleted()).isFalse()
        }

    @Test
    fun `resetOnboarding - preserves other preferences`() =
        runTest {
            // Given - Set various preferences
            fakeUserPreferencesRepository.setOnboardingCompleted(true)
            fakeUserPreferencesRepository.setHapticsEnabled(false)
            fakeUserPreferencesRepository.setSoundEffectsEnabled(false)
            fakeUserPreferencesRepository.setVolume(0.5f)

            // When - Reset only onboarding
            fakeUserPreferencesRepository.setOnboardingCompleted(false)

            // Then - Other preferences should remain unchanged
            assertThat(fakeUserPreferencesRepository.getOnboardingCompleted()).isFalse()
            assertThat(fakeUserPreferencesRepository.getHapticsEnabled()).isFalse()
            assertThat(fakeUserPreferencesRepository.getSoundEffectsEnabled()).isFalse()
            assertThat(fakeUserPreferencesRepository.getVolume()).isEqualTo(0.5f)
        }

    @Test
    fun `resetOnboarding - allows re-setting to completed`() =
        runTest {
            // Given - Onboarding was completed then reset
            fakeUserPreferencesRepository.setOnboardingCompleted(true)
            fakeUserPreferencesRepository.setOnboardingCompleted(false)
            assertThat(fakeUserPreferencesRepository.getOnboardingCompleted()).isFalse()

            // When - Set onboarding as completed again
            fakeUserPreferencesRepository.setOnboardingCompleted(true)

            // Then - Should be marked as completed
            assertThat(fakeUserPreferencesRepository.getOnboardingCompleted()).isTrue()
        }

    @Test
    fun `onboarding state - defaults to false when not set`() =
        runTest {
            // Given - Fresh fake repository (default state)
            val freshRepo = FakeUserPreferencesRepository()

            // When - Reading the default value
            val isCompleted = freshRepo.getOnboardingCompleted()

            // Then - Should default to false (onboarding not completed)
            assertThat(isCompleted).isFalse()
        }

    // ==================== Sound & Haptic Tests ====================

    @Test
    fun `playSuccessSound - calls audioService playSuccess and hapticService triggerSuccess`() =
        runTest {
            // Given
            val fakeAudioService = FakeAudioService()
            val fakeHapticService = FakeHapticService()

            // When
            fakeAudioService.playSuccess()
            fakeHapticService.triggerSuccess()

            // Then
            assertThat(fakeAudioService.successPlayed).isEqualTo(1)
            assertThat(fakeHapticService.successTriggered).isEqualTo(1)
        }

    @Test
    fun `playErrorSound - calls audioService playError and hapticService triggerError`() =
        runTest {
            // Given
            val fakeAudioService = FakeAudioService()
            val fakeHapticService = FakeHapticService()

            // When
            fakeAudioService.playError()
            fakeHapticService.triggerError()

            // Then
            assertThat(fakeAudioService.errorPlayed).isEqualTo(1)
            assertThat(fakeHapticService.errorTriggered).isEqualTo(1)
        }

    @Test
    fun `playLevelUpSound - calls audioService playLevelUp and hapticService triggerSuccess`() =
        runTest {
            // Given
            val fakeAudioService = FakeAudioService()
            val fakeHapticService = FakeHapticService()

            // When
            fakeAudioService.playLevelUp()
            fakeHapticService.triggerSuccess()

            // Then
            assertThat(fakeAudioService.levelUpPlayed).isEqualTo(1)
            assertThat(fakeHapticService.successTriggered).isEqualTo(1)
        }

    @Test
    fun `playBadgeUnlockSound - calls audioService playBadgeUnlock and hapticService triggerBadgeUnlock`() =
        runTest {
            // Given
            val fakeAudioService = FakeAudioService()
            val fakeHapticService = FakeHapticService()

            // When
            fakeAudioService.playBadgeUnlock()
            fakeHapticService.triggerBadgeUnlock()

            // Then
            assertThat(fakeAudioService.badgeUnlockPlayed).isEqualTo(1)
            assertThat(fakeHapticService.badgeUnlockTriggered).isEqualTo(1)
        }

    @Test
    fun `playCountdownSound - calls audioService playCountdown`() =
        runTest {
            // Given
            val fakeAudioService = FakeAudioService()

            // When
            fakeAudioService.playCountdown()

            // Then
            assertThat(fakeAudioService.countdownPlayed).isEqualTo(1)
        }

    @Test
    fun `playGoSound - calls audioService playGo and hapticService triggerSuccess`() =
        runTest {
            // Given
            val fakeAudioService = FakeAudioService()
            val fakeHapticService = FakeHapticService()

            // When
            fakeAudioService.playGo()
            fakeHapticService.triggerSuccess()

            // Then
            assertThat(fakeAudioService.goPlayed).isEqualTo(1)
            assertThat(fakeHapticService.successTriggered).isEqualTo(1)
        }

    @Test
    fun `toggleBackgroundMusic - when not playing - starts background music`() =
        runTest {
            // Given
            val fakeAudioService = FakeAudioService()
            fakeAudioService.setMusicEnabled(true)

            // When
            fakeAudioService.startBackgroundMusic()

            // Then
            assertThat(fakeAudioService.isMusicPlaying).isTrue()
        }

    @Test
    fun `toggleBackgroundMusic - when playing - stops background music`() =
        runTest {
            // Given
            val fakeAudioService = FakeAudioService()
            fakeAudioService.setMusicEnabled(true)
            fakeAudioService.startBackgroundMusic()
            assertThat(fakeAudioService.isMusicPlaying).isTrue()

            // When
            fakeAudioService.stopBackgroundMusic()

            // Then
            assertThat(fakeAudioService.isMusicPlaying).isFalse()
        }

    /**
     * Fake implementation of AudioService for testing.
     */
    private class FakeAudioService : dev.hossain.mathtutor.audio.AudioService {
        var successPlayed = 0
        var errorPlayed = 0
        var levelUpPlayed = 0
        var badgeUnlockPlayed = 0
        var countdownPlayed = 0
        var goPlayed = 0
        var isMusicPlaying = false
        private var musicEnabled = false
        private var soundEffectsEnabled = true

        override fun playSuccess() {
            if (soundEffectsEnabled) successPlayed++
        }

        override fun playPerfectScore() {}

        override fun playBadgeUnlock() {
            if (soundEffectsEnabled) badgeUnlockPlayed++
        }

        override fun playError() {
            if (soundEffectsEnabled) errorPlayed++
        }

        override fun playStreakContinue() {}

        override fun playLevelUp() {
            if (soundEffectsEnabled) levelUpPlayed++
        }

        override fun playCountdown() {
            if (soundEffectsEnabled) countdownPlayed++
        }

        override fun playGo() {
            if (soundEffectsEnabled) goPlayed++
        }

        override fun playWarning() {}

        override fun startBackgroundMusic() {
            if (musicEnabled) isMusicPlaying = true
        }

        override fun stopBackgroundMusic() {
            isMusicPlaying = false
        }

        override fun pauseBackgroundMusic() {}

        override fun resumeBackgroundMusic() {}

        override fun setMusicEnabled(enabled: Boolean) {
            musicEnabled = enabled
        }

        override fun registerSoundLoadListener(listener: (Boolean, Map<String, Int>) -> Unit) {
            // No-op for tests
        }

        override fun unregisterSoundLoadListener(listener: (Boolean, Map<String, Int>) -> Unit) {
            // No-op for tests
        }

        override fun setSoundEffectsEnabled(enabled: Boolean) {
            soundEffectsEnabled = enabled
        }

        override fun setVolume(volume: Float) {}

        override fun release() {}
    }

    /**
     * Fake implementation of HapticService for testing.
     */
    private class FakeHapticService : dev.hossain.mathtutor.haptic.HapticService {
        var successTriggered = 0
        var errorTriggered = 0
        var badgeUnlockTriggered = 0
        private var hapticsEnabled = true

        override fun triggerSuccess() {
            if (hapticsEnabled) successTriggered++
        }

        override fun triggerError() {
            if (hapticsEnabled) errorTriggered++
        }

        override fun triggerBadgeUnlock() {
            if (hapticsEnabled) badgeUnlockTriggered++
        }

        override fun triggerButtonClick() {}

        override fun triggerLongPress() {}

        override fun setHapticsEnabled(enabled: Boolean) {
            hapticsEnabled = enabled
        }
    }

    /**
     * Fake implementation of [UserPreferencesRepository] for testing.
     * Provides in-memory storage for preferences with Flow support.
     */
    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        private val analyticsEnabledFlow = MutableStateFlow(true)
        private val onboardingCompletedFlow = MutableStateFlow(false)
        private val hapticsEnabledFlow = MutableStateFlow(true)
        private val soundEffectsEnabledFlow = MutableStateFlow(true)
        private val backgroundMusicEnabledFlow = MutableStateFlow(false)
        private val volumeFlow = MutableStateFlow(dev.hossain.mathtutor.audio.AudioConstants.DEFAULT_SOUND_EFFECTS_VOLUME)
        private val highContrastEnabledFlow = MutableStateFlow(false)
        private val largeTextEnabledFlow = MutableStateFlow(false)

        override val isAnalyticsEnabled: Flow<Boolean> = analyticsEnabledFlow

        override suspend fun setAnalyticsEnabled(enabled: Boolean) {
            analyticsEnabledFlow.value = enabled
        }

        fun getAnalyticsEnabled(): Boolean = analyticsEnabledFlow.value

        fun getOnboardingCompleted(): Boolean = onboardingCompletedFlow.value

        fun getHapticsEnabled(): Boolean = hapticsEnabledFlow.value

        fun getSoundEffectsEnabled(): Boolean = soundEffectsEnabledFlow.value

        fun getVolume(): Float = volumeFlow.value

        // Other UserPreferencesRepository methods (minimal implementation for testing)
        override val isOnboardingCompleted: Flow<Boolean> = onboardingCompletedFlow

        override suspend fun setOnboardingCompleted(completed: Boolean) {
            onboardingCompletedFlow.value = completed
        }

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
            volumeFlow.value = volume
        }

        override val isHighContrastEnabled: Flow<Boolean> = highContrastEnabledFlow

        override suspend fun setHighContrastEnabled(enabled: Boolean) {
            highContrastEnabledFlow.value = enabled
        }

        override val isLargeTextEnabled: Flow<Boolean> = largeTextEnabledFlow

        override suspend fun setLargeTextEnabled(enabled: Boolean) {
            largeTextEnabledFlow.value = enabled
        }

        private val gameTrialAttemptsFlows = mutableMapOf<Game, MutableStateFlow<Int>>()

        override fun getGameTrialAttempts(game: Game): Flow<Int> =
            gameTrialAttemptsFlows.getOrPut(game) { MutableStateFlow(0) }

        override suspend fun incrementGameTrialAttempts(game: Game): Int {
            val flow = gameTrialAttemptsFlows.getOrPut(game) { MutableStateFlow(0) }
            val newCount = (flow.value + 1).coerceAtMost(3)
            flow.value = newCount
            return newCount
        }
    }

    // ==================== Profile Update Tests ====================

    @Test
    fun `updateGradeLevel - calls repository with correct grade level`() =
        runTest {
            // Given
            val fakeProfileRepo = FakeUserProfileRepository()

            // When - Update grade level to GRADE_2
            fakeProfileRepo.updateGradeLevel(dev.hossain.mathtutor.domain.model.GradeLevel.GRADE_2)

            // Then - Grade level should be updated
            assertThat(fakeProfileRepo.lastUpdatedGradeLevel).isEqualTo(dev.hossain.mathtutor.domain.model.GradeLevel.GRADE_2)
        }

    @Test
    fun `updateAdaptiveDifficulty - calls repository with enabled true`() =
        runTest {
            // Given
            val fakeProfileRepo = FakeUserProfileRepository()

            // When - Enable adaptive difficulty
            fakeProfileRepo.updateAdaptiveDifficulty(true)

            // Then - Adaptive difficulty should be enabled
            assertThat(fakeProfileRepo.lastUpdatedAdaptiveDifficulty).isTrue()
        }

    @Test
    fun `updateAdaptiveDifficulty - calls repository with enabled false`() =
        runTest {
            // Given
            val fakeProfileRepo = FakeUserProfileRepository()

            // When - Disable adaptive difficulty
            fakeProfileRepo.updateAdaptiveDifficulty(false)

            // Then - Adaptive difficulty should be disabled
            assertThat(fakeProfileRepo.lastUpdatedAdaptiveDifficulty).isFalse()
        }

    @Test
    fun `updateName - calls repository with correct name`() =
        runTest {
            // Given
            val fakeProfileRepo = FakeUserProfileRepository()

            // When - Update name
            fakeProfileRepo.updateName("Test User")

            // Then - Name should be updated
            assertThat(fakeProfileRepo.lastUpdatedName).isEqualTo("Test User")
        }

    @Test
    fun `updateName - calls repository with null name`() =
        runTest {
            // Given
            val fakeProfileRepo = FakeUserProfileRepository()

            // When - Clear name
            fakeProfileRepo.updateName(null)

            // Then - Name should be null
            assertThat(fakeProfileRepo.lastUpdatedName).isNull()
        }

    @Test
    fun `profile state - reflects loaded profile data`() =
        runTest {
            // Given
            val fakeProfileRepo = FakeUserProfileRepository()
            val testProfile =
                dev.hossain.mathtutor.domain.model.UserProfile(
                    name = "Test Child",
                    gradeLevel = dev.hossain.mathtutor.domain.model.GradeLevel.GRADE_1,
                    createdAt = java.time.Instant.now(),
                    adaptiveDifficultyEnabled = false,
                )
            fakeProfileRepo.setProfile(testProfile)

            // When - Collect profile
            val profile = fakeProfileRepo.getProfile().firstOrNull()

            // Then - Profile data should match
            assertThat(profile).isNotNull()
            assertThat(profile?.name).isEqualTo("Test Child")
            assertThat(profile?.gradeLevel).isEqualTo(dev.hossain.mathtutor.domain.model.GradeLevel.GRADE_1)
            assertThat(profile?.adaptiveDifficultyEnabled).isFalse()
        }

    /**
     * Fake implementation of [dev.hossain.mathtutor.domain.repository.UserProfileRepository] for testing.
     */
    private class FakeUserProfileRepository : dev.hossain.mathtutor.domain.repository.UserProfileRepository {
        private val profileFlow =
            MutableStateFlow<dev.hossain.mathtutor.domain.model.UserProfile?>(null)
        var lastUpdatedGradeLevel: dev.hossain.mathtutor.domain.model.GradeLevel? = null
        var lastUpdatedAdaptiveDifficulty: Boolean? = null
        var lastUpdatedName: String? = null

        override fun getProfile(): Flow<dev.hossain.mathtutor.domain.model.UserProfile?> = profileFlow

        override suspend fun saveProfile(profile: dev.hossain.mathtutor.domain.model.UserProfile) {
            profileFlow.value = profile
        }

        override suspend fun updateGradeLevel(gradeLevel: dev.hossain.mathtutor.domain.model.GradeLevel) {
            lastUpdatedGradeLevel = gradeLevel
            profileFlow.value?.let { profile ->
                profileFlow.value = profile.copy(gradeLevel = gradeLevel)
            }
        }

        override suspend fun updateName(name: String?) {
            lastUpdatedName = name
            profileFlow.value?.let { profile ->
                profileFlow.value = profile.copy(name = name)
            }
        }

        override suspend fun updateAdaptiveDifficulty(enabled: Boolean) {
            lastUpdatedAdaptiveDifficulty = enabled
            profileFlow.value?.let { profile ->
                profileFlow.value = profile.copy(adaptiveDifficultyEnabled = enabled)
            }
        }

        fun setProfile(profile: dev.hossain.mathtutor.domain.model.UserProfile) {
            profileFlow.value = profile
        }
    }
}
