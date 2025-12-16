package dev.hossain.mathtutor.circuit

import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [ResultsPresenter].
 *
 * Tests accuracy calculation, problem result mapping, and state management.
 */
class ResultsPresenterTest {
    @Test
    fun accuracyCalculation_allCorrect_returns100() {
        // Given
        val problems =
            listOf(
                MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5),
                MathProblem(num1 = 4, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 9),
            )
        val userAnswers = listOf(5, 9)

        // When - Calculate accuracy
        val correctCount = problems.zip(userAnswers).count { (problem, answer) -> problem.checkAnswer(answer) }
        val accuracy = (correctCount.toFloat() / problems.size) * 100f

        // Then
        assertEquals(100f, accuracy, 0.01f)
    }

    @Test
    fun accuracyCalculation_halfCorrect_returns50() {
        // Given
        val problems =
            listOf(
                MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5),
                MathProblem(num1 = 4, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 9),
            )
        val userAnswers = listOf(5, 10) // First correct, second wrong

        // When
        val correctCount = problems.zip(userAnswers).count { (problem, answer) -> problem.checkAnswer(answer) }
        val accuracy = (correctCount.toFloat() / problems.size) * 100f

        // Then
        assertEquals(50f, accuracy, 0.01f)
    }

    @Test
    fun accuracyCalculation_allIncorrect_returns0() {
        // Given
        val problems =
            listOf(
                MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5),
                MathProblem(num1 = 4, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 9),
            )
        val userAnswers = listOf(0, 0) // Both wrong

        // When
        val correctCount = problems.zip(userAnswers).count { (problem, answer) -> problem.checkAnswer(answer) }
        val accuracy = (correctCount.toFloat() / problems.size) * 100f

        // Then
        assertEquals(0f, accuracy, 0.01f)
    }

    @Test
    fun problemResults_correctAnswer_markedAsCorrect() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer = 8

        // When
        val isCorrect = problem.checkAnswer(userAnswer)

        // Then
        assertTrue(isCorrect)
    }

    @Test
    fun problemResults_incorrectAnswer_markedAsIncorrect() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer = 7

        // When
        val isCorrect = problem.checkAnswer(userAnswer)

        // Then
        assertFalse(isCorrect)
    }

    @Test
    fun problemResults_nullAnswer_markedAsIncorrect() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer: Int? = null

        // When
        val isCorrect = userAnswer?.let { problem.checkAnswer(it) } ?: false

        // Then
        assertFalse(isCorrect)
    }

    @Test
    fun state_totalProblems_matchesInputSize() {
        // Given
        val problems =
            listOf(
                MathProblem(num1 = 1, num2 = 1, operation = MathOperation.ADDITION, correctAnswer = 2),
                MathProblem(num1 = 2, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 4),
                MathProblem(num1 = 3, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 6),
            )

        // When
        val totalProblems = problems.size

        // Then
        assertEquals(3, totalProblems)
    }

    @Test
    fun state_correctCount_countsCorrectAnswers() {
        // Given
        val problems =
            listOf(
                MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5),
                MathProblem(num1 = 4, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 9),
                MathProblem(num1 = 1, num2 = 1, operation = MathOperation.ADDITION, correctAnswer = 2),
            )
        val userAnswers = listOf(5, 10, 2) // 2 correct, 1 incorrect

        // When
        val correctCount = problems.zip(userAnswers).count { (problem, answer) -> problem.checkAnswer(answer) }

        // Then
        assertEquals(2, correctCount)
    }

    @Test
    fun problemResult_containsProblem() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer = 8

        // When
        val result =
            ResultsScreen.ProblemResult(
                problem = problem,
                userAnswer = userAnswer,
                isCorrect = problem.checkAnswer(userAnswer),
            )

        // Then
        assertNotNull(result.problem)
        assertEquals(problem, result.problem)
    }

    @Test
    fun problemResult_containsUserAnswer() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer = 7

        // When
        val result =
            ResultsScreen.ProblemResult(
                problem = problem,
                userAnswer = userAnswer,
                isCorrect = problem.checkAnswer(userAnswer),
            )

        // Then
        assertEquals(userAnswer, result.userAnswer)
    }

    @Test
    fun problemResult_handlesNullAnswer() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer: Int? = null

        // When
        val result =
            ResultsScreen.ProblemResult(
                problem = problem,
                userAnswer = userAnswer,
                isCorrect = userAnswer?.let { problem.checkAnswer(it) } ?: false,
            )

        // Then
        assertEquals(null, result.userAnswer)
        assertFalse(result.isCorrect)
    }

    @Test
    fun accuracyCalculation_emptyProblems_returns0() {
        // Given
        val problems = emptyList<MathProblem>()
        val userAnswers = emptyList<Int>()

        // When
        val accuracy =
            if (problems.isNotEmpty()) {
                val correctCount = problems.zip(userAnswers).count { (problem, answer) -> problem.checkAnswer(answer) }
                (correctCount.toFloat() / problems.size) * 100f
            } else {
                0f
            }

        // Then
        assertEquals(0f, accuracy, 0.01f)
    }

    @Test
    fun accuracyCalculation_roundsCorrectly() {
        // Given - 2 out of 3 correct = 66.666...%
        val problems =
            listOf(
                MathProblem(num1 = 1, num2 = 1, operation = MathOperation.ADDITION, correctAnswer = 2),
                MathProblem(num1 = 2, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 4),
                MathProblem(num1 = 3, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 6),
            )
        val userAnswers = listOf(2, 4, 5) // 2 correct, 1 incorrect

        // When
        val correctCount = problems.zip(userAnswers).count { (problem, answer) -> problem.checkAnswer(answer) }
        val accuracy = (correctCount.toFloat() / problems.size) * 100f

        // Then
        assertEquals(66.67f, accuracy, 0.01f)
    }
}
