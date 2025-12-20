package dev.hossain.mathtutor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.hossain.mathtutor.data.local.entity.GameSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for game session database operations.
 * Provides methods to insert, query, and aggregate game session data.
 * All query methods return Flow for reactive updates.
 */
@Dao
interface GameSessionDao {
    /**
     * Inserts a new game session into the database.
     *
     * @param session The game session to insert
     * @return The ID of the inserted session
     */
    @Insert
    suspend fun insertSession(session: GameSessionEntity): Long

    /**
     * Retrieves all game sessions ordered by most recent first.
     *
     * @return Flow of all game sessions
     */
    @Query("SELECT * FROM game_sessions ORDER BY endTime DESC")
    fun getAllSessions(): Flow<List<GameSessionEntity>>

    /**
     * Retrieves all sessions for a specific game type ordered by most recent first.
     *
     * @param gameId The game identifier (e.g., "MATH_RACE")
     * @return Flow of sessions for the specified game
     */
    @Query("SELECT * FROM game_sessions WHERE gameId = :gameId ORDER BY endTime DESC")
    fun getSessionsByGame(gameId: String): Flow<List<GameSessionEntity>>

    /**
     * Retrieves the most recent N game sessions.
     *
     * @param limit Maximum number of sessions to retrieve
     * @return Flow of recent sessions
     */
    @Query("SELECT * FROM game_sessions ORDER BY endTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<GameSessionEntity>>

    /**
     * Retrieves the personal best (highest score) for a specific game.
     *
     * @param gameId The game identifier
     * @return Flow of the highest score, or null if no sessions exist
     */
    @Query("SELECT MAX(score) FROM game_sessions WHERE gameId = :gameId")
    fun getPersonalBest(gameId: String): Flow<Int?>

    /**
     * Retrieves the session with the personal best score for a specific game.
     *
     * @param gameId The game identifier
     * @return Flow of the best session, or null if no sessions exist
     */
    @Query("SELECT * FROM game_sessions WHERE gameId = :gameId ORDER BY score DESC LIMIT 1")
    fun getBestSession(gameId: String): Flow<GameSessionEntity?>

    /**
     * Counts the total number of times a specific game has been played.
     *
     * @param gameId The game identifier
     * @return Flow of the play count
     */
    @Query("SELECT COUNT(*) FROM game_sessions WHERE gameId = :gameId")
    fun getTotalGamesPlayed(gameId: String): Flow<Int>

    /**
     * Calculates the average score for a specific game.
     *
     * @param gameId The game identifier
     * @return Flow of the average score, or null if no sessions exist
     */
    @Query("SELECT AVG(CAST(score AS REAL)) FROM game_sessions WHERE gameId = :gameId")
    fun getAverageScore(gameId: String): Flow<Float?>

    /**
     * Retrieves the best accuracy percentage achieved for a specific game.
     *
     * @param gameId The game identifier
     * @return Flow of the best accuracy (0-100), or null if no sessions exist
     */
    @Query(
        """
        SELECT MAX(CAST(correctAnswers AS REAL) / totalAttempts * 100) 
        FROM game_sessions 
        WHERE gameId = :gameId AND totalAttempts > 0
        """,
    )
    fun getBestAccuracy(gameId: String): Flow<Float?>

    /**
     * Retrieves the timestamp of the most recent session for a specific game.
     *
     * @param gameId The game identifier
     * @return Flow of the most recent end time, or null if no sessions exist
     */
    @Query("SELECT MAX(endTime) FROM game_sessions WHERE gameId = :gameId")
    fun getLastPlayedTimestamp(gameId: String): Flow<Long?>

    /**
     * Calculates the total correct answers for a specific game.
     *
     * @param gameId The game identifier
     * @return Flow of total correct answers, or null if no sessions exist
     */
    @Query("SELECT SUM(correctAnswers) FROM game_sessions WHERE gameId = :gameId")
    fun getTotalCorrectAnswers(gameId: String): Flow<Int?>

    /**
     * Calculates the total attempts for a specific game.
     *
     * @param gameId The game identifier
     * @return Flow of total attempts, or null if no sessions exist
     */
    @Query("SELECT SUM(totalAttempts) FROM game_sessions WHERE gameId = :gameId")
    fun getTotalAttempts(gameId: String): Flow<Int?>

    /**
     * Counts the total number of game sessions across all games.
     *
     * @return Flow of total session count
     */
    @Query("SELECT COUNT(*) FROM game_sessions")
    fun getTotalSessionCount(): Flow<Int>

    /**
     * Counts the number of perfect games (100% accuracy) for a specific game.
     *
     * @param gameId The game identifier
     * @return Flow of perfect game count
     */
    @Query(
        """
        SELECT COUNT(*) FROM game_sessions 
        WHERE gameId = :gameId AND correctAnswers = totalAttempts AND totalAttempts > 0
        """,
    )
    fun getPerfectGameCount(gameId: String): Flow<Int>

    /**
     * Deletes all game sessions from the database.
     * Useful for testing or user-requested data reset.
     */
    @Query("DELETE FROM game_sessions")
    suspend fun deleteAllSessions()

    /**
     * Deletes all sessions for a specific game.
     *
     * @param gameId The game identifier
     */
    @Query("DELETE FROM game_sessions WHERE gameId = :gameId")
    suspend fun deleteSessionsByGame(gameId: String)
}
