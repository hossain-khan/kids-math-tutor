package dev.hossain.mathtutor.domain.model

/**
 * Settings for accessibility features in the app.
 *
 * This data class tracks the state of various accessibility features that enhance
 * the app's usability for children with different abilities.
 *
 * @property isHighContrastEnabled Whether high contrast mode is active for better visibility
 * @property isLargeTextEnabled Whether large text mode is active (respects system font scale)
 * @property isTalkBackEnabled Whether TalkBack screen reader is active
 */
data class AccessibilitySettings(
    val isHighContrastEnabled: Boolean = false,
    val isLargeTextEnabled: Boolean = false,
    val isTalkBackEnabled: Boolean = false,
)
