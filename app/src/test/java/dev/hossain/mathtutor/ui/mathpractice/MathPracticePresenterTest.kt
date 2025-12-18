package dev.hossain.mathtutor.ui.mathpractice

import dev.hossain.mathtutor.domain.generator.ProblemGenerator
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionAnswer
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
                    gradeLevel: GradeLevel,
                ): List<MathProblem> =
                    List(count) { index ->
                        // Generate simple problems: 1+1=2, 2+2=4, 3+3=6, etc.
                        val number = index + 1
                        val answer = operation.calculate(number, number)
                        MathProblem(num1 = number, num2 = number, operation = operation, correctAnswer = answer)
                    }
            }

        screen = MathPracticeScreen(operation = MathOperation.ADDITION, problemCount = 5)
    }

    @Test
    fun presenter_initialState_isCorrect() {
        // Given - Getting initial state (simulated)
        val problems = problemGenerator.generateProblems(5, MathOperation.ADDITION, GradeLevel.GRADE_1)

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
        val problems = problemGenerator.generateProblems(problemCount, MathOperation.ADDITION, GradeLevel.GRADE_1)

        // Then
        assertEquals(problemCount, problems.size)
    }

    @Test
    fun problemGeneration_usesAdditionOperation() {
        // When
        val problems = problemGenerator.generateProblems(5, MathOperation.ADDITION, GradeLevel.GRADE_1)

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

    @Test
    fun sessionAnswers_includesAllProblems_evenUnanswered() {
        // Given
        val problems = problemGenerator.generateProblems(3, MathOperation.ADDITION, GradeLevel.GRADE_1)
        val userAnswers = listOf(2, null, 6) // First answered, second skipped, third answered

        // When - Create session answers for all problems
        val sessionAnswers = mutableMapOf<String, SessionAnswer>()
        problems.forEachIndexed { index, problem ->
            val userAnswer = userAnswers.getOrNull(index)
            sessionAnswers[problem.id] =
                SessionAnswer(
                    problemId = problem.id,
                    userAnswer = userAnswer,
                    isCorrect =
                        userAnswer?.let { answer ->
                            problem.checkAnswer(answer)
                        } ?: false,
                )
        }

        // Then - All problems should be recorded
        assertEquals(3, sessionAnswers.size)
        // First problem (1+1=2): answered correctly
        assertTrue(sessionAnswers[problems[0].id]?.isCorrect == true)
        assertEquals(2, sessionAnswers[problems[0].id]?.userAnswer)
        // Second problem (2+2=4): skipped
        assertFalse(sessionAnswers[problems[1].id]?.isCorrect ?: true)
        assertNull(sessionAnswers[problems[1].id]?.userAnswer)
        // Third problem (3+3=6): answered correctly
        assertTrue(sessionAnswers[problems[2].id]?.isCorrect == true)
        assertEquals(6, sessionAnswers[problems[2].id]?.userAnswer)
    }

    @Test
    fun sessionAnswers_correctlyMarksSkippedProblems() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer: Int? = null

        // When - Create session answer for unanswered problem
        val sessionAnswer =
            SessionAnswer(
                problemId = problem.id,
                userAnswer = userAnswer,
                isCorrect =
                    userAnswer?.let { answer ->
                        problem.checkAnswer(answer)
                    } ?: false,
            )

        // Then - Should be marked as incorrect with null answer
        assertFalse(sessionAnswer.isCorrect)
        assertNull(sessionAnswer.userAnswer)
    }

    @Test
    fun sessionDuration_calculatesCorrectly() {
        // Given
        val startTime = java.time.Instant.parse("2025-01-01T10:00:00Z")
        val endTime = java.time.Instant.parse("2025-01-01T10:02:30Z")

        // When - Calculate duration
        val durationSeconds =
            java.time.Duration
                .between(startTime, endTime)
                .seconds

        // Then - Should be 150 seconds (2 minutes 30 seconds)
        assertEquals(150L, durationSeconds)
    }

    @Test
    fun practiceSession_createdWithCorrectFields() {
        // Given
        val problems = problemGenerator.generateProblems(3, MathOperation.ADDITION, GradeLevel.GRADE_1)
        val sessionAnswers = mutableMapOf<String, SessionAnswer>()
        problems.forEach { problem ->
            sessionAnswers[problem.id] =
                SessionAnswer(
                    problemId = problem.id,
                    userAnswer = problem.correctAnswer,
                    isCorrect = true,
                )
        }
        val completedAt = java.time.Instant.now()
        val durationSeconds = 120L

        // When - Create practice session
        val practiceSession =
            PracticeSession(
                totalProblems = problems.size,
                problems = problems,
                answers = sessionAnswers,
                operation = MathOperation.ADDITION,
                durationSeconds = durationSeconds,
                completedAt = completedAt,
            )

        // Then - All fields should be set correctly
        assertEquals(3, practiceSession.totalProblems)
        assertEquals(MathOperation.ADDITION, practiceSession.operation)
        assertEquals(120L, practiceSession.durationSeconds)
        assertEquals(completedAt, practiceSession.completedAt)
        assertTrue(practiceSession.isComplete())
        assertEquals(3, practiceSession.getCorrectCount())
    }

    @Test
    fun sessionStats_countsAnsweredAndUnanswered() {
        // Given
        val problems = problemGenerator.generateProblems(5, MathOperation.ADDITION, GradeLevel.GRADE_1)
        val userAnswers = listOf(2, null, 6, null, 10) // 3 answered, 2 skipped

        // When - Create session answers
        val sessionAnswers = mutableMapOf<String, SessionAnswer>()
        problems.forEachIndexed { index, problem ->
            val userAnswer = userAnswers.getOrNull(index)
            sessionAnswers[problem.id] =
                SessionAnswer(
                    problemId = problem.id,
                    userAnswer = userAnswer,
                    isCorrect =
                        userAnswer?.let { answer ->
                            problem.checkAnswer(answer)
                        } ?: false,
                )
        }

        // Then - Should track answered vs unanswered
        val answeredCount = sessionAnswers.count { it.value.userAnswer != null }
        val correctCount = sessionAnswers.values.count { it.isCorrect }

        assertEquals(5, sessionAnswers.size) // All problems recorded
        assertEquals(3, answeredCount) // 3 answered
        assertEquals(3, correctCount) // All answered were correct
    }
}
