package dev.hossain.mathtutor.data.mapper

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.data.local.entity.BadgeEntity
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.BadgeRequirement
import dev.hossain.mathtutor.domain.model.MathOperation
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
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.FIRST_STEPS.name,
                category = BadgeCategory.GETTING_STARTED,
                requirementType = "ProblemCount",
                requirementData = "count=10",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assertThat(badge.id).isEqualTo("test_badge")
        assertThat(badge.name).isEqualTo("Test Badge")
        assertThat(badge.description).isEqualTo("Test description")
        assertThat(badge.icon).isEqualTo(dev.hossain.mathtutor.domain.model.BadgeIcon.FIRST_STEPS.name)
        assertThat(badge.category).isEqualTo(BadgeCategory.GETTING_STARTED)
        assert(badge.requirement is BadgeRequirement.ProblemCount)
        assertThat((badge.requirement as BadgeRequirement.ProblemCount).count).isEqualTo(10)
        assertThat(badge.unlockedAt).isNull()
    }

    @Test
    fun `toDomain converts OperationCount badge correctly`() {
        val entity =
            BadgeEntity(
                id = "addition_badge",
                name = "Addition Badge",
                description = "Complete addition problems",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.ADDITION_EXPERT.name,
                category = BadgeCategory.OPERATION_MASTERY,
                requirementType = "OperationCount",
                requirementData = "operation=ADDITION,count=50",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.OperationCount)
        val requirement = badge.requirement as BadgeRequirement.OperationCount
        assertThat(requirement.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(requirement.count).isEqualTo(50)
    }

    @Test
    fun `toDomain converts ConsecutiveCorrect badge correctly`() {
        val entity =
            BadgeEntity(
                id = "streak_badge",
                name = "Streak Badge",
                description = "Get consecutive correct answers",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.STREAK_STARTER.name,
                category = BadgeCategory.SPEED_ACCURACY,
                requirementType = "ConsecutiveCorrect",
                requirementData = "count=5",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.ConsecutiveCorrect)
        assertThat((badge.requirement as BadgeRequirement.ConsecutiveCorrect).count).isEqualTo(5)
    }

    @Test
    fun `toDomain converts SessionAccuracy badge correctly`() {
        val entity =
            BadgeEntity(
                id = "accuracy_badge",
                name = "Accuracy Badge",
                description = "High accuracy sessions",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.FIRST_STEPS.name,
                category = BadgeCategory.SPEED_ACCURACY,
                requirementType = "SessionAccuracy",
                requirementData = "percentage=90.0,sessionCount=3",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.SessionAccuracy)
        val requirement = badge.requirement as BadgeRequirement.SessionAccuracy
        assertThat(requirement.percentage).isWithin(0.01f).of(90.0f)
        assertThat(requirement.sessionCount).isEqualTo(3)
    }

    @Test
    fun `toDomain converts DailyStreak badge correctly`() {
        val entity =
            BadgeEntity(
                id = "daily_badge",
                name = "Daily Badge",
                description = "Practice daily",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.STREAK_STARTER.name,
                category = BadgeCategory.STREAK,
                requirementType = "DailyStreak",
                requirementData = "days=7",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.DailyStreak)
        assertThat((badge.requirement as BadgeRequirement.DailyStreak).days).isEqualTo(7)
    }

    @Test
    fun `toDomain converts ProblemSpeed badge correctly`() {
        val entity =
            BadgeEntity(
                id = "speed_badge",
                name = "Speed Badge",
                description = "Solve quickly",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.QUICK_THINKER.name,
                category = BadgeCategory.SPEED_ACCURACY,
                requirementType = "ProblemSpeed",
                requirementData = "maxSeconds=3",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.ProblemSpeed)
        assertThat((badge.requirement as BadgeRequirement.ProblemSpeed).maxSeconds).isEqualTo(3)
    }

    @Test
    fun `toDomain converts MixedSessions badge correctly`() {
        val entity =
            BadgeEntity(
                id = "mixed_badge",
                name = "Mixed Badge",
                description = "Complete mixed sessions",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.MIX_MASTER.name,
                category = BadgeCategory.OPERATION_MASTERY,
                requirementType = "MixedSessions",
                requirementData = "count=10",
                unlockedAt = null,
            )

        val badge = BadgeMapper.toDomain(entity)

        assert(badge.requirement is BadgeRequirement.MixedSessions)
        assertThat((badge.requirement as BadgeRequirement.MixedSessions).count).isEqualTo(10)
    }

    @Test
    fun `toDomain preserves unlockedAt timestamp`() {
        val unlockTime = Instant.ofEpochMilli(1000000)
        val entity =
            BadgeEntity(
                id = "unlocked_badge",
                name = "Unlocked Badge",
                description = "Already unlocked",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.PERFECT_START.name,
                category = BadgeCategory.GETTING_STARTED,
                requirementType = "ProblemCount",
                requirementData = "count=1",
                unlockedAt = unlockTime,
            )

        val badge = BadgeMapper.toDomain(entity)

        assertThat(badge.unlockedAt).isNotNull()
        assertThat(badge.unlockedAt).isEqualTo(unlockTime)
    }

    @Test
    fun `toEntity converts badge with ProblemCount requirement correctly`() {
        val badge =
            Badge(
                id = "test_badge",
                name = "Test Badge",
                description = "Test description",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.FIRST_STEPS.name,
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(10),
                unlockedAt = null,
            )

        val entity = BadgeMapper.toEntity(badge)

        assertThat(entity.id).isEqualTo("test_badge")
        assertThat(entity.name).isEqualTo("Test Badge")
        assertThat(entity.description).isEqualTo("Test description")
        assertThat(entity.icon).isEqualTo(dev.hossain.mathtutor.domain.model.BadgeIcon.FIRST_STEPS.name)
        assertThat(entity.category).isEqualTo(BadgeCategory.GETTING_STARTED)
        assertThat(entity.requirementType).isEqualTo("ProblemCount")
        assert(entity.requirementData.contains("count=10"))
        assertThat(entity.unlockedAt).isNull()
    }

    @Test
    fun `toEntity converts badge with OperationCount requirement correctly`() {
        val badge =
            Badge(
                id = "addition_badge",
                name = "Addition Badge",
                description = "Complete addition problems",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.ADDITION_EXPERT.name,
                category = BadgeCategory.OPERATION_MASTERY,
                requirement = BadgeRequirement.OperationCount(MathOperation.ADDITION, 50),
                unlockedAt = null,
            )

        val entity = BadgeMapper.toEntity(badge)

        assertThat(entity.requirementType).isEqualTo("OperationCount")
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

        assertThat(entity.requirementType).isEqualTo("SessionAccuracy")
        assert(entity.requirementData.contains("percentage=90.0"))
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
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.PERFECT_START.name,
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(1),
                unlockedAt = unlockTime,
            )

        val entity = BadgeMapper.toEntity(badge)

        assertThat(entity.unlockedAt).isNotNull()
        assertThat(entity.unlockedAt).isEqualTo(unlockTime)
    }

    @Test
    fun `round trip conversion preserves badge data`() {
        val originalBadge =
            Badge(
                id = "test_badge",
                name = "Test Badge",
                description = "Test description",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.FIRST_STEPS.name,
                category = BadgeCategory.GETTING_STARTED,
                requirement = BadgeRequirement.ProblemCount(25),
                unlockedAt = null,
            )

        val entity = BadgeMapper.toEntity(originalBadge)
        val convertedBadge = BadgeMapper.toDomain(entity)

        assertThat(convertedBadge.id).isEqualTo(originalBadge.id)
        assertThat(convertedBadge.name).isEqualTo(originalBadge.name)
        assertThat(convertedBadge.description).isEqualTo(originalBadge.description)
        assertThat(convertedBadge.icon).isEqualTo(originalBadge.icon)
        assertThat(convertedBadge.category).isEqualTo(originalBadge.category)
        assertThat(convertedBadge.unlockedAt).isEqualTo(originalBadge.unlockedAt)

        val originalReq = originalBadge.requirement as BadgeRequirement.ProblemCount
        val convertedReq = convertedBadge.requirement as BadgeRequirement.ProblemCount
        assertThat(convertedReq.count).isEqualTo(originalReq.count)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws exception for unknown requirement type`() {
        val entity =
            BadgeEntity(
                id = "invalid_badge",
                name = "Invalid Badge",
                description = "Invalid requirement type",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.PERFECTIONIST.name,
                category = BadgeCategory.GETTING_STARTED,
                requirementType = "UnknownType",
                requirementData = "count=10",
                unlockedAt = null,
            )

        BadgeMapper.toDomain(entity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws exception for malformed requirement data`() {
        val entity =
            BadgeEntity(
                id = "malformed_badge",
                name = "Malformed Badge",
                description = "Malformed requirement data",
                icon = dev.hossain.mathtutor.domain.model.BadgeIcon.PERFECTIONIST.name,
                category = BadgeCategory.GETTING_STARTED,
                requirementType = "ProblemCount",
                requirementData = "invalid_data_without_equals_sign",
                unlockedAt = null,
            )

        BadgeMapper.toDomain(entity)
    }
}
