package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MathProblemTest {
    @Test
    fun `getDisplayString returns correct format for addition`() {
        val problem =
            MathProblem(
                num1 = 3,
                num2 = 5,
                operation = MathOperation.ADDITION,
                correctAnswer = 8,
            )

        assertEquals("3 + 5 = ?", problem.getDisplayString())
    }

    @Test
    fun `getDisplayString returns correct format for subtraction`() {
        val problem =
            MathProblem(
                num1 = 10,
                num2 = 4,
                operation = MathOperation.SUBTRACTION,
                correctAnswer = 6,
            )

        assertEquals("10 - 4 = ?", problem.getDisplayString())
    }

    @Test
    fun `getDisplayString returns correct format for multiplication`() {
        val problem =
            MathProblem(
                num1 = 7,
                num2 = 3,
                operation = MathOperation.MULTIPLICATION,
                correctAnswer = 21,
            )

        assertEquals("7 × 3 = ?", problem.getDisplayString())
    }

    @Test
    fun `getDisplayString returns correct format for division`() {
        val problem =
            MathProblem(
                num1 = 20,
                num2 = 5,
                operation = MathOperation.DIVISION,
                correctAnswer = 4,
            )

        assertEquals("20 ÷ 5 = ?", problem.getDisplayString())
    }

    @Test
    fun `checkAnswer returns true for correct answer`() {
        val problem =
            MathProblem(
                num1 = 3,
                num2 = 5,
                operation = MathOperation.ADDITION,
                correctAnswer = 8,
            )

        assertTrue(problem.checkAnswer(8))
    }

    @Test
    fun `checkAnswer returns false for incorrect answer`() {
        val problem =
            MathProblem(
                num1 = 3,
                num2 = 5,
                operation = MathOperation.ADDITION,
                correctAnswer = 8,
            )

        assertFalse(problem.checkAnswer(7))
        assertFalse(problem.checkAnswer(9))
    }

    @Test
    fun `problem has unique ID`() {
        val problem1 =
            MathProblem(
                num1 = 3,
                num2 = 5,
                operation = MathOperation.ADDITION,
                correctAnswer = 8,
            )

        val problem2 =
            MathProblem(
                num1 = 3,
                num2 = 5,
                operation = MathOperation.ADDITION,
                correctAnswer = 8,
            )

        // IDs should be different even if content is the same
        assertTrue(problem1.id != problem2.id)
    }

    @Test
    fun `getSpokenString returns correct format for addition`() {
        val problem =
            MathProblem(
                num1 = 3,
                num2 = 5,
                operation = MathOperation.ADDITION,
                correctAnswer = 8,
            )

        assertEquals("3 plus 5 equals", problem.getSpokenString())
    }

    @Test
    fun `getSpokenString returns correct format for subtraction`() {
        val problem =
            MathProblem(
                num1 = 10,
                num2 = 4,
                operation = MathOperation.SUBTRACTION,
                correctAnswer = 6,
            )

        assertEquals("10 minus 4 equals", problem.getSpokenString())
    }

    @Test
    fun `getSpokenString returns correct format for multiplication`() {
        val problem =
            MathProblem(
                num1 = 7,
                num2 = 3,
                operation = MathOperation.MULTIPLICATION,
                correctAnswer = 21,
            )

        assertEquals("7 times 3 equals", problem.getSpokenString())
    }

    @Test
    fun `getSpokenString returns correct format for division`() {
        val problem =
            MathProblem(
                num1 = 20,
                num2 = 5,
                operation = MathOperation.DIVISION,
                correctAnswer = 4,
            )

        assertEquals("20 divided by 5 equals", problem.getSpokenString())
    }
}
