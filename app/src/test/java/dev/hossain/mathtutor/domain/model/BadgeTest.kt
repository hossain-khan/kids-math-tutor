package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class BadgeTest {
    @Test
    fun `isUnlocked returns true when unlockedAt is set`() {
        val badge =
            Badge(
                id = "test_badge",
                name = "Test Badge",
                description = "Test description",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(10),
                unlockedAt = Instant.now(),
            )

        assertTrue("Badge should be unlocked when unlockedAt is set", badge.isUnlocked())
    }

    @Test
    fun `isUnlocked returns false when unlockedAt is null`() {
        val badge =
            Badge(
                id = "test_badge",
                name = "Test Badge",
                description = "Test description",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(10),
                unlockedAt = null,
            )

        assertFalse("Badge should be locked when unlockedAt is null", badge.isUnlocked())
    }

    @Test
    fun `badge with all properties is created correctly`() {
        val unlockedAt = Instant.ofEpochMilli(1000000)
        val badge =
            Badge(
                id = "first_steps",
                name = "First Steps",
                description = "Solve your first problem",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(1),
                unlockedAt = unlockedAt,
            )

        assertEquals("first_steps", badge.id)
        assertEquals("First Steps", badge.name)
        assertEquals("Solve your first problem", badge.description)
        assertEquals("🎯", badge.icon)
        assertEquals(BadgeCategory.GETTING_STARTED, badge.category)
        assertTrue(badge.requirement is BadgeRequirement.ProblemCount)
        assertEquals(1, (badge.requirement as BadgeRequirement.ProblemCount).count)
        assertEquals(unlockedAt, badge.unlockedAt)
    }

    @Test
    fun `badge defaults unlockedAt to null`() {
        val badge =
            Badge(
                id = "test_badge",
                name = "Test Badge",
                description = "Test description",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(10),
            )

        assertFalse("Badge should be locked by default", badge.isUnlocked())
    }
}
