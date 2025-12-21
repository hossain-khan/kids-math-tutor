package dev.hossain.mathtutor.data.repository

import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.data.local.dao.GameSessionDao
import dev.hossain.mathtutor.data.local.entity.GameSessionEntity
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GameSession
import dev.hossain.mathtutor.domain.model.GameStats
import dev.hossain.mathtutor.domain.repository.GameRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant

/**
 * Internal data class to hold detailed game statistics during combine operations.
 */
private data class GameStatsDetails(
    val bestAccuracy: Float,
    val lastPlayedAt: Instant?,
    val totalCorrectAnswers: Int,
    val totalAttempts: Int,
)

/**
 * Implementation of [GameRepository] using Room database.
 * Handles all game session data operations with Flow-based reactive streams.
 *
 * Uses [SessionRepository] to access total problems solved for game unlock checks.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class GameRepositoryImpl
    constructor(
        private val gameSessionDao: GameSessionDao,
        private val sessionRepository: SessionRepository,
        private val analyticsService: AnalyticsService,
    ) : GameRepository {
        override suspend fun saveGameSession(session: GameSession): Long {
            try {
                Timber.d(
                    "GameRepository: Saving game session - game=${session.game}, score=${session.score}, " +
                        "correctAnswers=${session.correctAnswers}, totalAttempts=${session.totalAttempts}, " +
                        "isNewRecord=${session.isNewRecord}",
                )
                val entity = GameSessionEntity.fromDomainModel(session)
                val sessionId = gameSessionDao.insertSession(entity)
                Timber.d("GameRepository: Game session saved with ID=$sessionId")
                return sessionId
            } catch (e: Exception) {
                Timber.e(e, "GameRepository: Failed to save game session")
                analyticsService.logError(e, "Game session save failed", isFatal = false)
                throw e
            }
        }

        override fun getPersonalBest(game: Game): Flow<Int> = gameSessionDao.getPersonalBest(game.name).map { it ?: 0 }

        override fun getBestSession(game: Game): Flow<GameSession?> =
            gameSessionDao.getBestSession(game.name).map { entity ->
                entity?.toDomainModel(isNewRecord = true)
            }

        override fun getGameStats(game: Game): Flow<GameStats> {
            // Combine the first group of flows (personal best, total games, average score)
            val basicStatsFlow =
                combine(
                    gameSessionDao.getPersonalBest(game.name),
                    gameSessionDao.getTotalGamesPlayed(game.name),
                    gameSessionDao.getAverageScore(game.name),
                ) { personalBest, totalGamesPlayed, averageScore ->
                    Triple(personalBest ?: 0, totalGamesPlayed, averageScore ?: 0f)
                }

            // Combine the second group of flows (accuracy, last played, correct answers, attempts)
            val detailedStatsFlow =
                combine(
                    gameSessionDao.getBestAccuracy(game.name),
                    gameSessionDao.getLastPlayedTimestamp(game.name),
                    gameSessionDao.getTotalCorrectAnswers(game.name),
                    gameSessionDao.getTotalAttempts(game.name),
                ) { bestAccuracy, lastPlayed, totalCorrect, totalAttempts ->
                    GameStatsDetails(
                        bestAccuracy = bestAccuracy ?: 0f,
                        lastPlayedAt = lastPlayed?.let { Instant.ofEpochMilli(it) },
                        totalCorrectAnswers = totalCorrect ?: 0,
                        totalAttempts = totalAttempts ?: 0,
                    )
                }

            // Combine both groups into final GameStats
            return combine(basicStatsFlow, detailedStatsFlow) { basic, detailed ->
                GameStats(
                    game = game,
                    personalBest = basic.first,
                    totalGamesPlayed = basic.second,
                    averageScore = basic.third,
                    bestAccuracy = detailed.bestAccuracy,
                    lastPlayedAt = detailed.lastPlayedAt,
                    totalCorrectAnswers = detailed.totalCorrectAnswers,
                    totalAttempts = detailed.totalAttempts,
                )
            }
        }

        override fun getTotalGamesPlayed(game: Game): Flow<Int> = gameSessionDao.getTotalGamesPlayed(game.name)

        override fun isGameUnlocked(game: Game): Flow<Boolean> =
            sessionRepository.getOverallStats().map { stats ->
                game.isUnlocked(stats.totalProblems)
            }

        override fun getSessionsByGame(game: Game): Flow<List<GameSession>> =
            gameSessionDao.getSessionsByGame(game.name).map { entities ->
                entities.map { it.toDomainModel() }
            }

        override fun getRecentSessions(limit: Int): Flow<List<GameSession>> =
            gameSessionDao.getRecentSessions(limit).map { entities ->
                entities.map { it.toDomainModel() }
            }

        override fun getAllGameStats(): Flow<Map<Game, GameStats>> {
            // Combine stats for all games
            val statsFlows =
                Game.entries.map { game ->
                    getGameStats(game).map { stats -> game to stats }
                }

            return combine(statsFlows) { statsArray ->
                statsArray.toMap()
            }
        }

        override fun getPerfectGameCount(game: Game): Flow<Int> = gameSessionDao.getPerfectGameCount(game.name)

        override suspend fun clearAllSessions() {
            try {
                Timber.d("GameRepository: Clearing all game sessions")
                gameSessionDao.deleteAllSessions()
                Timber.d("GameRepository: All game sessions cleared")
            } catch (e: Exception) {
                Timber.e(e, "GameRepository: Failed to clear game sessions")
                analyticsService.logError(e, "Game session clear failed", isFatal = false)
                throw e
            }
        }
    }
