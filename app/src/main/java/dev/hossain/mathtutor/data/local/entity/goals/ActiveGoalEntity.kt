package dev.hossain.mathtutor.data.local.entity.goals

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity representing the currently active goal for a child.
 * There should only be one active goal at a time per app instance.
 *
 * @property id Unique identifier for this active goal instance
 * @property goalId Reference to the goal in goals_catalog table
 * @property activatedAt Timestamp when the goal was activated (stored as epoch milliseconds)
 * @property currentComponentIndex Index of the component currently being worked on
 * @property componentProgress JSON serialized list of ComponentProgress objects
 */
@Entity(tableName = "active_goals")
data class ActiveGoalEntity(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val activatedAt: Instant,
    val currentComponentIndex: Int = 0,
    val componentProgress: String, // JSON serialized List<ComponentProgress>
)
