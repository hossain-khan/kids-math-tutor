package dev.hossain.mathtutor.ui.mathpractice

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import org.junit.Test

/**
 * Unit tests for custom challenge integration in [MathPracticePresenter].
 *
 * Tests the custom challenge practice flow including screen parameters,
 * problem loading, and results navigation.
 */
class MathPracticeCustomChallengeTest {
    @Test
    fun mathPracticeScreen_withCustomChallengeId_hasCorrectParameters() {
        // Given
        val challengeId = "test-challenge-123"

        // When
        val screen =
            MathPracticeScreen(
                operation = MathOperation.ADDITION,
                problemCount = 10,
                customChallengeId = challengeId,
            )

        // Then
        assertThat(screen.customChallengeId).isEqualTo(challengeId)
        assertThat(screen.problemCount).isEqualTo(10)
        assertThat(screen.operation).isEqualTo(MathOperation.ADDITION)
    }

    @Test
    fun mathPracticeScreen_withoutCustomChallengeId_isNull() {
        // When
        val screen =
            MathPracticeScreen(
                operation = MathOperation.SUBTRACTION,
                problemCount = 5,
            )

        // Then
        assertThat(screen.customChallengeId).isNull()
    }

    @Test
    fun mathPracticeState_withCustomChallengeTitle_includesTitle() {
        // Given
        val challengeTitle = "Emma's Math Challenge"

        // When
        val state =
            MathPracticeScreen.State(
                currentProblem = null,
                currentAnswer = "",
                currentProblemIndex = 0,
                totalProblems = 10,
                isCorrect = null,
                customChallengeTitle = challengeTitle,
                eventSink = {},
            )

        // Then
        assertThat(state.customChallengeTitle).isEqualTo(challengeTitle)
    }

    @Test
    fun customChallenge_providesProblems_forPractice() {
        // Given
        val problems =
            listOf(
                MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
                MathProblem(num1 = 7, num2 = 2, operation = MathOperation.SUBTRACTION, correctAnswer = 5),
                MathProblem(num1 = 4, num2 = 6, operation = MathOperation.MULTIPLICATION, correctAnswer = 24),
            )

        val challenge =
            CustomChallenge(
                id = "challenge-1",
                title = "Mixed Operations",
                subtitle = "Practice all operations",
                type = dev.hossain.mathtutor.domain.model.ChallengeType.EXPLICIT,
                problems = problems,
            )

        // Then
        assertThat(challenge.problems).hasSize(3)
        assertThat(challenge.getProblemCount()).isEqualTo(3)
        assertThat(challenge.title).isEqualTo("Mixed Operations")
    }

    @Test
    fun challengePracticeSession_tracksMetrics() {
        // Given
        val startTime = java.time.Instant.now()
        val endTime = startTime.plusSeconds(120) // 2 minutes

        // When
        val session =
            dev.hossain.mathtutor.domain.model.ChallengePracticeSession(
                startTime = startTime,
                endTime = endTime,
                problemsAttempted = 10,
                correctAnswers = 8,
                totalTimeMs = 120_000,
            )

        // Then
        assertThat(session.problemsAttempted).isEqualTo(10)
        assertThat(session.correctAnswers).isEqualTo(8)
        assertThat(session.getAccuracy()).isEqualTo(80.0f)
        assertThat(session.isComplete()).isTrue()
    }
}
