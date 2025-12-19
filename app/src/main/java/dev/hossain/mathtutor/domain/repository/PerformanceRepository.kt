package dev.hossain.mathtutor.domain.repository

import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.OperationPerformance
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for performance data management.
 * Provides methods to record and retrieve performance data for adaptive difficulty tracking.
 */
interface PerformanceRepository {
    /**
     * Records a single problem attempt for performance tracking.
     *
     * @param operation The math operation practiced
     * @param gradeLevel The grade level at which the problem was attempted
     * @param problemId Unique identifier of the problem
     * @param isCorrect Whether the answer was correct
     * @param timeSpentSeconds Time spent on the problem in seconds
     * @return The ID of the inserted record
     */
    suspend fun recordPerformance(
        operation: MathOperation,
        gradeLevel: GradeLevel,
        problemId: String,
        isCorrect: Boolean,
        timeSpentSeconds: Long,
    ): Long

    /**
     * Gets the aggregated performance statistics for a specific operation.
     * Includes total attempts, correct answers, average time, and recent accuracy.
     *
     * @param operation The math operation to get performance for
     * @param gradeLevel The grade level to get performance for
     * @return Flow of [OperationPerformance] for the operation
     */
    fun getPerformance(
        operation: MathOperation,
        gradeLevel: GradeLevel,
    ): Flow<OperationPerformance>

    /**
     * Calculates recent accuracy for a specific operation.
     * Returns the percentage of correct answers from the last N problems.
     *
     * @param operation The math operation to calculate accuracy for
     * @param count Number of recent problems to consider (default: 10)
     * @return The accuracy as a float (0-1), or null if no records exist
     */
    suspend fun getRecentAccuracy(
        operation: MathOperation,
        count: Int = 10,
    ): Float?

    /**
     * Gets the count of recent attempts for a specific operation.
     *
     * @param operation The math operation to count attempts for
     * @param limit Maximum number of records to count
     * @return The number of recent attempts
     */
    suspend fun getRecentAttemptCount(
        operation: MathOperation,
        limit: Int = 20,
    ): Int

    /**
     * Clears all performance records.
     * Useful for testing or user-requested data reset.
     */
    suspend fun clearAll()
}
