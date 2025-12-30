package dev.hossain.mathtutor.domain.model.goals

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Final result of a completed component within a goal history entry.
 * Used when storing goal completion history for analytics.
 *
 * @property componentIndex Index of this component in the original goal
 * @property component The goal component definition
 * @property completedSessions Number of sessions completed (should equal total sessions)
 * @property totalSessions Total sessions required (should equal component's sessionCount)
 * @property overallAccuracy Overall accuracy across all sessions
 * @property totalTimeSeconds Total time spent on all sessions
 */
@Parcelize
@Serializable
data class ComponentResult(
    val componentIndex: Int,
    val component: GoalComponent,
    val completedSessions: Int,
    val totalSessions: Int,
    val overallAccuracy: Float,
    val totalTimeSeconds: Long,
) : Parcelable
