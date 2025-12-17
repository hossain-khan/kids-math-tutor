package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeDefinitionsTest {
    @Test
    fun `getAllBadges returns 15 badges`() {
        val badges = BadgeDefinitions.getAllBadges()

        assertEquals("Should have exactly 15 badges", 15, badges.size)
    }

    @Test
    fun `getAllBadges returns unique badge ids`() {
        val badges = BadgeDefinitions.getAllBadges()
        val ids = badges.map { it.id }

        assertEquals("All badge IDs should be unique", ids.size, ids.distinct().size)
    }

    @Test
    fun `getAllBadges has correct number of badges per category`() {
        val badges = BadgeDefinitions.getAllBadges()

        val gettingStarted = badges.filter { it.category == BadgeCategory.GETTING_STARTED }
        val volume = badges.filter { it.category == BadgeCategory.VOLUME }
        val operationMastery = badges.filter { it.category == BadgeCategory.OPERATION_MASTERY }
        val speedAccuracy = badges.filter { it.category == BadgeCategory.SPEED_ACCURACY }
        val streak = badges.filter { it.category == BadgeCategory.STREAK }

        assertEquals("Should have 3 Getting Started badges", 3, gettingStarted.size)
        assertEquals("Should have 4 Volume badges", 4, volume.size)
        assertEquals("Should have 3 Operation Mastery badges", 3, operationMastery.size)
        assertEquals("Should have 3 Speed & Accuracy badges", 3, speedAccuracy.size)
        assertEquals("Should have 2 Streak badges", 2, streak.size)
    }

    @Test
    fun `getAllBadges returns badges with all required fields`() {
        val badges = BadgeDefinitions.getAllBadges()

        badges.forEach { badge ->
            assertTrue("Badge ID should not be empty", badge.id.isNotEmpty())
            assertTrue("Badge name should not be empty", badge.name.isNotEmpty())
            assertTrue("Badge description should not be empty", badge.description.isNotEmpty())
            assertTrue("Badge icon should not be empty", badge.icon.isNotEmpty())
        }
    }

    @Test
    fun `getAllBadges returns all badges unlocked by default`() {
        val badges = BadgeDefinitions.getAllBadges()

        badges.forEach { badge ->
            assertEquals("Badges should not be unlocked by default", null, badge.unlockedAt)
        }
    }

    @Test
    fun `first_steps badge has correct properties`() {
        val badges = BadgeDefinitions.getAllBadges()
        val firstSteps = badges.find { it.id == "first_steps" }

        assertEquals("First Steps", firstSteps?.name)
        assertEquals("Solve your first problem", firstSteps?.description)
        assertEquals("🎯", firstSteps?.icon)
        assertEquals(BadgeCategory.GETTING_STARTED, firstSteps?.category)
        assertTrue(firstSteps?.requirement is BadgeRequirement.ProblemCount)
        assertEquals(1, (firstSteps?.requirement as BadgeRequirement.ProblemCount).count)
    }

    @Test
    fun `math_legend badge has correct properties`() {
        val badges = BadgeDefinitions.getAllBadges()
        val mathLegend = badges.find { it.id == "math_legend" }

        assertEquals("Math Legend", mathLegend?.name)
        assertEquals("Solve 500 total problems", mathLegend?.description)
        assertEquals("🦅", mathLegend?.icon)
        assertEquals(BadgeCategory.VOLUME, mathLegend?.category)
        assertTrue(mathLegend?.requirement is BadgeRequirement.ProblemCount)
        assertEquals(500, (mathLegend?.requirement as BadgeRequirement.ProblemCount).count)
    }

    @Test
    fun `addition_expert badge has correct properties`() {
        val badges = BadgeDefinitions.getAllBadges()
        val additionExpert = badges.find { it.id == "addition_expert" }

        assertEquals("Addition Expert", additionExpert?.name)
        assertEquals("Solve 50 addition problems", additionExpert?.description)
        assertEquals("➕", additionExpert?.icon)
        assertEquals(BadgeCategory.OPERATION_MASTERY, additionExpert?.category)
        assertTrue(additionExpert?.requirement is BadgeRequirement.OperationCount)
        val requirement = additionExpert?.requirement as BadgeRequirement.OperationCount
        assertEquals(MathOperation.ADDITION, requirement.operation)
        assertEquals(50, requirement.count)
    }

    @Test
    fun `dedication_award badge has correct properties`() {
        val badges = BadgeDefinitions.getAllBadges()
        val dedication = badges.find { it.id == "dedication_award" }

        assertEquals("Dedication Award", dedication?.name)
        assertEquals("Practice 7 days in a row", dedication?.description)
        assertEquals("🏆", dedication?.icon)
        assertEquals(BadgeCategory.STREAK, dedication?.category)
        assertTrue(dedication?.requirement is BadgeRequirement.DailyStreak)
        assertEquals(7, (dedication?.requirement as BadgeRequirement.DailyStreak).days)
    }
}
