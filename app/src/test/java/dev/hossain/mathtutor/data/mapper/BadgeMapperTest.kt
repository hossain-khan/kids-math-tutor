package dev.hossain.mathtutor.data.mapper

import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class BadgeMapperTest {
    @Test
    fun `toDomain converts ProblemCount badge correctly`() {
        val entity =
            BadgeEntity(
                id = "test_badge",
                name = "Test Badge",
                description = "Test description",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirementType = "ProblemCount",
                requirementData = "count=10",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assertEquals("test_badge", badge.id)
        assertEquals("Test Badge", badge.name)
        assertEquals("Test description", badge.description)
        assertEquals("🎯", badge.icon)
        assertEquals(BadgeCategory.GETTING_STARTED, badge.category)
        assert(badge.requirement is BadgeRequirement.ProblemCount)
        assertEquals(10, (badge.requirement as BadgeRequirement.ProblemCount).count)
        assertNull(badge.unlockedAt)
    }

    @Test
    fun `toDomain converts OperationCount badge correctly`() {
        val entity =
            BadgeEntity(
                id = "addition_badge",
                name = "Addition Badge",
                description = "Complete addition problems",
                icon = "➕",
                category = BadgeCategory.OPERATION_MASTERY,
                requirementType = "OperationCount",
                requirementData = "operation=ADDITION,count=50",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.OperationCount)
        val requirement = badge.requirement as BadgeRequirement.OperationCount
        assertEquals(MathOperation.ADDITION, requirement.operation)
        assertEquals(50, requirement.count)
    }

    @Test
    fun `toDomain converts ConsecutiveCorrect badge correctly`() {
        val entity =
            BadgeEntity(
                id = "streak_badge",
                name = "Streak Badge",
                description = "Get consecutive correct answers",
                icon = "🔥",
                category = BadgeCategory.SPEED_ACCURACY,
                requirementType = "ConsecutiveCorrect",
                requirementData = "count=5",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.ConsecutiveCorrect)
        assertEquals(5, (badge.requirement as BadgeRequirement.ConsecutiveCorrect).count)
    }

    @Test
    fun `toDomain converts SessionAccuracy badge correctly`() {
        val entity =
            BadgeEntity(
                id = "accuracy_badge",
                name = "Accuracy Badge",
                description = "High accuracy sessions",
                icon = "🎯",
                category = BadgeCategory.SPEED_ACCURACY,
                requirementType = "SessionAccuracy",
                requirementData = "percentage=90.0,sessionCount=3",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.SessionAccuracy)
        val requirement = badge.requirement as BadgeRequirement.SessionAccuracy
        assertEquals(90.0f, requirement.percentage, 0.01f)
        assertEquals(3, requirement.sessionCount)
    }

    @Test
    fun `toDomain converts DailyStreak badge correctly`() {
        val entity =
            BadgeEntity(
                id = "daily_badge",
                name = "Daily Badge",
                description = "Practice daily",
                icon = "🔥",
                category = BadgeCategory.STREAK,
                requirementType = "DailyStreak",
                requirementData = "days=7",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.DailyStreak)
        assertEquals(7, (badge.requirement as BadgeRequirement.DailyStreak).days)
    }

    @Test
    fun `toDomain converts ProblemSpeed badge correctly`() {
        val entity =
            BadgeEntity(
                id = "speed_badge",
                name = "Speed Badge",
                description = "Solve quickly",
                icon = "⚡",
                category = BadgeCategory.SPEED_ACCURACY,
                requirementType = "ProblemSpeed",
                requirementData = "maxSeconds=3",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.ProblemSpeed)
        assertEquals(3, (badge.requirement as BadgeRequirement.ProblemSpeed).maxSeconds)
    }

    @Test
    fun `toDomain converts MixedSessions badge correctly`() {
        val entity =
            BadgeEntity(
                id = "mixed_badge",
                name = "Mixed Badge",
                description = "Complete mixed sessions",
                icon = "🔢",
                category = BadgeCategory.OPERATION_MASTERY,
                requirementType = "MixedSessions",
                requirementData = "count=10",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.MixedSessions)
        assertEquals(10, (badge.requirement as BadgeRequirement.MixedSessions).count)
    }

    @Test
    fun `toDomain preserves unlockedAt timestamp`() {
        val unlockTime = Instant.ofEpochMilli(1000000)
        val entity =
            BadgeEntity(
                id = "unlocked_badge",
                name = "Unlocked Badge",
                description = "Already unlocked",
                icon = "✅",
                category = BadgeCategory.GETTING_STARTED,
                requirementType = "ProblemCount",
                requirementData = "count=1",
                unlockedAt = unlockTime,
            )

        val badge = BadgeMapper.toDomain(entity)

        assertNotNull(badge.unlockedAt)
        assertEquals(unlockTime, badge.unlockedAt)
    }

    @Test
    fun `toEntity converts badge with ProblemCount requirement correctly`() {
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

        val entity = BadgeMapper.toEntity(badge)

        assertEquals("test_badge", entity.id)
        assertEquals("Test Badge", entity.name)
        assertEquals("Test description", entity.description)
        assertEquals("🎯", entity.icon)
        assertEquals(BadgeCategory.GETTING_STARTED, entity.category)
        assertEquals("ProblemCount", entity.requirementType)
        assert(entity.requirementData.contains("count=10"))
        assertNull(entity.unlockedAt)
    }

    @Test
    fun `toEntity converts badge with OperationCount requirement correctly`() {
        val badge =
            Badge(
                id = "addition_badge",
                name = "Addition Badge",
                description = "Complete addition problems",
                icon = "➕",
                category = BadgeCategory.OPERATION_MASTERY,
                requirement = BadgeRequirement.OperationCount(MathOperation.ADDITION, 50),
                unlockedAt = null,
            )

        val entity = BadgeMapper.toEntity(badge)

        assertEquals("OperationCount", entity.requirementType)
        assert(entity.requirementData.contains("operation=ADDITION"))
        assert(entity.requirementData.contains("count=50"))
    }

    @Test
    fun `toEntity converts badge with SessionAccuracy requirement correctly`() {
        val badge =
            Badge(
                id = "accuracy_badge",
                name = "Accuracy Badge",
                description = "High accuracy sessions",
                icon = "🎯",
                category = BadgeCategory.SPEED_ACCURACY,
                requirement = BadgeRequirement.SessionAccuracy(90f, 3),
                unlockedAt = null,
            )

        val entity = BadgeMapper.toEntity(badge)

        assertEquals("SessionAccuracy", entity.requirementType)
        assert(entity.requirementData.contains("percentage=90"))
        assert(entity.requirementData.contains("sessionCount=3"))
    }

    @Test
    fun `toEntity preserves unlockedAt timestamp`() {
        val unlockTime = Instant.ofEpochMilli(1000000)
        val badge =
            Badge(
                id = "unlocked_badge",
                name = "Unlocked Badge",
                description = "Already unlocked",
                icon = "✅",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(1),
                unlockedAt = unlockTime,
            )

        val entity = BadgeMapper.toEntity(badge)

        assertNotNull(entity.unlockedAt)
        assertEquals(unlockTime, entity.unlockedAt)
    }

    @Test
    fun `round trip conversion preserves badge data`() {
        val originalBadge =
            Badge(
                id = "test_badge",
                name = "Test Badge",
                description = "Test description",
                icon = "🎯",
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(25),
                unlockedAt = null,
            )

        val entity = BadgeMapper.toEntity(originalBadge)
        val convertedBadge = BadgeMapper.toDomain(entity)

        assertEquals(originalBadge.id, convertedBadge.id)
        assertEquals(originalBadge.name, convertedBadge.name)
        assertEquals(originalBadge.description, convertedBadge.description)
        assertEquals(originalBadge.icon, convertedBadge.icon)
        assertEquals(originalBadge.category, convertedBadge.category)
        assertEquals(originalBadge.unlockedAt, convertedBadge.unlockedAt)

        val originalReq = originalBadge.requirement as BadgeRequirement.ProblemCount
        val convertedReq = convertedBadge.requirement as BadgeRequirement.ProblemCount
        assertEquals(originalReq.count, convertedReq.count)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws exception for unknown requirement type`() {
        val entity =
            BadgeEntity(
                id = "invalid_badge",
                name = "Invalid Badge",
                description = "Invalid requirement type",
                icon = "❌",
                category = BadgeCategory.GETTING_STARTED,
                requirementType = "UnknownType",
                requirementData = "count=10",
                unlockedAt = null,
            )

        BadgeMapper.toDomain(entity)
    }
}
