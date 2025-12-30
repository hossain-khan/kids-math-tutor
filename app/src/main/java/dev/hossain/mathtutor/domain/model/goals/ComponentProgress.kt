package dev.hossain.mathtutor.domain.model.goals

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Tracks the progress of a specific component within an active goal.
 *
 * @property componentIndex The index of this component in the parent goal's components list
 * @property completedSessions Number of sessions completed for this component
 * @property totalSessions Total sessions required to complete this component
 * @property accuracy Overall accuracy across all completed sessions (0-100)
 * @property totalTimeSeconds Total time spent on all sessions for this component
 * @property sessionResults List of individual session results
 */
@Parcelize
@Serializable
data class ComponentProgress(
    val componentIndex: Int,
    val completedSessions: Int = 0,
    val totalSessions: Int,
    val accuracy: Float = 0f,
    val totalTimeSeconds: Long = 0L,
    val sessionResults: List<SessionMetadata> = emptyList(),
) : Parcelable {
    /**
     * Returns true if this component is fully completed.
     */
    fun isComplete(): Boolean = completedSessions >= totalSessions

    /**
     * Returns progress as a fraction (0.0 to 1.0).
     */
    fun getProgressFraction(): Float {
        if (totalSessions == 0) return 0f
        return completedSessions.toFloat() / totalSessions
    }

    /**
     * Returns average time per session in seconds, or 0 if no sessions.
     */
    fun getAverageTimePerSession(): Long {
        if (completedSessions == 0) return 0L
        return totalTimeSeconds / completedSessions
    }
}
