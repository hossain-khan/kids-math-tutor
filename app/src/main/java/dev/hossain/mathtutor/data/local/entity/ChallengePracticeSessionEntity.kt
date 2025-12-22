package dev.hossain.mathtutor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity representing a practice session for a custom challenge.
 * Stores session data including timing, problems attempted, and performance metrics.
 *
 * @property sessionId Unique identifier for this session
 * @property challengeId Reference to the parent custom challenge
 * @property startTime When the practice session started (stored as epoch milliseconds)
 * @property endTime When the practice session ended (null if still in progress, stored as epoch milliseconds)
 * @property problemsAttempted Number of problems attempted in this session
 * @property correctAnswers Number of correct answers in this session
 * @property totalTimeMs Total time spent in milliseconds
 */
@Entity(
    tableName = "challenge_practice_sessions",
    foreignKeys = [
        ForeignKey(
            entity = CustomChallengeEntity::class,
            parentColumns = ["id"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["challengeId"])],
)
data class ChallengePracticeSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val challengeId: String,
    val startTime: Instant,
    val endTime: Instant?,
    val problemsAttempted: Int,
    val correctAnswers: Int,
    val totalTimeMs: Long,
)
