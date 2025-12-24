package dev.hossain.mathtutor.audio

/**
 * Constants for audio configuration.
 *
 * Centralized constants for audio behavior and default settings.
 */
object AudioConstants {
    /**
     * Default volume for sound effects (30%).
     *
     * Set to 0.3f to prevent startling children with loud sounds while still providing
     * clear audio feedback for correct answers, errors, badges, and other interactions.
     * Users can adjust this in the audio settings (range: 0.0 - 1.0).
     */
    const val DEFAULT_SOUND_EFFECTS_VOLUME = 0.3f

    /**
     * Background music volume multiplier (60% of sound effects volume).
     *
     * Background music is played at this fraction of the main volume setting.
     * With DEFAULT_SOUND_EFFECTS_VOLUME of 0.3f, background music plays at:
     * 0.3f × 0.6f = 0.18f (18% of maximum volume)
     *
     * This ensures background music provides ambient sound without overpowering
     * sound effects and speech, creating a better audio balance for children.
     */
    const val BACKGROUND_MUSIC_VOLUME_MULTIPLIER = 0.6f
}
