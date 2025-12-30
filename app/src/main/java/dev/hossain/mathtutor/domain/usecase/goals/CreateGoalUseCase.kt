package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for creating a new goal.
 * Validates input parameters and delegates to the repository for persistence.
 *
 * @property repository The goal repository
 */
class CreateGoalUseCase
    @Inject
    constructor(
        private val repository: GoalRepository,
    ) {
        /**
         * Creates a new goal with the given parameters.
         *
         * @param title The title of the goal (required, max 100 chars)
         * @param description Optional description of the goal
         * @param components List of goal components (required, at least one)
         * @return Result containing the created Goal or GoalError
         *
         * Validation:
         * - Title is not empty and less than 100 characters
         * - At least one component provided
         * - Each component has valid session count (1-10)
         */
        suspend operator fun invoke(
            title: String,
            description: String = "",
            components: List<GoalComponent>,
        ): Result<Goal> {
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

            // Validate each component
            for (component in components) {
                if (component.sessionCount < 1 || component.sessionCount > 10) {
                    return Result.failure(
                        GoalError.InvalidComponent(
                            "Component session count must be between 1 and 10, got ${component.sessionCount}",
                        ),
                    )
                }
            }

            // Delegate to repository
            return repository.createGoal(title, description, components)
        }
    }
