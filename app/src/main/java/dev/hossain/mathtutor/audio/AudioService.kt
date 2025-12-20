package dev.hossain.mathtutor.audio

/**
 * Audio service interface for playing sound effects and background music.
 *
 * This service provides methods for:
 * - Playing short sound effects (success, error, badge unlock, etc.)
 * - Managing background music playback
 * - Controlling audio settings (volume, enable/disable)
 *
 * Sound effects are played using SoundPool for low-latency playback.
 * Background music is played using ExoPlayer for efficient looping.
 */
interface AudioService {
    // ==================== Sound Effects ====================

    /**
     * Play the success sound effect (correct answer).
     */
    fun playSuccess()

    /**
     * Play the perfect score sound effect (100% accuracy).
     */
    fun playPerfectScore()

    /**
     * Play the badge unlock sound effect.
     */
    fun playBadgeUnlock()

    /**
     * Play a gentle error sound effect (incorrect answer).
     */
    fun playError()

    /**
     * Play the streak continue sound effect (maintaining streak).
     */
    fun playStreakContinue()

    /**
     * Play the level up sound effect (difficulty increased).
     */
    fun playLevelUp()

    // ==================== Game Sound Effects ====================

    /**
     * Play the countdown sound effect (3-2-1 countdown before game starts).
     * Should be called for each countdown tick.
     */
    fun playCountdown()

    /**
     * Play the "GO!" sound effect when the game starts after countdown.
     */
    fun playGo()

    /**
     * Play the warning sound effect (10-second warning during game).
     */
    fun playWarning()

    // ==================== Background Music ====================

    /**
     * Start playing background music. Music loops continuously.
     */
    fun startBackgroundMusic()

    /**
     * Stop the background music completely.
     */
    fun stopBackgroundMusic()

    /**
     * Pause the background music (can be resumed).
     */
    fun pauseBackgroundMusic()

    /**
     * Resume the background music from paused state.
     */
    fun resumeBackgroundMusic()

    // ==================== Settings ====================

    /**
     * Enable or disable background music.
     *
     * @param enabled true to enable music, false to disable
     */
    fun setMusicEnabled(enabled: Boolean)

    /**
     * Enable or disable sound effects.
     *
     * @param enabled true to enable sound effects, false to disable
     */
    fun setSoundEffectsEnabled(enabled: Boolean)

    /**
     * Set the master volume for all audio.
     *
     * @param volume Volume level from 0.0 (silent) to 1.0 (full volume)
     */
    fun setVolume(volume: Float)

    // ==================== Lifecycle ====================

    /**
     * Release all audio resources. Call this when the service is no longer needed.
     * After calling this method, the service should not be used.
     */
    fun release()
}
