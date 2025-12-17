package dev.hossain.mathtutor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.hossain.mathtutor.domain.model.MathOperation
import java.time.Instant

/**
 * Room entity representing a completed practice session.
 * Stores summary statistics for each practice session to track progress over time.
 *
 * @property id Unique identifier for this session (auto-generated)
 * @property operation The type of math operation practiced (Addition, Subtraction, etc.)
 * @property totalProblems Total number of problems in this session
 * @property correctAnswers Number of problems answered correctly
 * @property incorrectAnswers Number of problems answered incorrectly
 * @property accuracy Calculated accuracy percentage (correctAnswers / totalProblems) * 100
 * @property durationSeconds Time spent completing the session in seconds
 * @property timestamp When the session was completed
 * @property gradeLevel Optional grade level (K=0, 1st=1, 2nd=2)
 */
@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operation: MathOperation,
    val totalProblems: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val accuracy: Float,
    val durationSeconds: Long,
    val timestamp: Instant,
    val gradeLevel: Int? = null,
)
