package dev.hossain.mathtutor.data.mapper

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionAnswer
import org.junit.Test

class SessionMapperTest {
    @Test
    fun `toEntity converts PracticeSession with all correct answers`() {
        val problems =
            listOf(
                MathProblem(num1 = 1, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 3),
                MathProblem(num1 = 3, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 7),
            )
        val session =
            PracticeSession(
                totalProblems = 2,
                problems = problems,
                answers =
                    mutableMapOf(
                        problems[0].id to SessionAnswer(problemId = problems[0].id, userAnswer = 3, isCorrect = true, attemptCount = 1),
                        problems[1].id to SessionAnswer(problemId = problems[1].id, userAnswer = 7, isCorrect = true, attemptCount = 1),
                    ),
            )

        val entity = SessionMapper.toEntity(session, MathOperation.ADDITION, 120L, 1)

        assertThat(entity.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(entity.totalProblems).isEqualTo(2)
        assertThat(entity.correctAnswers).isEqualTo(2)
        assertThat(entity.incorrectAnswers).isEqualTo(0)
        assertThat(entity.accuracy).isEqualTo(100f)
        assertThat(entity.durationSeconds).isEqualTo(120L)
        assertThat(entity.gradeLevel).isEqualTo(1)
        assertThat(entity.timestamp).isNotNull()
    }

    @Test
    fun `toEntity converts PracticeSession with mixed answers`() {
        val problems =
            listOf(
                MathProblem(num1 = 1, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 3),
                MathProblem(num1 = 3, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 7),
                MathProblem(num1 = 5, num2 = 6, operation = MathOperation.ADDITION, correctAnswer = 11),
            )
        val session =
            PracticeSession(
                totalProblems = 3,
                problems = problems,
                answers =
                    mutableMapOf(
                        problems[0].id to SessionAnswer(problemId = problems[0].id, userAnswer = 3, isCorrect = true, attemptCount = 1),
                        problems[1].id to SessionAnswer(problemId = problems[1].id, userAnswer = 8, isCorrect = false, attemptCount = 2),
                        problems[2].id to SessionAnswer(problemId = problems[2].id, userAnswer = 11, isCorrect = true, attemptCount = 1),
                    ),
            )

        val entity = SessionMapper.toEntity(session, MathOperation.ADDITION, 180L, 2)

        assertThat(entity.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(entity.totalProblems).isEqualTo(3)
        assertThat(entity.correctAnswers).isEqualTo(2)
        assertThat(entity.incorrectAnswers).isEqualTo(1)
        assertThat(entity.accuracy).isWithin(0.001f).of(66.666664f)
        assertThat(entity.durationSeconds).isEqualTo(180L)
        assertThat(entity.gradeLevel).isEqualTo(2)
    }

    @Test
    fun `toEntity converts PracticeSession with all incorrect answers`() {
        val problems =
            listOf(
                MathProblem(num1 = 1, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 3),
                MathProblem(num1 = 3, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 7),
            )
        val session =
            PracticeSession(
                totalProblems = 2,
                problems = problems,
                answers =
                    mutableMapOf(
                        problems[0].id to SessionAnswer(problemId = problems[0].id, userAnswer = 5, isCorrect = false, attemptCount = 2),
                        problems[1].id to SessionAnswer(problemId = problems[1].id, userAnswer = 9, isCorrect = false, attemptCount = 3),
                    ),
            )

        val entity = SessionMapper.toEntity(session, MathOperation.ADDITION, 240L, null)

        assertThat(entity.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(entity.totalProblems).isEqualTo(2)
        assertThat(entity.correctAnswers).isEqualTo(0)
        assertThat(entity.incorrectAnswers).isEqualTo(2)
        assertThat(entity.accuracy).isEqualTo(0f)
        assertThat(entity.durationSeconds).isEqualTo(240L)
        assertThat(entity.gradeLevel).isNull()
    }

    @Test
    fun `toEntity handles different operations`() {
        val problems = listOf(MathProblem(num1 = 5, num2 = 3, operation = MathOperation.SUBTRACTION, correctAnswer = 2))
        val session =
            PracticeSession(
                totalProblems = 1,
                problems = problems,
                answers =
                    mutableMapOf(
                        problems[0].id to SessionAnswer(problemId = problems[0].id, userAnswer = 2, isCorrect = true, attemptCount = 1),
                    ),
            )

        val entity = SessionMapper.toEntity(session, MathOperation.SUBTRACTION, 60L)

        assertThat(entity.operation).isEqualTo(MathOperation.SUBTRACTION)
        assertThat(entity.totalProblems).isEqualTo(1)
        assertThat(entity.correctAnswers).isEqualTo(1)
        assertThat(entity.incorrectAnswers).isEqualTo(0)
    }

    @Test
    fun `toEntity handles large session with 10 problems`() {
        val problems =
            (1..10).map { i ->
                MathProblem(num1 = i, num2 = i + 1, operation = MathOperation.ADDITION, correctAnswer = i + i + 1)
            }
        val answers =
            problems
                .mapIndexed { index, problem ->
                    problem.id to
                        SessionAnswer(
                            problemId = problem.id,
                            userAnswer = problem.correctAnswer,
                            isCorrect = index % 2 == 0,
                            attemptCount = 1,
                        )
                }.toMap()
                .toMutableMap()

        val session =
            PracticeSession(
                totalProblems = 10,
                problems = problems,
                answers = answers,
            )

        val entity = SessionMapper.toEntity(session, MathOperation.ADDITION, 300L, 0)

        assertThat(entity.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(entity.totalProblems).isEqualTo(10)
        assertThat(entity.correctAnswers).isEqualTo(5) // Every other answer is correct
        assertThat(entity.incorrectAnswers).isEqualTo(5)
        assertThat(entity.accuracy).isEqualTo(50f)
        assertThat(entity.durationSeconds).isEqualTo(300L)
        assertThat(entity.gradeLevel).isEqualTo(0) // Kindergarten
    }
}
