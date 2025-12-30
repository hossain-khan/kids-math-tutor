package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.analytics.GoalAnalyticsTracker
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for activating a goal.
 * Checks if a goal exists, verifies no other active goal is in progress, activates the goal, and tracks analytics.
 *
 * @property repository The goal repository
 * @property analyticsTracker The analytics tracker for logging goal events
 */
class ActivateGoalUseCase
    @Inject
    constructor(
        private val repository: GoalRepository,
        private val analyticsTracker: GoalAnalyticsTracker,
    ) {
        /**
         * Activates a goal for a child to start practicing.
         *
         * @param goalId The ID of the goal to activate
         * @return Result containing the activated ActiveGoal or GoalError
         *
         * Validation:
         * - Goal with given ID exists
         * - No other active goal is currently in progress
         *
         * Side effects:
         * - Initializes component progress tracking
         * - Records goal start timestamp
         * - Tracks goal activation event with analytics
         */
        suspend operator fun invoke(goalId: String): Result<ActiveGoal> {
            // Validate goalId is not empty
            if (goalId.isBlank()) {
                return Result.failure(GoalError.InvalidGoal("Goal ID cannot be empty"))
            }

            // Delegate to repository to check goal exists and activate
            val result = repository.activateGoal(goalId)

            // Track successful activation
            if (result.isSuccess) {
                val activeGoal = result.getOrNull()
                if (activeGoal != null) {
                    analyticsTracker.trackGoalActivated(
                        goalId = activeGoal.goalId,
                        goalTitle = activeGoal.goal.title,
                        totalSessions = activeGoal.goal.getTotalSessions(),
                    )
                }
            }

            return result
        }
    }
