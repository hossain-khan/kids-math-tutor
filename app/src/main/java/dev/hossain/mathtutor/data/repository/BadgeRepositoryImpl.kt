package dev.hossain.mathtutor.data.repository

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

        override fun getUnlockedBadges(): Flow<List<Badge>> = getAllBadges().map { badges -> badges.filter { it.isUnlocked() } }

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
            badgeDao.unlockBadge(badgeId, unlockedAt)
        }

        override suspend fun initializeBadges() {
            val existingBadges = badgeDao.getAllBadges().first()
            if (existingBadges.isEmpty()) {
                val defaultBadges = BadgeDefinitions.getAllBadges()
                badgeDao.insertBadges(defaultBadges.map { BadgeMapper.toEntity(it) })
            }
        }
    }
