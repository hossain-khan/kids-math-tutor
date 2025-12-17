package dev.hossain.mathtutor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.hossain.mathtutor.data.local.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for streak database operations.
 * Manages a singleton table (only one row) for streak data.
 */
@Dao
interface StreakDao {
    /**
     * Retrieves the current streak data.
     * Returns a Flow that emits the streak whenever it changes.
     *
     * @return Flow of StreakEntity, or null if no streak data exists
     */
    @Query("SELECT * FROM streak WHERE id = 1")
    fun getStreak(): Flow<StreakEntity?>

    /**
     * Inserts or updates the streak data.
     * Uses REPLACE strategy to update the single row if it already exists.
     *
     * @param streak The streak data to save (id should always be 1)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: StreakEntity)

    /**
     * Deletes all streak data.
     * Useful for testing or user-requested data reset.
     */
    @Query("DELETE FROM streak")
    suspend fun deleteStreak()
}
