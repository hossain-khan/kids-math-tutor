package dev.hossain.mathtutor.domain.model

/**
 * Represents a recommended difficulty adjustment based on performance.
 */
enum class DifficultyAdjustment {
    /**
     * Recommend moving to an easier difficulty level.
     * Triggered when accuracy is below 50% with sufficient attempts.
     */
    EASIER,

    /**
     * Recommend staying at the current difficulty level.
     * Default when performance doesn't meet thresholds for change.
     */
    CURRENT,

    /**
     * Recommend moving to a harder difficulty level.
     * Triggered when accuracy is at or above 85% with sufficient attempts.
     */
    HARDER,
}
