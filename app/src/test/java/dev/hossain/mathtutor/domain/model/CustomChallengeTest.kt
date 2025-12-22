package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant

class CustomChallengeTest {
    private fun createMockProblem(
        id: String,
        num1: Int = 5,
        num2: Int = 3,
    ): MathProblem =
        MathProblem(
            id = id,
            num1 = num1,
            num2 = num2,
            operation = MathOperation.ADDITION,
            correctAnswer = num1 + num2,
        )

    @Test
    fun `creates challenge with all properties`() {
        val createdAt = Instant.now()
        val problems =
            listOf(
                createMockProblem("1"),
                createMockProblem("2"),
                createMockProblem("3"),
            )
        val sessions =
            listOf(
                ChallengePracticeSession(
                    startTime = Instant.now(),
                    endTime = Instant.now().plusSeconds(60),
                    problemsAttempted = 3,
                    correctAnswers = 2,
                    totalTimeMs = 60000L,
                ),
            )

        val challenge =
            CustomChallenge(
                id = "test-id",
                title = "Addition Practice",
                subtitle = "Basic addition",
                type = ChallengeType.GENERATED,
                problems = problems,
                createdAt = createdAt,
                isArchived = false,
                practiceHistory = sessions,
            )

        assertThat(challenge.id).isEqualTo("test-id")
        assertThat(challenge.title).isEqualTo("Addition Practice")
        assertThat(challenge.subtitle).isEqualTo("Basic addition")
        assertThat(challenge.type).isEqualTo(ChallengeType.GENERATED)
        assertThat(challenge.problems).hasSize(3)
        assertThat(challenge.createdAt).isEqualTo(createdAt)
        assertThat(challenge.isArchived).isFalse()
        assertThat(challenge.practiceHistory).hasSize(1)
    }

    @Test
    fun `challenge has unique ID when not specified`() {
        val challenge1 =
            CustomChallenge(
                title = "Challenge 1",
                type = ChallengeType.GENERATED,
                problems = emptyList(),
            )
        val challenge2 =
            CustomChallenge(
                title = "Challenge 2",
                type = ChallengeType.GENERATED,
                problems = emptyList(),
            )

        assertThat(challenge1.id).isNotEmpty()
        assertThat(challenge2.id).isNotEmpty()
        assertThat(challenge1.id).isNotEqualTo(challenge2.id)
    }

    @Test
    fun `creates challenge with default values`() {
        val challenge =
            CustomChallenge(
                title = "Test Challenge",
                type = ChallengeType.EXPLICIT,
                problems = emptyList(),
            )

        assertThat(challenge.id).isNotEmpty()
        assertThat(challenge.subtitle).isNull()
        assertThat(challenge.createdAt).isNotNull()
        assertThat(challenge.isArchived).isFalse()
        assertThat(challenge.practiceHistory).isEmpty()
    }

    @Test
    fun `getProblemCount returns correct count`() {
        val problems =
            listOf(
                createMockProblem("1"),
                createMockProblem("2"),
                createMockProblem("3"),
                createMockProblem("4"),
                createMockProblem("5"),
            )
        val challenge =
            CustomChallenge(
                title = "Test",
                type = ChallengeType.GENERATED,
                problems = problems,
            )

        assertThat(challenge.getProblemCount()).isEqualTo(5)
    }

    @Test
    fun `getProblemCount returns 0 for empty problems`() {
        val challenge =
            CustomChallenge(
                title = "Test",
                type = ChallengeType.EXPLICIT,
                problems = emptyList(),
            )

        assertThat(challenge.getProblemCount()).isEqualTo(0)
    }

    @Test
    fun `getTotalPracticeSessions counts completed sessions only`() {
        val sessions =
            listOf(
                ChallengePracticeSession(
                    startTime = Instant.now(),
                    endTime = Instant.now().plusSeconds(60),
                    problemsAttempted = 5,
                    correctAnswers = 4,
                    totalTimeMs = 60000L,
                ),
                ChallengePracticeSession(
                    startTime = Instant.now(),
                    endTime = null, // Incomplete session
                    problemsAttempted = 3,
                    correctAnswers = 2,
                    totalTimeMs = 30000L,
                ),
                ChallengePracticeSession(
                    startTime = Instant.now(),
                    endTime = Instant.now().plusSeconds(90),
                    problemsAttempted = 5,
                    correctAnswers = 5,
                    totalTimeMs = 90000L,
                ),
            )

        val challenge =
            CustomChallenge(
                title = "Test",
                type = ChallengeType.GENERATED,
                problems = emptyList(),
                practiceHistory = sessions,
            )

        assertThat(challenge.getTotalPracticeSessions()).isEqualTo(2)
    }

    @Test
    fun `getTotalPracticeSessions returns 0 when no sessions`() {
        val challenge =
            CustomChallenge(
                title = "Test",
                type = ChallengeType.GENERATED,
                problems = emptyList(),
            )

        assertThat(challenge.getTotalPracticeSessions()).isEqualTo(0)
    }

    @Test
    fun `getAverageAccuracy calculates correct average`() {
        val sessions =
            listOf(
                ChallengePracticeSession(
                    startTime = Instant.now(),
                    endTime = Instant.now().plusSeconds(60),
                    problemsAttempted = 10,
                    correctAnswers = 8, // 80%
                    totalTimeMs = 60000L,
                ),
                ChallengePracticeSession(
                    startTime = Instant.now(),
                    endTime = Instant.now().plusSeconds(60),
                    problemsAttempted = 10,
                    correctAnswers = 10, // 100%
                    totalTimeMs = 60000L,
                ),
            )

        val challenge =
            CustomChallenge(
                title = "Test",
                type = ChallengeType.GENERATED,
                problems = emptyList(),
                practiceHistory = sessions,
            )

        // Average of 80% and 100% = 90%
        assertThat(challenge.getAverageAccuracy()).isWithin(0.01f).of(90f)
    }

    @Test
    fun `getAverageAccuracy ignores incomplete sessions`() {
        val sessions =
            listOf(
                ChallengePracticeSession(
                    startTime = Instant.now(),
                    endTime = Instant.now().plusSeconds(60),
                    problemsAttempted = 10,
                    correctAnswers = 8, // 80%
                    totalTimeMs = 60000L,
                ),
                ChallengePracticeSession(
                    startTime = Instant.now(),
                    endTime = null, // Incomplete - should be ignored
                    problemsAttempted = 10,
                    correctAnswers = 0,
                    totalTimeMs = 30000L,
                ),
            )

        val challenge =
            CustomChallenge(
                title = "Test",
                type = ChallengeType.GENERATED,
                problems = emptyList(),
                practiceHistory = sessions,
            )

        // Should only average the completed session (80%)
        assertThat(challenge.getAverageAccuracy()).isWithin(0.01f).of(80f)
    }

    @Test
    fun `getAverageAccuracy returns 0 when no completed sessions`() {
        val challenge =
            CustomChallenge(
                title = "Test",
                type = ChallengeType.GENERATED,
                problems = emptyList(),
                practiceHistory = emptyList(),
            )

        assertThat(challenge.getAverageAccuracy()).isWithin(0.01f).of(0f)
    }

    @Test
    fun `challenge can be created with GENERATED type`() {
        val challenge =
            CustomChallenge(
                title = "Generated Challenge",
                type = ChallengeType.GENERATED,
                problems = listOf(createMockProblem("1")),
            )

        assertThat(challenge.type).isEqualTo(ChallengeType.GENERATED)
    }

    @Test
    fun `challenge can be created with EXPLICIT type`() {
        val challenge =
            CustomChallenge(
                title = "Explicit Challenge",
                type = ChallengeType.EXPLICIT,
                problems = listOf(createMockProblem("1")),
            )

        assertThat(challenge.type).isEqualTo(ChallengeType.EXPLICIT)
    }

    @Test
    fun `challenge can be archived`() {
        val challenge =
            CustomChallenge(
                title = "Archived Challenge",
                type = ChallengeType.GENERATED,
                problems = emptyList(),
                isArchived = true,
            )

        assertThat(challenge.isArchived).isTrue()
    }

    @Test
    fun `challenge subtitle can be null`() {
        val challenge =
            CustomChallenge(
                title = "Challenge Without Subtitle",
                subtitle = null,
                type = ChallengeType.GENERATED,
                problems = emptyList(),
            )

        assertThat(challenge.subtitle).isNull()
    }

    @Test
    fun `challenge with multiple practice sessions calculates average correctly`() {
        val sessions =
            listOf(
                ChallengePracticeSession(
                    startTime = Instant.now(),
                    endTime = Instant.now().plusSeconds(60),
                    problemsAttempted = 10,
                    correctAnswers = 10, // 100%
                    totalTimeMs = 60000L,
                ),
                ChallengePracticeSession(
                    startTime = Instant.now(),
                    endTime = Instant.now().plusSeconds(60),
                    problemsAttempted = 10,
                    correctAnswers = 8, // 80%
                    totalTimeMs = 60000L,
                ),
                ChallengePracticeSession(
                    startTime = Instant.now(),
                    endTime = Instant.now().plusSeconds(60),
                    problemsAttempted = 10,
                    correctAnswers = 6, // 60%
                    totalTimeMs = 60000L,
                ),
            )

        val challenge =
            CustomChallenge(
                title = "Test",
                type = ChallengeType.GENERATED,
                problems = emptyList(),
                practiceHistory = sessions,
            )

        // Average of 100%, 80%, 60% = 80%
        assertThat(challenge.getAverageAccuracy()).isWithin(0.01f).of(80f)
    }
}
