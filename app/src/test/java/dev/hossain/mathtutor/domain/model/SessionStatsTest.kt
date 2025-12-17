package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionStatsTest {
    @Test
    fun `getStarRating returns 5 stars for 90-100 percent accuracy`() {
        val stats = SessionStats(100, 90, 90f, 1)
        assertEquals(5, stats.getStarRating())

        val stats95 = SessionStats(100, 95, 95f, 1)
        assertEquals(5, stats95.getStarRating())

        val stats100 = SessionStats(100, 100, 100f, 1)
        assertEquals(5, stats100.getStarRating())
    }

    @Test
    fun `getStarRating returns 4 stars for 80-89 percent accuracy`() {
        val stats = SessionStats(100, 80, 80f, 1)
        assertEquals(4, stats.getStarRating())

        val stats85 = SessionStats(100, 85, 85f, 1)
        assertEquals(4, stats85.getStarRating())

        val stats89 = SessionStats(100, 89, 89f, 1)
        assertEquals(4, stats89.getStarRating())
    }

    @Test
    fun `getStarRating returns 3 stars for 70-79 percent accuracy`() {
        val stats = SessionStats(100, 70, 70f, 1)
        assertEquals(3, stats.getStarRating())

        val stats75 = SessionStats(100, 75, 75f, 1)
        assertEquals(3, stats75.getStarRating())

        val stats79 = SessionStats(100, 79, 79f, 1)
        assertEquals(3, stats79.getStarRating())
    }

    @Test
    fun `getStarRating returns 2 stars for 60-69 percent accuracy`() {
        val stats = SessionStats(100, 60, 60f, 1)
        assertEquals(2, stats.getStarRating())

        val stats65 = SessionStats(100, 65, 65f, 1)
        assertEquals(2, stats65.getStarRating())

        val stats69 = SessionStats(100, 69, 69f, 1)
        assertEquals(2, stats69.getStarRating())
    }

    @Test
    fun `getStarRating returns 1 star for less than 60 percent accuracy`() {
        val stats = SessionStats(100, 59, 59f, 1)
        assertEquals(1, stats.getStarRating())

        val stats50 = SessionStats(100, 50, 50f, 1)
        assertEquals(1, stats50.getStarRating())

        val stats0 = SessionStats(100, 0, 0f, 1)
        assertEquals(1, stats0.getStarRating())
    }

    @Test
    fun `EMPTY has all zero values`() {
        assertEquals(0, SessionStats.EMPTY.totalProblems)
        assertEquals(0, SessionStats.EMPTY.correctCount)
        assertEquals(0f, SessionStats.EMPTY.accuracy)
        assertEquals(0, SessionStats.EMPTY.sessionCount)
    }

    @Test
    fun `EMPTY returns 1 star rating`() {
        assertEquals(1, SessionStats.EMPTY.getStarRating())
    }
}
