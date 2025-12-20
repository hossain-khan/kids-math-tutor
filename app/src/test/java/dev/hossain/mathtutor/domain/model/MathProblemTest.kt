package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
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

        assertThat(problem.getDisplayString().isEqualTo("3 + 5 = ?"))
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

        assertThat(problem.getDisplayString().isEqualTo("10 - 4 = ?"))
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

        assertThat(problem.getDisplayString().isEqualTo("7 × 3 = ?"))
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

        assertThat(problem.getDisplayString().isEqualTo("20 ÷ 5 = ?"))
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

        assertThat(problem.checkAnswer(8)).isTrue()
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

        assertThat(problem.checkAnswer(7)).isFalse()
        assertThat(problem.checkAnswer(9)).isFalse()
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
        assertThat(problem1.id != problem2.id).isTrue()
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

        assertThat(problem.getSpokenString().isEqualTo("3 plus 5 equals"))
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

        assertThat(problem.getSpokenString().isEqualTo("10 minus 4 equals"))
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

        assertThat(problem.getSpokenString().isEqualTo("7 times 3 equals"))
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

        assertThat(problem.getSpokenString().isEqualTo("20 divided by 5 equals"))
    }
}
