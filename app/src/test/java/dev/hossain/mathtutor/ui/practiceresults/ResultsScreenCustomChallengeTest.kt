package dev.hossain.mathtutor.ui.practiceresults

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import org.junit.Test

/**
 * Unit tests for custom challenge integration in [ResultsScreen].
 *
 * Tests the results screen with custom challenge metadata including
 * challenge title display and result presentation.
 */
class ResultsScreenCustomChallengeTest {
    @Test
    fun resultsScreen_withCustomChallengeData_hasCorrectParameters() {
        // Given
        val problems =
            listOf(
                MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
                MathProblem(num1 = 7, num2 = 2, operation = MathOperation.SUBTRACTION, correctAnswer = 5),
            )
        val userAnswers = listOf(8, 5)
        val challengeId = "challenge-123"
        val challengeTitle = "Emma's Challenge"

        // When
        val screen =
            ResultsScreen(
                problems = problems,
                userAnswers = userAnswers,
                badgesAlreadyChecked = true,
                customChallengeId = challengeId,
                customChallengeTitle = challengeTitle,
            )

        // Then
        assertThat(screen.problems).hasSize(2)
        assertThat(screen.userAnswers).hasSize(2)
        assertThat(screen.customChallengeId).isEqualTo(challengeId)
        assertThat(screen.customChallengeTitle).isEqualTo(challengeTitle)
    }

    @Test
    fun resultsScreen_withoutCustomChallenge_hasNullMetadata() {
        // Given
        val problems =
            listOf(
                MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
            )
        val userAnswers = listOf(8)

        // When
        val screen =
            ResultsScreen(
                problems = problems,
                userAnswers = userAnswers,
                badgesAlreadyChecked = true,
            )

        // Then
        assertThat(screen.customChallengeId).isNull()
        assertThat(screen.customChallengeTitle).isNull()
    }

    @Test
    fun resultsState_withCustomChallengeTitle_includesTitle() {
        // Given
        val challengeTitle = "Parent's Custom Challenge"
        val problemResults =
            listOf(
                ResultsScreen.ProblemResult(
                    problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
                    userAnswer = 8,
                    isCorrect = true,
                ),
            )

        // When
        val state =
            ResultsScreen.State(
                totalProblems = 1,
                correctCount = 1,
                accuracyPercentage = 100.0f,
                problemResults = problemResults,
                customChallengeTitle = challengeTitle,
                eventSink = {},
            )

        // Then
        assertThat(state.customChallengeTitle).isEqualTo(challengeTitle)
        assertThat(state.accuracyPercentage).isEqualTo(100.0f)
    }

    @Test
    fun resultsState_calculatesCorrectAccuracy_forCustomChallenge() {
        // Given
        val problemResults =
            listOf(
                ResultsScreen.ProblemResult(
                    problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
                    userAnswer = 8,
                    isCorrect = true,
                ),
                ResultsScreen.ProblemResult(
                    problem = MathProblem(num1 = 7, num2 = 2, operation = MathOperation.SUBTRACTION, correctAnswer = 5),
                    userAnswer = 4,
                    isCorrect = false,
                ),
                ResultsScreen.ProblemResult(
                    problem = MathProblem(num1 = 4, num2 = 6, operation = MathOperation.MULTIPLICATION, correctAnswer = 24),
                    userAnswer = 24,
                    isCorrect = true,
                ),
            )

        // When
        val correctCount = problemResults.count { it.isCorrect }
        val totalProblems = problemResults.size
        val accuracyPercentage = (correctCount.toFloat() / totalProblems) * 100f

        val state =
            ResultsScreen.State(
                totalProblems = totalProblems,
                correctCount = correctCount,
                accuracyPercentage = accuracyPercentage,
                problemResults = problemResults,
                customChallengeTitle = "Mixed Operations Challenge",
                eventSink = {},
            )

        // Then
        assertThat(state.totalProblems).isEqualTo(3)
        assertThat(state.correctCount).isEqualTo(2)
        assertThat(state.accuracyPercentage).isWithin(0.01f).of(66.67f)
    }
}
