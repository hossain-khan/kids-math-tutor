package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BadgeDefinitionsTest {
    @Test
    fun `getAllBadges returns 19 badges`() {
        val badges = BadgeDefinitions.getAllBadges()

        assertThat(badges.size).isEqualTo(19)
    }

    @Test
    fun `getAllBadges returns unique badge ids`() {
        val badges = BadgeDefinitions.getAllBadges()
        val ids = badges.map { it.id }

        assertThat(ids.size).isEqualTo(ids.distinct().size)
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

        assertThat(gettingStarted.size).isEqualTo(3)
        assertThat(volume.size).isEqualTo(4)
        assertThat(operationMastery.size).isEqualTo(3)
        assertThat(speedAccuracy.size).isEqualTo(3)
        assertThat(streak.size).isEqualTo(2)
        assertThat(games.size).isEqualTo(4)
    }

    @Test
    fun `getAllBadges returns badges with all required fields`() {
        val badges = BadgeDefinitions.getAllBadges()

        badges.forEach { badge ->
            assertThat(badge.id.isNotEmpty()).isTrue()
            assertThat(badge.name.isNotEmpty()).isTrue()
            assertThat(badge.description.isNotEmpty()).isTrue()
            assertThat(badge.icon.isNotEmpty()).isTrue()
        }
    }

    @Test
    fun `getAllBadges returns all badges unlocked by default`() {
        val badges = BadgeDefinitions.getAllBadges()

        badges.forEach { badge ->
            assertThat(badge.unlockedAt).isEqualTo(null)
        }
    }

    @Test
    fun `first_steps badge has correct properties`() {
        val badges = BadgeDefinitions.getAllBadges()
        val firstSteps = badges.find { it.id == "first_steps" }

        assertThat(firstSteps?.name).isEqualTo("First Steps")
        assertThat(firstSteps?.description).isEqualTo("Solve your first problem")
        assertThat(firstSteps?.icon).isEqualTo(dev.hossain.mathtutor.domain.model.BadgeIcon.FIRST_STEPS.name)
        assertThat(firstSteps?.category).isEqualTo(BadgeCategory.GETTING_STARTED)
        assertThat(firstSteps?.requirement is BadgeRequirement.ProblemCount).isTrue()
        assertThat((firstSteps?.requirement as BadgeRequirement.ProblemCount).count).isEqualTo(1)
    }

    @Test
    fun `math_legend badge has correct properties`() {
        val badges = BadgeDefinitions.getAllBadges()
        val mathLegend = badges.find { it.id == "math_legend" }

        assertThat(mathLegend?.name).isEqualTo("Math Legend")
        assertThat(mathLegend?.description).isEqualTo("Solve 500 total problems")
        assertThat(mathLegend?.icon).isEqualTo(dev.hossain.mathtutor.domain.model.BadgeIcon.MATH_LEGEND.name)
        assertThat(mathLegend?.category).isEqualTo(BadgeCategory.VOLUME)
        assertThat(mathLegend?.requirement is BadgeRequirement.ProblemCount).isTrue()
        assertThat((mathLegend?.requirement as BadgeRequirement.ProblemCount).count).isEqualTo(500)
    }

    @Test
    fun `addition_expert badge has correct properties`() {
        val badges = BadgeDefinitions.getAllBadges()
        val additionExpert = badges.find { it.id == "addition_expert" }

        assertThat(additionExpert?.name).isEqualTo("Addition Expert")
        assertThat(additionExpert?.description).isEqualTo("Solve 50 addition problems")
        assertThat(additionExpert?.icon).isEqualTo(dev.hossain.mathtutor.domain.model.BadgeIcon.ADDITION_EXPERT.name)
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
        assertThat(dedication?.icon).isEqualTo(dev.hossain.mathtutor.domain.model.BadgeIcon.DEDICATION_AWARD.name)
        assertThat(dedication?.category).isEqualTo(BadgeCategory.STREAK)
        assertThat(dedication?.requirement is BadgeRequirement.DailyStreak).isTrue()
        assertThat((dedication?.requirement as BadgeRequirement.DailyStreak).days).isEqualTo(7)
    }
}
