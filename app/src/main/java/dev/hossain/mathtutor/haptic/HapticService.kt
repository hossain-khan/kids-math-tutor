package dev.hossain.mathtutor.haptic

/**
 * Haptic feedback service interface for providing tactile confirmation to user interactions.
 *
 * This service provides methods for:
 * - Triggering success vibrations (correct answers)
 * - Triggering error vibrations (incorrect answers)
 * - Triggering badge unlock vibrations (achievements)
 * - Triggering button click vibrations (UI interactions)
 * - Triggering long press vibrations (contextual feedback)
 * - Controlling haptic feedback settings (enable/disable)
 *
 * Different vibration patterns are used for different events to create a multi-sensory experience:
 * - Success: Light, pleasant (50ms)
 * - Error: Distinct but not harsh (double pulse ~200ms)
 * - Badge unlock: Celebratory crescendo pattern (~400ms)
 * - Button click: Very subtle tap (10ms)
 * - Long press: Firm, noticeable (100ms)
 */
interface HapticService {
    /**
     * Trigger a pleasant vibration pattern for correct answers.
     *
     * Pattern: Single gentle pulse (~50ms)
     * Feel: Light and confirming
     */
    fun triggerSuccess()

    /**
     * Trigger a distinct vibration pattern for incorrect answers.
     *
     * Pattern: Double pulse with gap (~200ms total)
     * Feel: Distinct but not harsh
     */
    fun triggerError()

    /**
     * Trigger a celebratory vibration pattern for badge unlocks.
     *
     * Pattern: Crescendo with increasing intensity (~400ms)
     * Feel: Exciting and celebratory
     */
    fun triggerBadgeUnlock()

    /**
     * Trigger a subtle vibration for button clicks.
     *
     * Pattern: Very light tap (~10ms)
     * Feel: Minimal and subtle
     */
    fun triggerButtonClick()

    /**
     * Trigger a firm vibration for long press actions.
     *
     * Pattern: Single heavy pulse (~100ms)
     * Feel: Firm and noticeable
     */
    fun triggerLongPress()

    /**
     * Enable or disable haptic feedback.
     *
     * @param enabled true to enable haptics, false to disable
     */
    fun setHapticsEnabled(enabled: Boolean)
}
