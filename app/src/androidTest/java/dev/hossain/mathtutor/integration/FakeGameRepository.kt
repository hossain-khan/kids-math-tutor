package dev.hossain.mathtutor.integration

import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GameSession
import dev.hossain.mathtutor.domain.model.GameStats
import dev.hossain.mathtutor.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake implementation of GameRepository for testing.
 * Provides test doubles for game-related operations without requiring a real database.
 */
class FakeGameRepository : GameRepository {
    private val gameSessions = mutableListOf<GameSession>()

    override suspend fun saveGameSession(session: GameSession): Long {
        gameSessions.add(session)
        return gameSessions.size.toLong()
    }

    override fun getPersonalBest(game: Game): Flow<Int> {
        val bestScore = gameSessions.filter { it.game == game }.maxOfOrNull { it.score } ?: 0
        return flowOf(bestScore)
    }

    override fun getBestSession(game: Game): Flow<GameSession?> {
        val bestSession = gameSessions.filter { it.game == game }.maxByOrNull { it.score }
        return flowOf(bestSession)
    }

    override fun getGameStats(game: Game): Flow<GameStats> {
        val sessions = gameSessions.filter { it.game == game }
        val stats =
            GameStats(
                game = game,
                personalBest = sessions.maxOfOrNull { it.score } ?: 0,
                totalGamesPlayed = sessions.size,
                averageScore = if (sessions.isNotEmpty()) sessions.map { it.score }.average().toFloat() else 0f,
                bestAccuracy = sessions.maxOfOrNull { it.accuracy } ?: 0f,
                lastPlayedAt = sessions.maxOfOrNull { it.endTime },
                totalCorrectAnswers = sessions.sumOf { it.correctAnswers },
                totalAttempts = sessions.sumOf { it.totalAttempts },
            )
        return flowOf(stats)
    }

    override fun getTotalGamesPlayed(game: Game): Flow<Int> {
        val count = gameSessions.count { it.game == game }
        return flowOf(count)
    }

    override fun isGameUnlocked(game: Game): Flow<Boolean> = flowOf(true)

    override fun getSessionsByGame(game: Game): Flow<List<GameSession>> {
        val sessions = gameSessions.filter { it.game == game }
        return flowOf(sessions)
    }

    override fun getRecentSessions(limit: Int): Flow<List<GameSession>> {
        val sessions = gameSessions.takeLast(limit)
        return flowOf(sessions)
    }

    override fun getAllGameStats(): Flow<Map<Game, GameStats>> {
        val statsMap =
            Game.entries.associateWith { game ->
                val sessions = gameSessions.filter { it.game == game }
                GameStats(
                    game = game,
                    personalBest = sessions.maxOfOrNull { it.score } ?: 0,
                    totalGamesPlayed = sessions.size,
                    averageScore = if (sessions.isNotEmpty()) sessions.map { it.score }.average().toFloat() else 0f,
                    bestAccuracy = sessions.maxOfOrNull { it.accuracy } ?: 0f,
                    lastPlayedAt = sessions.maxOfOrNull { it.endTime },
                    totalCorrectAnswers = sessions.sumOf { it.correctAnswers },
                    totalAttempts = sessions.sumOf { it.totalAttempts },
                )
            }
        return flowOf(statsMap)
    }

    override fun getPerfectGameCount(game: Game): Flow<Int> {
        val count = gameSessions.filter { it.game == game }.count { it.isPerfectGame }
        return flowOf(count)
    }

    override suspend fun clearAllSessions() {
        gameSessions.clear()
    }
}
