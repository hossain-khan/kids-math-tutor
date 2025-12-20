package dev.hossain.mathtutor.domain.model

/**
 * Categories for organizing badges in the badge system.
 * Each category represents a different achievement focus area.
 *
 * @property GETTING_STARTED Initial badges for first-time achievements
 * @property VOLUME Badges earned by solving a high volume of problems
 * @property OPERATION_MASTERY Badges for mastering specific math operations
 * @property SPEED_ACCURACY Badges for speed and accuracy achievements
 * @property STREAK Badges for maintaining daily practice streaks
 * @property GAMES Badges for mini-game achievements
 */
enum class BadgeCategory {
    GETTING_STARTED,
    VOLUME,
    OPERATION_MASTERY,
    SPEED_ACCURACY,
    STREAK,
    GAMES,
}
