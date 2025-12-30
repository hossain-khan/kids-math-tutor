package dev.hossain.mathtutor.analytics

import dev.hossain.mathtutor.domain.model.goals.Goal
import dev.hossain.mathtutor.domain.model.goals.GoalHistory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import timber.log.Timber

/**
 * Implementation of GoalAnalyticsTracker using the existing AnalyticsService.
 * Converts goal-specific events into generic analytics events for tracking.
 *
 * @property analyticsService The underlying analytics service (Firebase, etc.)
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class GoalAnalyticsTrackerImpl(
    private val analyticsService: AnalyticsService,
) : GoalAnalyticsTracker {
    override fun trackGoalCreated(
        goal: Goal,
        componentCount: Int,
        componentTypes: List<String>,
    ) {
        try {
            analyticsService.logEvent(
                eventName = "goal_created",
                parameters =
                    mapOf(
                        "goal_id" to goal.id,
                        "goal_title" to goal.title,
                        "component_count" to componentCount,
                        "component_types" to componentTypes.joinToString(","),
                        "has_description" to (goal.description != null),
                    ),
            )
            Timber.d("Tracked goal creation: ${goal.title} with $componentCount components")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track goal creation")
        }
    }

    override fun trackGoalActivated(
        goalId: String,
        goalTitle: String,
        totalSessions: Int,
    ) {
        try {
            analyticsService.logEvent(
                eventName = "goal_activated",
                parameters =
                    mapOf(
                        "goal_id" to goalId,
                        "goal_title" to goalTitle,
                        "total_sessions" to totalSessions,
                    ),
            )
            Timber.d("Tracked goal activation: $goalTitle")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track goal activation")
        }
    }

    override fun trackComponentStarted(
        goalId: String,
        componentIndex: Int,
        componentDescription: String,
    ) {
        try {
            analyticsService.logEvent(
                eventName = "goal_component_started",
                parameters =
                    mapOf(
                        "goal_id" to goalId,
                        "component_index" to componentIndex,
                        "component_description" to componentDescription,
                    ),
            )
            Timber.d("Tracked component start: $componentDescription (index=$componentIndex)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track component start")
        }
    }

    override fun trackSessionCompleted(
        goalId: String,
        componentIndex: Int,
        accuracy: Float,
        durationSeconds: Long,
        problemsCompleted: Int,
    ) {
        try {
            analyticsService.logEvent(
                eventName = "goal_session_completed",
                parameters =
                    mapOf(
                        "goal_id" to goalId,
                        "component_index" to componentIndex,
                        "accuracy" to "%.1f".format(accuracy),
                        "duration_seconds" to durationSeconds,
                        "problems_completed" to problemsCompleted,
                    ),
            )
            Timber.d("Tracked session completion: $accuracy% accuracy, ${durationSeconds}s duration")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track session completion")
        }
    }

    override fun trackGoalCompleted(
        goalHistory: GoalHistory,
        totalDaysActive: Int,
        achievedAccuracy: Float,
        gameLevelsUnlocked: Int,
    ) {
        try {
            analyticsService.logEvent(
                eventName = "goal_completed",
                parameters =
                    mapOf(
                        "goal_id" to goalHistory.goalId,
                        "goal_title" to goalHistory.goalTitle,
                        "total_days_active" to totalDaysActive,
                        "achieved_accuracy" to "%.1f".format(achievedAccuracy),
                        "game_levels_unlocked" to gameLevelsUnlocked,
                        "total_time_seconds" to goalHistory.totalTimeSeconds,
                    ),
            )
            Timber.d("Tracked goal completion: ${goalHistory.goalTitle} with $achievedAccuracy% accuracy")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track goal completion")
        }
    }

    override fun trackGameLocked(
        gameType: String,
        goalId: String,
        goalTitle: String,
        progressPercent: Float,
    ) {
        try {
            analyticsService.logEvent(
                eventName = "game_locked_by_goal",
                parameters =
                    mapOf(
                        "game_type" to gameType,
                        "blocking_goal_id" to goalId,
                        "blocking_goal_title" to goalTitle,
                        "goal_progress_percent" to "%.1f".format(progressPercent),
                    ),
            )
            Timber.d("Tracked game lock: $gameType blocked by goal ($progressPercent% progress)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track game locked event")
        }
    }

    override fun trackResumeDialogShown(
        goalId: String,
        goalTitle: String,
        componentIndex: Int,
        minutesSincePause: Long,
    ) {
        try {
            analyticsService.logEvent(
                eventName = "goal_resume_dialog_shown",
                parameters =
                    mapOf(
                        "goal_id" to goalId,
                        "goal_title" to goalTitle,
                        "component_index" to componentIndex,
                        "minutes_since_pause" to minutesSincePause,
                    ),
            )
            Timber.d("Tracked resume dialog shown for $goalTitle (paused ${minutesSincePause}m ago)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track resume dialog shown event")
        }
    }

    override fun trackSessionResumed(
        goalId: String,
        componentIndex: Int,
    ) {
        try {
            analyticsService.logEvent(
                eventName = "goal_session_resumed",
                parameters =
                    mapOf(
                        "goal_id" to goalId,
                        "component_index" to componentIndex,
                    ),
            )
            Timber.d("Tracked session resume for goal (component=$componentIndex)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track session resumed event")
        }
    }

    override fun trackSessionRestarted(
        goalId: String,
        componentIndex: Int,
    ) {
        try {
            analyticsService.logEvent(
                eventName = "goal_session_restarted",
                parameters =
                    mapOf(
                        "goal_id" to goalId,
                        "component_index" to componentIndex,
                    ),
            )
            Timber.d("Tracked session restart for goal (component=$componentIndex)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track session restarted event")
        }
    }

    override fun trackGoalDeleted(
        goalId: String,
        goalTitle: String,
        hadCompletions: Boolean,
        completionCount: Int,
    ) {
        try {
            analyticsService.logEvent(
                eventName = "goal_deleted",
                parameters =
                    mapOf(
                        "goal_id" to goalId,
                        "goal_title" to goalTitle,
                        "had_completions" to hadCompletions,
                        "completion_count" to completionCount,
                    ),
            )
            Timber.d("Tracked goal deletion: $goalTitle ($completionCount completions)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track goal deletion")
        }
    }

    override fun trackBadgeEarned(
        badgeType: String,
        goalId: String,
        goalTitle: String,
        badgeCount: Int,
    ) {
        try {
            analyticsService.logEvent(
                eventName = "goal_badge_earned",
                parameters =
                    mapOf(
                        "badge_type" to badgeType,
                        "goal_id" to goalId,
                        "goal_title" to goalTitle,
                        "total_badges" to badgeCount,
                    ),
            )
            Timber.d("Tracked badge earned: $badgeType for $goalTitle (total=$badgeCount)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track badge earned")
        }
    }
}
