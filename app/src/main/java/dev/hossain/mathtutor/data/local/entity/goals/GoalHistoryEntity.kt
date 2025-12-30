package dev.hossain.mathtutor.data.local.entity.goals

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity representing a completed goal for history and analytics tracking.
 * Created when a child finishes all components of an active goal.
 *
 * @property id Unique identifier for this history entry
 * @property goalId Reference to the goal in goals_catalog table
 * @property goalTitle Copy of goal title at completion time (for display even if goal is deleted)
 * @property completedAt Timestamp when the goal was completed (stored as epoch milliseconds)
 * @property totalTimeSeconds Total time spent on all components in seconds
 * @property overallAccuracy Overall accuracy across all components (0-100)
 * @property componentResults JSON serialized list of ComponentResult objects
 */
@Entity(
    tableName = "goal_history",
    indices = [
        Index("goalId"),
        Index("completedAt"),
        Index("goalId", "completedAt"),
    ],
)
data class GoalHistoryEntity(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val goalTitle: String,
    val completedAt: Instant,
    val totalTimeSeconds: Long,
    val overallAccuracy: Float,
    val componentResults: String, // JSON serialized List<ComponentResult>
)
