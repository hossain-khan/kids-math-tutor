package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.analytics.GoalAnalyticsTracker
import dev.hossain.mathtutor.domain.model.goals.ActiveGoal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for updating progress on the current component of an active goal.
 * Validates progress metrics, tracks analytics, and delegates to the repository for persistence.
 *
 * @property repository The goal repository
 * @property analyticsTracker The analytics tracker for logging goal events
 */
class UpdateGoalProgressUseCase
    @Inject
    constructor(
        private val repository: GoalRepository,
        private val analyticsTracker: GoalAnalyticsTracker,
    ) {
        /**
         * Updates the progress of a component in the active goal.
         * Automatically moves to the next component if current component is completed.
         *
         * @param componentIndex The index of the component being updated
         * @param completedSessions Number of sessions completed for this component
         * @param accuracy The accuracy percentage (0-100)
         * @param timeSeconds Total time spent in seconds
         * @param problemsCompleted Number of problems completed in this session (for analytics)
         * @return Result containing the updated ActiveGoal or GoalError
         *
         * Validation:
         * - An active goal exists
         * - Component index is valid (within goal's component range)
         * - Sessions count is positive
         * - Accuracy is between 0 and 100
         * - Time is non-negative
         *
         * Side effects:
         * - Tracks session completion with accuracy and duration
         */
        suspend operator fun invoke(
            componentIndex: Int,
            completedSessions: Int,
            accuracy: Float,
            timeSeconds: Long,
            problemsCompleted: Int = 10,
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
            val result =
                repository.updateComponentProgress(
                    componentIndex = componentIndex,
                    completedSessions = completedSessions,
                    accuracy = accuracy,
                    timeSeconds = timeSeconds,
                )

            // Track session completion
            if (result.isSuccess) {
                val activeGoal = result.getOrNull()
                if (activeGoal != null && componentIndex < activeGoal.goal.components.size) {
                    val component = activeGoal.goal.components[componentIndex]
                    val componentDescription =
                        when (component) {
                            is GoalComponent.OperationBased -> component.getDescription()
                            is GoalComponent.CustomChallengeBased -> component.getDescription()
                        }

                    analyticsTracker.trackSessionCompleted(
                        goalId = activeGoal.goalId,
                        componentIndex = componentIndex,
                        accuracy = accuracy,
                        durationSeconds = timeSeconds,
                        problemsCompleted = problemsCompleted,
                    )
                }
            }

            return result
        }
    }
