package dev.hossain.mathtutor.data.local.dao.goals

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.hossain.mathtutor.data.local.entity.goals.ActiveGoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for active goal operations.
 * Manages the currently active goal for a child, tracking progress through components.
 */
@Dao
interface ActiveGoalDao {
    /**
     * Inserts or updates an active goal.
     * If an active goal with the same ID exists, it will be replaced.
     *
     * @param activeGoal The active goal to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activeGoal: ActiveGoalEntity)

    /**
     * Updates an existing active goal.
     *
     * @param activeGoal The active goal to update
     */
    @Update
    suspend fun update(activeGoal: ActiveGoalEntity)

    /**
     * Deletes an active goal.
     *
     * @param activeGoal The active goal to delete
     */
    @Delete
    suspend fun delete(activeGoal: ActiveGoalEntity)

    /**
     * Observes the currently active goal.
     * Since there should only be one active goal at a time, this returns a single item Flow.
     *
     * @return Flow of the active goal, or null if none exists
     */
    @Query("SELECT * FROM active_goals LIMIT 1")
    fun getActiveGoal(): Flow<ActiveGoalEntity?>

    /**
     * Gets a specific active goal by ID.
     *
     * @param activeGoalId The ID of the active goal
     * @return The active goal entity, or null if not found
     */
    @Query("SELECT * FROM active_goals WHERE id = :activeGoalId")
    suspend fun getActiveGoalById(activeGoalId: String): ActiveGoalEntity?

    /**
     * Deletes all active goals (should only be one at a time).
     * Used when completing or resetting the current goal.
     */
    @Query("DELETE FROM active_goals")
    suspend fun clearActiveGoal()

    /**
     * Updates the component progress for an active goal.
     *
     * @param activeGoalId The ID of the active goal
     * @param componentIndex The current component index
     * @param componentProgress JSON string of updated component progress list
     */
    @Query(
        "UPDATE active_goals SET currentComponentIndex = :componentIndex, componentProgress = :componentProgress WHERE id = :activeGoalId",
    )
    suspend fun updateComponentProgress(
        activeGoalId: String,
        componentIndex: Int,
        componentProgress: String,
    )

    /**
     * Gets the count of active goals (should be 0 or 1).
     *
     * @return Number of active goals
     */
    @Query("SELECT COUNT(*) FROM active_goals")
    suspend fun getActiveGoalCount(): Int
}
