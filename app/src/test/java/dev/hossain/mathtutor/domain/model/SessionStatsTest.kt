package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SessionStatsTest {
    @Test
    fun `getStarRating returns 5 stars for 90-100 percent accuracy`() {
        val stats = SessionStats(100, 90, 90f, 1)
        assertThat(stats.getStarRating().isEqualTo(5))

        val stats95 = SessionStats(100, 95, 95f, 1)
        assertThat(stats95.getStarRating().isEqualTo(5))

        val stats100 = SessionStats(100, 100, 100f, 1)
        assertThat(stats100.getStarRating().isEqualTo(5))
    }

    @Test
    fun `getStarRating returns 4 stars for 80-89 percent accuracy`() {
        val stats = SessionStats(100, 80, 80f, 1)
        assertThat(stats.getStarRating().isEqualTo(4))

        val stats85 = SessionStats(100, 85, 85f, 1)
        assertThat(stats85.getStarRating().isEqualTo(4))

        val stats89 = SessionStats(100, 89, 89f, 1)
        assertThat(stats89.getStarRating().isEqualTo(4))
    }

    @Test
    fun `getStarRating returns 3 stars for 70-79 percent accuracy`() {
        val stats = SessionStats(100, 70, 70f, 1)
        assertThat(stats.getStarRating().isEqualTo(3))

        val stats75 = SessionStats(100, 75, 75f, 1)
        assertThat(stats75.getStarRating().isEqualTo(3))

        val stats79 = SessionStats(100, 79, 79f, 1)
        assertThat(stats79.getStarRating().isEqualTo(3))
    }

    @Test
    fun `getStarRating returns 2 stars for 60-69 percent accuracy`() {
        val stats = SessionStats(100, 60, 60f, 1)
        assertThat(stats.getStarRating().isEqualTo(2))

        val stats65 = SessionStats(100, 65, 65f, 1)
        assertThat(stats65.getStarRating().isEqualTo(2))

        val stats69 = SessionStats(100, 69, 69f, 1)
        assertThat(stats69.getStarRating().isEqualTo(2))
    }

    @Test
    fun `getStarRating returns 1 star for less than 60 percent accuracy`() {
        val stats = SessionStats(100, 59, 59f, 1)
        assertThat(stats.getStarRating().isEqualTo(1))

        val stats50 = SessionStats(100, 50, 50f, 1)
        assertThat(stats50.getStarRating().isEqualTo(1))

        val stats0 = SessionStats(100, 0, 0f, 1)
        assertThat(stats0.getStarRating().isEqualTo(1))
    }

    @Test
    fun `EMPTY has all zero values`() {
        assertThat(SessionStats.EMPTY.totalProblems).isEqualTo(0)
        assertThat(SessionStats.EMPTY.correctCount).isEqualTo(0)
        assertThat(SessionStats.EMPTY.accuracy).isEqualTo(0f)
        assertThat(SessionStats.EMPTY.sessionCount).isEqualTo(0)
    }

    @Test
    fun `EMPTY returns 1 star rating`() {
        assertThat(SessionStats.EMPTY.getStarRating().isEqualTo(1))
    }
}
