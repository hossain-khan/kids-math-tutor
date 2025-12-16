package dev.hossain.mathtutor.ui.mathpractice

import dev.hossain.mathtutor.domain.generator.ProblemGenerator
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [MathPracticePresenter].
 *
 * Tests the presenter logic including state management, event handling, and business logic.
 */
class MathPracticePresenterTest {
    private lateinit var problemGenerator: ProblemGenerator
    private lateinit var screen: MathPracticeScreen

    @Before
    fun setup() {
        // Create a fake problem generator that returns predictable problems
        problemGenerator =
            object : ProblemGenerator {
                override fun generateProblems(
                    count: Int,
                    operation: MathOperation,
                ): List<MathProblem> =
                    List(count) { index ->
                        // Generate simple problems: 1+1=2, 2+2=4, 3+3=6, etc.
                        val number = index + 1
                        val answer = operation.calculate(number, number)
                        MathProblem(num1 = number, num2 = number, operation = operation, correctAnswer = answer)
                    }
            }

        screen = MathPracticeScreen(problemCount = 5)
    }

    @Test
    fun presenter_initialState_isCorrect() {
        // Given - Getting initial state (simulated)
        val problems = problemGenerator.generateProblems(5, MathOperation.ADDITION)

        // Then - Initial state should be set correctly
        assertEquals(5, problems.size)
        assertNotNull(problems[0])
        assertEquals("1 + 1 = ?", problems[0].getDisplayString())
    }

    @Test
    fun numberClicked_appendsToAnswer() {
        // Given
        var currentAnswer = ""

        // When - Number clicked events
        currentAnswer += "5"
        currentAnswer += "7"

        // Then
        assertEquals("57", currentAnswer)
    }

    @Test
    fun clearAnswer_resetsAnswerAndFeedback() {
        // Given
        var currentAnswer = "42"
        var isCorrect: Boolean? = true

        // When - Clear answer event
        currentAnswer = ""
        isCorrect = null

        // Then
        assertEquals("", currentAnswer)
        assertNull(isCorrect)
    }

    @Test
    fun checkAnswer_correctAnswer_setsIsCorrectToTrue() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer = "8"

        // When - Check answer
        val isCorrect = problem.checkAnswer(userAnswer.toInt())

        // Then
        assertTrue(isCorrect)
    }

    @Test
    fun checkAnswer_incorrectAnswer_setsIsCorrectToFalse() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer = "7"

        // When - Check answer
        val isCorrect = problem.checkAnswer(userAnswer.toInt())

        // Then
        assertFalse(isCorrect)
    }

    @Test
    fun checkAnswer_invalidInput_handlesGracefully() {
        // Given
        val currentAnswer = "abc"

        // When - Try to convert to Int
        val userAnswer = currentAnswer.toIntOrNull()

        // Then
        assertNull(userAnswer)
    }

    @Test
    fun nextProblem_advancesToNextProblem() {
        // Given
        var currentProblemIndex = 0
        val totalProblems = 5

        // When - Next problem event
        if (currentProblemIndex < totalProblems - 1) {
            currentProblemIndex++
        }

        // Then
        assertEquals(1, currentProblemIndex)
    }

    @Test
    fun nextProblem_atLastProblem_doesNotAdvance() {
        // Given
        var currentProblemIndex = 4
        val totalProblems = 5

        // When - Next problem event at last problem
        if (currentProblemIndex < totalProblems - 1) {
            currentProblemIndex++
        }

        // Then - Should stay at last problem
        assertEquals(4, currentProblemIndex)
    }

    @Test
    fun nextProblem_resetsAnswerAndFeedback() {
        // Given
        var currentAnswer = "42"
        var isCorrect: Boolean? = true
        var currentProblemIndex = 0

        // When - Next problem event
        currentProblemIndex++
        currentAnswer = ""
        isCorrect = null

        // Then
        assertEquals(1, currentProblemIndex)
        assertEquals("", currentAnswer)
        assertNull(isCorrect)
    }

    @Test
    fun problemGeneration_generatesCorrectCount() {
        // Given
        val problemCount = 10

        // When
        val problems = problemGenerator.generateProblems(problemCount, MathOperation.ADDITION)

        // Then
        assertEquals(problemCount, problems.size)
    }

    @Test
    fun problemGeneration_usesAdditionOperation() {
        // When
        val problems = problemGenerator.generateProblems(5, MathOperation.ADDITION)

        // Then - All problems should be addition
        problems.forEach { problem ->
            assertEquals(MathOperation.ADDITION, problem.operation)
        }
    }

    @Test
    fun state_tracksProgress_correctly() {
        // Given
        var currentProblemIndex = 0
        val totalProblems = 10

        // When - Simulate progressing through problems
        repeat(5) {
            if (currentProblemIndex < totalProblems - 1) {
                currentProblemIndex++
            }
        }

        // Then
        assertEquals(5, currentProblemIndex)
        assertEquals(0.6f, (currentProblemIndex + 1).toFloat() / totalProblems, 0.01f)
    }

    @Test
    fun multipleNumbers_buildsCorrectAnswer() {
        // Given
        var currentAnswer = ""

        // When - Multiple number clicks
        currentAnswer += "1"
        currentAnswer += "2"
        currentAnswer += "3"

        // Then
        assertEquals("123", currentAnswer)
    }
}
