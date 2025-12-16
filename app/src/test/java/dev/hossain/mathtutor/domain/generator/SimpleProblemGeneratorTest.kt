package dev.hossain.mathtutor.domain.generator

import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleProblemGeneratorTest {
    private val generator = SimpleProblemGenerator()

    @Test
    fun `generateProblems returns correct count`() {
        val problems = generator.generateProblems(10, MathOperation.ADDITION)
        assertEquals(10, problems.size)
    }

    @Test
    fun `generateProblems returns single problem when count is 1`() {
        val problems = generator.generateProblems(1, MathOperation.ADDITION)
        assertEquals(1, problems.size)
    }

    @Test
    fun `generateProblems with large count works correctly`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION)
        assertEquals(100, problems.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateProblems throws exception for zero count`() {
        generator.generateProblems(0, MathOperation.ADDITION)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateProblems throws exception for negative count`() {
        generator.generateProblems(-5, MathOperation.ADDITION)
    }

    @Test
    fun `generated addition problems have numbers in range 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION)

        problems.forEach { problem ->
            assertTrue("num1 should be 1-10, got: ${problem.num1}", problem.num1 in 1..10)
            assertTrue("num2 should be 1-10, got: ${problem.num2}", problem.num2 in 1..10)
        }
    }

    @Test
    fun `generated addition problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.ADDITION)

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
        val problems = generator.generateProblems(20, MathOperation.ADDITION)

        problems.forEach { problem ->
            assertEquals(MathOperation.ADDITION, problem.operation)
        }
    }

    @Test
    fun `generated problems have unique IDs`() {
        val problems = generator.generateProblems(10, MathOperation.ADDITION)
        val ids = problems.map { it.id }.toSet()

        assertEquals("All problem IDs should be unique", problems.size, ids.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateProblems throws exception for SUBTRACTION in Phase 1`() {
        generator.generateProblems(5, MathOperation.SUBTRACTION)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateProblems throws exception for MULTIPLICATION in Phase 1`() {
        generator.generateProblems(5, MathOperation.MULTIPLICATION)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateProblems throws exception for DIVISION in Phase 1`() {
        generator.generateProblems(5, MathOperation.DIVISION)
    }

    @Test
    fun `generated problems produce variety of numbers`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION)
        val num1Values = problems.map { it.num1 }.toSet()
        val num2Values = problems.map { it.num2 }.toSet()

        // With 100 problems, we should see multiple different values
        // (not a guarantee due to randomness, but very likely)
        assertTrue("Should generate variety of num1 values", num1Values.size > 3)
        assertTrue("Should generate variety of num2 values", num2Values.size > 3)
    }
}
