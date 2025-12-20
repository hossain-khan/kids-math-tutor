package dev.hossain.mathtutor.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

        assertEquals(1, audioService.successPlayed)
    }

    @Test
    fun `playSuccess does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playSuccess()

        assertEquals(0, audioService.successPlayed)
    }

    @Test
    fun `playPerfectScore plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playPerfectScore()

        assertEquals(1, audioService.perfectScorePlayed)
    }

    @Test
    fun `playPerfectScore does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playPerfectScore()

        assertEquals(0, audioService.perfectScorePlayed)
    }

    @Test
    fun `playBadgeUnlock plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playBadgeUnlock()

        assertEquals(1, audioService.badgeUnlockPlayed)
    }

    @Test
    fun `playBadgeUnlock does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playBadgeUnlock()

        assertEquals(0, audioService.badgeUnlockPlayed)
    }

    @Test
    fun `playError plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playError()

        assertEquals(1, audioService.errorPlayed)
    }

    @Test
    fun `playError does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playError()

        assertEquals(0, audioService.errorPlayed)
    }

    @Test
    fun `playStreakContinue plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playStreakContinue()

        assertEquals(1, audioService.streakContinuePlayed)
    }

    @Test
    fun `playStreakContinue does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playStreakContinue()

        assertEquals(0, audioService.streakContinuePlayed)
    }

    @Test
    fun `playLevelUp plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playLevelUp()

        assertEquals(1, audioService.levelUpPlayed)
    }

    @Test
    fun `playLevelUp does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playLevelUp()

        assertEquals(0, audioService.levelUpPlayed)
    }

    // ==================== Game Sound Effects Tests ====================

    @Test
    fun `playCountdown plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playCountdown()

        assertEquals(1, audioService.countdownPlayed)
    }

    @Test
    fun `playCountdown does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playCountdown()

        assertEquals(0, audioService.countdownPlayed)
    }

    @Test
    fun `playGo plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playGo()

        assertEquals(1, audioService.goPlayed)
    }

    @Test
    fun `playGo does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playGo()

        assertEquals(0, audioService.goPlayed)
    }

    @Test
    fun `playWarning plays when sound effects are enabled`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playWarning()

        assertEquals(1, audioService.warningPlayed)
    }

    @Test
    fun `playWarning does not play when sound effects are disabled`() {
        audioService.setSoundEffectsEnabled(false)

        audioService.playWarning()

        assertEquals(0, audioService.warningPlayed)
    }

    // ==================== Background Music Tests ====================

    @Test
    fun `startBackgroundMusic starts when music is enabled`() {
        audioService.setMusicEnabled(true)

        audioService.startBackgroundMusic()

        assertTrue(audioService.isMusicPlaying)
    }

    @Test
    fun `startBackgroundMusic does not start when music is disabled`() {
        audioService.setMusicEnabled(false)

        audioService.startBackgroundMusic()

        assertFalse(audioService.isMusicPlaying)
    }

    @Test
    fun `stopBackgroundMusic stops music`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()

        audioService.stopBackgroundMusic()

        assertFalse(audioService.isMusicPlaying)
    }

    @Test
    fun `pauseBackgroundMusic pauses music`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()

        audioService.pauseBackgroundMusic()

        assertTrue(audioService.isMusicPaused)
    }

    @Test
    fun `resumeBackgroundMusic resumes when music is enabled`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()
        audioService.pauseBackgroundMusic()

        audioService.resumeBackgroundMusic()

        assertFalse(audioService.isMusicPaused)
    }

    @Test
    fun `resumeBackgroundMusic does not resume when music is disabled`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()
        audioService.pauseBackgroundMusic()
        audioService.setMusicEnabled(false)

        audioService.resumeBackgroundMusic()

        assertTrue(audioService.isMusicPaused)
    }

    // ==================== Volume Control Tests ====================

    @Test
    fun `setVolume sets volume correctly`() {
        audioService.setVolume(0.5f)

        assertEquals(0.5f, audioService.currentVolume, 0.01f)
    }

    @Test
    fun `setVolume clamps volume below 0 to 0`() {
        audioService.setVolume(-0.5f)

        assertEquals(0f, audioService.currentVolume, 0.01f)
    }

    @Test
    fun `setVolume clamps volume above 1 to 1`() {
        audioService.setVolume(1.5f)

        assertEquals(1f, audioService.currentVolume, 0.01f)
    }

    @Test
    fun `initial volume is 1`() {
        assertEquals(1f, audioService.currentVolume, 0.01f)
    }

    // ==================== Settings Tests ====================

    @Test
    fun `sound effects are enabled by default`() {
        val newService = FakeAudioService()

        assertTrue(newService.isSoundEffectsEnabled)
    }

    @Test
    fun `music is enabled by default`() {
        val newService = FakeAudioService()

        assertTrue(newService.isMusicEnabled)
    }

    @Test
    fun `disabling music pauses background music`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()

        audioService.setMusicEnabled(false)

        assertTrue(audioService.isMusicPaused)
    }

    // ==================== Lifecycle Tests ====================

    @Test
    fun `release stops music and releases resources`() {
        audioService.setMusicEnabled(true)
        audioService.startBackgroundMusic()

        audioService.release()

        assertTrue(audioService.isReleased)
        assertFalse(audioService.isMusicPlaying)
    }

    @Test
    fun `multiple play calls increment count`() {
        audioService.setSoundEffectsEnabled(true)

        audioService.playSuccess()
        audioService.playSuccess()
        audioService.playSuccess()

        assertEquals(3, audioService.successPlayed)
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
        var musicEnabledState = true

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

        override fun release() {
            isReleased = true
            isMusicPlaying = false
        }
    }
}
