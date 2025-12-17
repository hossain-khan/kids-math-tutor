package dev.hossain.mathtutor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.domain.model.BadgeCategory
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Data Access Object for badge database operations.
 * All query methods return Flow for reactive updates.
 */
@Dao
interface BadgeDao {
    /**
     * Retrieves all badges ordered by category and id.
     *
     * @return Flow of all badges
     */
    @Query("SELECT * FROM badges ORDER BY category, id")
    fun getAllBadges(): Flow<List<BadgeEntity>>

    /**
     * Retrieves the most recently unlocked badges.
     * Default limit of 3 badges should be applied at the repository layer.
     *
     * @param limit Maximum number of badges to retrieve
     * @return Flow of recently unlocked badges ordered by unlock date descending
     */
    @Query("SELECT * FROM badges WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC LIMIT :limit")
    fun getRecentlyUnlockedBadges(limit: Int): Flow<List<BadgeEntity>>

    /**
     * Retrieves all badges for a specific category.
     *
     * @param category The badge category to filter by
     * @return Flow of badges in the specified category ordered by id
     */
    @Query("SELECT * FROM badges WHERE category = :category ORDER BY id")
    fun getBadgesByCategory(category: BadgeCategory): Flow<List<BadgeEntity>>

    /**
     * Counts the number of unlocked badges.
     *
     * @return Flow of unlocked badge count
     */
    @Query("SELECT COUNT(*) FROM badges WHERE unlockedAt IS NOT NULL")
    fun getUnlockedCount(): Flow<Int>

    /**
     * Counts the total number of badges.
     *
     * @return Flow of total badge count
     */
    @Query("SELECT COUNT(*) FROM badges")
    fun getTotalCount(): Flow<Int>

    /**
     * Updates an existing badge in the database.
     *
     * @param badge The badge to update
     */
    @Update
    suspend fun updateBadge(badge: BadgeEntity)

    /**
     * Unlocks a badge by setting its unlockedAt timestamp.
     *
     * @param badgeId The id of the badge to unlock
     * @param unlockedAt The timestamp when the badge was unlocked
     */
    @Query("UPDATE badges SET unlockedAt = :unlockedAt WHERE id = :badgeId")
    suspend fun unlockBadge(
        badgeId: String,
        unlockedAt: Instant,
    )

    /**
     * Inserts multiple badges into the database.
     * Replaces existing badges with the same id.
     *
     * @param badges List of badges to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<BadgeEntity>)
}
