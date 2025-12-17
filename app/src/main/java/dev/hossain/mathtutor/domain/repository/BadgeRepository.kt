package dev.hossain.mathtutor.domain.repository

import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for badge data management.
 * Provides methods to retrieve, unlock, and manage badges.
 */
interface BadgeRepository {
    /**
     * Retrieves all badges ordered by category and id.
     *
     * @return Flow of all badges
     */
    fun getAllBadges(): Flow<List<Badge>>

    /**
     * Retrieves the most recently unlocked badges.
     *
     * @param limit Maximum number of badges to retrieve (default: 3)
     * @return Flow of recently unlocked badges ordered by unlock date descending
     */
    fun getRecentlyUnlockedBadges(limit: Int = 3): Flow<List<Badge>>

    /**
     * Retrieves all badges for a specific category.
     *
     * @param category The badge category to filter by
     * @return Flow of badges in the specified category ordered by id
     */
    fun getBadgesByCategory(category: BadgeCategory): Flow<List<Badge>>

    /**
     * Retrieves only unlocked badges.
     *
     * @return Flow of unlocked badges
     */
    fun getUnlockedBadges(): Flow<List<Badge>>

    /**
     * Retrieves a summary of badge progress (unlocked count vs total count).
     *
     * @return Flow of badge progress summary
     */
    fun getProgressSummary(): Flow<BadgeProgress>

    /**
     * Unlocks a badge by setting its unlock timestamp.
     *
     * @param badgeId The id of the badge to unlock
     * @param unlockedAt The timestamp when the badge was unlocked (defaults to now)
     */
    suspend fun unlockBadge(
        badgeId: String,
        unlockedAt: Instant = Instant.now(),
    )

    /**
     * Initializes the badge database with default badges if empty.
     * Should be called once during first app launch.
     */
    suspend fun initializeBadges()
}

/**
 * Data class representing badge progress summary.
 *
 * @property unlockedCount Number of badges that have been unlocked
 * @property totalCount Total number of badges available
 */
data class BadgeProgress(
    val unlockedCount: Int,
    val totalCount: Int,
) {
    /**
     * Calculates the percentage of badges unlocked.
     *
     * @return Percentage (0-100) of unlocked badges, or 0 if no badges exist
     */
    val percentage: Float
        get() = if (totalCount > 0) (unlockedCount.toFloat() / totalCount) * 100 else 0f
}
