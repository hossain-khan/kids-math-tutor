package dev.hossain.mathtutor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.hossain.mathtutor.data.local.entity.PerformanceEntity
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for performance records.
 * Provides methods to insert and query performance data for adaptive difficulty tracking.
 */
@Dao
interface PerformanceDao {
    /**
     * Inserts a new performance record into the database.
     *
     * @param performance The performance record to insert
     * @return The row ID of the inserted record
     */
    @Insert
    suspend fun insertPerformance(performance: PerformanceEntity): Long

    /**
     * Gets all performance records for a specific operation, ordered by timestamp descending.
     *
     * @param operation The math operation to filter by
     * @return Flow of performance records for the operation
     */
    @Query("SELECT * FROM performance_records WHERE operation = :operation ORDER BY timestamp DESC")
    fun getPerformanceByOperation(operation: MathOperation): Flow<List<PerformanceEntity>>

    /**
     * Gets the most recent N performance records for a specific operation.
     *
     * @param operation The math operation to filter by
     * @param limit Number of records to retrieve
     * @return Flow of recent performance records
     */
    @Query(
        "SELECT * FROM performance_records WHERE operation = :operation " +
            "ORDER BY timestamp DESC LIMIT :limit",
    )
    fun getRecentPerformance(
        operation: MathOperation,
        limit: Int,
    ): Flow<List<PerformanceEntity>>

    /**
     * Gets the most recent N performance records for a specific operation and grade level.
     *
     * @param operation The math operation to filter by
     * @param gradeLevel The grade level to filter by
     * @param limit Number of records to retrieve
     * @return Flow of recent performance records
     */
    @Query(
        "SELECT * FROM performance_records WHERE operation = :operation AND gradeLevel = :gradeLevel " +
            "ORDER BY timestamp DESC LIMIT :limit",
    )
    fun getRecentPerformanceByGrade(
        operation: MathOperation,
        gradeLevel: GradeLevel,
        limit: Int,
    ): Flow<List<PerformanceEntity>>

    /**
     * Gets the count of correct answers for a specific operation.
     *
     * @param operation The math operation to filter by
     * @return Flow of correct answer count
     */
    @Query("SELECT COUNT(*) FROM performance_records WHERE operation = :operation AND isCorrect = 1")
    fun getCorrectCountByOperation(operation: MathOperation): Flow<Int>

    /**
     * Gets the total count of attempts for a specific operation.
     *
     * @param operation The math operation to filter by
     * @return Flow of total attempt count
     */
    @Query("SELECT COUNT(*) FROM performance_records WHERE operation = :operation")
    fun getTotalCountByOperation(operation: MathOperation): Flow<Int>

    /**
     * Gets the average time spent on problems for a specific operation.
     *
     * @param operation The math operation to filter by
     * @return Flow of average time in seconds, or null if no records
     */
    @Query("SELECT AVG(timeSpentSeconds) FROM performance_records WHERE operation = :operation")
    fun getAverageTimeByOperation(operation: MathOperation): Flow<Float?>

    /**
     * Calculates recent accuracy for a specific operation (last N records).
     * Returns the percentage of correct answers as a float (0-1).
     *
     * @param operation The math operation to filter by
     * @param limit Number of recent records to consider
     * @return Flow of recent accuracy percentage
     */
    @Query(
        """
        SELECT CAST(SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) 
        FROM (SELECT isCorrect FROM performance_records 
              WHERE operation = :operation 
              ORDER BY timestamp DESC 
              LIMIT :limit)
    """,
    )
    fun getRecentAccuracy(
        operation: MathOperation,
        limit: Int,
    ): Flow<Float?>

    /**
     * Gets the count of recent attempts for a specific operation.
     *
     * @param operation The math operation to filter by
     * @param limit Maximum number of records to count
     * @return Flow of attempt count (up to limit)
     */
    @Query(
        """
        SELECT COUNT(*) FROM (
            SELECT id FROM performance_records 
            WHERE operation = :operation 
            ORDER BY timestamp DESC 
            LIMIT :limit
        )
    """,
    )
    fun getRecentAttemptCount(
        operation: MathOperation,
        limit: Int,
    ): Flow<Int>

    /**
     * Deletes all performance records.
     * Useful for testing or user-requested data reset.
     */
    @Query("DELETE FROM performance_records")
    suspend fun clearAll()
}
