package dev.hossain.mathtutor.domain.model.goals

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * Represents a goal created by a parent for a child to work towards.
 * A goal consists of multiple components (operations or custom challenges) that the child must complete.
 *
 * @property id Unique identifier for this goal
 * @property title The title of the goal (e.g., "Math Master Challenge")
 * @property description Optional description of the goal
 * @property components List of goal components (operations or custom challenges) to complete
 * @property createdAt Timestamp when the goal was created
 * @property isArchived Whether the goal is archived (no longer active)
 */
@Parcelize
@Serializable
data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val components: List<GoalComponent> = emptyList(),
    @Contextual
    val createdAt: Instant = Instant.now(),
    val isArchived: Boolean = false,
) : Parcelable {
    /**
     * Returns the total number of sessions needed to complete this goal.
     */
    fun getTotalSessions(): Int = components.sumOf { it.sessionCount }

    /**
     * Returns a human-readable summary of components in this goal.
     */
    fun getComponentsSummary(): String = components.joinToString(", ") { it.getDescription() }
}
