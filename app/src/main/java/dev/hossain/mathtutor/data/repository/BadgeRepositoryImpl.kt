package dev.hossain.mathtutor.data.repository

import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsParam
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.data.local.dao.BadgeDao
import dev.hossain.mathtutor.data.mapper.BadgeMapper
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeDefinitions
import dev.hossain.mathtutor.domain.repository.BadgeProgress
import dev.hossain.mathtutor.domain.repository.BadgeRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant

/**
 * Implementation of [BadgeRepository] using Room database.
 * Handles all badge data operations with Flow-based reactive streams.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class BadgeRepositoryImpl
    constructor(
        private val badgeDao: BadgeDao,
        private val analyticsService: AnalyticsService,
    ) : BadgeRepository {
        override fun getAllBadges(): Flow<List<Badge>> =
            badgeDao
                .getAllBadges()
                .map { entities -> entities.map { BadgeMapper.toDomain(it) } }

        override fun getRecentlyUnlockedBadges(limit: Int): Flow<List<Badge>> =
            badgeDao
                .getRecentlyUnlockedBadges(limit)
                .map { entities -> entities.map { BadgeMapper.toDomain(it) } }

        override fun getBadgesByCategory(category: BadgeCategory): Flow<List<Badge>> =
            badgeDao
                .getBadgesByCategory(category)
                .map { entities -> entities.map { BadgeMapper.toDomain(it) } }

        override fun getUnlockedBadges(): Flow<List<Badge>> =
            badgeDao
                .getUnlockedBadges()
                .map { entities -> entities.map { BadgeMapper.toDomain(it) } }

        override fun getProgressSummary(): Flow<BadgeProgress> =
            combine(
                badgeDao.getUnlockedCount(),
                badgeDao.getTotalCount(),
            ) { unlocked, total ->
                BadgeProgress(unlocked, total)
            }

        override suspend fun unlockBadge(
            badgeId: String,
            unlockedAt: Instant,
        ) {
            try {
                Timber.d("BadgeRepository: Unlocking badge - id=$badgeId, unlockedAt=$unlockedAt")
                badgeDao.unlockBadge(badgeId, unlockedAt)
                Timber.d("BadgeRepository: Badge unlocked successfully - id=$badgeId")

                // Get badge details for analytics after successful unlock
                val badge = badgeDao.getAllBadges().first().find { it.id == badgeId }
                if (badge != null) {
                    analyticsService.logEvent(
                        AnalyticsEvent.BADGE_UNLOCKED,
                        mapOf(
                            AnalyticsParam.BADGE_ID to badgeId,
                            AnalyticsParam.BADGE_NAME to badge.name,
                            AnalyticsParam.BADGE_CATEGORY to badge.category.name,
                        ),
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "BadgeRepository: Failed to unlock badge - id=$badgeId")
                analyticsService.logError(e, "Badge unlock failed", isFatal = false)
                throw e
            }
        }

        override suspend fun initializeBadges() {
            Timber.d("BadgeRepository: Initializing badges")
            val existingBadges = badgeDao.getAllBadges().first()
            if (existingBadges.isEmpty()) {
                val defaultBadges = BadgeDefinitions.getAllBadges()
                Timber.d("BadgeRepository: Inserting ${defaultBadges.size} default badges")
                badgeDao.insertBadges(defaultBadges.map { BadgeMapper.toEntity(it) })
                Timber.d("BadgeRepository: Badges initialized successfully")
            } else {
                Timber.d("BadgeRepository: Badges already initialized (count=${existingBadges.size})")
            }
        }
    }
