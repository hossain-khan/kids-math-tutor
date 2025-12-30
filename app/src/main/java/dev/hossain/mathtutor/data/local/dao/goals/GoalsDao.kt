package dev.hossain.mathtutor.data.local.dao.goals

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.hossain.mathtutor.data.local.entity.goals.GoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for goal catalog operations.
 * Provides methods for creating, reading, updating, and deleting goals in the goal catalog.
 */
@Dao
interface GoalsDao {
    /**
     * Inserts or updates a goal in the catalog.
     * If a goal with the same ID exists, it will be replaced.
     *
     * @param goal The goal to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    /**
     * Inserts or updates multiple goals.
     * If goals with the same IDs exist, they will be replaced.
     *
     * @param goals The list of goals to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<GoalEntity>)

    /**
     * Deletes a goal from the catalog.
     *
     * @param goal The goal to delete
     */
    @Delete
    suspend fun delete(goal: GoalEntity)

    /**
     * Gets a specific goal by ID.
     *
     * @param goalId The ID of the goal
     * @return The goal entity, or null if not found
     */
    @Query("SELECT * FROM goals_catalog WHERE id = :goalId")
    suspend fun getGoalById(goalId: String): GoalEntity?

    /**
     * Observes all active (non-archived) goals ordered by creation date (newest first).
     * Returns a Flow that emits whenever the data changes.
     *
     * @return Flow of list of active goals
     */
    @Query("SELECT * FROM goals_catalog WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    /**
     * Observes all archived goals ordered by creation date (newest first).
     *
     * @return Flow of list of archived goals
     */
    @Query("SELECT * FROM goals_catalog WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedGoals(): Flow<List<GoalEntity>>

    /**
     * Archives a goal (soft delete).
     * This prevents it from appearing in the active goals list.
     *
     * @param goalId The ID of the goal to archive
     */
    @Query("UPDATE goals_catalog SET isArchived = 1 WHERE id = :goalId")
    suspend fun archiveGoal(goalId: String)

    /**
     * Unarchives a goal.
     *
     * @param goalId The ID of the goal to unarchive
     */
    @Query("UPDATE goals_catalog SET isArchived = 0 WHERE id = :goalId")
    suspend fun unarchiveGoal(goalId: String)

    /**
     * Gets the count of all active goals.
     *
     * @return Number of active goals
     */
    @Query("SELECT COUNT(*) FROM goals_catalog WHERE isArchived = 0")
    suspend fun getActiveGoalCount(): Int
}
