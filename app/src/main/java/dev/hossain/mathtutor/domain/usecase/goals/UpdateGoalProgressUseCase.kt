package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for updating progress on the current component of an active goal.
 * Validates progress metrics and delegates to the repository for persistence.
 *
 * @property repository The goal repository
 */
class UpdateGoalProgressUseCase
    @Inject
    constructor(
        private val repository: GoalRepository,
    ) {
        /**
         * Updates the progress of a component in the active goal.
         * Automatically moves to the next component if current component is completed.
         *
         * @param componentIndex The index of the component being updated
         * @param completedSessions Number of sessions completed for this component
         * @param accuracy The accuracy percentage (0-100)
         * @param timeSeconds Total time spent in seconds
         * @return Result containing the updated ActiveGoal or GoalError
         *
         * Validation:
         * - An active goal exists
         * - Component index is valid (within goal's component range)
         * - Sessions count is positive
         * - Accuracy is between 0 and 100
         * - Time is non-negative
         */
        suspend operator fun invoke(
            componentIndex: Int,
            completedSessions: Int,
            accuracy: Float,
            timeSeconds: Long,
        ): Result<ActiveGoal> {
            // Validate inputs
            if (componentIndex < 0) {
                return Result.failure(GoalError.InvalidComponent("Component index cannot be negative"))
            }

            if (completedSessions < 0) {
                return Result.failure(GoalError.InvalidComponent("Completed sessions cannot be negative"))
            }

            if (accuracy < 0f || accuracy > 100f) {
                return Result.failure(GoalError.InvalidAccuracy(accuracy))
            }

            if (timeSeconds < 0) {
                return Result.failure(GoalError.InvalidComponent("Time cannot be negative"))
            }

            // Delegate to repository
            return repository.updateComponentProgress(
                componentIndex = componentIndex,
                completedSessions = completedSessions,
                accuracy = accuracy,
                timeSeconds = timeSeconds,
            )
        }
    }
