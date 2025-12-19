package dev.hossain.mathtutor.data.repository

import dev.hossain.mathtutor.data.local.dao.PerformanceDao
import dev.hossain.mathtutor.data.local.entity.PerformanceEntity
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.OperationPerformance
import dev.hossain.mathtutor.domain.repository.PerformanceRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * Implementation of [PerformanceRepository] using Room database.
 * Provides methods to record and retrieve performance data for adaptive difficulty tracking.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class PerformanceRepositoryImpl
    constructor(
        private val performanceDao: PerformanceDao,
    ) : PerformanceRepository {
        companion object {
            /**
             * Number of recent problems to consider for accuracy calculation.
             */
            const val RECENT_PROBLEMS_COUNT = 20
        }

        override suspend fun recordPerformance(
            operation: MathOperation,
            gradeLevel: GradeLevel,
            problemId: String,
            isCorrect: Boolean,
            timeSpentSeconds: Long,
        ): Long {
            val entity =
                PerformanceEntity(
                    operation = operation,
                    gradeLevel = gradeLevel,
                    problemId = problemId,
                    isCorrect = isCorrect,
                    attemptCount = 1,
                    timeSpentSeconds = timeSpentSeconds,
                    timestamp = Instant.now(),
                )
            return performanceDao.insertPerformance(entity)
        }

        override fun getPerformance(
            operation: MathOperation,
            gradeLevel: GradeLevel,
        ): Flow<OperationPerformance> =
            combine(
                performanceDao.getTotalCountByOperation(operation),
                performanceDao.getCorrectCountByOperation(operation),
                performanceDao.getAverageTimeByOperation(operation),
                performanceDao.getRecentAccuracy(operation, RECENT_PROBLEMS_COUNT),
                performanceDao.getRecentAttemptCount(operation, RECENT_PROBLEMS_COUNT),
            ) { totalCount, correctCount, avgTime, recentAccuracy, recentAttempts ->
                OperationPerformance(
                    operation = operation,
                    gradeLevel = gradeLevel,
                    totalAttempts = totalCount,
                    correctAnswers = correctCount,
                    averageTimeSeconds = avgTime ?: 0f,
                    recentAccuracy = recentAccuracy ?: 0f,
                    recentAttempts = recentAttempts,
                )
            }

        override suspend fun getRecentAccuracy(
            operation: MathOperation,
            count: Int,
        ): Float? = performanceDao.getRecentAccuracy(operation, count).first()

        override suspend fun getRecentAttemptCount(
            operation: MathOperation,
            limit: Int,
        ): Int = performanceDao.getRecentAttemptCount(operation, limit).first()

        override suspend fun clearAll() {
            performanceDao.clearAll()
        }
    }
