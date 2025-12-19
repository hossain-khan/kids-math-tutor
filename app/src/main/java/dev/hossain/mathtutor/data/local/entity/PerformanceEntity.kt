package dev.hossain.mathtutor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import java.time.Instant

/**
 * Room entity representing a single problem performance record.
 * Each record tracks the result of a single problem attempt for performance analysis.
 *
 * @property id Unique identifier for this record (auto-generated)
 * @property operation The type of math operation for this problem
 * @property gradeLevel The grade level at which the problem was attempted
 * @property problemId The unique identifier of the problem attempted
 * @property isCorrect Whether the answer was correct
 * @property attemptCount Number of attempts the user made on this problem (for future use)
 * @property timeSpentSeconds Time in seconds spent on this problem
 * @property timestamp When this attempt was recorded
 */
@Entity(tableName = "performance_records")
data class PerformanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operation: MathOperation,
    val gradeLevel: GradeLevel,
    val problemId: String,
    val isCorrect: Boolean,
    val attemptCount: Int = 1,
    val timeSpentSeconds: Long,
    val timestamp: Instant,
)
