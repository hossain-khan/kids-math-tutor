package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant

class GameSessionTest {
    @Test
    fun `accuracy is calculated correctly with all correct answers`() {
        val session = createSession(correctAnswers = 10, totalAttempts = 10)
        assertThat(session.accuracy).isEqualTo(100f)
    }

    @Test
    fun `accuracy is calculated correctly with partial correct answers`() {
        val session = createSession(correctAnswers = 15, totalAttempts = 18)
        assertThat(session.accuracy).isWithin(0.001f).of(83.333336f)
    }

    @Test
    fun `accuracy returns 0 when no attempts`() {
        val session = createSession(correctAnswers = 0, totalAttempts = 0)
        assertThat(session.accuracy).isEqualTo(0f)
    }

    @Test
    fun `averageTimePerProblem is calculated correctly`() {
        val session = createSession(durationSeconds = 60, totalAttempts = 20)
        assertThat(session.averageTimePerProblem).isEqualTo(3f)
    }

    @Test
    fun `averageTimePerProblem returns 0 when no attempts`() {
        val session = createSession(durationSeconds = 60, totalAttempts = 0)
        assertThat(session.averageTimePerProblem).isEqualTo(0f)
    }

    @Test
    fun `problemsPerMinute is calculated correctly`() {
        val session = createSession(durationSeconds = 60, totalAttempts = 20)
        assertThat(session.problemsPerMinute).isEqualTo(20f)
    }

    @Test
    fun `problemsPerMinute returns 0 when duration is 0`() {
        val session = createSession(durationSeconds = 0, totalAttempts = 20)
        assertThat(session.problemsPerMinute).isEqualTo(0f)
    }

    @Test
    fun `isPerfectGame returns true when all correct`() {
        val session = createSession(correctAnswers = 15, totalAttempts = 15)
        assertThat(session.isPerfectGame).isTrue()
    }

    @Test
    fun `isPerfectGame returns false when not all correct`() {
        val session = createSession(correctAnswers = 14, totalAttempts = 15)
        assertThat(session.isPerfectGame).isFalse()
    }

    @Test
    fun `isPerfectGame returns false when no attempts`() {
        val session = createSession(correctAnswers = 0, totalAttempts = 0)
        assertThat(session.isPerfectGame).isFalse()
    }

    @Test
    fun `getStarRating returns 5 for 90+ accuracy`() {
        val session = createSession(correctAnswers = 9, totalAttempts = 10)
        assertThat(session.getStarRating()).isEqualTo(5)
    }

    @Test
    fun `getStarRating returns 4 for 80-89 accuracy`() {
        val session = createSession(correctAnswers = 8, totalAttempts = 10)
        assertThat(session.getStarRating()).isEqualTo(4)
    }

    @Test
    fun `getStarRating returns 3 for 70-79 accuracy`() {
        val session = createSession(correctAnswers = 7, totalAttempts = 10)
        assertThat(session.getStarRating()).isEqualTo(3)
    }

    @Test
    fun `getStarRating returns 2 for 60-69 accuracy`() {
        val session = createSession(correctAnswers = 6, totalAttempts = 10)
        assertThat(session.getStarRating()).isEqualTo(2)
    }

    @Test
    fun `getStarRating returns 1 for less than 60 accuracy`() {
        val session = createSession(correctAnswers = 5, totalAttempts = 10)
        assertThat(session.getStarRating()).isEqualTo(1)
    }

    @Test
    fun `startNew creates session with default values`() {
        val session = GameSession.startNew(Game.MATH_RACE, GradeLevel.GRADE_1)

        assertThat(session.game).isEqualTo(Game.MATH_RACE)
        assertThat(session.gradeLevel).isEqualTo(GradeLevel.GRADE_1)
        assertThat(session.score).isEqualTo(0)
        assertThat(session.correctAnswers).isEqualTo(0)
        assertThat(session.totalAttempts).isEqualTo(0)
        assertThat(session.durationSeconds).isEqualTo(0)
        assertThat(session.isNewRecord).isFalse()
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
