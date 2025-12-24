package dev.hossain.mathtutor.domain.repository

import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.DailyAccuracy
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for session data management.
 * Provides methods to save, retrieve, and analyze practice session data.
 */
interface SessionRepository {
    /**
     * Saves a completed practice session to the database.
     *
     * @param session The practice session to save
     * @param operation The math operation practiced
     * @param durationSeconds Time spent completing the session in seconds
     * @param gradeLevel Optional grade level (K=0, 1st=1, 2nd=2)
     * @return The ID of the inserted session
     */
    suspend fun saveSession(
        session: PracticeSession,
        operation: MathOperation,
        durationSeconds: Long,
        gradeLevel: Int? = null,
    ): Long

    /**
     * Retrieves all practice sessions ordered by most recent first.
     *
     * @return Flow of all sessions
     */
    fun getAllSessions(): Flow<List<PracticeSessionEntity>>

    /**
     * Retrieves the most recent N practice sessions.
     *
     * @param limit Maximum number of sessions to retrieve (default: 10)
     * @return Flow of recent sessions
     */
    fun getRecentSessions(limit: Int = 10): Flow<List<PracticeSessionEntity>>

    /**
     * Retrieves all sessions for a specific math operation.
     *
     * @param operation The math operation to filter by
     * @return Flow of sessions for the specified operation
     */
    fun getSessionsByOperation(operation: MathOperation): Flow<List<PracticeSessionEntity>>

    /**
     * Retrieves overall statistics across all sessions.
     * Combines total problems, correct count, and session count to calculate overall stats.
     *
     * @return Flow of overall session statistics
     */
    fun getOverallStats(): Flow<SessionStats>

    /**
     * Retrieves statistics for a specific math operation.
     * Aggregates data only from sessions of the specified operation.
     *
     * @param operation The math operation to get stats for
     * @return Flow of operation-specific statistics
     */
    fun getStatsByOperation(operation: MathOperation): Flow<SessionStats>

    /**
     * Retrieves daily accuracy statistics for all practice sessions.
     * Groups sessions by date and calculates accuracy for each day.
     *
     * @return Flow of daily accuracy data, sorted by date (most recent first)
     */
    fun getDailyAccuracy(): Flow<List<DailyAccuracy>>

    /**
     * Deletes all practice sessions from the database.
     * Useful for testing or user-requested data reset.
     */
    suspend fun clearAllSessions()
}
