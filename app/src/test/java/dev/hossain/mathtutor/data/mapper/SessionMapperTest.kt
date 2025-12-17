package dev.hossain.mathtutor.data.mapper

import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionAnswer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SessionMapperTest {
    @Test
    fun `toEntity converts PracticeSession with all correct answers`() {
        val problems =
            listOf(
                MathProblem(1, 2, MathOperation.ADDITION, 3),
                MathProblem(3, 4, MathOperation.ADDITION, 7),
            )
        val session =
            PracticeSession(
                totalProblems = 2,
                problems = problems,
                answers =
                    mutableMapOf(
                        problems[0].id to SessionAnswer(3, true, 1),
                        problems[1].id to SessionAnswer(7, true, 1),
                    ),
            )

        val entity = SessionMapper.toEntity(session, MathOperation.ADDITION, 120L, 1)

        assertEquals(MathOperation.ADDITION, entity.operation)
        assertEquals(2, entity.totalProblems)
        assertEquals(2, entity.correctAnswers)
        assertEquals(0, entity.incorrectAnswers)
        assertEquals(100f, entity.accuracy)
        assertEquals(120L, entity.durationSeconds)
        assertEquals(1, entity.gradeLevel)
        assertNotNull(entity.timestamp)
    }

    @Test
    fun `toEntity converts PracticeSession with mixed answers`() {
        val problems =
            listOf(
                MathProblem(1, 2, MathOperation.ADDITION, 3),
                MathProblem(3, 4, MathOperation.ADDITION, 7),
                MathProblem(5, 6, MathOperation.ADDITION, 11),
            )
        val session =
            PracticeSession(
                totalProblems = 3,
                problems = problems,
                answers =
                    mutableMapOf(
                        problems[0].id to SessionAnswer(3, true, 1),
                        problems[1].id to SessionAnswer(8, false, 2),
                        problems[2].id to SessionAnswer(11, true, 1),
                    ),
            )

        val entity = SessionMapper.toEntity(session, MathOperation.ADDITION, 180L, 2)

        assertEquals(MathOperation.ADDITION, entity.operation)
        assertEquals(3, entity.totalProblems)
        assertEquals(2, entity.correctAnswers)
        assertEquals(1, entity.incorrectAnswers)
        assertEquals(66.666664f, entity.accuracy, 0.001f)
        assertEquals(180L, entity.durationSeconds)
        assertEquals(2, entity.gradeLevel)
    }

    @Test
    fun `toEntity converts PracticeSession with all incorrect answers`() {
        val problems =
            listOf(
                MathProblem(1, 2, MathOperation.ADDITION, 3),
                MathProblem(3, 4, MathOperation.ADDITION, 7),
            )
        val session =
            PracticeSession(
                totalProblems = 2,
                problems = problems,
                answers =
                    mutableMapOf(
                        problems[0].id to SessionAnswer(5, false, 2),
                        problems[1].id to SessionAnswer(9, false, 3),
                    ),
            )

        val entity = SessionMapper.toEntity(session, MathOperation.ADDITION, 240L, null)

        assertEquals(MathOperation.ADDITION, entity.operation)
        assertEquals(2, entity.totalProblems)
        assertEquals(0, entity.correctAnswers)
        assertEquals(2, entity.incorrectAnswers)
        assertEquals(0f, entity.accuracy)
        assertEquals(240L, entity.durationSeconds)
        assertNull(entity.gradeLevel)
    }

    @Test
    fun `toEntity handles different operations`() {
        val problems = listOf(MathProblem(5, 3, MathOperation.SUBTRACTION, 2))
        val session =
            PracticeSession(
                totalProblems = 1,
                problems = problems,
                answers = mutableMapOf(problems[0].id to SessionAnswer(2, true, 1)),
            )

        val entity = SessionMapper.toEntity(session, MathOperation.SUBTRACTION, 60L)

        assertEquals(MathOperation.SUBTRACTION, entity.operation)
        assertEquals(1, entity.totalProblems)
        assertEquals(1, entity.correctAnswers)
        assertEquals(0, entity.incorrectAnswers)
    }

    @Test
    fun `toEntity handles large session with 10 problems`() {
        val problems =
            (1..10).map { i ->
                MathProblem(i, i + 1, MathOperation.ADDITION, i + i + 1)
            }
        val answers =
            problems
                .mapIndexed { index, problem ->
                    problem.id to SessionAnswer(problem.correctAnswer, index % 2 == 0, 1)
                }.toMap()
                .toMutableMap()

        val session =
            PracticeSession(
                totalProblems = 10,
                problems = problems,
                answers = answers,
            )

        val entity = SessionMapper.toEntity(session, MathOperation.ADDITION, 300L, 0)

        assertEquals(MathOperation.ADDITION, entity.operation)
        assertEquals(10, entity.totalProblems)
        assertEquals(5, entity.correctAnswers) // Every other answer is correct
        assertEquals(5, entity.incorrectAnswers)
        assertEquals(50f, entity.accuracy)
        assertEquals(300L, entity.durationSeconds)
        assertEquals(0, entity.gradeLevel) // Kindergarten
    }
}
