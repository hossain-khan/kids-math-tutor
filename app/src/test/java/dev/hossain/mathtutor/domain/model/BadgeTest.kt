package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
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

        assertThat(badge.isUnlocked()).isTrue()
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

        assertThat(badge.isUnlocked()).isFalse()
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

        assertThat(badge.id).isEqualTo("first_steps")
        assertThat(badge.name).isEqualTo("First Steps")
        assertThat(badge.description).isEqualTo("Solve your first problem")
        assertThat(badge.icon).isEqualTo("🎯")
        assertThat(badge.category).isEqualTo(BadgeCategory.GETTING_STARTED)
        assertThat(badge.requirement is BadgeRequirement.ProblemCount).isTrue()
        assertThat((badge.requirement as BadgeRequirement.ProblemCount).count).isEqualTo(1)
        assertThat(badge.unlockedAt).isEqualTo(unlockedAt)
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

        assertThat(badge.isUnlocked()).isFalse()
    }
}
