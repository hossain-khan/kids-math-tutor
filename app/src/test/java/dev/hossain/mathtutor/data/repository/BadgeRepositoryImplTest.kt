package dev.hossain.mathtutor.data.repository

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsParam
import dev.hossain.mathtutor.analytics.FakeAnalyticsService
import dev.hossain.mathtutor.data.local.dao.BadgeDao
import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.repository.BadgeProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

class BadgeRepositoryImplTest {
    private lateinit var fakeDao: FakeBadgeDao
    private lateinit var fakeAnalytics: FakeAnalyticsService
    private lateinit var repository: BadgeRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeBadgeDao()
        fakeAnalytics = FakeAnalyticsService()
        repository = BadgeRepositoryImpl(fakeDao, fakeAnalytics)
    }

    @Test
    fun `getAllBadges returns mapped badges from DAO`() =
        runTest {
            val entity1 = createBadgeEntity("badge1", "Badge 1")
            val entity2 = createBadgeEntity("badge2", "Badge 2")
            fakeDao.allBadges.value = listOf(entity1, entity2)

            val badges = repository.getAllBadges().first()

            assertThat(badges.size).isEqualTo(2)
            assertThat(badges[0].id).isEqualTo("badge1")
            assertThat(badges[0].name).isEqualTo("Badge 1")
            assertThat(badges[1].id).isEqualTo("badge2")
            assertThat(badges[1].name).isEqualTo("Badge 2")
        }

    @Test
    fun `getAllBadges returns empty list when no badges exist`() =
        runTest {
            fakeDao.allBadges.value = emptyList()

            val badges = repository.getAllBadges().first()

            assertThat(badges.isEmpty()).isTrue()
        }

    @Test
    fun `getRecentlyUnlockedBadges returns limited unlocked badges`() =
        runTest {
            val now = Instant.now()
            val entity1 = createBadgeEntity("badge1", "Badge 1", unlockedAt = now.minusSeconds(100))
            val entity2 = createBadgeEntity("badge2", "Badge 2", unlockedAt = now.minusSeconds(50))
            val entity3 = createBadgeEntity("badge3", "Badge 3", unlockedAt = now)
            fakeDao.recentUnlockedBadges.value = listOf(entity3, entity2, entity1)

            val badges = repository.getRecentlyUnlockedBadges(3).first()

            assertThat(badges.size).isEqualTo(3)
            assertThat(badges[0].id).isEqualTo("badge3")
            assertThat(badges[1].id).isEqualTo("badge2")
            assertThat(badges[2].id).isEqualTo("badge1")
        }

    @Test
    fun `getBadgesByCategory returns only badges in specified category`() =
        runTest {
            val entity1 = createBadgeEntity("badge1", "Badge 1", category = BadgeCategory.GETTING_STARTED)
            val entity2 = createBadgeEntity("badge2", "Badge 2", category = BadgeCategory.GETTING_STARTED)
            fakeDao.badgesByCategory[BadgeCategory.GETTING_STARTED] = MutableStateFlow(listOf(entity1, entity2))

            val badges = repository.getBadgesByCategory(BadgeCategory.GETTING_STARTED).first()

            assertThat(badges.size).isEqualTo(2)
            assertThat(badges.all { it.category == BadgeCategory.GETTING_STARTED }).isTrue()
        }

    @Test
    fun `getUnlockedBadges returns only unlocked badges`() =
        runTest {
            val now = Instant.now()
            val unlockedEntity = createBadgeEntity("unlocked", "Unlocked Badge", unlockedAt = now)
            val lockedEntity = createBadgeEntity("locked", "Locked Badge", unlockedAt = null)
            fakeDao.unlockedBadges.value = listOf(unlockedEntity)

            val badges = repository.getUnlockedBadges().first()

            assertThat(badges.size).isEqualTo(1)
            assertThat(badges[0].id).isEqualTo("unlocked")
            assertThat(badges[0].isUnlocked()).isTrue()
        }

    @Test
    fun `getUnlockedBadges returns empty list when no badges are unlocked`() =
        runTest {
            val lockedEntity1 = createBadgeEntity("locked1", "Locked Badge 1", unlockedAt = null)
            val lockedEntity2 = createBadgeEntity("locked2", "Locked Badge 2", unlockedAt = null)
            fakeDao.unlockedBadges.value = emptyList()

            val badges = repository.getUnlockedBadges().first()

            assertThat(badges.isEmpty()).isTrue()
        }

    @Test
    fun `getProgressSummary calculates correct percentage`() =
        runTest {
            fakeDao.unlockedCount.value = 8
            fakeDao.totalCount.value = 15

            val progress = repository.getProgressSummary().first()

            assertThat(progress.unlockedCount).isEqualTo(8)
            assertThat(progress.totalCount).isEqualTo(15)
            assertThat(progress.percentage).isWithin(0.01f).of(53.33f)
        }

    @Test
    fun `getProgressSummary returns zero percentage when no badges exist`() =
        runTest {
            fakeDao.unlockedCount.value = 0
            fakeDao.totalCount.value = 0

            val progress = repository.getProgressSummary().first()

            assertThat(progress.unlockedCount).isEqualTo(0)
            assertThat(progress.totalCount).isEqualTo(0)
            assertThat(progress.percentage).isWithin(0.01f).of(0f)
        }

    @Test
    fun `unlockBadge calls DAO with correct parameters`() =
        runTest {
            val badgeId = "test_badge"
            val unlockTime = Instant.now()
            val badgeEntity = createBadgeEntity(badgeId, "Test Badge")
            fakeDao.allBadges.value = listOf(badgeEntity)

            repository.unlockBadge(badgeId, unlockTime)

            assertThat(fakeDao.unlockCalls.size).isEqualTo(1)
            assertThat(fakeDao.unlockCalls[0].first).isEqualTo(badgeId)
            assertThat(fakeDao.unlockCalls[0].second).isEqualTo(unlockTime)
        }

    @Test
    fun `unlockBadge logs analytics event with badge details`() =
        runTest {
            val badgeId = "test_badge"
            val badgeName = "Test Badge"
            val badgeCategory = BadgeCategory.GETTING_STARTED
            val unlockTime = Instant.now()
            val badgeEntity = createBadgeEntity(badgeId, badgeName, category = badgeCategory)
            fakeDao.allBadges.value = listOf(badgeEntity)

            repository.unlockBadge(badgeId, unlockTime)

            // Verify analytics event logged
            val events = fakeAnalytics.getEventsWithName(AnalyticsEvent.BADGE_UNLOCKED)
            assertThat(events).hasSize(1)
            assertThat(events.first().parameters[AnalyticsParam.BADGE_ID]).isEqualTo(badgeId)
            assertThat(events.first().parameters[AnalyticsParam.BADGE_NAME]).isEqualTo(badgeName)
            assertThat(events.first().parameters[AnalyticsParam.BADGE_CATEGORY]).isEqualTo(badgeCategory.name)
        }

    @Test
    fun `unlockBadge logs error on failure`() =
        runTest {
            val badgeId = "test_badge"
            val unlockTime = Instant.now()
            fakeDao.shouldThrowOnUnlock = true

            try {
                repository.unlockBadge(badgeId, unlockTime)
            } catch (e: Exception) {
                // Expected exception
            }

            // Verify error logged
            assertThat(fakeAnalytics.errors).hasSize(1)
            assertThat(fakeAnalytics.errors.first().context).isEqualTo("Badge unlock failed")
            assertThat(fakeAnalytics.errors.first().isFatal).isFalse()
        }

    @Test
    fun `unlockBadge updates total badges unlocked user property`() =
        runTest {
            val badgeId = "test_badge"
            val unlockTime = Instant.now()
            val badgeEntity = createBadgeEntity(badgeId, "Test Badge")
            fakeDao.allBadges.value = listOf(badgeEntity)
            fakeDao.unlockedBadges.value = listOf(badgeEntity) // Simulate 1 unlocked badge

            repository.unlockBadge(badgeId, unlockTime)

            // Verify user property updated
            assertThat(fakeAnalytics.userProperties).isNotEmpty()
            val totalBadgesProperty =
                fakeAnalytics.userProperties.find { it.propertyName == "total_badges_unlocked" }
            assertThat(totalBadgesProperty).isNotNull()
            assertThat(totalBadgesProperty?.value).isEqualTo("1")
        }

    @Test
    fun `initializeBadges inserts default badges when database is empty`() =
        runTest {
            fakeDao.allBadges.value = emptyList()

            repository.initializeBadges()

            assertThat(fakeDao.insertBadgesCalls).isEqualTo(1)
            assertThat(fakeDao.lastInsertedBadges).isNotNull()
            assertThat(fakeDao.lastInsertedBadges!!.size).isEqualTo(19) // 19 default badges
        }

    @Test
    fun `initializeBadges does not insert when badges already exist`() =
        runTest {
            val existingBadge = createBadgeEntity("existing", "Existing Badge")
            fakeDao.allBadges.value = listOf(existingBadge)

            repository.initializeBadges()

            assertThat(fakeDao.insertBadgesCalls).isEqualTo(0)
        }

    @Test
    fun `BadgeProgress calculates percentage correctly`() {
        val progress = BadgeProgress(unlockedCount = 12, totalCount = 15)
        assertThat(progress.percentage).isWithin(0.01f).of(80f)
    }

    @Test
    fun `BadgeProgress handles zero total count`() {
        val progress = BadgeProgress(unlockedCount = 0, totalCount = 0)
        assertThat(progress.percentage).isWithin(0.01f).of(0f)
    }

    private fun createBadgeEntity(
        id: String,
        name: String,
        category: BadgeCategory = BadgeCategory.GETTING_STARTED,
        unlockedAt: Instant? = null,
    ): BadgeEntity =
        BadgeEntity(
            id = id,
            name = name,
            description = "Test description",
            icon = "🎯",
            category = category,
            requirementType = "ProblemCount",
            requirementData = "count=10",
            unlockedAt = unlockedAt,
        )

    /**
     * Fake implementation of BadgeDao for testing.
     */
    private class FakeBadgeDao : BadgeDao {
        val allBadges = MutableStateFlow<List<BadgeEntity>>(emptyList())
        val recentUnlockedBadges = MutableStateFlow<List<BadgeEntity>>(emptyList())
        val badgesByCategory = mutableMapOf<BadgeCategory, MutableStateFlow<List<BadgeEntity>>>()
        val unlockedBadges = MutableStateFlow<List<BadgeEntity>>(emptyList())
        val unlockedCount = MutableStateFlow(0)
        val totalCount = MutableStateFlow(0)

        val unlockCalls = mutableListOf<Pair<String, Instant>>()
        var insertBadgesCalls = 0
        var lastInsertedBadges: List<BadgeEntity>? = null
        var shouldThrowOnUnlock = false

        override fun getAllBadges(): Flow<List<BadgeEntity>> = allBadges

        override fun getRecentlyUnlockedBadges(limit: Int): Flow<List<BadgeEntity>> = recentUnlockedBadges

        override fun getBadgesByCategory(category: BadgeCategory): Flow<List<BadgeEntity>> =
            badgesByCategory.getOrPut(category) { MutableStateFlow(emptyList()) }

        override fun getUnlockedBadges(): Flow<List<BadgeEntity>> = unlockedBadges

        override fun getUnlockedCount(): Flow<Int> = unlockedCount

        override fun getTotalCount(): Flow<Int> = totalCount

        override suspend fun updateBadge(badge: BadgeEntity) {
            // Not needed for these tests
        }

        override suspend fun unlockBadge(
            badgeId: String,
            unlockedAt: Instant,
        ) {
            if (shouldThrowOnUnlock) {
                throw RuntimeException("Failed to unlock badge")
            }
            unlockCalls.add(Pair(badgeId, unlockedAt))
        }

        override suspend fun insertBadges(badges: List<BadgeEntity>) {
            insertBadgesCalls++
            lastInsertedBadges = badges
        }
    }
}
