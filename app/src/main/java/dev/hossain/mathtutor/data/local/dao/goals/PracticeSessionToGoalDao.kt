package dev.hossain.mathtutor.data.local.dao.goals

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.hossain.mathtutor.data.local.entity.goals.PracticeSessionToGoalEntity

/**
 * Data Access Object for practice session to goal linking operations.
 * Manages the relationship between completed practice sessions and active goal components.
 * This allows tracking which sessions contribute to goal progress.
 */
@Dao
interface PracticeSessionToGoalDao {
    /**
     * Inserts or updates a session-to-goal link.
     * If a link with the same ID exists, it will be replaced.
     *
     * @param link The session-to-goal link to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: PracticeSessionToGoalEntity)

    /**
     * Inserts or updates multiple session-to-goal links.
     *
     * @param links The list of links to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<PracticeSessionToGoalEntity>)

    /**
     * Deletes a session-to-goal link.
     *
     * @param link The link to delete
     */
    @Delete
    suspend fun delete(link: PracticeSessionToGoalEntity)

    /**
     * Gets all sessions linked to a specific active goal.
     *
     * @param activeGoalId The ID of the active goal
     * @return List of all session-to-goal links for this goal
     */
    @Query("SELECT * FROM practice_session_to_goal WHERE activeGoalId = :activeGoalId")
    suspend fun getSessionsForActiveGoal(activeGoalId: String): List<PracticeSessionToGoalEntity>

    /**
     * Deletes all session-to-goal links for a specific active goal.
     * Typically used when completing or clearing an active goal.
     *
     * @param activeGoalId The ID of the active goal
     */
    @Query("DELETE FROM practice_session_to_goal WHERE activeGoalId = :activeGoalId")
    suspend fun deleteLinksForActiveGoal(activeGoalId: String)

    /**
     * Gets the link for a specific practice session (if it's part of a goal).
     *
     * @param sessionId The ID of the practice session
     * @return The session-to-goal link, or null if the session is not part of a goal
     */
    @Query("SELECT * FROM practice_session_to_goal WHERE sessionId = :sessionId")
    suspend fun getLinkBySessionId(sessionId: String): PracticeSessionToGoalEntity?

    /**
     * Gets all sessions linked to a specific component of an active goal.
     *
     * @param activeGoalId The ID of the active goal
     * @param componentIndex The component index
     * @return List of session-to-goal links for this component
     */
    @Query("SELECT * FROM practice_session_to_goal WHERE activeGoalId = :activeGoalId AND componentIndex = :componentIndex")
    suspend fun getSessionsForComponent(
        activeGoalId: String,
        componentIndex: Int,
    ): List<PracticeSessionToGoalEntity>

    /**
     * Gets the count of sessions linked to a specific active goal.
     *
     * @param activeGoalId The ID of the active goal
     * @return Number of sessions linked to this goal
     */
    @Query("SELECT COUNT(*) FROM practice_session_to_goal WHERE activeGoalId = :activeGoalId")
    suspend fun getSessionCountForActiveGoal(activeGoalId: String): Int
}
