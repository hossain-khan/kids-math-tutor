package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.domain.repository.GoalRepository
import dev.hossain.mathtutor.domain.repository.GoalStatistics
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving goal completion analytics and statistics.
 * Aggregates data from goal history to provide insights into goal completion patterns.
 *
 * @property repository The goal repository
 */
class GetGoalAnalyticsUseCase
    @Inject
    constructor(
        private val repository: GoalRepository,
    ) {
        /**
         * Gets overall goal completion statistics.
         * Includes total goals, completed goals, active goal status, and aggregate metrics.
         *
         * @return Flow of GoalStatistics with aggregated analytics
         *
         * Statistics provided:
         * - totalGoals: Total number of goals in catalog
         * - completedGoals: Number of times any goal has been completed
         * - activeGoal: Whether a goal is currently in progress
         * - averageCompletionTimeSeconds: Average time to complete a goal
         * - totalAccuracy: Average accuracy across all completed goals
         *
         * Use cases:
         * - Display on dashboard showing child's progress
         * - Show achievement statistics
         * - Track trends over time
         */
        operator fun invoke(): Flow<GoalStatistics> = repository.getGoalStatistics()
    }
