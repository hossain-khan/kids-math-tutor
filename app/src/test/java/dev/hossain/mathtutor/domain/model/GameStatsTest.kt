package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class GameStatsTest {
    @Test
    fun `overallAccuracy is calculated correctly`() {
        val stats = createStats(totalCorrectAnswers = 80, totalAttempts = 100)
        assertEquals(80f, stats.overallAccuracy)
    }

    @Test
    fun `overallAccuracy returns 0 when no attempts`() {
        val stats = createStats(totalCorrectAnswers = 0, totalAttempts = 0)
        assertEquals(0f, stats.overallAccuracy)
    }

    @Test
    fun `hasPlayed returns true when totalGamesPlayed greater than 0`() {
        val stats = createStats(totalGamesPlayed = 1)
        assertTrue(stats.hasPlayed)
    }

    @Test
    fun `hasPlayed returns false when totalGamesPlayed is 0`() {
        val stats = createStats(totalGamesPlayed = 0)
        assertFalse(stats.hasPlayed)
    }

    @Test
    fun `getStarRating returns 0 when never played`() {
        val stats = createStats(totalGamesPlayed = 0, bestAccuracy = 100f)
        assertEquals(0, stats.getStarRating())
    }

    @Test
    fun `getStarRating returns 5 for 90+ best accuracy`() {
        val stats = createStats(totalGamesPlayed = 1, bestAccuracy = 95f)
        assertEquals(5, stats.getStarRating())
    }

    @Test
    fun `getStarRating returns 4 for 80-89 best accuracy`() {
        val stats = createStats(totalGamesPlayed = 1, bestAccuracy = 85f)
        assertEquals(4, stats.getStarRating())
    }

    @Test
    fun `getStarRating returns 3 for 70-79 best accuracy`() {
        val stats = createStats(totalGamesPlayed = 1, bestAccuracy = 75f)
        assertEquals(3, stats.getStarRating())
    }

    @Test
    fun `getStarRating returns 2 for 60-69 best accuracy`() {
        val stats = createStats(totalGamesPlayed = 1, bestAccuracy = 65f)
        assertEquals(2, stats.getStarRating())
    }

    @Test
    fun `getStarRating returns 1 for less than 60 best accuracy`() {
        val stats = createStats(totalGamesPlayed = 1, bestAccuracy = 50f)
        assertEquals(1, stats.getStarRating())
    }

    @Test
    fun `empty creates stats with all zeroed values`() {
        val stats = GameStats.empty(Game.MATH_RACE)

        assertEquals(Game.MATH_RACE, stats.game)
        assertEquals(0, stats.personalBest)
        assertEquals(0, stats.totalGamesPlayed)
        assertEquals(0f, stats.averageScore)
        assertEquals(0f, stats.bestAccuracy)
        assertNull(stats.lastPlayedAt)
        assertEquals(0, stats.totalCorrectAnswers)
        assertEquals(0, stats.totalAttempts)
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
