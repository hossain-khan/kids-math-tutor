package dev.hossain.mathtutor.domain.usecase.goals

import dev.hossain.mathtutor.analytics.GoalAnalyticsTracker
import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.GoalError
import dev.hossain.mathtutor.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for creating a new goal.
 * Validates input parameters, delegates to the repository for persistence, and tracks analytics.
 *
 * @property repository The goal repository
 * @property analyticsTracker The analytics tracker for logging goal events
 */
class CreateGoalUseCase
    @Inject
    constructor(
        private val repository: GoalRepository,
        private val analyticsTracker: GoalAnalyticsTracker,
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
         *
         * Side effects:
         * - On success, tracks goal creation event with analytics
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
            val result = repository.createGoal(title, description, components)

            // Track successful creation
            if (result.isSuccess) {
                val goal = result.getOrNull()
                if (goal != null) {
                    val componentTypes = components.map { component ->
                        when (component) {
                            is GoalComponent.OperationBased -> component.operation.displayName
                            is GoalComponent.CustomChallengeBased -> "Custom: ${component.challengeTitle}"
                        }
                    }
                    analyticsTracker.trackGoalCreated(
                        goal = goal,
                        componentCount = components.size,
                        componentTypes = componentTypes,
                    )
                }
            }

            return result
        }
    }
