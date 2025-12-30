package dev.hossain.mathtutor.data.local.dao.goals

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.hossain.mathtutor.data.local.entity.goals.GoalHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for goal history operations.
 * Manages completed goal records for analytics and history tracking.
 */
@Dao
interface GoalHistoryDao {
    /**
     * Inserts or updates a goal history record.
     * If a history record with the same ID exists, it will be replaced.
     *
     * @param history The goal history to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: GoalHistoryEntity)

    /**
     * Inserts or updates multiple goal history records.
     *
     * @param histories The list of goal history records to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<GoalHistoryEntity>)

    /**
     * Observes all goal history records for a specific goal, ordered by completion date (newest first).
     *
     * @param goalId The ID of the goal
     * @return Flow of list of goal history records
     */
    @Query("SELECT * FROM goal_history WHERE goalId = :goalId ORDER BY completedAt DESC")
    fun getHistoryByGoalId(goalId: String): Flow<List<GoalHistoryEntity>>

    /**
     * Observes recent goal history records across all goals.
     *
     * @param limit Maximum number of records to return (default: 10)
     * @return Flow of list of recent goal history records
     */
    @Query("SELECT * FROM goal_history ORDER BY completedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 10): Flow<List<GoalHistoryEntity>>

    /**
     * Observes all goal history records ordered by completion date (newest first).
     *
     * @return Flow of list of all goal history records
     */
    @Query("SELECT * FROM goal_history ORDER BY completedAt DESC")
    fun getAllHistory(): Flow<List<GoalHistoryEntity>>

    /**
     * Gets the total count of completed instances for a specific goal.
     *
     * @param goalId The ID of the goal
     * @return Number of times the goal has been completed
     */
    @Query("SELECT COUNT(*) FROM goal_history WHERE goalId = :goalId")
    suspend fun getCompletionCount(goalId: String): Int

    /**
     * Calculates the average accuracy across all completions of a specific goal.
     *
     * @param goalId The ID of the goal
     * @return Average accuracy (0-100), or null if no completions
     */
    @Query("SELECT AVG(overallAccuracy) FROM goal_history WHERE goalId = :goalId")
    suspend fun getAverageAccuracy(goalId: String): Float?

    /**
     * Calculates the average time taken to complete a specific goal.
     *
     * @param goalId The ID of the goal
     * @return Average completion time in seconds, or 0 if no completions
     */
    @Query("SELECT AVG(totalTimeSeconds) FROM goal_history WHERE goalId = :goalId")
    suspend fun getAverageCompletionTime(goalId: String): Long?

    /**
     * Gets the total number of all goal completions.
     *
     * @return Count of all goal history records
     */
    @Query("SELECT COUNT(*) FROM goal_history")
    suspend fun getTotalCompletions(): Int
}
