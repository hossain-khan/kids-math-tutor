package dev.hossain.mathtutor.data.local.entity.goals

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity representing a goal in the goal catalog.
 * Parents create goals and can reuse them multiple times by activating them for different children or time periods.
 *
 * @property id Unique identifier for this goal
 * @property title The title of the goal
 * @property description Optional description of the goal
 * @property components JSON serialized list of GoalComponent objects
 * @property createdAt Timestamp when the goal was created (stored as epoch milliseconds)
 * @property isArchived Whether the goal is archived (soft delete)
 */
@Entity(
    tableName = "goals_catalog",
    indices = [
        Index("createdAt"),
        Index("isArchived"),
        Index("isArchived", "createdAt"),
    ],
)
data class GoalEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val components: String, // JSON serialized List<GoalComponent>
    val createdAt: Instant,
    val isArchived: Boolean = false,
)
