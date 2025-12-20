package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BadgeDefinitionsTest {
    @Test
    fun `getAllBadges returns 19 badges`() {
        val badges = BadgeDefinitions.getAllBadges()

        assertThat(19, badges.size).isEqualTo("Should have exactly 19 badges")
    }

    @Test
    fun `getAllBadges returns unique badge ids`() {
        val badges = BadgeDefinitions.getAllBadges()
        val ids = badges.map { it.id }

        assertThat(ids.size, ids.distinct().isEqualTo("All badge IDs should be unique").size)
    }

    @Test
    fun `getAllBadges has correct number of badges per category`() {
        val badges = BadgeDefinitions.getAllBadges()

        val gettingStarted = badges.filter { it.category == BadgeCategory.GETTING_STARTED }
        val volume = badges.filter { it.category == BadgeCategory.VOLUME }
        val operationMastery = badges.filter { it.category == BadgeCategory.OPERATION_MASTERY }
        val speedAccuracy = badges.filter { it.category == BadgeCategory.SPEED_ACCURACY }
        val streak = badges.filter { it.category == BadgeCategory.STREAK }
        val games = badges.filter { it.category == BadgeCategory.GAMES }

        assertThat(3, gettingStarted.size).isEqualTo("Should have 3 Getting Started badges")
        assertThat(4, volume.size).isEqualTo("Should have 4 Volume badges")
        assertThat(3, operationMastery.size).isEqualTo("Should have 3 Operation Mastery badges")
        assertThat(3, speedAccuracy.size).isEqualTo("Should have 3 Speed & Accuracy badges")
        assertThat(2, streak.size).isEqualTo("Should have 2 Streak badges")
        assertThat(4, games.size).isEqualTo("Should have 4 Games badges")
    }

    @Test
    fun `getAllBadges returns badges with all required fields`() {
        val badges = BadgeDefinitions.getAllBadges()

        badges.forEach { badge ->
            assertThat("Badge ID should not be empty", badge.id.isNotEmpty()).isTrue()
            assertThat("Badge name should not be empty", badge.name.isNotEmpty()).isTrue()
            assertThat("Badge description should not be empty", badge.description.isNotEmpty()).isTrue()
            assertThat("Badge icon should not be empty", badge.icon.isNotEmpty()).isTrue()
        }
    }

    @Test
    fun `getAllBadges returns all badges unlocked by default`() {
        val badges = BadgeDefinitions.getAllBadges()

        badges.forEach { badge ->
            assertThat(null, badge.unlockedAt).isEqualTo("Badges should not be unlocked by default")
        }
    }

    @Test
    fun `first_steps badge has correct properties`() {
        val badges = BadgeDefinitions.getAllBadges()
        val firstSteps = badges.find { it.id == "first_steps" }

        assertThat(firstSteps?.name).isEqualTo("First Steps")
        assertThat(firstSteps?.description).isEqualTo("Solve your first problem")
        assertThat(firstSteps?.icon).isEqualTo("🎯")
        assertThat(firstSteps?.category).isEqualTo(BadgeCategory.GETTING_STARTED)
        assertThat(firstSteps?.requirement is BadgeRequirement.ProblemCount).isTrue()
        assertThat((firstSteps?.requirement as BadgeRequirement.ProblemCount).isEqualTo(1).count)
    }

    @Test
    fun `math_legend badge has correct properties`() {
        val badges = BadgeDefinitions.getAllBadges()
        val mathLegend = badges.find { it.id == "math_legend" }

        assertThat(mathLegend?.name).isEqualTo("Math Legend")
        assertThat(mathLegend?.description).isEqualTo("Solve 500 total problems")
        assertThat(mathLegend?.icon).isEqualTo("🦅")
        assertThat(mathLegend?.category).isEqualTo(BadgeCategory.VOLUME)
        assertThat(mathLegend?.requirement is BadgeRequirement.ProblemCount).isTrue()
        assertThat((mathLegend?.requirement as BadgeRequirement.ProblemCount).isEqualTo(500).count)
    }

    @Test
    fun `addition_expert badge has correct properties`() {
        val badges = BadgeDefinitions.getAllBadges()
        val additionExpert = badges.find { it.id == "addition_expert" }

        assertThat(additionExpert?.name).isEqualTo("Addition Expert")
        assertThat(additionExpert?.description).isEqualTo("Solve 50 addition problems")
        assertThat(additionExpert?.icon).isEqualTo("➕")
        assertThat(additionExpert?.category).isEqualTo(BadgeCategory.OPERATION_MASTERY)
        assertThat(additionExpert?.requirement is BadgeRequirement.OperationCount).isTrue()
        val requirement = additionExpert?.requirement as BadgeRequirement.OperationCount
        assertThat(requirement.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(requirement.count).isEqualTo(50)
    }

    @Test
    fun `dedication_award badge has correct properties`() {
        val badges = BadgeDefinitions.getAllBadges()
        val dedication = badges.find { it.id == "dedication_award" }

        assertThat(dedication?.name).isEqualTo("Dedication Award")
        assertThat(dedication?.description).isEqualTo("Practice 7 days in a row")
        assertThat(dedication?.icon).isEqualTo("🏆")
        assertThat(dedication?.category).isEqualTo(BadgeCategory.STREAK)
        assertThat(dedication?.requirement is BadgeRequirement.DailyStreak).isTrue()
        assertThat((dedication?.requirement as BadgeRequirement.DailyStreak).isEqualTo(7).days)
    }
}
