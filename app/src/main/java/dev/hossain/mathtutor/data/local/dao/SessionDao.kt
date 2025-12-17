package dev.hossain.mathtutor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.domain.model.MathOperation
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for practice session database operations.
 * All query methods return Flow for reactive updates.
 */
@Dao
interface SessionDao {
    /**
     * Inserts a new practice session into the database.
     *
     * @param session The practice session to insert
     * @return The ID of the inserted session
     */
    @Insert
    suspend fun insertSession(session: PracticeSessionEntity): Long

    /**
     * Retrieves all practice sessions ordered by most recent first.
     *
     * @return Flow of all sessions
     */
    @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PracticeSessionEntity>>

    /**
     * Retrieves the most recent N practice sessions.
     *
     * @param limit Maximum number of sessions to retrieve (default: 10)
     * @return Flow of recent sessions
     */
    @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<PracticeSessionEntity>>

    /**
     * Retrieves all sessions for a specific math operation.
     *
     * @param operation The math operation to filter by
     * @return Flow of sessions for the specified operation
     */
    @Query("SELECT * FROM practice_sessions WHERE operation = :operation ORDER BY timestamp DESC")
    fun getSessionsByOperation(operation: MathOperation): Flow<List<PracticeSessionEntity>>

    /**
     * Calculates the total number of problems attempted across all sessions.
     *
     * @return Flow of total problem count, or null if no sessions exist
     */
    @Query("SELECT SUM(totalProblems) FROM practice_sessions")
    fun getTotalProblemsCount(): Flow<Int?>

    /**
     * Calculates the total number of correct answers across all sessions.
     *
     * @return Flow of total correct count, or null if no sessions exist
     */
    @Query("SELECT SUM(correctAnswers) FROM practice_sessions")
    fun getTotalCorrectCount(): Flow<Int?>

    /**
     * Counts the total number of practice sessions.
     *
     * @return Flow of session count
     */
    @Query("SELECT COUNT(*) FROM practice_sessions")
    fun getSessionCount(): Flow<Int>

    /**
     * Deletes all practice sessions from the database.
     * Useful for testing or user-requested data reset.
     */
    @Query("DELETE FROM practice_sessions")
    suspend fun deleteAllSessions()

    /**
     * Retrieves all sessions completed today.
     * Uses SQLite date functions to compare timestamps.
     *
     * @return Flow of today's sessions
     */
    @Query(
        """
        SELECT * FROM practice_sessions 
        WHERE date(timestamp / 1000, 'unixepoch') = date('now')
        ORDER BY timestamp DESC
    """,
    )
    fun getTodaySessions(): Flow<List<PracticeSessionEntity>>
}
