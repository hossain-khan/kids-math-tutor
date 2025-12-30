package dev.hossain.mathtutor.domain.repository

import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for goal data management.
 * Provides methods to create, retrieve, update, and complete goals for children.
 */
interface GoalRepository {
    /**
     * Creates a new goal and adds it to the catalog.
     *
     * @param title The title of the goal
     * @param description The description of the goal
     * @param components List of goal components (operations or challenges)
     * @return Result with created Goal or GoalError
     */
    suspend fun createGoal(
        title: String,
        description: String,
        components: List<GoalComponent>,
    ): Result<Goal>

    /**
     * Retrieves all available goals from the catalog.
     *
     * @return Flow of all goals
     */
    fun getAllGoals(): Flow<List<Goal>>

    /**
     * Retrieves a specific goal by ID.
     *
     * @param goalId The ID of the goal
     * @return Flow of the goal or null if not found
     */
    fun getGoalById(goalId: String): Flow<Goal?>

    /**
     * Archives a goal (marks it as inactive in the catalog).
     *
     * @param goalId The ID of the goal to archive
     * @return Result with success or GoalError
     */
    suspend fun archiveGoal(goalId: String): Result<Unit>

    /**
     * Activates a goal for a child to start practicing.
     *
     * @param goalId The ID of the goal to activate
     * @return Result with ActiveGoal or GoalError
     */
    suspend fun activateGoal(goalId: String): Result<ActiveGoal>

    /**
     * Retrieves the currently active goal for a child.
     *
     * @return Flow of active goal or null if none is active
     */
    fun getActiveGoal(): Flow<ActiveGoal?>

    /**
     * Updates the progress of a specific component in the active goal.
     *
     * @param componentIndex The index of the component to update
     * @param completedSessions Number of completed sessions
     * @param accuracy Current accuracy percentage (0-100)
     * @param timeSeconds Total time spent in seconds
     * @return Result with updated ActiveGoal or GoalError
     */
    suspend fun updateComponentProgress(
        componentIndex: Int,
        completedSessions: Int,
        accuracy: Float,
        timeSeconds: Long,
    ): Result<ActiveGoal>

    /**
     * Completes the current active goal and moves it to history.
     *
     * @return Result with completed GoalHistory or GoalError
     */
    suspend fun completeActiveGoal(): Result<GoalHistory>

    /**
     * Clears the current active goal without completing it.
     *
     * @return Result with success or GoalError
     */
    suspend fun clearActiveGoal(): Result<Unit>

    /**
     * Retrieves the history of completed goals.
     *
     * @return Flow of completed goals
     */
    fun getGoalHistory(): Flow<List<GoalHistory>>

    /**
     * Retrieves recently completed goals.
     *
     * @param limit Maximum number of goals to retrieve
     * @return Flow of recent goals
     */
    fun getRecentGoalHistory(limit: Int = 10): Flow<List<GoalHistory>>

    /**
     * Links a practice session to the active goal component.
     *
     * @param sessionId The ID of the practice session
     * @return Result with success or GoalError
     */
    suspend fun linkSessionToActiveGoal(sessionId: String): Result<Unit>

    /**
     * Gets all sessions linked to the active goal's current component.
     *
     * @return Flow of session IDs linked to active goal component
     */
    fun getSessionsForActiveGoalComponent(): Flow<List<String>>

    /**
     * Calculates goal completion statistics.
     *
     * @return Flow of statistics (total goals, completed, average completion time)
     */
    fun getGoalStatistics(): Flow<GoalStatistics>
}

/**
 * Statistics for goal completion.
 */
data class GoalStatistics(
    val totalGoals: Int,
    val completedGoals: Int,
    val activeGoal: Boolean,
    val averageCompletionTimeSeconds: Long,
    val totalAccuracy: Float,
)
