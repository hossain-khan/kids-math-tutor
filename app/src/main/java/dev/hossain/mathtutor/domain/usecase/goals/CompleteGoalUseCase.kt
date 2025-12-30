package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import dev.hossain.mathtutor.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for completing the current active goal.
 * Verifies all components are completed, creates a history record, and clears the active goal.
 *
 * @property repository The goal repository
 */
class CompleteGoalUseCase
    @Inject
    constructor(
        private val repository: GoalRepository,
    ) {
        /**
         * Completes the current active goal and moves it to goal history.
         *
         * @return Result containing the completed GoalHistory or GoalError
         *
         * Validation:
         * - An active goal is in progress
         * - All components have been completed
         *
         * Side effects:
         * - Creates a history record with completion timestamp
         * - Calculates and stores overall accuracy and completion time
         * - Clears the active goal status
         * - Marks goal as ready for reuse
         */
        suspend operator fun invoke(): Result<GoalHistory> {
            // Delegate to repository to complete the active goal
            return repository.completeActiveGoal()
        }
    }
