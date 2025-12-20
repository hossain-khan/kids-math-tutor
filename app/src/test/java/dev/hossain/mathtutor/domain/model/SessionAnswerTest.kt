package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SessionAnswerTest {
    @Test
    fun `SessionAnswer stores correct values`() {
        val answer =
            SessionAnswer(
                problemId = "problem-123",
                userAnswer = 8,
                isCorrect = true,
                attemptCount = 1,
                timeSpentSeconds = 15,
            )

        assertThat(answer.problemId).isEqualTo("problem-123")
        assertThat(answer.userAnswer).isEqualTo(8)
        assertThat(answer.isCorrect).isTrue()
        assertThat(answer.attemptCount).isEqualTo(1)
        assertThat(answer.timeSpentSeconds).isEqualTo(15L)
    }

    @Test
    fun `SessionAnswer uses default attempt count of 1`() {
        val answer =
            SessionAnswer(
                problemId = "problem-123",
                userAnswer = 5,
                isCorrect = false,
            )

        assertThat(answer.attemptCount).isEqualTo(1)
    }

    @Test
    fun `SessionAnswer uses default time spent of 0`() {
        val answer =
            SessionAnswer(
                problemId = "problem-123",
                userAnswer = 5,
                isCorrect = false,
            )

        assertThat(answer.timeSpentSeconds).isEqualTo(0L)
    }

    @Test
    fun `SessionAnswer can track incorrect answer`() {
        val answer =
            SessionAnswer(
                problemId = "problem-456",
                userAnswer = 7,
                isCorrect = false,
                attemptCount = 2,
            )

        assertThat(answer.isCorrect).isFalse()
        assertThat(answer.attemptCount).isEqualTo(2)
    }

    @Test
    fun `SessionAnswer can track multiple attempts`() {
        val answer =
            SessionAnswer(
                problemId = "problem-789",
                userAnswer = 10,
                isCorrect = true,
                attemptCount = 3,
                timeSpentSeconds = 45,
            )

        assertThat(answer.attemptCount).isEqualTo(3)
        assertThat(answer.timeSpentSeconds).isEqualTo(45L)
    }
}
