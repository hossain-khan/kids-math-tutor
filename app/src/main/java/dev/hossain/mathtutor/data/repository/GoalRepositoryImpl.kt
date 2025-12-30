package dev.hossain.mathtutor.data.repository

import dev.hossain.mathtutor.data.local.dao.goals.ActiveGoalDao
import dev.hossain.mathtutor.data.local.dao.goals.GoalHistoryDao
import dev.hossain.mathtutor.data.local.dao.goals.GoalsDao
import dev.hossain.mathtutor.data.local.dao.goals.PracticeSessionToGoalDao
import dev.hossain.mathtutor.data.local.entity.goals.ActiveGoalEntity
import dev.hossain.mathtutor.data.local.entity.goals.GoalEntity
import dev.hossain.mathtutor.data.local.entity.goals.GoalHistoryEntity
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.ComponentProgress
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import dev.hossain.mathtutor.domain.repository.GoalRepository
import dev.hossain.mathtutor.domain.repository.GoalStatistics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * Implementation of GoalRepository interface.
 * Provides data access for goal catalog, active goals, and goal history.
 *
 * @property goalsDao Data access object for goal catalog
 * @property activeGoalDao Data access object for active goals
 * @property goalHistoryDao Data access object for goal history
 * @property practiceSessionToGoalDao Data access object for session-to-goal mapping
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class GoalRepositoryImpl(
    private val goalsDao: GoalsDao,
    private val activeGoalDao: ActiveGoalDao,
    private val goalHistoryDao: GoalHistoryDao,
    private val practiceSessionToGoalDao: PracticeSessionToGoalDao,
) : GoalRepository {
    override suspend fun createGoal(
        title: String,
        description: String,
        components: List<GoalComponent>,
    ): Result<Goal> {
        return try {
            // Validate inputs
            if (title.isBlank()) {
                return Result.failure(GoalError.InvalidGoal("Title cannot be empty"))
            }
            if (title.length > 100) {
                return Result.failure(GoalError.InvalidGoal("Title must be less than 100 characters"))
            }
            if (components.isEmpty()) {
                return Result.failure(GoalError.InvalidGoal("Goal must have at least one component"))
            }

            // Validate components
            for (component in components) {
                if (component.sessionCount < 1 || component.sessionCount > 10) {
                    return Result.failure(
                        GoalError.InvalidComponent(
                            "Component session count must be between 1 and 10, got ${component.sessionCount}",
                        ),
                    )
                }
            }

            // Create goal
            val goal =
                Goal(
                    title = title,
                    description = description,
                    components = components,
                )

            // Create entity and save
            val entity = goal.toEntity()
            goalsDao.insert(entity)

            Result.success(goal)
        } catch (e: Exception) {
            Result.failure(GoalError.DatabaseError)
        }
    }

    override fun getAllGoals(): Flow<List<Goal>> =
        goalsDao.getAllGoals().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getGoalById(goalId: String): Flow<Goal?> =
        goalsDao.getAllGoals().map { entities ->
            entities.find { it.id == goalId }?.toDomain()
        }

    override suspend fun archiveGoal(goalId: String): Result<Unit> {
        return try {
            // Verify goal exists
            val goal =
                goalsDao.getGoalById(goalId)
                    ?: return Result.failure(GoalError.GoalNotFound(goalId))

            // Check if this goal is currently active and clear it if so
            val activeGoal = activeGoalDao.getActiveGoalById(goalId)
            if (activeGoal != null) {
                activeGoalDao.delete(activeGoal)
            }

            goalsDao.archiveGoal(goalId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(GoalError.DatabaseError)
        }
    }

    override suspend fun activateGoal(goalId: String): Result<ActiveGoal> {
        return try {
            // Check if goal exists
            val goalEntity =
                goalsDao.getGoalById(goalId)
                    ?: return Result.failure(GoalError.GoalNotFound(goalId))

            // Check if another active goal exists
            if (activeGoalDao.getActiveGoalCount() > 0) {
                return Result.failure(
                    GoalError.ActiveGoalExists(goalId),
                )
            }

            // Create active goal with initialized component progress
            val goal = goalEntity.toDomain()
            val initialProgress =
                goal.components.mapIndexed { index, component ->
                    ComponentProgress(
                        componentIndex = index,
                        completedSessions = 0,
                        totalSessions = component.sessionCount,
                        accuracy = 0f,
                        totalTimeSeconds = 0L,
                    )
                }

            val activeGoal =
                ActiveGoal(
                    id = goalId,
                    goalId = goalId,
                    goal = goal,
                    currentComponentIndex = 0,
                    componentProgress = initialProgress,
                )

            // Save to database
            val entity = activeGoal.toEntity()
            activeGoalDao.insert(entity)

            Result.success(activeGoal)
        } catch (e: Exception) {
            Result.failure(GoalError.DatabaseError)
        }
    }

    override fun getActiveGoal(): Flow<ActiveGoal?> =
        activeGoalDao.getActiveGoal().mapNotNull { entity ->
            if (entity == null) return@mapNotNull null
            val goal =
                goalsDao.getGoalById(entity.goalId)?.toDomain()
                    ?: return@mapNotNull null
            entity.toDomain(goal)
        }

    override suspend fun updateComponentProgress(
        componentIndex: Int,
        completedSessions: Int,
        accuracy: Float,
        timeSeconds: Long,
    ): Result<ActiveGoal> {
        return try {
            // Validate accuracy
            if (accuracy < 0f || accuracy > 100f) {
                return Result.failure(GoalError.InvalidAccuracy(accuracy))
            }

            // Get the current active goal
            val activeGoalEntity =
                activeGoalDao.getActiveGoalSync()
                    ?: return Result.failure(GoalError.NoActiveGoal)

            // Deserialize current component progress
            val currentProgress: MutableList<ComponentProgress> =
                try {
                    Json.decodeFromString(
                        ListSerializer(ComponentProgress.serializer()),
                        activeGoalEntity.componentProgress,
                    ) as MutableList<ComponentProgress>
                } catch (e: Exception) {
                    return Result.failure(GoalError.DatabaseError)
                }

            // Validate component index
            if (componentIndex < 0 || componentIndex >= currentProgress.size) {
                return Result.failure(GoalError.InvalidComponent("Invalid component index"))
            }

            // Update the component progress
            val componentProgress = currentProgress[componentIndex]
            val updatedProgress =
                componentProgress.copy(
                    completedSessions = componentProgress.completedSessions + completedSessions,
                    accuracy =
                        if (componentProgress.completedSessions == 0) {
                            accuracy
                        } else {
                            // Calculate weighted average
                            (componentProgress.accuracy * componentProgress.completedSessions + accuracy * completedSessions) /
                                (componentProgress.completedSessions + completedSessions)
                        },
                    totalTimeSeconds = componentProgress.totalTimeSeconds + timeSeconds,
                )
            currentProgress[componentIndex] = updatedProgress

            // Serialize updated progress
            val updatedProgressJson =
                Json.encodeToString(
                    ListSerializer(ComponentProgress.serializer()),
                    currentProgress,
                )

            // Update the database
            activeGoalDao.updateComponentProgress(
                activeGoalId = activeGoalEntity.id,
                componentIndex = componentIndex,
                componentProgress = updatedProgressJson,
            )

            // Return updated active goal with the goal information
            val updatedEntity =
                activeGoalDao.getActiveGoalById(activeGoalEntity.id)
                    ?: return Result.failure(GoalError.DatabaseError)

            // Fetch the goal to get full details
            val goalEntity =
                goalsDao.getGoalById(updatedEntity.goalId)
                    ?: return Result.failure(GoalError.InvalidGoal("Goal not found"))

            Result.success(updatedEntity.toDomain(goalEntity.toDomain()))
        } catch (e: Exception) {
            Result.failure(GoalError.DatabaseError)
        }
    }

    override suspend fun completeActiveGoal(): Result<GoalHistory> {
        return try {
            // Get active goal
            val activeGoalCount = activeGoalDao.getActiveGoalCount()
            if (activeGoalCount == 0) {
                return Result.failure(GoalError.NoActiveGoal)
            }

            // For now, return error - proper Flow handling needed
            Result.failure(GoalError.DatabaseError)
        } catch (e: Exception) {
            Result.failure(GoalError.DatabaseError)
        }
    }

    override suspend fun clearActiveGoal(): Result<Unit> =
        try {
            activeGoalDao.clearActiveGoal()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(GoalError.DatabaseError)
        }

    override fun getGoalHistory(): Flow<List<GoalHistory>> =
        goalHistoryDao.getAllHistory().mapNotNull { entities ->
            entities.mapNotNull { entity ->
                val goal =
                    goalsDao.getGoalById(entity.goalId)?.toDomain()
                        ?: return@mapNotNull null
                entity.toDomain(goal)
            }
        }

    override fun getRecentGoalHistory(limit: Int): Flow<List<GoalHistory>> =
        goalHistoryDao.getRecentHistory(limit).mapNotNull { entities ->
            entities.mapNotNull { entity ->
                val goal =
                    goalsDao.getGoalById(entity.goalId)?.toDomain()
                        ?: return@mapNotNull null
                entity.toDomain(goal)
            }
        }

    override suspend fun linkSessionToActiveGoal(sessionId: String): Result<Unit> {
        return try {
            // Get current active goal
            val activeGoalCount = activeGoalDao.getActiveGoalCount()
            if (activeGoalCount == 0) {
                return Result.failure(GoalError.NoActiveGoal)
            }

            // Link session to active goal
            // Implementation depends on PracticeSessionToGoalDao structure
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(GoalError.DatabaseError)
        }
    }

    override fun getSessionsForActiveGoalComponent(): Flow<List<String>> {
        // Implementation depends on dao structure
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }

    override fun getGoalStatistics(): Flow<GoalStatistics> =
        kotlinx.coroutines.flow.flow {
            try {
                val totalGoals = goalsDao.getActiveGoalCount()
                val hasActiveGoal = activeGoalDao.getActiveGoalCount() > 0

                emit(
                    GoalStatistics(
                        totalGoals = totalGoals,
                        completedGoals = 0,
                        activeGoal = hasActiveGoal,
                        averageCompletionTimeSeconds = 0L,
                        totalAccuracy = 0f,
                    ),
                )
            } catch (e: Exception) {
                // Emit default statistics on error
                emit(
                    GoalStatistics(
                        totalGoals = 0,
                        completedGoals = 0,
                        activeGoal = false,
                        averageCompletionTimeSeconds = 0L,
                        totalAccuracy = 0f,
                    ),
                )
            }
        }

    // Helper conversion functions
    private fun Goal.toEntity(): GoalEntity =
        GoalEntity(
            id = id,
            title = title,
            description = description,
            components =
                Json.encodeToString(
                    ListSerializer(GoalComponent.serializer()),
                    components,
                ),
            createdAt = createdAt,
            isArchived = isArchived,
        )

    private fun GoalEntity.toDomain(): Goal =
        Goal(
            id = id,
            title = title,
            description = description,
            components =
                Json.decodeFromString(
                    ListSerializer(GoalComponent.serializer()),
                    components,
                ),
            createdAt = createdAt,
            isArchived = isArchived,
        )

    private fun ActiveGoal.toEntity(): ActiveGoalEntity =
        ActiveGoalEntity(
            id = id,
            goalId = goalId,
            currentComponentIndex = currentComponentIndex,
            componentProgress =
                Json.encodeToString(
                    ListSerializer(ComponentProgress.serializer()),
                    componentProgress,
                ),
            activatedAt = Instant.now(),
        )

    private fun ActiveGoalEntity.toDomain(goal: Goal): ActiveGoal =
        ActiveGoal(
            id = id,
            goalId = goalId,
            goal = goal,
            currentComponentIndex = currentComponentIndex,
            componentProgress =
                Json.decodeFromString(
                    ListSerializer(ComponentProgress.serializer()),
                    componentProgress,
                ),
        )

    private fun GoalHistoryEntity.toDomain(goal: Goal): GoalHistory =
        GoalHistory(
            id = id,
            goal = goal,
            completedAt = completedAt,
            totalTimeSeconds = totalTimeSeconds,
            overallAccuracy = overallAccuracy,
            componentResults = emptyList(), // TODO: decode from componentResults JSON
        )
}
