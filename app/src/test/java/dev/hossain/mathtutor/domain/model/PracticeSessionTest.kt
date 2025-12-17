package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PracticeSessionTest {
    private fun createMockProblem(id: String): MathProblem =
        MathProblem(
            id = id,
            num1 = 3,
            num2 = 5,
            operation = MathOperation.ADDITION,
            correctAnswer = 8,
        )

    @Test
    fun `getCorrectCount returns zero for empty answers`() {
        val session =
            PracticeSession(
                totalProblems = 10,
                problems = emptyList(),
            )

        assertEquals(0, session.getCorrectCount())
    }

    @Test
    fun `getCorrectCount returns correct number of correct answers`() {
        val session =
            PracticeSession(
                totalProblems = 5,
                problems = emptyList(),
            )

        session.answers["1"] = SessionAnswer("1", 8, isCorrect = true)
        session.answers["2"] = SessionAnswer("2", 5, isCorrect = false)
        session.answers["3"] = SessionAnswer("3", 8, isCorrect = true)
        session.answers["4"] = SessionAnswer("4", 8, isCorrect = true)
        session.answers["5"] = SessionAnswer("5", 7, isCorrect = false)

        assertEquals(3, session.getCorrectCount())
    }

    @Test
    fun `getAccuracy returns zero for empty answers`() {
        val session =
            PracticeSession(
                totalProblems = 10,
                problems = emptyList(),
            )

        assertEquals(0f, session.getAccuracy(), 0.01f)
    }

    @Test
    fun `getAccuracy calculates correct percentage`() {
        val session =
            PracticeSession(
                totalProblems = 10,
                problems = emptyList(),
            )

        // 8 correct out of 10 = 80%
        repeat(8) { i ->
            session.answers[i.toString()] = SessionAnswer(i.toString(), 8, isCorrect = true)
        }
        repeat(2) { i ->
            session.answers[(i + 8).toString()] =
                SessionAnswer((i + 8).toString(), 5, isCorrect = false)
        }

        assertEquals(80f, session.getAccuracy(), 0.01f)
    }

    @Test
    fun `getAccuracy returns 100 for all correct answers`() {
        val session =
            PracticeSession(
                totalProblems = 5,
                problems = emptyList(),
            )

        repeat(5) { i ->
            session.answers[i.toString()] = SessionAnswer(i.toString(), 8, isCorrect = true)
        }

        assertEquals(100f, session.getAccuracy(), 0.01f)
    }

    @Test
    fun `getAccuracy returns 0 for all incorrect answers`() {
        val session =
            PracticeSession(
                totalProblems = 5,
                problems = emptyList(),
            )

        repeat(5) { i ->
            session.answers[i.toString()] = SessionAnswer(i.toString(), 5, isCorrect = false)
        }

        assertEquals(0f, session.getAccuracy(), 0.01f)
    }

    @Test
    fun `session stores problems correctly`() {
        val problems =
            listOf(
                createMockProblem("1"),
                createMockProblem("2"),
                createMockProblem("3"),
            )

        val session =
            PracticeSession(
                totalProblems = 3,
                problems = problems,
            )

        assertEquals(3, session.problems.size)
        assertEquals(3, session.totalProblems)
    }

    @Test
    fun `isComplete returns false when completedAt is null`() {
        val session =
            PracticeSession(
                totalProblems = 5,
                problems = emptyList(),
                completedAt = null,
            )

        assertFalse(session.isComplete())
    }

    @Test
    fun `isComplete returns true when completedAt is set`() {
        val session =
            PracticeSession(
                totalProblems = 5,
                problems = emptyList(),
                completedAt = Instant.now(),
            )

        assertTrue(session.isComplete())
    }

    @Test
    fun `session stores operation correctly`() {
        val session =
            PracticeSession(
                totalProblems = 5,
                problems = emptyList(),
                operation = MathOperation.ADDITION,
            )

        assertEquals(MathOperation.ADDITION, session.operation)
    }

    @Test
    fun `session stores durationSeconds correctly`() {
        val session =
            PracticeSession(
                totalProblems = 5,
                problems = emptyList(),
                durationSeconds = 120L,
            )

        assertEquals(120L, session.durationSeconds)
    }

    @Test
    fun `session can be created with all new fields`() {
        val completedAt = Instant.now()
        val session =
            PracticeSession(
                totalProblems = 10,
                problems = emptyList(),
                operation = MathOperation.SUBTRACTION,
                durationSeconds = 180L,
                completedAt = completedAt,
            )

        assertEquals(MathOperation.SUBTRACTION, session.operation)
        assertEquals(180L, session.durationSeconds)
        assertEquals(completedAt, session.completedAt)
        assertTrue(session.isComplete())
    }
}
