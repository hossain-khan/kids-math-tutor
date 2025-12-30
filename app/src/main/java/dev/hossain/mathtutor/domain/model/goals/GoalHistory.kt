package dev.hossain.mathtutor.domain.model.goals

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * Records a completed goal for analytics and history tracking.
 * Created when a child finishes all components of an active goal.
 *
 * @property id Unique identifier for this history entry
 * @property goal The completed goal
 * @property completedAt Timestamp when the goal was completed
 * @property totalTimeSeconds Total time spent on all components
 * @property overallAccuracy Overall accuracy across all components
 * @property componentResults Results breakdown for each component
 */
@Parcelize
@Serializable
data class GoalHistory(
    val id: String = UUID.randomUUID().toString(),
    val goal: Goal,
    @Contextual
    val completedAt: Instant = Instant.now(),
    val totalTimeSeconds: Long,
    val overallAccuracy: Float,
    val componentResults: List<ComponentResult> = emptyList(),
) : Parcelable {
    /**
     * Returns the number of components in this goal.
     */
    fun getComponentCount(): Int = componentResults.size

    /**
     * Returns the total number of sessions completed.
     */
    fun getTotalSessionsCompleted(): Int = componentResults.sumOf { it.completedSessions }
}
