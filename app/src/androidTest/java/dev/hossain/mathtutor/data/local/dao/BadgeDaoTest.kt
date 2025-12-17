package dev.hossain.mathtutor.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.hossain.mathtutor.data.local.MathDatabase
import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.domain.model.BadgeCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Instrumented tests for BadgeDao database operations.
 * Uses in-memory database for testing to ensure isolated test environment.
 */
@RunWith(AndroidJUnit4::class)
class BadgeDaoTest {
    private lateinit var database: MathDatabase
    private lateinit var badgeDao: BadgeDao

    @Before
    fun setup() {
        // Create in-memory database for testing
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    MathDatabase::class.java,
                ).allowMainThreadQueries() // For testing only
                .build()
        badgeDao = database.badgeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertBadges_andRetrieve_returnsCorrectData() =
        runTest {
            val badges =
                listOf(
                    createTestBadge(id = "badge1"),
                    createTestBadge(id = "badge2"),
                )
            badgeDao.insertBadges(badges)

            val retrieved = badgeDao.getAllBadges().first()

            assertEquals(2, retrieved.size)
        }

    @Test
    fun getAllBadges_emptyDatabase_returnsEmptyList() =
        runTest {
            val badges = badgeDao.getAllBadges().first()

            assertTrue("Should return empty list for empty database", badges.isEmpty())
        }

    @Test
    fun getAllBadges_orderedByCategoryAndId() =
        runTest {
            badgeDao.insertBadges(
                listOf(
                    createTestBadge(id = "badge2", category = BadgeCategory.VOLUME),
                    createTestBadge(id = "badge1", category = BadgeCategory.GETTING_STARTED),
                    createTestBadge(id = "badge3", category = BadgeCategory.GETTING_STARTED),
                ),
            )

            val badges = badgeDao.getAllBadges().first()

            assertEquals(3, badges.size)
            // Should be ordered by category first, then by id
            assertEquals(BadgeCategory.GETTING_STARTED, badges[0].category)
            assertEquals("badge1", badges[0].id)
            assertEquals(BadgeCategory.GETTING_STARTED, badges[1].category)
            assertEquals("badge3", badges[1].id)
            assertEquals(BadgeCategory.VOLUME, badges[2].category)
        }

    @Test
    fun getRecentlyUnlockedBadges_returnsOnlyUnlocked() =
        runTest {
            val now = Instant.now()
            badgeDao.insertBadges(
                listOf(
                    createTestBadge(id = "badge1", unlockedAt = now.minusSeconds(100)),
                    createTestBadge(id = "badge2", unlockedAt = now.minusSeconds(50)),
                    createTestBadge(id = "badge3", unlockedAt = null),
                ),
            )

            val recentlyUnlocked = badgeDao.getRecentlyUnlockedBadges().first()

            assertEquals(2, recentlyUnlocked.size)
            // Should be ordered by unlockedAt DESC (most recent first)
            assertEquals("badge2", recentlyUnlocked[0].id)
            assertEquals("badge1", recentlyUnlocked[1].id)
        }

    @Test
    fun getRecentlyUnlockedBadges_limitsResults() =
        runTest {
            val now = Instant.now()
            badgeDao.insertBadges(
                listOf(
                    createTestBadge(id = "badge1", unlockedAt = now.minusSeconds(100)),
                    createTestBadge(id = "badge2", unlockedAt = now.minusSeconds(75)),
                    createTestBadge(id = "badge3", unlockedAt = now.minusSeconds(50)),
                    createTestBadge(id = "badge4", unlockedAt = now.minusSeconds(25)),
                ),
            )

            val recentlyUnlocked = badgeDao.getRecentlyUnlockedBadges(limit = 2).first()

            assertEquals("Should return only 2 most recent badges", 2, recentlyUnlocked.size)
            assertEquals("badge4", recentlyUnlocked[0].id)
            assertEquals("badge3", recentlyUnlocked[1].id)
        }

    @Test
    fun getBadgesByCategory_filtersCorrectly() =
        runTest {
            badgeDao.insertBadges(
                listOf(
                    createTestBadge(id = "badge1", category = BadgeCategory.GETTING_STARTED),
                    createTestBadge(id = "badge2", category = BadgeCategory.VOLUME),
                    createTestBadge(id = "badge3", category = BadgeCategory.GETTING_STARTED),
                ),
            )

            val gettingStarted = badgeDao.getBadgesByCategory(BadgeCategory.GETTING_STARTED).first()
            val volume = badgeDao.getBadgesByCategory(BadgeCategory.VOLUME).first()

            assertEquals(2, gettingStarted.size)
            assertEquals(1, volume.size)
            assertEquals(BadgeCategory.VOLUME, volume[0].category)
        }

    @Test
    fun getUnlockedCount_countsCorrectly() =
        runTest {
            val now = Instant.now()
            badgeDao.insertBadges(
                listOf(
                    createTestBadge(id = "badge1", unlockedAt = now),
                    createTestBadge(id = "badge2", unlockedAt = now),
                    createTestBadge(id = "badge3", unlockedAt = null),
                    createTestBadge(id = "badge4", unlockedAt = null),
                ),
            )

            val unlockedCount = badgeDao.getUnlockedCount().first()

            assertEquals(2, unlockedCount)
        }

    @Test
    fun getUnlockedCount_emptyDatabase_returnsZero() =
        runTest {
            val count = badgeDao.getUnlockedCount().first()

            assertEquals(0, count)
        }

    @Test
    fun getTotalCount_countsAllBadges() =
        runTest {
            badgeDao.insertBadges(
                listOf(
                    createTestBadge(id = "badge1"),
                    createTestBadge(id = "badge2"),
                    createTestBadge(id = "badge3"),
                ),
            )

            val totalCount = badgeDao.getTotalCount().first()

            assertEquals(3, totalCount)
        }

    @Test
    fun getTotalCount_emptyDatabase_returnsZero() =
        runTest {
            val count = badgeDao.getTotalCount().first()

            assertEquals(0, count)
        }

    @Test
    fun updateBadge_modifiesExistingBadge() =
        runTest {
            val badge = createTestBadge(id = "badge1", name = "Original Name")
            badgeDao.insertBadges(listOf(badge))

            val updated = badge.copy(name = "Updated Name")
            badgeDao.updateBadge(updated)

            val retrieved = badgeDao.getAllBadges().first()
            assertEquals("Updated Name", retrieved[0].name)
        }

    @Test
    fun unlockBadge_setsUnlockedAt() =
        runTest {
            val badge = createTestBadge(id = "badge1", unlockedAt = null)
            badgeDao.insertBadges(listOf(badge))

            val unlockTime = Instant.now()
            badgeDao.unlockBadge("badge1", unlockTime)

            val retrieved = badgeDao.getAllBadges().first()
            assertNotNull("Badge should have unlockedAt set", retrieved[0].unlockedAt)
            assertEquals(unlockTime, retrieved[0].unlockedAt)
        }

    @Test
    fun insertBadges_withConflict_replacesBadge() =
        runTest {
            val badge1 = createTestBadge(id = "badge1", name = "Original")
            badgeDao.insertBadges(listOf(badge1))

            val badge2 = createTestBadge(id = "badge1", name = "Replacement")
            badgeDao.insertBadges(listOf(badge2))

            val badges = badgeDao.getAllBadges().first()
            assertEquals(1, badges.size)
            assertEquals("Replacement", badges[0].name)
        }

    @Test
    fun badgeEntity_storesAllFields() =
        runTest {
            val unlockedAt = Instant.ofEpochMilli(5000)
            val badge =
                BadgeEntity(
                    id = "test_badge",
                    name = "Test Badge",
                    description = "Test description",
                    icon = "🎯",
                    category = BadgeCategory.SPEED_ACCURACY,
                    requirementType = "ProblemCount",
                    requirementData = "{\"count\":25}",
                    unlockedAt = unlockedAt,
                )

            badgeDao.insertBadges(listOf(badge))
            val retrieved = badgeDao.getAllBadges().first()[0]

            assertNotNull(retrieved)
            assertEquals("test_badge", retrieved.id)
            assertEquals("Test Badge", retrieved.name)
            assertEquals("Test description", retrieved.description)
            assertEquals("🎯", retrieved.icon)
            assertEquals(BadgeCategory.SPEED_ACCURACY, retrieved.category)
            assertEquals("ProblemCount", retrieved.requirementType)
            assertEquals("{\"count\":25}", retrieved.requirementData)
            assertEquals(5000L, retrieved.unlockedAt?.toEpochMilli())
        }

    @Test
    fun badgeEntity_withNullUnlockedAt_storesCorrectly() =
        runTest {
            val badge =
                BadgeEntity(
                    id = "locked_badge",
                    name = "Locked Badge",
                    description = "Not yet unlocked",
                    icon = "🔒",
                    category = BadgeCategory.STREAK,
                    requirementType = "DailyStreak",
                    requirementData = "{\"days\":7}",
                    unlockedAt = null,
                )

            badgeDao.insertBadges(listOf(badge))
            val retrieved = badgeDao.getAllBadges().first()[0]

            assertNull("Badge should have null unlockedAt", retrieved.unlockedAt)
        }

    // Helper function to create test badge
    private fun createTestBadge(
        id: String = "test_badge",
        name: String = "Test Badge",
        description: String = "Test description",
        icon: String = "🎯",
        category: BadgeCategory = BadgeCategory.GETTING_STARTED,
        requirementType: String = "ProblemCount",
        requirementData: String = "{\"count\":10}",
        unlockedAt: Instant? = null,
    ): BadgeEntity =
        BadgeEntity(
            id = id,
            name = name,
            description = description,
            icon = icon,
            category = category,
            requirementType = requirementType,
            requirementData = requirementData,
            unlockedAt = unlockedAt,
        )
}
