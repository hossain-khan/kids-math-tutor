package dev.hossain.mathtutor.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.UUID

/**
 * Represents a single practice session for a custom challenge.
 *
 * This tracks a child's practice session including timing, problems attempted,
 * and performance metrics.
 *
 * @property sessionId Unique identifier for the session
 * @property startTime When the practice session started
 * @property endTime When the practice session ended (null if still in progress)
 * @property problemsAttempted Number of problems attempted in this session
 * @property correctAnswers Number of correct answers in this session
 * @property totalTimeMs Total time spent in milliseconds
 */
@Parcelize
data class ChallengePracticeSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val startTime: Instant,
    val endTime: Instant? = null,
    val problemsAttempted: Int,
    val correctAnswers: Int,
    val totalTimeMs: Long,
) : Parcelable {
    /**
     * Calculates the accuracy percentage for this session.
     *
     * @return Accuracy as a percentage (0-100), or 0 if no problems attempted
     */
    fun getAccuracy(): Float {
        if (problemsAttempted == 0) return 0f
        return (correctAnswers.toFloat() / problemsAttempted) * 100
    }

    /**
     * Checks if the session is complete.
     *
     * A session is considered complete when it has an end time.
     *
     * @return true if the session is complete, false otherwise
     */
    fun isComplete(): Boolean = endTime != null
}
