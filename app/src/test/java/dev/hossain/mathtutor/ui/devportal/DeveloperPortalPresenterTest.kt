package dev.hossain.mathtutor.ui.devportal

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.analytics.FakeAnalyticsService
import dev.hossain.mathtutor.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        private val volumeFlow = MutableStateFlow(0.7f)
        private val highContrastEnabledFlow = MutableStateFlow(false)
        private val largeTextEnabledFlow = MutableStateFlow(false)

        override val isAnalyticsEnabled: Flow<Boolean> = analyticsEnabledFlow

        override suspend fun setAnalyticsEnabled(enabled: Boolean) {
            analyticsEnabledFlow.value = enabled
        }

        fun getAnalyticsEnabled(): Boolean = analyticsEnabledFlow.value

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
    }
}
