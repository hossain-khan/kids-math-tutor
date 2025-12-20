package dev.hossain.mathtutor.domain.generator

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Test

class SimpleProblemGeneratorTest {
    private val generator = SimpleProblemGenerator()
    private val defaultGrade = GradeLevel.GRADE_1 // Default grade for SimpleProblemGenerator tests

    @Test
    fun `generateProblems returns correct count`() {
        val problems = generator.generateProblems(10, MathOperation.ADDITION, defaultGrade)
        assertThat(problems.size).isEqualTo(10)
    }

    @Test
    fun `generateProblems returns single problem when count is 1`() {
        val problems = generator.generateProblems(1, MathOperation.ADDITION, defaultGrade)
        assertThat(problems.size).isEqualTo(1)
    }

    @Test
    fun `generateProblems with large count works correctly`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, defaultGrade)
        assertThat(problems.size).isEqualTo(100)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateProblems throws exception for zero count`() {
        generator.generateProblems(0, MathOperation.ADDITION, defaultGrade)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateProblems throws exception for negative count`() {
        generator.generateProblems(-5, MathOperation.ADDITION, defaultGrade)
    }

    @Test
    fun `generated addition problems have numbers in range 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, defaultGrade)

        problems.forEach { problem ->
            assertThat(problem.num1 in 1..10).isTrue()
            assertThat(problem.num2 in 1..10).isTrue()
        }
    }

    @Test
    fun `generated addition problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.ADDITION, defaultGrade)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 + problem.num2
            assertThat(expectedAnswer).isEqualTo(problem.correctAnswer)
        }
    }

    @Test
    fun `generated problems have ADDITION operation`() {
        val problems = generator.generateProblems(20, MathOperation.ADDITION, defaultGrade)

        problems.forEach { problem ->
            assertThat(problem.operation).isEqualTo(MathOperation.ADDITION)
        }
    }

    @Test
    fun `generated problems have unique IDs`() {
        val problems = generator.generateProblems(10, MathOperation.ADDITION, defaultGrade)
        val ids = problems.map { it.id }.toSet()

        assertThat(problems.size).isEqualTo(ids.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateProblems throws exception for MULTIPLICATION in Phase 1`() {
        generator.generateProblems(5, MathOperation.MULTIPLICATION, defaultGrade)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateProblems throws exception for DIVISION in Phase 1`() {
        generator.generateProblems(5, MathOperation.DIVISION, defaultGrade)
    }

    @Test
    fun `generated problems produce variety of numbers`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, defaultGrade)
        val num1Values = problems.map { it.num1 }.toSet()
        val num2Values = problems.map { it.num2 }.toSet()

        // With 100 problems, we should see multiple different values
        // (not a guarantee due to randomness, but very likely)
        assertThat(num1Values.size > 3).isTrue()
        assertThat(num2Values.size > 3).isTrue()
    }

    // ==================== Subtraction Tests ====================

    @Test
    fun `generateProblems returns correct count for subtraction`() {
        val problems = generator.generateProblems(10, MathOperation.SUBTRACTION, defaultGrade)
        assertThat(problems.size).isEqualTo(10)
    }

    @Test
    fun `generated subtraction problems have numbers in range 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, defaultGrade)

        problems.forEach { problem ->
            assertThat(problem.num1 in 1..10).isTrue()
            assertThat(problem.num2 in 1..10).isTrue()
        }
    }

    @Test
    fun `generated subtraction problems never produce negative results`() {
        val problems = generator.generateProblems(200, MathOperation.SUBTRACTION, defaultGrade)

        problems.forEach { problem ->
            assertThat(
                problem.correctAnswer >= 0,
            ).isTrue()
        }
    }

    @Test
    fun `generated subtraction problems have larger number first`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, defaultGrade)

        problems.forEach { problem ->
            assertThat(
                problem.num1 >= problem.num2,
            ).isTrue()
        }
    }

    @Test
    fun `generated subtraction problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.SUBTRACTION, defaultGrade)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 - problem.num2
            assertThat(expectedAnswer).isEqualTo(problem.correctAnswer)
        }
    }

    @Test
    fun `generated subtraction problems have SUBTRACTION operation`() {
        val problems = generator.generateProblems(20, MathOperation.SUBTRACTION, defaultGrade)

        problems.forEach { problem ->
            assertThat(problem.operation).isEqualTo(MathOperation.SUBTRACTION)
        }
    }

    @Test
    fun `subtraction problems produce variety of numbers`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, defaultGrade)
        val num1Values = problems.map { it.num1 }.toSet()
        val num2Values = problems.map { it.num2 }.toSet()

        // With 100 problems, we should see multiple different values
        assertThat(num1Values.size > 3).isTrue()
        assertThat(num2Values.size > 3).isTrue()
    }

    // ==================== Mixed Mode Tests ====================

    @Test
    fun `generateProblems returns correct count for mixed mode`() {
        val problems = generator.generateProblems(10, MathOperation.MIXED, defaultGrade)
        assertThat(problems.size).isEqualTo(10)
    }

    @Test
    fun `mixed mode produces both addition and subtraction problems`() {
        val problems = generator.generateProblems(100, MathOperation.MIXED, defaultGrade)

        val additionCount = problems.count { it.operation == MathOperation.ADDITION }
        val subtractionCount = problems.count { it.operation == MathOperation.SUBTRACTION }

        assertThat(additionCount > 0).isTrue()
        assertThat(subtractionCount > 0).isTrue()
        assertThat(100).isEqualTo(additionCount + subtractionCount)
    }

    @Test
    fun `mixed mode problems have numbers in range 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.MIXED, defaultGrade)

        problems.forEach { problem ->
            assertThat(problem.num1 in 1..10).isTrue()
            assertThat(problem.num2 in 1..10).isTrue()
        }
    }

    @Test
    fun `mixed mode never produces negative results`() {
        val problems = generator.generateProblems(200, MathOperation.MIXED, defaultGrade)

        problems.forEach { problem ->
            assertThat(
                problem.correctAnswer >= 0,
            ).isTrue()
        }
    }

    @Test
    fun `mixed mode problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.MIXED, defaultGrade)

        problems.forEach { problem ->
            val expectedAnswer =
                when (problem.operation) {
                    MathOperation.ADDITION -> problem.num1 + problem.num2
                    MathOperation.SUBTRACTION -> problem.num1 - problem.num2
                    else -> throw IllegalStateException("Unexpected operation: ${problem.operation}")
                }
            assertThat(expectedAnswer).isEqualTo(problem.correctAnswer)
        }
    }

    @Test
    fun `mixed mode distribution is roughly 50-50`() {
        val problems = generator.generateProblems(200, MathOperation.MIXED, defaultGrade)

        val additionCount = problems.count { it.operation == MathOperation.ADDITION }
        val subtractionCount = problems.count { it.operation == MathOperation.SUBTRACTION }

        // With 200 problems and random 50/50, we expect roughly 100 of each
        // Allow a reasonable margin: 30-70% range (60-140 out of 200)
        assertThat(
            additionCount in 60..140,
        ).isTrue()
        assertThat(
            subtractionCount in 60..140,
        ).isTrue()
    }
}
