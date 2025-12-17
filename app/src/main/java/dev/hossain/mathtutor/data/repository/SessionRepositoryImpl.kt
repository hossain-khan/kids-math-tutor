package dev.hossain.mathtutor.data.repository

import dev.hossain.mathtutor.data.local.dao.SessionDao
import dev.hossain.mathtutor.data.local.entity.PracticeSessionEntity
import dev.hossain.mathtutor.data.mapper.SessionMapper
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionStats
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Implementation of [SessionRepository] using Room database.
 * Handles all session data operations with Flow-based reactive streams.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SessionRepositoryImpl
    constructor(
        private val sessionDao: SessionDao,
    ) : SessionRepository {
        override suspend fun saveSession(
            session: PracticeSession,
            operation: MathOperation,
            durationSeconds: Long,
            gradeLevel: Int?,
        ): Long {
            val entity = SessionMapper.toEntity(session, operation, durationSeconds, gradeLevel)
            return sessionDao.insertSession(entity)
        }

        override fun getAllSessions(): Flow<List<PracticeSessionEntity>> = sessionDao.getAllSessions()

        override fun getRecentSessions(limit: Int): Flow<List<PracticeSessionEntity>> = sessionDao.getRecentSessions(limit)

        override fun getSessionsByOperation(operation: MathOperation): Flow<List<PracticeSessionEntity>> =
            sessionDao.getSessionsByOperation(operation)

        override fun getOverallStats(): Flow<SessionStats> =
            combine(
                sessionDao.getTotalProblemsCount(),
                sessionDao.getTotalCorrectCount(),
                sessionDao.getSessionCount(),
            ) { totalProblems, correctCount, sessionCount ->
                if (totalProblems == null || correctCount == null || sessionCount == 0) {
                    SessionStats.EMPTY
                } else {
                    val accuracy = (correctCount.toFloat() / totalProblems) * 100f
                    SessionStats(
                        totalProblems = totalProblems,
                        correctCount = correctCount,
                        accuracy = accuracy,
                        sessionCount = sessionCount,
                    )
                }
            }

        override fun getStatsByOperation(operation: MathOperation): Flow<SessionStats> =
            sessionDao.getSessionsByOperation(operation).combine(
                sessionDao.getSessionCount(),
            ) { sessions, _ ->
                if (sessions.isEmpty()) {
                    SessionStats.EMPTY
                } else {
                    val totalProblems = sessions.sumOf { it.totalProblems }
                    val correctCount = sessions.sumOf { it.correctAnswers }
                    val accuracy = (correctCount.toFloat() / totalProblems) * 100f
                    SessionStats(
                        totalProblems = totalProblems,
                        correctCount = correctCount,
                        accuracy = accuracy,
                        sessionCount = sessions.size,
                    )
                }
            }

        override suspend fun clearAllSessions() {
            sessionDao.deleteAllSessions()
        }
    }
