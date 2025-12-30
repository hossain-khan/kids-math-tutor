package dev.hossain.mathtutor.domain.model.goals

/**
 * Sealed class representing errors that can occur in the goals feature.
 * Used for proper error handling and reporting.
 * Each error type can be mapped to user-friendly messages in the UI.
 */
sealed class GoalError : Throwable() {
    /**
     * Goal data is invalid (e.g., empty title, invalid components).
     * User-friendly: "Please check your goal details and try again."
     */
    data class InvalidGoal(
        val errorMessage: String,
    ) : GoalError()

    /**
     * Requested goal was not found in the catalog.
     * User-friendly: "This goal is no longer available. It may have been deleted."
     */
    data class GoalNotFound(
        val goalId: String,
    ) : GoalError()

    /**
     * A goal component is invalid.
     * User-friendly: "There's an issue with one of the goal steps. Please try creating the goal again."
     */
    data class InvalidComponent(
        val reason: String,
    ) : GoalError()

    /**
     * An active goal already exists (can't activate another).
     * User-friendly: "You already have an active goal. Complete or pause it before starting a new one."
     */
    data class ActiveGoalExists(
        val goalId: String,
    ) : GoalError()

    /**
     * No active goal exists when one is expected.
     * User-friendly: "No active goal found. Create or activate a goal to get started."
     */
    data object NoActiveGoal : GoalError()

    /**
     * Requested practice session was not found.
     * User-friendly: "This practice session is no longer available. Please try again."
     */
    data class SessionNotFound(
        val sessionId: String,
    ) : GoalError()

    /**
     * Accuracy value is outside valid range (0-100).
     * User-friendly: "Invalid accuracy value. Please try the session again."
     */
    data class InvalidAccuracy(
        val accuracy: Float,
    ) : GoalError()

    /**
     * Generic database error occurred.
     * User-friendly: "We encountered a technical issue. Please try again later."
     * Recovery: Retry the operation, or sync the database if available
     */
    data object DatabaseError : GoalError()

    /**
     * Corrupted goal data detected in database.
     * User-friendly: "There's an issue with your goal data. Please try recreating this goal."
     * Recovery: Suggest user to archive/delete the corrupted goal and create a new one
     */
    data class CorruptedData(
        val goalId: String,
        val reason: String,
    ) : GoalError()

    /**
     * Goal data is missing expected fields (deserialization failure).
     * User-friendly: "This goal cannot be loaded. Please archive it and create a new one."
     * Recovery: Archive the goal, sync database
     */
    data class DataDeserializationError(
        val goalId: String,
        val fieldName: String,
    ) : GoalError()

    /**
     * Network or sync error when accessing cloud features.
     * User-friendly: "Check your internet connection and try again."
     * Recovery: Automatic retry, offline mode
     */
    data class SyncError(
        val reason: String,
    ) : GoalError()

    /**
     * User has reached limit (e.g., max concurrent goals).
     * User-friendly: "You've reached the maximum number of active goals."
     * Recovery: Complete or archive existing goals
     */
    data class LimitExceeded(
        val limitType: String,
        val currentCount: Int,
        val maxAllowed: Int,
    ) : GoalError()

    /**
     * Transient error that might be recoverable with retry.
     * User-friendly: "We encountered a temporary issue. Please try again."
     * Recovery: Automatic retry with exponential backoff
     */
    data class TransientError(
        val reason: String,
    ) : GoalError()

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
                is CorruptedData -> "Corrupted goal data: ${this.reason}"
                is DataDeserializationError -> "Failed to load goal data: ${this.fieldName} is missing"
                is SyncError -> "Sync error: ${this.reason}"
                is LimitExceeded -> "Limit exceeded for ${this.limitType}: ${this.currentCount}/${this.maxAllowed}"
                is TransientError -> "Temporary error: ${this.reason}"
            }

    /**
     * Returns whether this error is retryable.
     * Transient errors and sync errors should be retried.
     */
    fun isRetryable(): Boolean =
        when (this) {
            is TransientError, is SyncError -> true
            else -> false
        }

    /**
     * Returns a user-friendly error message suitable for display in the UI.
     */
    fun getUserMessage(): String =
        when (this) {
            is InvalidGoal -> "Please check your goal details and try again."
            is GoalNotFound -> "This goal is no longer available. It may have been deleted."
            is InvalidComponent -> "There's an issue with one of the goal steps. Please try creating the goal again."
            is ActiveGoalExists -> "You already have an active goal. Complete or pause it before starting a new one."
            NoActiveGoal -> "No active goal found. Create or activate a goal to get started."
            is SessionNotFound -> "This practice session is no longer available. Please try again."
            is InvalidAccuracy -> "Invalid accuracy value. Please try the session again."
            DatabaseError -> "We encountered a technical issue. Please try again later."
            is CorruptedData -> "There's an issue with your goal data. Please try recreating this goal."
            is DataDeserializationError -> "This goal cannot be loaded. Please archive it and create a new one."
            is SyncError -> "Check your internet connection and try again."
            is LimitExceeded -> "You've reached the maximum number of ${this.limitType}. Complete or archive existing goals."
            is TransientError -> "We encountered a temporary issue. Please try again."
        }

    /**
     * Returns the type of error for categorization.
     */
    fun getErrorType(): String = this::class.simpleName ?: "UnknownError"
}
