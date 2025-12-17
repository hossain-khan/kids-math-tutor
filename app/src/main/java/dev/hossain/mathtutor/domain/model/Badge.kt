package dev.hossain.mathtutor.domain.model

import java.time.Instant

/**
 * Domain model representing a badge that can be earned by completing specific achievements.
 *
 * @property id Unique identifier for the badge
 * @property name Display name of the badge
 * @property description Detailed description of what the badge represents
 * @property icon Emoji or icon representation of the badge
 * @property category The category this badge belongs to
 * @property requirement The criteria that must be met to unlock this badge
 * @property unlockedAt Timestamp when the badge was unlocked, null if still locked
 */
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: BadgeCategory,
    val requirement: BadgeRequirement,
    val unlockedAt: Instant? = null,
) {
    /**
     * Checks if the badge has been unlocked.
     *
     * @return true if the badge is unlocked (unlockedAt is not null), false otherwise
     */
    fun isUnlocked(): Boolean = unlockedAt != null
}
