package dev.hossain.mathtutor.data.repository

import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsParam
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.analytics.UserProperty
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
import kotlinx.coroutines.flow.first
import timber.log.Timber

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
        private val analyticsService: AnalyticsService,
    ) : SessionRepository {
        override suspend fun saveSession(
            session: PracticeSession,
            operation: MathOperation,
            durationSeconds: Long,
            gradeLevel: Int?,
        ): Long {
            try {
                Timber.d(
                    "SessionRepository: Saving session - operation=$operation, " +
                        "problems=${session.totalProblems}, duration=${durationSeconds}s, gradeLevel=$gradeLevel",
                )
                val entity = SessionMapper.toEntity(session, operation, durationSeconds, gradeLevel)
                val sessionId = sessionDao.insertSession(entity)
                Timber.d("SessionRepository: Session saved with ID=$sessionId")

                // Track session completion in analytics
                val correctCount = session.getCorrectCount()
                val accuracy =
                    if (session.totalProblems > 0) {
                        (correctCount.toFloat() / session.totalProblems) * 100f
                    } else {
                        0f
                    }

                analyticsService.logEvent(
                    AnalyticsEvent.PRACTICE_SESSION_COMPLETED,
                    mapOf(
                        AnalyticsParam.OPERATION_TYPE to operation.name,
                        AnalyticsParam.PROBLEM_COUNT to session.totalProblems,
                        AnalyticsParam.CORRECT_ANSWERS to correctCount,
                        AnalyticsParam.ACCURACY to accuracy,
                        AnalyticsParam.SESSION_DURATION to durationSeconds,
                    ),
                )

                // Update total problems solved user property
                getOverallStats().first().let { stats ->
                    analyticsService.setUserProperty(
                        UserProperty.TOTAL_PROBLEMS_SOLVED,
                        stats.totalProblems.toString(),
                    )
                }

                return sessionId
            } catch (e: Exception) {
                Timber.e(e, "SessionRepository: Failed to save session")
                analyticsService.logError(e, "Session save failed", isFatal = false)
                throw e
            }
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
            try {
                Timber.d("SessionRepository: Clearing all sessions")
                sessionDao.deleteAllSessions()
                Timber.d("SessionRepository: All sessions cleared")
            } catch (e: Exception) {
                Timber.e(e, "SessionRepository: Failed to clear sessions")
                analyticsService.logError(e, "Session clear failed", isFatal = false)
                throw e
            }
        }
    }
