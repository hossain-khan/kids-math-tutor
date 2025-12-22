package dev.hossain.mathtutor.domain.model.testutil

import dev.hossain.mathtutor.domain.model.ChallengeImportSpec
import dev.hossain.mathtutor.domain.model.ChallengePracticeSession
import dev.hossain.mathtutor.domain.model.ChallengeType
import dev.hossain.mathtutor.domain.model.CustomChallenge
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.NumberRange
import dev.hossain.mathtutor.domain.model.ProblemSpec
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

/**
 * Factory object for creating test data for custom challenge domain models.
 *
 * Provides convenient methods to create domain objects with sensible defaults
 * while allowing customization of specific fields.
 */
object TestDataFactory {
    /**
     * Creates a [ChallengeImportSpec.Generated] with default or custom values.
     *
     * @param title The challenge title
     * @param subtitle Optional subtitle
     * @param operation The math operation type
     * @param problemCount Number of problems to generate
     * @param numberRange Range for number generation
     * @return A generated challenge import spec
     */
    fun createGeneratedChallengeSpec(
        title: String = "Test Challenge",
        subtitle: String? = null,
        operation: MathOperation = MathOperation.ADDITION,
        problemCount: Int = 10,
        numberRange: NumberRange = NumberRange(1, 10),
    ): ChallengeImportSpec.Generated =
        ChallengeImportSpec.Generated(
            title = title,
            subtitle = subtitle,
            operation = operation,
            problemCount = problemCount,
            numberRange = numberRange,
        )

    /**
     * Creates a [ChallengeImportSpec.Explicit] with default or custom values.
     *
     * @param title The challenge title
     * @param subtitle Optional subtitle
     * @param problems List of problem specifications
     * @return An explicit challenge import spec
     */
    fun createExplicitChallengeSpec(
        title: String = "Test Challenge",
        subtitle: String? = null,
        problems: List<ProblemSpec> =
            listOf(
                ProblemSpec(3, 5, MathOperation.ADDITION),
                ProblemSpec(8, 2, MathOperation.SUBTRACTION),
            ),
    ): ChallengeImportSpec.Explicit =
        ChallengeImportSpec.Explicit(
            title = title,
            subtitle = subtitle,
            problems = problems,
        )

    /**
     * Creates a [CustomChallenge] with default or custom values.
     *
     * @param id Unique identifier
     * @param title The challenge title
     * @param subtitle Optional subtitle
     * @param type Challenge type
     * @param problems List of math problems
     * @param createdAt Creation timestamp
     * @param isArchived Archive status
     * @param practiceHistory List of practice sessions
     * @return A custom challenge
     */
    fun createCustomChallenge(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Challenge",
        subtitle: String? = null,
        type: ChallengeType = ChallengeType.GENERATED,
        problems: List<MathProblem> =
            listOf(
                createMathProblem(1, 2, MathOperation.ADDITION, 3),
                createMathProblem(3, 4, MathOperation.ADDITION, 7),
            ),
        createdAt: Instant = Instant.now(),
        isArchived: Boolean = false,
        practiceHistory: List<ChallengePracticeSession> = emptyList(),
    ): CustomChallenge =
        CustomChallenge(
            id = id,
            title = title,
            subtitle = subtitle,
            type = type,
            problems = problems,
            createdAt = createdAt,
            isArchived = isArchived,
            practiceHistory = practiceHistory,
        )

    /**
     * Creates a [MathProblem] with specified values.
     *
     * @param num1 First operand
     * @param num2 Second operand
     * @param operation Math operation
     * @param correctAnswer The correct answer
     * @return A math problem
     */
    fun createMathProblem(
        num1: Int,
        num2: Int,
        operation: MathOperation,
        correctAnswer: Int,
    ): MathProblem =
        MathProblem(
            num1 = num1,
            num2 = num2,
            operation = operation,
            correctAnswer = correctAnswer,
        )

    /**
     * Creates a [ChallengePracticeSession] with default or custom values.
     *
     * @param sessionId Unique session identifier
     * @param startTime Session start time
     * @param endTime Session end time (null if in progress)
     * @param problemsAttempted Number of problems attempted
     * @param correctAnswers Number of correct answers
     * @param totalTimeMs Total time in milliseconds
     * @return A practice session
     */
    fun createPracticeSession(
        sessionId: String = UUID.randomUUID().toString(),
        startTime: Instant = Instant.now(),
        endTime: Instant? = Instant.now().plusSeconds(300),
        problemsAttempted: Int = 10,
        correctAnswers: Int = 8,
        totalTimeMs: Long = 5.minutes.inWholeMilliseconds,
    ): ChallengePracticeSession =
        ChallengePracticeSession(
            sessionId = sessionId,
            startTime = startTime,
            endTime = endTime,
            problemsAttempted = problemsAttempted,
            correctAnswers = correctAnswers,
            totalTimeMs = totalTimeMs,
        )

    /**
     * Creates a list of [ProblemSpec] for testing explicit challenges.
     *
     * @param count Number of problem specs to create
     * @param operation Operation type (null for mixed)
     * @return List of problem specifications
     */
    fun createProblemSpecs(
        count: Int = 5,
        operation: MathOperation? = MathOperation.ADDITION,
    ): List<ProblemSpec> =
        List(count) { index ->
            val num1 = (index + 1) * 2
            val num2 = index + 1
            val op = operation ?: MathOperation.entries.filter { it != MathOperation.MIXED }.random()
            ProblemSpec(num1, num2, op)
        }
}
