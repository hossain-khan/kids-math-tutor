package dev.hossain.mathtutor.domain.model.goals

/**
 * Sealed class representing errors that can occur in the goals feature.
 * Used for proper error handling and reporting.
 */
sealed class GoalError : Throwable() {
    /**
     * Goal data is invalid (e.g., empty title, invalid components).
     */
    data class InvalidGoal(
        val errorMessage: String,
    ) : GoalError()

    /**
     * Requested goal was not found in the catalog.
     */
    data class GoalNotFound(
        val goalId: String,
    ) : GoalError()

    /**
     * A goal component is invalid.
     */
    data class InvalidComponent(
        val reason: String,
    ) : GoalError()

    /**
     * An active goal already exists (can't activate another).
     */
    data class ActiveGoalExists(
        val goalId: String,
    ) : GoalError()

    /**
     * No active goal exists when one is expected.
     */
    data object NoActiveGoal : GoalError()

    /**
     * Requested practice session was not found.
     */
    data class SessionNotFound(
        val sessionId: String,
    ) : GoalError()

    /**
     * Accuracy value is outside valid range (0-100).
     */
    data class InvalidAccuracy(
        val accuracy: Float,
    ) : GoalError()

    /**
     * Generic database error occurred.
     */
    data object DatabaseError : GoalError()

    override val message: String
        get() =
            when (this) {
                is InvalidGoal -> this.errorMessage
                is GoalNotFound -> "Goal not found: ${this.goalId}"
                is InvalidComponent -> "Invalid component: ${this.reason}"
                is ActiveGoalExists -> "Active goal already exists: ${this.goalId}"
                NoActiveGoal -> "No active goal found"
                is SessionNotFound -> "Session not found: ${this.sessionId}"
                is InvalidAccuracy -> "Invalid accuracy: ${this.accuracy}. Expected 0-100."
                DatabaseError -> "Database error occurred"
            }
}
