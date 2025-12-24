package dev.hossain.mathtutor.audio

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AudioService behavior.
 *
 * These tests use a fake implementation of AudioService to verify
 * the expected behavior without requiring actual audio playback.
 */
class AudioServiceTest {
    private lateinit var audioService: FakeAudioService

    @Before
    fun setup() {
        audioService = FakeAudioService()
    }

    // ==================== Sound Effects Tests ====================

    @Test
    fun `playSuccess plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playSuccess()

        assertThat(audioService.successPlayed).isEqualTo(1)
    }

    @Test
    fun `playSuccess does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playSuccess()

        assertThat(audioService.successPlayed).isEqualTo(0)
    }

    @Test
    fun `playPerfectScore plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playPerfectScore()

        assertThat(audioService.perfectScorePlayed).isEqualTo(1)
    }

    @Test
    fun `playPerfectScore does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playPerfectScore()

        assertThat(audioService.perfectScorePlayed).isEqualTo(0)
    }

    @Test
    fun `playBadgeUnlock plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playBadgeUnlock()

        assertThat(audioService.badgeUnlockPlayed).isEqualTo(1)
    }

    @Test
    fun `playBadgeUnlock does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playBadgeUnlock()

        assertThat(audioService.badgeUnlockPlayed).isEqualTo(0)
    }

    @Test
    fun `playError plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playError()

        assertThat(audioService.errorPlayed).isEqualTo(1)
    }

    @Test
    fun `playError does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playError()

        assertThat(audioService.errorPlayed).isEqualTo(0)
    }

    @Test
    fun `playStreakContinue plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playStreakContinue()

        assertThat(audioService.streakContinuePlayed).isEqualTo(1)
    }

    @Test
    fun `playStreakContinue does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playStreakContinue()

        assertThat(audioService.streakContinuePlayed).isEqualTo(0)
    }

    @Test
    fun `playLevelUp plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playLevelUp()

        assertThat(audioService.levelUpPlayed).isEqualTo(1)
    }

    @Test
    fun `playLevelUp does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playLevelUp()

        assertThat(audioService.levelUpPlayed).isEqualTo(0)
    }

    // ==================== Game Sound Effects Tests ====================

    @Test
    fun `playCountdown plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playCountdown()

        assertThat(audioService.countdownPlayed).isEqualTo(1)
    }

    @Test
    fun `playCountdown does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playCountdown()

        assertThat(audioService.countdownPlayed).isEqualTo(0)
    }

    @Test
    fun `playGo plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playGo()

        assertThat(audioService.goPlayed).isEqualTo(1)
    }

    @Test
    fun `playGo does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playGo()

        assertThat(audioService.goPlayed).isEqualTo(0)
    }

    @Test
    fun `playWarning plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playWarning()

        assertThat(audioService.warningPlayed).isEqualTo(1)
    }

    @Test
    fun `playWarning does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playWarning()

        assertThat(audioService.warningPlayed).isEqualTo(0)
    }

    // ==================== Background Music Tests ====================

    @Test
    fun `startBackgroundMusic starts when music is enabled`() {
        audioService.setMusicEnabled(true)

        audioService.startBackgroundMusic()

        assertThat(audioService.isMusicPlaying).isTrue()
    }

    @Test
    fun `startBackgroundMusic does not start when music is disabled`() {
        audioService.setMusicEnabled(false)

        audioService.startBackgroundMusic()

        assertThat(audioService.isMusicPlaying).isFalse()
    }

    @Test
    fun `stopBackgroundMusic stops music`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()

        audioService.stopBackgroundMusic()

        assertThat(audioService.isMusicPlaying).isFalse()
    }

    @Test
    fun `pauseBackgroundMusic pauses music`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()

        audioService.pauseBackgroundMusic()

        assertThat(audioService.isMusicPaused).isTrue()
    }

    @Test
    fun `resumeBackgroundMusic resumes when music is enabled`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()
        audioService.pauseBackgroundMusic()

        audioService.resumeBackgroundMusic()

        assertThat(audioService.isMusicPaused).isFalse()
    }

    @Test
    fun `resumeBackgroundMusic does not resume when music is disabled`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()
        audioService.pauseBackgroundMusic()
        audioService.setMusicEnabled(false)

        audioService.resumeBackgroundMusic()

        assertThat(audioService.isMusicPaused).isTrue()
    }

    // ==================== Volume Control Tests ====================

    @Test
    fun `setVolume sets volume correctly`() {
        audioService.setVolume(0.5f)

        assertThat(audioService.currentVolume).isWithin(0.01f).of(0.5f)
    }

    @Test
    fun `setVolume clamps volume below 0 to 0`() {
        audioService.setVolume(-0.5f)

        assertThat(audioService.currentVolume).isWithin(0.01f).of(0f)
    }

    @Test
    fun `setVolume clamps volume above 1 to 1`() {
        audioService.setVolume(1.5f)

        assertThat(audioService.currentVolume).isWithin(0.01f).of(1f)
    }

    @Test
    fun `initial volume is 1`() {
        assertThat(audioService.currentVolume).isWithin(0.01f).of(1f)
    }

    // ==================== Settings Tests ====================

    @Test
    fun `sound effects are enabled by default`() {
        val newService = FakeAudioService()

        assertThat(newService.isSoundEffectsEnabled).isTrue()
    }

    @Test
    fun `music is disabled by default`() {
        val newService = FakeAudioService()

        assertThat(newService.isMusicEnabled).isFalse()
    }

    @Test
    fun `disabling music pauses background music`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()

        audioService.setMusicEnabled(false)

        assertThat(audioService.isMusicPaused).isTrue()
    }

    // ==================== Lifecycle Tests ====================

    @Test
    fun `release stops music and releases resources`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()

        audioService.release()

        assertThat(audioService.isReleased).isTrue()
        assertThat(audioService.isMusicPlaying).isFalse()
    }

    @Test
    fun `multiple play calls increment count`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playSuccess()
        audioService.playSuccess()
        audioService.playSuccess()

        assertThat(audioService.successPlayed).isEqualTo(3)
    }

    /**
     * Fake implementation of AudioService for testing.
     * Tracks all calls and state without actual audio playback.
     */
    private class FakeAudioService : AudioService {
        // Play counters
        var successPlayed = 0
        var perfectScorePlayed = 0
        var badgeUnlockPlayed = 0
        var errorPlayed = 0
        var streakContinuePlayed = 0
        var levelUpPlayed = 0
        var countdownPlayed = 0
        var goPlayed = 0
        var warningPlayed = 0

        // State
        var isMusicPlaying = false
        var isMusicPaused = false
        var isReleased = false
        var currentVolume = 1.0f
        var soundEffectsEnabledState = true
        var musicEnabledState = false // Default OFF to match AudioServiceImpl

        val isSoundEffectsEnabled: Boolean get() = soundEffectsEnabledState
        val isMusicEnabled: Boolean get() = musicEnabledState

        override fun playSuccess() {
            if (soundEffectsEnabledState) successPlayed++
        }

        override fun playPerfectScore() {
            if (soundEffectsEnabledState) perfectScorePlayed++
        }

        override fun playBadgeUnlock() {
            if (soundEffectsEnabledState) badgeUnlockPlayed++
        }

        override fun playError() {
            if (soundEffectsEnabledState) errorPlayed++
        }

        override fun playStreakContinue() {
            if (soundEffectsEnabledState) streakContinuePlayed++
        }

        override fun playLevelUp() {
            if (soundEffectsEnabledState) levelUpPlayed++
        }

        override fun playCountdown() {
            if (soundEffectsEnabledState) countdownPlayed++
        }

        override fun playGo() {
            if (soundEffectsEnabledState) goPlayed++
        }

        override fun playWarning() {
            if (soundEffectsEnabledState) warningPlayed++
        }

        override fun startBackgroundMusic() {
            if (musicEnabledState) {
                isMusicPlaying = true
                isMusicPaused = false
            }
        }

        override fun stopBackgroundMusic() {
            isMusicPlaying = false
            isMusicPaused = false
        }

        override fun pauseBackgroundMusic() {
            if (isMusicPlaying) {
                isMusicPaused = true
            }
        }

        override fun resumeBackgroundMusic() {
            if (musicEnabledState && isMusicPaused) {
                isMusicPaused = false
            }
        }

        override fun setMusicEnabled(enabled: Boolean) {
            musicEnabledState = enabled
            if (!enabled && isMusicPlaying) {
                isMusicPaused = true
            }
        }

        override fun setSoundEffectsEnabled(enabled: Boolean) {
            soundEffectsEnabledState = enabled
        }

        override fun setVolume(volume: Float) {
            currentVolume = volume.coerceIn(0f, 1f)
        }

        override fun registerSoundLoadListener(listener: (loaded: Boolean, sampleIds: Map<String, Int>) -> Unit) {
            // No-op in tests
        }

        override fun unregisterSoundLoadListener(listener: (loaded: Boolean, sampleIds: Map<String, Int>) -> Unit) {
            // No-op in tests
        }

        override fun isDeviceAudioSuppressed(): Boolean {
            // Return false by default in tests
            return false
        }

        override fun release() {
            isReleased = true
            isMusicPlaying = false
        }
    }
}
