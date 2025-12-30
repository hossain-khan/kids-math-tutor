package dev.hossain.mathtutor.analytics

import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.domain.model.goals.GoalHistory

/**
 * Analytics tracker for goal-related events and user interactions.
 * Tracks user engagement with the goals feature including creation, activation, progress, and completion.
 *
 * This abstraction allows goal tracking to be independent of the underlying analytics implementation
 * (e.g., Firebase Analytics). Implementations can be swapped without affecting the domain layer.
 */
interface GoalAnalyticsTracker {
    /**
     * Tracks when a user creates a new goal.
     * Called after successful goal creation in the database.
     *
     * @param goal The newly created goal
     * @param componentCount Number of components in the goal
     * @param componentTypes List of component types (e.g., "Addition", "Custom Challenge")
     */
    fun trackGoalCreated(
        goal: Goal,
        componentCount: Int,
        componentTypes: List<String>,
    )

    /**
     * Tracks when a user activates a goal for their child.
     * Called after successfully activating a goal.
     *
     * @param goalId ID of the activated goal
     * @param goalTitle Title of the goal
     * @param totalSessions Total number of sessions in the goal
     */
    fun trackGoalActivated(
        goalId: String,
        goalTitle: String,
        totalSessions: Int,
    )

    /**
     * Tracks when a user starts or resumes a specific component in a goal.
     * Called when the child begins practice on a component.
     *
     * @param goalId ID of the goal being practiced
     * @param componentIndex Index of the component (0-based)
     * @param componentDescription Description of the component (e.g., "Addition (2x Sessions)")
     */
    fun trackComponentStarted(
        goalId: String,
        componentIndex: Int,
        componentDescription: String,
    )

    /**
     * Tracks when a user completes a practice session for a goal component.
     * Called after a practice session is completed (e.g., 10 math problems).
     *
     * @param goalId ID of the goal
     * @param componentIndex Index of the component being practiced
     * @param accuracy Accuracy percentage for this session (0-100)
     * @param durationSeconds Duration of the session in seconds
     * @param problemsCompleted Number of problems completed in the session
     */
    fun trackSessionCompleted(
        goalId: String,
        componentIndex: Int,
        accuracy: Float,
        durationSeconds: Long,
        problemsCompleted: Int,
    )

    /**
     * Tracks when a user completes an entire goal.
     * Called after all components are finished and the goal is moved to history.
     *
     * @param goalHistory The completed goal history record
     * @param totalDaysActive Number of days from activation to completion
     * @param achievedAccuracy Overall accuracy achieved (0-100)
     * @param gameLevelsUnlocked Number of game levels/milestones unlocked by this goal
     */
    fun trackGoalCompleted(
        goalHistory: GoalHistory,
        totalDaysActive: Int,
        achievedAccuracy: Float,
        gameLevelsUnlocked: Int,
    )

    /**
     * Tracks when a game is blocked pending goal completion.
     * Called when user attempts to access a game that requires goal completion.
     *
     * @param gameType The type of game being blocked (e.g., "MATH_RACE", "MEMORY_MATCH")
     * @param goalId ID of the goal that blocks access
     * @param goalTitle Title of the goal
     * @param progressPercent Current progress on the blocking goal (0-100)
     */
    fun trackGameLocked(
        gameType: String,
        goalId: String,
        goalTitle: String,
        progressPercent: Float,
    )

    /**
     * Tracks when the session resume dialog is shown to the user.
     * Called when user has a paused goal session and can choose to resume.
     *
     * @param goalId ID of the goal with paused session
     * @param goalTitle Title of the goal
     * @param componentIndex Index of the component being resumed
     * @param minutesSincePause Minutes elapsed since the session was paused
     */
    fun trackResumeDialogShown(
        goalId: String,
        goalTitle: String,
        componentIndex: Int,
        minutesSincePause: Long,
    )

    /**
     * Tracks when user opts to resume a paused session.
     * Called after user confirms resuming from the resume dialog.
     *
     * @param goalId ID of the goal being resumed
     * @param componentIndex Index of the component being resumed
     */
    fun trackSessionResumed(
        goalId: String,
        componentIndex: Int,
    )

    /**
     * Tracks when user opts to start a new session instead of resuming.
     * Called after user chooses to restart from the resume dialog.
     *
     * @param goalId ID of the goal
     * @param componentIndex Index of the component being restarted
     */
    fun trackSessionRestarted(
        goalId: String,
        componentIndex: Int,
    )

    /**
     * Tracks when user deletes or archives a goal from the catalog.
     * Called after successful deletion from the database.
     *
     * @param goalId ID of the deleted goal
     * @param goalTitle Title of the goal
     * @param hadCompletions Whether this goal had any prior completions
     * @param completionCount Number of times the goal was completed before deletion
     */
    fun trackGoalDeleted(
        goalId: String,
        goalTitle: String,
        hadCompletions: Boolean,
        completionCount: Int,
    )

    /**
     * Tracks when goal achievement badges are earned.
     * Called when user reaches milestone in goal progress.
     *
     * @param badgeType Type of badge earned (e.g., "GOAL_COMPLETION", "ACCURACY_MASTER")
     * @param goalId ID of the goal that triggered the badge
     * @param goalTitle Title of the goal
     * @param badgeCount Total badges earned by this user (cumulative)
     */
    fun trackBadgeEarned(
        badgeType: String,
        goalId: String,
        goalTitle: String,
        badgeCount: Int,
    )
}
