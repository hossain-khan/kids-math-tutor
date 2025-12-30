package dev.hossain.mathtutor.domain.model.goals

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Metadata for a single practice session completed within a goal component.
 * Tracks the session's performance metrics for analytics purposes.
 *
 * @property sessionId The ID of the practice session from PracticeSession
 * @property componentIndex Which component this session was for
 * @property accuracy Accuracy percentage for this session (0-100)
 * @property timeSeconds Duration of the session in seconds
 * @property completedAt Timestamp when the session was completed
 */
@Parcelize
@Serializable
data class SessionMetadata(
    val sessionId: String,
    val componentIndex: Int,
    val accuracy: Float,
    val timeSeconds: Long,
    @Contextual
    val completedAt: Instant,
) : Parcelable
