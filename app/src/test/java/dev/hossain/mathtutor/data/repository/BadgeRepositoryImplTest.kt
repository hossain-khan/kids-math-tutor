package dev.hossain.mathtutor.data.repository

import dev.hossain.mathtutor.data.local.dao.BadgeDao
import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.repository.BadgeProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class BadgeRepositoryImplTest {
    private lateinit var fakeDao: FakeBadgeDao
    private lateinit var repository: BadgeRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeBadgeDao()
        repository = BadgeRepositoryImpl(fakeDao)
    }

    @Test
    fun `getAllBadges returns mapped badges from DAO`() =
        runTest {
            val entity1 = createBadgeEntity("badge1", "Badge 1")
            val entity2 = createBadgeEntity("badge2", "Badge 2")
            fakeDao.allBadges.value = listOf(entity1, entity2)

            val badges = repository.getAllBadges().first()

            assertEquals(2, badges.size)
            assertEquals("badge1", badges[0].id)
            assertEquals("Badge 1", badges[0].name)
            assertEquals("badge2", badges[1].id)
            assertEquals("Badge 2", badges[1].name)
        }

    @Test
    fun `getAllBadges returns empty list when no badges exist`() =
        runTest {
            fakeDao.allBadges.value = emptyList()

            val badges = repository.getAllBadges().first()

            assertTrue(badges.isEmpty())
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

            assertEquals(3, badges.size)
            assertEquals("badge3", badges[0].id)
            assertEquals("badge2", badges[1].id)
            assertEquals("badge1", badges[2].id)
        }

    @Test
    fun `getBadgesByCategory returns only badges in specified category`() =
        runTest {
            val entity1 = createBadgeEntity("badge1", "Badge 1", category = BadgeCategory.GETTING_STARTED)
            val entity2 = createBadgeEntity("badge2", "Badge 2", category = BadgeCategory.GETTING_STARTED)
            fakeDao.badgesByCategory[BadgeCategory.GETTING_STARTED] = MutableStateFlow(listOf(entity1, entity2))

            val badges = repository.getBadgesByCategory(BadgeCategory.GETTING_STARTED).first()

            assertEquals(2, badges.size)
            assertTrue(badges.all { it.category == BadgeCategory.GETTING_STARTED })
        }

    @Test
    fun `getUnlockedBadges returns only unlocked badges`() =
        runTest {
            val now = Instant.now()
            val unlockedEntity = createBadgeEntity("unlocked", "Unlocked Badge", unlockedAt = now)
            val lockedEntity = createBadgeEntity("locked", "Locked Badge", unlockedAt = null)
            fakeDao.unlockedBadges.value = listOf(unlockedEntity)

            val badges = repository.getUnlockedBadges().first()

            assertEquals(1, badges.size)
            assertEquals("unlocked", badges[0].id)
            assertTrue(badges[0].isUnlocked())
        }

    @Test
    fun `getUnlockedBadges returns empty list when no badges are unlocked`() =
        runTest {
            val lockedEntity1 = createBadgeEntity("locked1", "Locked Badge 1", unlockedAt = null)
            val lockedEntity2 = createBadgeEntity("locked2", "Locked Badge 2", unlockedAt = null)
            fakeDao.unlockedBadges.value = emptyList()

            val badges = repository.getUnlockedBadges().first()

            assertTrue(badges.isEmpty())
        }

    @Test
    fun `getProgressSummary calculates correct percentage`() =
        runTest {
            fakeDao.unlockedCount.value = 8
            fakeDao.totalCount.value = 15

            val progress = repository.getProgressSummary().first()

            assertEquals(8, progress.unlockedCount)
            assertEquals(15, progress.totalCount)
            assertEquals(53.33f, progress.percentage, 0.01f)
        }

    @Test
    fun `getProgressSummary returns zero percentage when no badges exist`() =
        runTest {
            fakeDao.unlockedCount.value = 0
            fakeDao.totalCount.value = 0

            val progress = repository.getProgressSummary().first()

            assertEquals(0, progress.unlockedCount)
            assertEquals(0, progress.totalCount)
            assertEquals(0f, progress.percentage, 0.01f)
        }

    @Test
    fun `unlockBadge calls DAO with correct parameters`() =
        runTest {
            val badgeId = "test_badge"
            val unlockTime = Instant.now()

            repository.unlockBadge(badgeId, unlockTime)

            assertEquals(1, fakeDao.unlockCalls.size)
            assertEquals(badgeId, fakeDao.unlockCalls[0].first)
            assertEquals(unlockTime, fakeDao.unlockCalls[0].second)
        }

    @Test
    fun `initializeBadges inserts default badges when database is empty`() =
        runTest {
            fakeDao.allBadges.value = emptyList()

            repository.initializeBadges()

            assertEquals(1, fakeDao.insertBadgesCalls)
            assertNotNull(fakeDao.lastInsertedBadges)
            assertEquals(19, fakeDao.lastInsertedBadges!!.size) // 19 default badges
        }

    @Test
    fun `initializeBadges does not insert when badges already exist`() =
        runTest {
            val existingBadge = createBadgeEntity("existing", "Existing Badge")
            fakeDao.allBadges.value = listOf(existingBadge)

            repository.initializeBadges()

            assertEquals(0, fakeDao.insertBadgesCalls)
        }

    @Test
    fun `BadgeProgress calculates percentage correctly`() {
        val progress = BadgeProgress(unlockedCount = 12, totalCount = 15)
        assertEquals(80f, progress.percentage, 0.01f)
    }

    @Test
    fun `BadgeProgress handles zero total count`() {
        val progress = BadgeProgress(unlockedCount = 0, totalCount = 0)
        assertEquals(0f, progress.percentage, 0.01f)
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
            unlockCalls.add(Pair(badgeId, unlockedAt))
        }

        override suspend fun insertBadges(badges: List<BadgeEntity>) {
            insertBadgesCalls++
            lastInsertedBadges = badges
        }
    }
}
