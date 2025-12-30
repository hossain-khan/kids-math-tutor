package dev.hossain.mathtutor.domain.model.goals

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * Represents the currently active goal for a child.
 * Tracks the child's progress through the goal's components.
 *
 * @property id Unique identifier for this active goal instance
 * @property goalId Reference to the goal in the goal catalog
 * @property goal The full goal object (for easy access to title, components, etc.)
 * @property activatedAt Timestamp when the goal was activated
 * @property currentComponentIndex Index of the component currently being worked on
 * @property componentProgress List of progress tracking for each component
 * @property isCompleted Whether the goal has been completed
 */
@Parcelize
@Serializable
data class ActiveGoal(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val goal: Goal,
    @Contextual
    val activatedAt: Instant = Instant.now(),
    val currentComponentIndex: Int = 0,
    val componentProgress: List<ComponentProgress> = emptyList(),
    val isCompleted: Boolean = false,
) : Parcelable {
    /**
     * Returns the current component being worked on.
     */
    fun getCurrentComponent(): GoalComponent? {
        if (currentComponentIndex >= goal.components.size) return null
        return goal.components[currentComponentIndex]
    }

    /**
     * Returns the progress for the current component.
     */
    fun getCurrentComponentProgress(): ComponentProgress? = componentProgress.find { it.componentIndex == currentComponentIndex }

    /**
     * Returns total sessions completed across all components.
     */
    fun getTotalCompletedSessions(): Int = componentProgress.sumOf { it.completedSessions }

    /**
     * Returns total sessions required for the entire goal.
     */
    fun getTotalSessions(): Int = goal.getTotalSessions()

    /**
     * Returns overall progress as a fraction (0.0 to 1.0).
     */
    fun getOverallProgress(): Float {
        val total = getTotalSessions()
        if (total == 0) return 0f
        return getTotalCompletedSessions().toFloat() / total
    }

    /**
     * Returns overall accuracy across all components.
     */
    fun getOverallAccuracy(): Float {
        if (componentProgress.isEmpty()) return 0f
        val totalAccuracy = componentProgress.sumOf { it.accuracy.toDouble() * it.completedSessions }
        val totalSessions = componentProgress.sumOf { it.completedSessions }
        if (totalSessions == 0) return 0f
        return (totalAccuracy / totalSessions).toFloat()
    }
}
