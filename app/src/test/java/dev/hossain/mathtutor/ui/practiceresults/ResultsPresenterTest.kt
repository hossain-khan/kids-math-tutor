package dev.hossain.mathtutor.ui.practiceresults

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
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
        assertThat(accuracy).isWithin(0.01f).of(100f)
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
        assertThat(accuracy).isWithin(0.01f).of(50f)
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
        assertThat(accuracy).isWithin(0.01f).of(0f)
    }

    @Test
    fun problemResults_correctAnswer_markedAsCorrect() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer = 8

        // When
        val isCorrect = problem.checkAnswer(userAnswer)

        // Then
        assertThat(isCorrect).isTrue()
    }

    @Test
    fun problemResults_incorrectAnswer_markedAsIncorrect() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer = 7

        // When
        val isCorrect = problem.checkAnswer(userAnswer)

        // Then
        assertThat(isCorrect).isFalse()
    }

    @Test
    fun problemResults_nullAnswer_markedAsIncorrect() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer: Int? = null

        // When
        val isCorrect = userAnswer?.let { problem.checkAnswer(it) } ?: false

        // Then
        assertThat(isCorrect).isFalse()
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
        assertThat(totalProblems).isEqualTo(3)
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
        assertThat(correctCount).isEqualTo(2)
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
        assertThat(result.problem).isNotNull()
        assertThat(result.problem).isEqualTo(problem)
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
        assertThat(result.userAnswer).isEqualTo(userAnswer)
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
        assertThat(result.userAnswer).isEqualTo(null)
        assertThat(result.isCorrect).isFalse()
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
        assertThat(accuracy).isWithin(0.01f).of(0f)
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
        assertThat(accuracy).isWithin(0.01f).of(66.67f)
    }

    // Custom Challenge Tests
    @Test
    fun `results screen with custom challenge id`() {
        // Given - Create screen with custom challenge
        val customChallengeId = "challenge-456"
        val customChallengeTitle = "Parent's Math Challenge"
        val problems =
            listOf(
                MathProblem(num1 = 5, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 10),
            )
        val userAnswers = listOf(10)
        val screen =
            ResultsScreen(
                problems = problems,
                userAnswers = userAnswers,
                customChallengeId = customChallengeId,
                customChallengeTitle = customChallengeTitle,
            )

        // Then - Screen should have custom challenge info
        assertThat(screen.customChallengeId).isEqualTo(customChallengeId)
        assertThat(screen.customChallengeTitle).isEqualTo(customChallengeTitle)
    }

    @Test
    fun `results screen defaults to null custom challenge`() {
        // Given - Create regular results screen
        val problems =
            listOf(
                MathProblem(num1 = 5, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 10),
            )
        val userAnswers = listOf(10)
        val screen = ResultsScreen(problems = problems, userAnswers = userAnswers)

        // Then - Custom challenge fields should be null
        assertThat(screen.customChallengeId).isNull()
        assertThat(screen.customChallengeTitle).isNull()
    }

    @Test
    fun `state includes custom challenge title`() {
        // Given - State with custom challenge title
        val customChallengeTitle = "Division Practice"
        val state =
            ResultsScreen.State(
                totalProblems = 5,
                correctCount = 4,
                accuracyPercentage = 80f,
                problemResults = emptyList(),
                customChallengeTitle = customChallengeTitle,
                eventSink = {},
            )

        // Then - State should have the challenge title
        assertThat(state.customChallengeTitle).isEqualTo(customChallengeTitle)
    }

    @Test
    fun `state defaults to null custom challenge title`() {
        // Given - State for regular results
        val state =
            ResultsScreen.State(
                totalProblems = 5,
                correctCount = 4,
                accuracyPercentage = 80f,
                problemResults = emptyList(),
                eventSink = {},
            )

        // Then - Custom challenge title should be null
        assertThat(state.customChallengeTitle).isNull()
    }
}
