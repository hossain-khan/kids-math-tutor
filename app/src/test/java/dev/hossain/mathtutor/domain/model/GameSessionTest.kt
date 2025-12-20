package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class GameSessionTest {
    @Test
    fun `accuracy is calculated correctly with all correct answers`() {
        val session = createSession(correctAnswers = 10, totalAttempts = 10)
        assertEquals(100f, session.accuracy)
    }

    @Test
    fun `accuracy is calculated correctly with partial correct answers`() {
        val session = createSession(correctAnswers = 15, totalAttempts = 18)
        assertEquals(83.333336f, session.accuracy, 0.001f)
    }

    @Test
    fun `accuracy returns 0 when no attempts`() {
        val session = createSession(correctAnswers = 0, totalAttempts = 0)
        assertEquals(0f, session.accuracy)
    }

    @Test
    fun `averageTimePerProblem is calculated correctly`() {
        val session = createSession(durationSeconds = 60, totalAttempts = 20)
        assertEquals(3f, session.averageTimePerProblem)
    }

    @Test
    fun `averageTimePerProblem returns 0 when no attempts`() {
        val session = createSession(durationSeconds = 60, totalAttempts = 0)
        assertEquals(0f, session.averageTimePerProblem)
    }

    @Test
    fun `problemsPerMinute is calculated correctly`() {
        val session = createSession(durationSeconds = 60, totalAttempts = 20)
        assertEquals(20f, session.problemsPerMinute)
    }

    @Test
    fun `problemsPerMinute returns 0 when duration is 0`() {
        val session = createSession(durationSeconds = 0, totalAttempts = 20)
        assertEquals(0f, session.problemsPerMinute)
    }

    @Test
    fun `isPerfectGame returns true when all correct`() {
        val session = createSession(correctAnswers = 15, totalAttempts = 15)
        assertTrue(session.isPerfectGame)
    }

    @Test
    fun `isPerfectGame returns false when not all correct`() {
        val session = createSession(correctAnswers = 14, totalAttempts = 15)
        assertFalse(session.isPerfectGame)
    }

    @Test
    fun `isPerfectGame returns false when no attempts`() {
        val session = createSession(correctAnswers = 0, totalAttempts = 0)
        assertFalse(session.isPerfectGame)
    }

    @Test
    fun `getStarRating returns 5 for 90+ accuracy`() {
        val session = createSession(correctAnswers = 9, totalAttempts = 10)
        assertEquals(5, session.getStarRating())
    }

    @Test
    fun `getStarRating returns 4 for 80-89 accuracy`() {
        val session = createSession(correctAnswers = 8, totalAttempts = 10)
        assertEquals(4, session.getStarRating())
    }

    @Test
    fun `getStarRating returns 3 for 70-79 accuracy`() {
        val session = createSession(correctAnswers = 7, totalAttempts = 10)
        assertEquals(3, session.getStarRating())
    }

    @Test
    fun `getStarRating returns 2 for 60-69 accuracy`() {
        val session = createSession(correctAnswers = 6, totalAttempts = 10)
        assertEquals(2, session.getStarRating())
    }

    @Test
    fun `getStarRating returns 1 for less than 60 accuracy`() {
        val session = createSession(correctAnswers = 5, totalAttempts = 10)
        assertEquals(1, session.getStarRating())
    }

    @Test
    fun `startNew creates session with default values`() {
        val session = GameSession.startNew(Game.MATH_RACE, GradeLevel.GRADE_1)

        assertEquals(Game.MATH_RACE, session.game)
        assertEquals(GradeLevel.GRADE_1, session.gradeLevel)
        assertEquals(0, session.score)
        assertEquals(0, session.correctAnswers)
        assertEquals(0, session.totalAttempts)
        assertEquals(0, session.durationSeconds)
        assertFalse(session.isNewRecord)
    }

    private fun createSession(
        correctAnswers: Int = 0,
        totalAttempts: Int = 0,
        durationSeconds: Int = 60,
        score: Int = correctAnswers,
    ): GameSession =
        GameSession(
            id = 1,
            game = Game.MATH_RACE,
            startTime = Instant.now(),
            endTime = Instant.now(),
            score = score,
            correctAnswers = correctAnswers,
            totalAttempts = totalAttempts,
            durationSeconds = durationSeconds,
            gradeLevel = GradeLevel.GRADE_1,
        )
}
