package dev.hossain.mathtutor.domain.generator

import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleProblemGeneratorTest {
    private val generator = SimpleProblemGenerator()
    private val defaultGrade = GradeLevel.GRADE_1 // Default grade for SimpleProblemGenerator tests

    @Test
    fun `generateProblems returns correct count`() {
        val problems = generator.generateProblems(10, MathOperation.ADDITION, defaultGrade)
        assertEquals(10, problems.size)
    }

    @Test
    fun `generateProblems returns single problem when count is 1`() {
        val problems = generator.generateProblems(1, MathOperation.ADDITION, defaultGrade)
        assertEquals(1, problems.size)
    }

    @Test
    fun `generateProblems with large count works correctly`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, defaultGrade)
        assertEquals(100, problems.size)
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
            assertTrue("num1 should be 1-10, got: ${problem.num1}", problem.num1 in 1..10)
            assertTrue("num2 should be 1-10, got: ${problem.num2}", problem.num2 in 1..10)
        }
    }

    @Test
    fun `generated addition problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.ADDITION, defaultGrade)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 + problem.num2
            assertEquals(
                "Problem ${problem.num1} + ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
        }
    }

    @Test
    fun `generated problems have ADDITION operation`() {
        val problems = generator.generateProblems(20, MathOperation.ADDITION, defaultGrade)

        problems.forEach { problem ->
            assertEquals(MathOperation.ADDITION, problem.operation)
        }
    }

    @Test
    fun `generated problems have unique IDs`() {
        val problems = generator.generateProblems(10, MathOperation.ADDITION, defaultGrade)
        val ids = problems.map { it.id }.toSet()

        assertEquals("All problem IDs should be unique", problems.size, ids.size)
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
        assertTrue("Should generate variety of num1 values", num1Values.size > 3)
        assertTrue("Should generate variety of num2 values", num2Values.size > 3)
    }

    // ==================== Subtraction Tests ====================

    @Test
    fun `generateProblems returns correct count for subtraction`() {
        val problems = generator.generateProblems(10, MathOperation.SUBTRACTION, defaultGrade)
        assertEquals(10, problems.size)
    }

    @Test
    fun `generated subtraction problems have numbers in range 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, defaultGrade)

        problems.forEach { problem ->
            assertTrue("num1 should be 1-10, got: ${problem.num1}", problem.num1 in 1..10)
            assertTrue("num2 should be 1-10, got: ${problem.num2}", problem.num2 in 1..10)
        }
    }

    @Test
    fun `generated subtraction problems never produce negative results`() {
        val problems = generator.generateProblems(200, MathOperation.SUBTRACTION, defaultGrade)

        problems.forEach { problem ->
            assertTrue(
                "Answer should not be negative: ${problem.num1} - ${problem.num2} = ${problem.correctAnswer}",
                problem.correctAnswer >= 0,
            )
        }
    }

    @Test
    fun `generated subtraction problems have larger number first`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, defaultGrade)

        problems.forEach { problem ->
            assertTrue(
                "First number should be >= second number: ${problem.num1} >= ${problem.num2}",
                problem.num1 >= problem.num2,
            )
        }
    }

    @Test
    fun `generated subtraction problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.SUBTRACTION, defaultGrade)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 - problem.num2
            assertEquals(
                "Problem ${problem.num1} - ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
        }
    }

    @Test
    fun `generated subtraction problems have SUBTRACTION operation`() {
        val problems = generator.generateProblems(20, MathOperation.SUBTRACTION, defaultGrade)

        problems.forEach { problem ->
            assertEquals(MathOperation.SUBTRACTION, problem.operation)
        }
    }

    @Test
    fun `subtraction problems produce variety of numbers`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, defaultGrade)
        val num1Values = problems.map { it.num1 }.toSet()
        val num2Values = problems.map { it.num2 }.toSet()

        // With 100 problems, we should see multiple different values
        assertTrue("Should generate variety of num1 values", num1Values.size > 3)
        assertTrue("Should generate variety of num2 values", num2Values.size > 3)
    }

    // ==================== Mixed Mode Tests ====================

    @Test
    fun `generateProblems returns correct count for mixed mode`() {
        val problems = generator.generateProblems(10, MathOperation.MIXED, defaultGrade)
        assertEquals(10, problems.size)
    }

    @Test
    fun `mixed mode produces both addition and subtraction problems`() {
        val problems = generator.generateProblems(100, MathOperation.MIXED, defaultGrade)

        val additionCount = problems.count { it.operation == MathOperation.ADDITION }
        val subtractionCount = problems.count { it.operation == MathOperation.SUBTRACTION }

        assertTrue("Should have at least one addition problem, got: $additionCount", additionCount > 0)
        assertTrue("Should have at least one subtraction problem, got: $subtractionCount", subtractionCount > 0)
        assertEquals("All problems should be counted", 100, additionCount + subtractionCount)
    }

    @Test
    fun `mixed mode problems have numbers in range 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.MIXED, defaultGrade)

        problems.forEach { problem ->
            assertTrue("num1 should be 1-10, got: ${problem.num1}", problem.num1 in 1..10)
            assertTrue("num2 should be 1-10, got: ${problem.num2}", problem.num2 in 1..10)
        }
    }

    @Test
    fun `mixed mode never produces negative results`() {
        val problems = generator.generateProblems(200, MathOperation.MIXED, defaultGrade)

        problems.forEach { problem ->
            assertTrue(
                "Answer should not be negative: ${problem.num1} ${problem.operation.symbol} ${problem.num2} = ${problem.correctAnswer}",
                problem.correctAnswer >= 0,
            )
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
            assertEquals(
                "Problem ${problem.num1} ${problem.operation.symbol} ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
        }
    }

    @Test
    fun `mixed mode distribution is roughly 50-50`() {
        val problems = generator.generateProblems(200, MathOperation.MIXED, defaultGrade)

        val additionCount = problems.count { it.operation == MathOperation.ADDITION }
        val subtractionCount = problems.count { it.operation == MathOperation.SUBTRACTION }

        // With 200 problems and random 50/50, we expect roughly 100 of each
        // Allow a reasonable margin: 30-70% range (60-140 out of 200)
        assertTrue(
            "Addition count should be between 60-140, got: $additionCount",
            additionCount in 60..140,
        )
        assertTrue(
            "Subtraction count should be between 60-140, got: $subtractionCount",
            subtractionCount in 60..140,
        )
    }
}
