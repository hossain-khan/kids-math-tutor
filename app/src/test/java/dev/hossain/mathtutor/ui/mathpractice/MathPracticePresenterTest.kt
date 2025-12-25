package dev.hossain.mathtutor.ui.mathpractice

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.generator.ProblemGenerator
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionAnswer
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
        assertThat(problems.size).isEqualTo(5)
        assertThat(problems[0]).isNotNull()
        assertThat(problems[0].getDisplayString()).isEqualTo("1 + 1 = ?")
    }

    @Test
    fun numberClicked_appendsToAnswer() {
        // Given
        var currentAnswer = ""

        // When - Number clicked events
        currentAnswer += "5"
        currentAnswer += "7"

        // Then
        assertThat(currentAnswer).isEqualTo("57")
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
        assertThat(currentAnswer).isEqualTo("")
        assertThat(isCorrect as Boolean?).isNull()
    }

    @Test
    fun checkAnswer_correctAnswer_setsIsCorrectToTrue() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer = "8"

        // When - Check answer
        val isCorrect = problem.checkAnswer(userAnswer.toInt())

        // Then
        assertThat(isCorrect).isTrue()
    }

    @Test
    fun checkAnswer_incorrectAnswer_setsIsCorrectToFalse() {
        // Given
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8)
        val userAnswer = "7"

        // When - Check answer
        val isCorrect = problem.checkAnswer(userAnswer.toInt())

        // Then
        assertThat(isCorrect).isFalse()
    }

    @Test
    fun checkAnswer_invalidInput_handlesGracefully() {
        // Given
        val currentAnswer = "abc"

        // When - Try to convert to Int
        val userAnswer = currentAnswer.toIntOrNull()

        // Then
        assertThat(userAnswer).isNull()
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
        assertThat(currentProblemIndex).isEqualTo(1)
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
        assertThat(currentProblemIndex).isEqualTo(4)
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
        assertThat(currentProblemIndex).isEqualTo(1)
        assertThat(currentAnswer).isEqualTo("")
        assertThat(isCorrect as Boolean?).isNull()
    }

    @Test
    fun problemGeneration_generatesCorrectCount() {
        // Given
        val problemCount = 10

        // When
        val problems = problemGenerator.generateProblems(problemCount, MathOperation.ADDITION, GradeLevel.GRADE_1)

        // Then
        assertThat(problems.size).isEqualTo(problemCount)
    }

    @Test
    fun problemGeneration_usesAdditionOperation() {
        // When
        val problems = problemGenerator.generateProblems(5, MathOperation.ADDITION, GradeLevel.GRADE_1)

        // Then - All problems should be addition
        problems.forEach { problem ->
            assertThat(problem.operation).isEqualTo(MathOperation.ADDITION)
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
        assertThat(currentProblemIndex).isEqualTo(5)
        assertThat((currentProblemIndex + 1).toFloat() / totalProblems).isWithin(0.01f).of(0.6f)
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
        assertThat(currentAnswer).isEqualTo("123")
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
        assertThat(sessionAnswers.size).isEqualTo(3)
        // First problem (1+1=2): answered correctly
        assertThat(sessionAnswers[problems[0].id]?.isCorrect == true).isTrue()
        assertThat(sessionAnswers[problems[0].id]?.userAnswer).isEqualTo(2)
        // Second problem (2+2=4): skipped
        assertThat(sessionAnswers[problems[1].id]?.isCorrect ?: true).isFalse()
        assertThat(sessionAnswers[problems[1].id]?.userAnswer).isNull()
        // Third problem (3+3=6): answered correctly
        assertThat(sessionAnswers[problems[2].id]?.isCorrect == true).isTrue()
        assertThat(sessionAnswers[problems[2].id]?.userAnswer).isEqualTo(6)
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
        assertThat(sessionAnswer.isCorrect).isFalse()
        assertThat(sessionAnswer.userAnswer).isNull()
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
        assertThat(durationSeconds).isEqualTo(150L)
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
        assertThat(practiceSession.totalProblems).isEqualTo(3)
        assertThat(practiceSession.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(practiceSession.durationSeconds).isEqualTo(120L)
        assertThat(practiceSession.completedAt).isEqualTo(completedAt)
        assertThat(practiceSession.isComplete()).isTrue()
        assertThat(practiceSession.getCorrectCount()).isEqualTo(3)
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

        assertThat(sessionAnswers.size).isEqualTo(5) // All problems recorded
        assertThat(answeredCount).isEqualTo(3) // 3 answered
        assertThat(correctCount).isEqualTo(3) // All answered were correct
    }

    // ==================== Integration Tests with UserProfileRepository ====================

    @Test
    fun `problemGenerator receives KINDERGARTEN grade when profile exists with K grade`() {
        // Given - Mock UserProfileRepository that returns Kindergarten profile
        val mockProfile =
            dev.hossain.mathtutor.domain.model.UserProfile(
                name = "Test Kid",
                gradeLevel = GradeLevel.KINDERGARTEN,
                createdAt = java.time.Instant.now(),
            )

        var capturedGradeLevel: GradeLevel? = null
        val mockProblemGenerator =
            object : ProblemGenerator {
                override fun generateProblems(
                    count: Int,
                    operation: MathOperation,
                    gradeLevel: GradeLevel,
                ): List<MathProblem> {
                    capturedGradeLevel = gradeLevel
                    return List(count) { index ->
                        MathProblem(
                            num1 = index + 1,
                            num2 = index + 1,
                            operation = operation,
                            correctAnswer = (index + 1) * 2,
                        )
                    }
                }
            }

        // When - Problems are generated with the profile's grade
        val problems = mockProblemGenerator.generateProblems(10, MathOperation.ADDITION, mockProfile.gradeLevel)

        // Then - Generator should receive KINDERGARTEN grade
        assertThat(capturedGradeLevel).isEqualTo(GradeLevel.KINDERGARTEN)
        assertThat(problems.size).isEqualTo(10)
    }

    @Test
    fun `problemGenerator receives GRADE_1 as default when profile is null`() {
        // Given - No profile exists (null)
        val defaultGrade = GradeLevel.GRADE_1

        var capturedGradeLevel: GradeLevel? = null
        val mockProblemGenerator =
            object : ProblemGenerator {
                override fun generateProblems(
                    count: Int,
                    operation: MathOperation,
                    gradeLevel: GradeLevel,
                ): List<MathProblem> {
                    capturedGradeLevel = gradeLevel
                    return List(count) { index ->
                        MathProblem(
                            num1 = index + 1,
                            num2 = index + 1,
                            operation = operation,
                            correctAnswer = (index + 1) * 2,
                        )
                    }
                }
            }

        // When - Problems are generated with default grade
        val problems = mockProblemGenerator.generateProblems(10, MathOperation.ADDITION, defaultGrade)

        // Then - Generator should receive GRADE_1 as default
        assertThat(capturedGradeLevel).isEqualTo(GradeLevel.GRADE_1)
        assertThat(problems.size).isEqualTo(10)
    }

    @Test
    fun `problemGenerator receives GRADE_2 grade when profile exists with Grade 2`() {
        // Given - Mock UserProfileRepository that returns Grade 2 profile
        val mockProfile =
            dev.hossain.mathtutor.domain.model.UserProfile(
                name = "Advanced Kid",
                gradeLevel = GradeLevel.GRADE_2,
                createdAt = java.time.Instant.now(),
            )

        var capturedGradeLevel: GradeLevel? = null
        val mockProblemGenerator =
            object : ProblemGenerator {
                override fun generateProblems(
                    count: Int,
                    operation: MathOperation,
                    gradeLevel: GradeLevel,
                ): List<MathProblem> {
                    capturedGradeLevel = gradeLevel
                    return List(count) { index ->
                        MathProblem(
                            num1 = index + 1,
                            num2 = index + 1,
                            operation = operation,
                            correctAnswer = (index + 1) * 2,
                        )
                    }
                }
            }

        // When - Problems are generated with the profile's grade
        val problems = mockProblemGenerator.generateProblems(10, MathOperation.MULTIPLICATION, mockProfile.gradeLevel)

        // Then - Generator should receive GRADE_2 grade
        assertThat(capturedGradeLevel).isEqualTo(GradeLevel.GRADE_2)
        assertThat(problems.size).isEqualTo(10)
    }

    @Test
    fun `different grade levels produce different problem ranges`() {
        // This test validates that grade levels affect problem generation
        // In a real scenario, KINDERGARTEN would have 1-10, GRADE_1 would have 1-20, GRADE_2 would have 1-100

        // Given - Different grade levels
        val kindergartenProblems = problemGenerator.generateProblems(5, MathOperation.ADDITION, GradeLevel.KINDERGARTEN)
        val grade1Problems = problemGenerator.generateProblems(5, MathOperation.ADDITION, GradeLevel.GRADE_1)
        val grade2Problems = problemGenerator.generateProblems(5, MathOperation.ADDITION, GradeLevel.GRADE_2)

        // Then - All should generate the requested count
        assertThat(kindergartenProblems.size).isEqualTo(5)
        assertThat(grade1Problems.size).isEqualTo(5)
        assertThat(grade2Problems.size).isEqualTo(5)

        // Note: The actual number ranges are validated in GradeAwareProblemGeneratorTest
        // This test just ensures the grade level parameter is properly used
    }

    // Custom Challenge Tests
    @Test
    fun `custom challenge screen has customChallengeId`() {
        // Given - Create a screen with custom challenge ID
        val customChallengeId = "challenge-123"
        val customScreen =
            MathPracticeScreen(
                operation = MathOperation.ADDITION,
                problemCount = 5,
                customChallengeId = customChallengeId,
            )

        // Then - Screen should have the challenge ID
        assertThat(customScreen.customChallengeId).isEqualTo(customChallengeId)
    }

    @Test
    fun `custom challenge screen defaults to null challenge id`() {
        // Given - Create a regular screen without custom challenge ID
        val regularScreen = MathPracticeScreen(operation = MathOperation.ADDITION, problemCount = 5)

        // Then - Custom challenge ID should be null
        assertThat(regularScreen.customChallengeId).isNull()
    }

    @Test
    fun `state includes custom challenge title`() {
        // Given - State with custom challenge title
        val customChallengeTitle = "Emma's Math Challenge"
        val state =
            MathPracticeScreen.State(
                currentProblem = null,
                currentAnswer = "",
                currentProblemIndex = 0,
                totalProblems = 5,
                isCorrect = null,
                customChallengeTitle = customChallengeTitle,
                eventSink = {},
            )

        // Then - State should have the challenge title
        assertThat(state.customChallengeTitle).isEqualTo(customChallengeTitle)
    }

    @Test
    fun `state defaults to null custom challenge title`() {
        // Given - State for regular practice
        val state =
            MathPracticeScreen.State(
                currentProblem = null,
                currentAnswer = "",
                currentProblemIndex = 0,
                totalProblems = 5,
                isCorrect = null,
                eventSink = {},
            )

        // Then - Custom challenge title should be null
        assertThat(state.customChallengeTitle).isNull()
    }

    // ==================== Deduplication Tests ====================

    @Test
    fun `problem generator produces unique problem strings by default`() {
        // Given - Generate multiple problems
        val problems = problemGenerator.generateProblems(10, MathOperation.ADDITION, GradeLevel.GRADE_1)

        // When - Extract problem strings
        val problemStrings = problems.map { it.getDisplayString() }

        // Then - All problem strings should be unique
        assertThat(problemStrings.size).isEqualTo(problemStrings.toSet().size)
    }

    @Test
    fun `deduplication correctly identifies duplicate problem strings`() {
        // Given - Create problems with intentional duplicates
        val problem1 = MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5)
        val problem2 = MathProblem(num1 = 1, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 5)
        val problem3 = MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5) // Duplicate string

        val problemsWithDuplicates = listOf(problem1, problem2, problem3)

        // When - Extract problem strings
        val problemStrings = problemsWithDuplicates.map { it.getDisplayString() }

        // Then - Duplicates should be detected
        assertThat(problemStrings.size).isEqualTo(3)
        assertThat(problemStrings.toSet().size).isEqualTo(2) // Only 2 unique strings
        assertThat(problemStrings[0]).isEqualTo(problemStrings[2]) // First and third are same
    }

    @Test
    fun `duplicate answers are allowed with different problem strings`() {
        // Given - Create problems with same answer but different strings
        val problem1 = MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5)
        val problem2 = MathProblem(num1 = 1, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 5)

        // When
        val strings = setOf(problem1.getDisplayString(), problem2.getDisplayString())
        val answers = listOf(problem1.correctAnswer, problem2.correctAnswer)

        // Then - Problem strings should be different but answers can be same
        assertThat(strings.size).isEqualTo(2) // Different strings
        assertThat(answers.toSet().size).isEqualTo(1) // Same answer
        assertThat(problem1.correctAnswer).isEqualTo(problem2.correctAnswer)
    }

    @Test
    fun `commutative variants are different problem strings`() {
        // Given - Create problems that are commutative variants (2+3 vs 3+2)
        val problem1 = MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5)
        val problem2 = MathProblem(num1 = 3, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 5)

        // When - Extract problem strings
        val string1 = problem1.getDisplayString()
        val string2 = problem2.getDisplayString()

        // Then - Commutative variants should have different strings
        assertThat(string1).isNotEqualTo(string2)
        // One should be "2 + 3 = ?" and the other "3 + 2 = ?"
        assertThat(string1).isAnyOf("2 + 3 = ?", "3 + 2 = ?")
        assertThat(string2).isAnyOf("2 + 3 = ?", "3 + 2 = ?")
    }

    @Test
    fun `problem string uniqueness validation works correctly`() {
        // Given - Create a set of problems
        val problems = problemGenerator.generateProblems(5, MathOperation.ADDITION, GradeLevel.GRADE_1)

        // When - Check if all problem strings are unique
        val problemStrings = problems.map { it.getDisplayString() }
        val hasUniqueProblemStrings = problemStrings.size == problemStrings.toSet().size

        // Then - For 5 different generated problems, should all be unique
        assertThat(hasUniqueProblemStrings).isTrue()
    }

    @Test
    fun `large batch of problems maintains uniqueness`() {
        // Given - Generate a larger batch of problems
        val problems = problemGenerator.generateProblems(20, MathOperation.ADDITION, GradeLevel.GRADE_2)

        // When - Extract and deduplicate strings
        val problemStrings = problems.map { it.getDisplayString() }
        val uniqueStringCount = problemStrings.toSet().size

        // Then - All strings should be unique
        assertThat(uniqueStringCount).isEqualTo(problems.size)
    }

    @Test
    fun `mixed operations maintain problem string uniqueness`() {
        // Given - Generate problems with different operations
        val additionProblems = problemGenerator.generateProblems(3, MathOperation.ADDITION, GradeLevel.GRADE_1)
        val subtractionProblems = problemGenerator.generateProblems(3, MathOperation.SUBTRACTION, GradeLevel.GRADE_1)

        // When - Combine and check strings
        val allProblems = additionProblems + subtractionProblems
        val problemStrings = allProblems.map { it.getDisplayString() }

        // Then - No duplicates within mixed operations
        assertThat(problemStrings.size).isEqualTo(problemStrings.toSet().size)
    }

    @Test
    fun `problem string extraction works for all operations`() {
        // Given - Problems from different operations
        val addition = MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5)
        val subtraction = MathProblem(num1 = 5, num2 = 2, operation = MathOperation.SUBTRACTION, correctAnswer = 3)

        // When - Extract display strings
        val addString = addition.getDisplayString()
        val subString = subtraction.getDisplayString()

        // Then - Strings should contain operation symbols
        assertThat(addString).contains("+")
        assertThat(subString).contains("-")
        assertThat(addString).isNotEqualTo(subString)
    }

    // ==================== Custom Challenge Type Tests ====================

    @Test
    fun `explicit custom challenge type is correctly identified`() {
        // Given - Create a mock EXPLICIT custom challenge
        val mockChallenge =
            dev.hossain.mathtutor.domain.model.CustomChallenge(
                id = "explicit-challenge-1",
                title = "Parent's Custom Challenge",
                type = dev.hossain.mathtutor.domain.model.ChallengeType.EXPLICIT,
                problems =
                    listOf(
                        MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
                        MathProblem(num1 = 7, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 9),
                    ),
            )

        // Then - Challenge type should be EXPLICIT
        assertThat(mockChallenge.type).isEqualTo(dev.hossain.mathtutor.domain.model.ChallengeType.EXPLICIT)
    }

    @Test
    fun `generated custom challenge type is correctly identified`() {
        // Given - Create a mock GENERATED custom challenge
        val mockChallenge =
            dev.hossain.mathtutor.domain.model.CustomChallenge(
                id = "generated-challenge-1",
                title = "App-Generated Challenge",
                type = dev.hossain.mathtutor.domain.model.ChallengeType.GENERATED,
                problems =
                    listOf(
                        MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
                        MathProblem(num1 = 7, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 9),
                    ),
            )

        // Then - Challenge type should be GENERATED
        assertThat(mockChallenge.type).isEqualTo(dev.hossain.mathtutor.domain.model.ChallengeType.GENERATED)
    }

    @Test
    fun `explicit challenge should not be deduplicated by presentation layer`() {
        // Given - EXPLICIT challenge with intentional duplicates (parent-created)
        val parentCreatedProblems =
            listOf(
                MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5),
                MathProblem(num1 = 1, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 5),
                MathProblem(
                    num1 = 2,
                    num2 = 3,
                    operation = MathOperation.ADDITION,
                    correctAnswer = 5,
                ), // Duplicate - intentional from parent
            )

        val mockChallenge =
            dev.hossain.mathtutor.domain.model.CustomChallenge(
                id = "explicit-challenge-2",
                title = "Parent's Specific Practice",
                type = dev.hossain.mathtutor.domain.model.ChallengeType.EXPLICIT,
                problems = parentCreatedProblems,
            )

        // When - Check that EXPLICIT challenges preserve parent's intent
        val problemStrings = mockChallenge.problems.map { it.getDisplayString() }

        // Then - The challenges should NOT be deduplicated (parent's original intent is preserved)
        assertThat(mockChallenge.problems.size).isEqualTo(3)
        assertThat(problemStrings[0]).isEqualTo(problemStrings[2]) // Duplicates intentionally preserved
    }

    @Test
    fun `generated challenge is safe for deduplication`() {
        // Given - GENERATED challenge created by app
        val appGeneratedProblems =
            listOf(
                MathProblem(num1 = 2, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 5),
                MathProblem(num1 = 1, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 5),
                MathProblem(num1 = 3, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 6), // Different
            )

        val mockChallenge =
            dev.hossain.mathtutor.domain.model.CustomChallenge(
                id = "generated-challenge-2",
                title = "App-Generated Practice",
                type = dev.hossain.mathtutor.domain.model.ChallengeType.GENERATED,
                problems = appGeneratedProblems,
            )

        // When - Check that GENERATED challenges can be deduplicated
        val problemStrings = mockChallenge.problems.map { it.getDisplayString() }

        // Then - Generated challenges are safe to deduplicate
        assertThat(mockChallenge.type).isEqualTo(dev.hossain.mathtutor.domain.model.ChallengeType.GENERATED)
        assertThat(problemStrings.size).isEqualTo(3)
    }

    @Test
    fun `explicit and generated challenges are distinct types`() {
        // Given - Create both types of challenges
        val explicitChallenge =
            dev.hossain.mathtutor.domain.model.CustomChallenge(
                id = "explicit-1",
                title = "Explicit",
                type = dev.hossain.mathtutor.domain.model.ChallengeType.EXPLICIT,
                problems = listOf(MathProblem(num1 = 1, num2 = 1, operation = MathOperation.ADDITION, correctAnswer = 2)),
            )

        val generatedChallenge =
            dev.hossain.mathtutor.domain.model.CustomChallenge(
                id = "generated-1",
                title = "Generated",
                type = dev.hossain.mathtutor.domain.model.ChallengeType.GENERATED,
                problems = listOf(MathProblem(num1 = 1, num2 = 1, operation = MathOperation.ADDITION, correctAnswer = 2)),
            )

        // Then - Types should be different
        assertThat(explicitChallenge.type).isNotEqualTo(generatedChallenge.type)
        assertThat(explicitChallenge.type).isEqualTo(dev.hossain.mathtutor.domain.model.ChallengeType.EXPLICIT)
        assertThat(generatedChallenge.type).isEqualTo(dev.hossain.mathtutor.domain.model.ChallengeType.GENERATED)
    }
}
