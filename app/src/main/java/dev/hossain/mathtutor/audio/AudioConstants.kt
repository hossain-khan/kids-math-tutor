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
}
