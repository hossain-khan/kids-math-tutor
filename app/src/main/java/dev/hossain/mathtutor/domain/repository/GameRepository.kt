package dev.hossain.mathtutor.domain.repository

import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GameSession
import dev.hossain.mathtutor.domain.model.GameStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for game session data management.
 * Provides methods to save, retrieve, and analyze game session data.
 */
interface GameRepository {
    /**
     * Saves a completed game session to the database.
     *
     * @param session The game session to save
     * @return The ID of the inserted session
     */
    suspend fun saveGameSession(session: GameSession): Long

    /**
     * Retrieves the personal best (highest score) for a specific game.
     *
     * @param game The game to get personal best for
     * @return Flow of the highest score, or 0 if no sessions exist
     */
    fun getPersonalBest(game: Game): Flow<Int>

    /**
     * Retrieves the session with the personal best score for a specific game.
     *
     * @param game The game to get the best session for
     * @return Flow of the best session, or null if no sessions exist
     */
    fun getBestSession(game: Game): Flow<GameSession?>

    /**
     * Retrieves aggregated statistics for a specific game.
     *
     * @param game The game to get stats for
     * @return Flow of aggregated game statistics
     */
    fun getGameStats(game: Game): Flow<GameStats>

    /**
     * Retrieves the total number of games played for a specific game type.
     *
     * @param game The game to count sessions for
     * @return Flow of the play count
     */
    fun getTotalGamesPlayed(game: Game): Flow<Int>

    /**
     * Checks if a game is unlocked based on total problems solved.
     *
     * @param game The game to check
     * @return Flow of true if unlocked, false otherwise
     */
    fun isGameUnlocked(game: Game): Flow<Boolean>

    /**
     * Retrieves all sessions for a specific game type.
     *
     * @param game The game to get sessions for
     * @return Flow of game sessions, ordered by most recent first
     */
    fun getSessionsByGame(game: Game): Flow<List<GameSession>>

    /**
     * Retrieves recent game sessions across all games.
     *
     * @param limit Maximum number of sessions to retrieve
     * @return Flow of recent game sessions
     */
    fun getRecentSessions(limit: Int = 10): Flow<List<GameSession>>

    /**
     * Retrieves statistics for all games.
     *
     * @return Flow of map from Game to GameStats
     */
    fun getAllGameStats(): Flow<Map<Game, GameStats>>

    /**
     * Gets the count of perfect games (100% accuracy) for a specific game.
     *
     * @param game The game to check
     * @return Flow of perfect game count
     */
    fun getPerfectGameCount(game: Game): Flow<Int>

    /**
     * Deletes all game sessions from the database.
     * Useful for testing or user-requested data reset.
     */
    suspend fun clearAllSessions()
}
