package dev.hossain.mathtutor.data.repository

import dev.hossain.mathtutor.data.local.dao.StreakDao
import dev.hossain.mathtutor.data.mapper.StreakMapper
import dev.hossain.mathtutor.domain.model.DailyStreak
import dev.hossain.mathtutor.domain.repository.StreakRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Implementation of [StreakRepository] using Room database.
 * Manages daily practice streak data with Flow-based reactive streams.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class StreakRepositoryImpl
    constructor(
        private val streakDao: StreakDao,
    ) : StreakRepository {
        override fun getStreak(): Flow<DailyStreak?> =
            streakDao.getStreak().map { entity ->
                entity?.let { StreakMapper.toDomain(it) }
            }

        override suspend fun saveStreak(streak: DailyStreak) {
            Timber.d(
                "StreakRepository: Saving streak - currentStreak=${streak.currentStreak}, " +
                    "longestStreak=${streak.longestStreak}, lastPracticeDate=${streak.lastPracticeDate}",
            )
            val entity = StreakMapper.toEntity(streak)
            streakDao.insertStreak(entity)
            Timber.d("StreakRepository: Streak saved successfully")
        }
    }
