package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant

class GameStatsTest {
    @Test
    fun `overallAccuracy is calculated correctly`() {
        val stats = createStats(totalCorrectAnswers = 80, totalAttempts = 100)
        assertThat(stats.overallAccuracy).isEqualTo(80f)
    }

    @Test
    fun `overallAccuracy returns 0 when no attempts`() {
        val stats = createStats(totalCorrectAnswers = 0, totalAttempts = 0)
        assertThat(stats.overallAccuracy).isEqualTo(0f)
    }

    @Test
    fun `hasPlayed returns true when totalGamesPlayed greater than 0`() {
        val stats = createStats(totalGamesPlayed = 1)
        assertThat(stats.hasPlayed).isTrue()
    }

    @Test
    fun `hasPlayed returns false when totalGamesPlayed is 0`() {
        val stats = createStats(totalGamesPlayed = 0)
        assertThat(stats.hasPlayed).isFalse()
    }

    @Test
    fun `getStarRating returns 0 when never played`() {
        val stats = createStats(totalGamesPlayed = 0, bestAccuracy = 100f)
        assertThat(stats.getStarRating()).isEqualTo(0)
    }

    @Test
    fun `getStarRating returns 5 for 90+ best accuracy`() {
        val stats = createStats(totalGamesPlayed = 1, bestAccuracy = 95f)
        assertThat(stats.getStarRating()).isEqualTo(5)
    }

    @Test
    fun `getStarRating returns 4 for 80-89 best accuracy`() {
        val stats = createStats(totalGamesPlayed = 1, bestAccuracy = 85f)
        assertThat(stats.getStarRating()).isEqualTo(4)
    }

    @Test
    fun `getStarRating returns 3 for 70-79 best accuracy`() {
        val stats = createStats(totalGamesPlayed = 1, bestAccuracy = 75f)
        assertThat(stats.getStarRating()).isEqualTo(3)
    }

    @Test
    fun `getStarRating returns 2 for 60-69 best accuracy`() {
        val stats = createStats(totalGamesPlayed = 1, bestAccuracy = 65f)
        assertThat(stats.getStarRating()).isEqualTo(2)
    }

    @Test
    fun `getStarRating returns 1 for less than 60 best accuracy`() {
        val stats = createStats(totalGamesPlayed = 1, bestAccuracy = 50f)
        assertThat(stats.getStarRating()).isEqualTo(1)
    }

    @Test
    fun `empty creates stats with all zeroed values`() {
        val stats = GameStats.empty(Game.MATH_RACE)

        assertThat(stats.game).isEqualTo(Game.MATH_RACE)
        assertThat(stats.personalBest).isEqualTo(0)
        assertThat(stats.totalGamesPlayed).isEqualTo(0)
        assertThat(stats.averageScore).isEqualTo(0f)
        assertThat(stats.bestAccuracy).isEqualTo(0f)
        assertThat(stats.lastPlayedAt).isNull()
        assertThat(stats.totalCorrectAnswers).isEqualTo(0)
        assertThat(stats.totalAttempts).isEqualTo(0)
    }

    private fun createStats(
        game: Game = Game.MATH_RACE,
        personalBest: Int = 20,
        totalGamesPlayed: Int = 5,
        averageScore: Float = 15f,
        bestAccuracy: Float = 90f,
        lastPlayedAt: Instant? = Instant.now(),
        totalCorrectAnswers: Int = 100,
        totalAttempts: Int = 120,
    ): GameStats =
        GameStats(
            game = game,
            personalBest = personalBest,
            totalGamesPlayed = totalGamesPlayed,
            averageScore = averageScore,
            bestAccuracy = bestAccuracy,
            lastPlayedAt = lastPlayedAt,
            totalCorrectAnswers = totalCorrectAnswers,
            totalAttempts = totalAttempts,
        )
}
