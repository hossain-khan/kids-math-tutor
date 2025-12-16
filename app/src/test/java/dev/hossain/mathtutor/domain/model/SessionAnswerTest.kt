package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

        assertEquals("problem-123", answer.problemId)
        assertEquals(8, answer.userAnswer)
        assertTrue(answer.isCorrect)
        assertEquals(1, answer.attemptCount)
        assertEquals(15L, answer.timeSpentSeconds)
    }

    @Test
    fun `SessionAnswer uses default attempt count of 1`() {
        val answer =
            SessionAnswer(
                problemId = "problem-123",
                userAnswer = 5,
                isCorrect = false,
            )

        assertEquals(1, answer.attemptCount)
    }

    @Test
    fun `SessionAnswer uses default time spent of 0`() {
        val answer =
            SessionAnswer(
                problemId = "problem-123",
                userAnswer = 5,
                isCorrect = false,
            )

        assertEquals(0L, answer.timeSpentSeconds)
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

        assertFalse(answer.isCorrect)
        assertEquals(2, answer.attemptCount)
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

        assertEquals(3, answer.attemptCount)
        assertEquals(45L, answer.timeSpentSeconds)
    }
}
