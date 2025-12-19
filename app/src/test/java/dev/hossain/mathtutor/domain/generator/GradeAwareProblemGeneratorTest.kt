package dev.hossain.mathtutor.domain.generator

import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeAwareProblemGeneratorTest {
    private val generator = GradeAwareProblemGenerator()

    // ==================== Basic Tests ====================

    @Test
    fun `generateProblems returns correct count`() {
        val problems = generator.generateProblems(10, MathOperation.ADDITION, GradeLevel.GRADE_1)
        assertEquals(10, problems.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateProblems throws exception for zero count`() {
        generator.generateProblems(0, MathOperation.ADDITION, GradeLevel.GRADE_1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generateProblems throws exception for negative count`() {
        generator.generateProblems(-5, MathOperation.ADDITION, GradeLevel.GRADE_1)
    }

    @Test
    fun `generated problems have unique IDs`() {
        val problems = generator.generateProblems(20, MathOperation.ADDITION, GradeLevel.GRADE_1)
        val ids = problems.map { it.id }.toSet()
        assertEquals("All problem IDs should be unique", problems.size, ids.size)
    }

    // ==================== Kindergarten Addition Tests ====================

    @Test
    fun `Kindergarten addition uses numbers 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertTrue("num1 should be 1-10, got: ${problem.num1}", problem.num1 in 1..10)
            assertTrue("num2 should be 1-10, got: ${problem.num2}", problem.num2 in 1..10)
        }
    }

    @Test
    fun `Kindergarten addition results are 2-18`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertTrue(
                "Answer should be 2-18, got: ${problem.correctAnswer} from ${problem.num1} + ${problem.num2}",
                problem.correctAnswer in 2..18,
            )
        }
    }

    @Test
    fun `Kindergarten addition problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.ADDITION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 + problem.num2
            assertEquals(
                "Problem ${problem.num1} + ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
        }
    }

    // ==================== Kindergarten Subtraction Tests ====================

    @Test
    fun `Kindergarten subtraction uses numbers 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertTrue("num1 should be 1-10, got: ${problem.num1}", problem.num1 in 1..10)
            assertTrue("num2 should be 1-10, got: ${problem.num2}", problem.num2 in 1..10)
        }
    }

    @Test
    fun `Kindergarten subtraction results are 0-9`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertTrue(
                "Answer should be 0-9, got: ${problem.correctAnswer} from ${problem.num1} - ${problem.num2}",
                problem.correctAnswer in 0..9,
            )
        }
    }

    @Test
    fun `Kindergarten subtraction never produces negative results`() {
        val problems = generator.generateProblems(200, MathOperation.SUBTRACTION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertTrue(
                "Answer should not be negative: ${problem.num1} - ${problem.num2} = ${problem.correctAnswer}",
                problem.correctAnswer >= 0,
            )
            assertTrue(
                "First number should be >= second number: ${problem.num1} >= ${problem.num2}",
                problem.num1 >= problem.num2,
            )
        }
    }

    @Test
    fun `Kindergarten subtraction problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.SUBTRACTION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 - problem.num2
            assertEquals(
                "Problem ${problem.num1} - ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
        }
    }

    // ==================== Kindergarten Multiplication/Division Fallback Tests ====================

    @Test
    fun `Kindergarten multiplication falls back to addition`() {
        val problems = generator.generateProblems(50, MathOperation.MULTIPLICATION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            // Should be addition problems with K ranges
            assertEquals("Should fall back to ADDITION", MathOperation.ADDITION, problem.operation)
            assertTrue("num1 should be 1-10", problem.num1 in 1..10)
            assertTrue("num2 should be 1-10", problem.num2 in 1..10)
            assertEquals("Should have correct addition answer", problem.num1 + problem.num2, problem.correctAnswer)
        }
    }

    @Test
    fun `Kindergarten division falls back to subtraction`() {
        val problems = generator.generateProblems(50, MathOperation.DIVISION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            // Should be subtraction problems with K ranges
            assertEquals("Should fall back to SUBTRACTION", MathOperation.SUBTRACTION, problem.operation)
            assertTrue("num1 should be 1-10", problem.num1 in 1..10)
            assertTrue("num2 should be 1-10", problem.num2 in 1..10)
            assertTrue("Should not produce negative", problem.correctAnswer >= 0)
        }
    }

    // ==================== Grade 1 Addition Tests ====================

    @Test
    fun `Grade 1 addition uses numbers 1-20`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertTrue("num1 should be 1-20, got: ${problem.num1}", problem.num1 in 1..20)
            assertTrue("num2 should be 1-20, got: ${problem.num2}", problem.num2 in 1..20)
        }
    }

    @Test
    fun `Grade 1 addition results are 2-40`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertTrue(
                "Answer should be 2-40, got: ${problem.correctAnswer} from ${problem.num1} + ${problem.num2}",
                problem.correctAnswer in 2..40,
            )
        }
    }

    @Test
    fun `Grade 1 addition problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.ADDITION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 + problem.num2
            assertEquals(
                "Problem ${problem.num1} + ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
        }
    }

    // ==================== Grade 1 Subtraction Tests ====================

    @Test
    fun `Grade 1 subtraction uses numbers 1-20`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertTrue("num1 should be 1-20, got: ${problem.num1}", problem.num1 in 1..20)
            assertTrue("num2 should be 1-20, got: ${problem.num2}", problem.num2 in 1..20)
        }
    }

    @Test
    fun `Grade 1 subtraction results are 0-19`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertTrue(
                "Answer should be 0-19, got: ${problem.correctAnswer} from ${problem.num1} - ${problem.num2}",
                problem.correctAnswer in 0..19,
            )
        }
    }

    @Test
    fun `Grade 1 subtraction never produces negative results`() {
        val problems = generator.generateProblems(200, MathOperation.SUBTRACTION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertTrue(
                "Answer should not be negative: ${problem.num1} - ${problem.num2} = ${problem.correctAnswer}",
                problem.correctAnswer >= 0,
            )
        }
    }

    // ==================== Grade 1 Multiplication Tests ====================

    @Test
    fun `Grade 1 multiplication only uses x2 x5 x10 tables`() {
        val problems = generator.generateProblems(100, MathOperation.MULTIPLICATION, GradeLevel.GRADE_1)

        val allowedMultipliers = setOf(2, 5, 10)
        problems.forEach { problem ->
            assertTrue(
                "Second number should be 2, 5, or 10, got: ${problem.num2}",
                problem.num2 in allowedMultipliers,
            )
        }
    }

    @Test
    fun `Grade 1 multiplication first operand is 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.MULTIPLICATION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertTrue(
                "First operand should be 1-10, got: ${problem.num1}",
                problem.num1 in 1..10,
            )
        }
    }

    @Test
    fun `Grade 1 multiplication problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.MULTIPLICATION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 * problem.num2
            assertEquals(
                "Problem ${problem.num1} × ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
        }
    }

    // ==================== Grade 1 Division Fallback Tests ====================

    @Test
    fun `Grade 1 division falls back to subtraction`() {
        val problems = generator.generateProblems(50, MathOperation.DIVISION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            // Should be subtraction problems with Grade 1 ranges
            assertEquals("Should fall back to SUBTRACTION", MathOperation.SUBTRACTION, problem.operation)
            assertTrue("num1 should be 1-20", problem.num1 in 1..20)
            assertTrue("num2 should be 1-20", problem.num2 in 1..20)
            assertTrue("Should not produce negative", problem.correctAnswer >= 0)
        }
    }

    // ==================== Grade 2 Addition Tests ====================

    @Test
    fun `Grade 2 addition uses numbers 1-100`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertTrue("num1 should be 1-100, got: ${problem.num1}", problem.num1 in 1..100)
            assertTrue("num2 should be 1-100, got: ${problem.num2}", problem.num2 in 1..100)
        }
    }

    @Test
    fun `Grade 2 addition generates variety across full range`() {
        val problems = generator.generateProblems(200, MathOperation.ADDITION, GradeLevel.GRADE_2)

        // Check that we're generating numbers across the full range, not just small numbers
        val num1Values = problems.map { it.num1 }
        val num2Values = problems.map { it.num2 }

        // At least some numbers should be > 50
        assertTrue("Should have some num1 > 50", num1Values.any { it > 50 })
        assertTrue("Should have some num2 > 50", num2Values.any { it > 50 })

        // Should have variety (at least 30 different values out of 200 problems)
        assertTrue("Should have variety in num1", num1Values.toSet().size >= 30)
        assertTrue("Should have variety in num2", num2Values.toSet().size >= 30)
    }

    @Test
    fun `Grade 2 addition problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.ADDITION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 + problem.num2
            assertEquals(
                "Problem ${problem.num1} + ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
        }
    }

    // ==================== Grade 2 Subtraction Tests ====================

    @Test
    fun `Grade 2 subtraction uses numbers 1-100`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertTrue("num1 should be 1-100, got: ${problem.num1}", problem.num1 in 1..100)
            assertTrue("num2 should be 1-100, got: ${problem.num2}", problem.num2 in 1..100)
        }
    }

    @Test
    fun `Grade 2 subtraction never produces negative results`() {
        val problems = generator.generateProblems(200, MathOperation.SUBTRACTION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertTrue(
                "Answer should not be negative: ${problem.num1} - ${problem.num2} = ${problem.correctAnswer}",
                problem.correctAnswer >= 0,
            )
        }
    }

    @Test
    fun `Grade 2 subtraction problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.SUBTRACTION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 - problem.num2
            assertEquals(
                "Problem ${problem.num1} - ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
        }
    }

    // ==================== Grade 2 Multiplication Tests ====================

    @Test
    fun `Grade 2 multiplication uses tables 2-10`() {
        val problems = generator.generateProblems(100, MathOperation.MULTIPLICATION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertTrue(
                "Multiplier should be 2-10, got: ${problem.num2}",
                problem.num2 in 2..10,
            )
        }
    }

    @Test
    fun `Grade 2 multiplication first operand is 1-12`() {
        val problems = generator.generateProblems(100, MathOperation.MULTIPLICATION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertTrue(
                "First operand should be 1-12, got: ${problem.num1}",
                problem.num1 in 1..12,
            )
        }
    }

    @Test
    fun `Grade 2 multiplication problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.MULTIPLICATION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 * problem.num2
            assertEquals(
                "Problem ${problem.num1} × ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
        }
    }

    // ==================== Grade 2 Division Tests ====================

    @Test
    fun `Grade 2 division uses divisors 2-10`() {
        val problems = generator.generateProblems(100, MathOperation.DIVISION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertTrue(
                "Divisor should be 2-10, got: ${problem.num2}",
                problem.num2 in 2..10,
            )
        }
    }

    @Test
    fun `Grade 2 division always divides evenly`() {
        val problems = generator.generateProblems(100, MathOperation.DIVISION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            val remainder = problem.num1 % problem.num2
            assertEquals(
                "Division should have no remainder: ${problem.num1} ÷ ${problem.num2}",
                0,
                remainder,
            )
        }
    }

    @Test
    fun `Grade 2 division problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.DIVISION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 / problem.num2
            assertEquals(
                "Problem ${problem.num1} ÷ ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
            // Also verify the multiplication fact
            assertEquals(
                "Quotient × Divisor should equal Dividend",
                problem.num1,
                problem.correctAnswer * problem.num2,
            )
        }
    }

    // ==================== Mixed Mode Tests ====================

    @Test
    fun `Kindergarten mixed produces only addition and subtraction`() {
        val problems = generator.generateProblems(100, MathOperation.MIXED, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertTrue(
                "Kindergarten mixed should only have ADD or SUB, got: ${problem.operation}",
                problem.operation in listOf(MathOperation.ADDITION, MathOperation.SUBTRACTION),
            )
        }

        // Should have both operations
        val hasAddition = problems.any { it.operation == MathOperation.ADDITION }
        val hasSubtraction = problems.any { it.operation == MathOperation.SUBTRACTION }
        assertTrue("Should have at least one addition problem", hasAddition)
        assertTrue("Should have at least one subtraction problem", hasSubtraction)
    }

    @Test
    fun `Grade 1 mixed produces addition subtraction and multiplication`() {
        val problems = generator.generateProblems(100, MathOperation.MIXED, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertTrue(
                "Grade 1 mixed should only have ADD, SUB, or MUL, got: ${problem.operation}",
                problem.operation in
                    listOf(
                        MathOperation.ADDITION,
                        MathOperation.SUBTRACTION,
                        MathOperation.MULTIPLICATION,
                    ),
            )
        }

        // Should have all three operations
        val hasAddition = problems.any { it.operation == MathOperation.ADDITION }
        val hasSubtraction = problems.any { it.operation == MathOperation.SUBTRACTION }
        val hasMultiplication = problems.any { it.operation == MathOperation.MULTIPLICATION }
        assertTrue("Should have at least one addition problem", hasAddition)
        assertTrue("Should have at least one subtraction problem", hasSubtraction)
        assertTrue("Should have at least one multiplication problem", hasMultiplication)
    }

    @Test
    fun `Grade 2 mixed produces all four operations`() {
        val problems = generator.generateProblems(200, MathOperation.MIXED, GradeLevel.GRADE_2)

        // Should have all four operations
        val hasAddition = problems.any { it.operation == MathOperation.ADDITION }
        val hasSubtraction = problems.any { it.operation == MathOperation.SUBTRACTION }
        val hasMultiplication = problems.any { it.operation == MathOperation.MULTIPLICATION }
        val hasDivision = problems.any { it.operation == MathOperation.DIVISION }

        assertTrue("Should have at least one addition problem", hasAddition)
        assertTrue("Should have at least one subtraction problem", hasSubtraction)
        assertTrue("Should have at least one multiplication problem", hasMultiplication)
        assertTrue("Should have at least one division problem", hasDivision)
    }

    @Test
    fun `Mixed mode problems have correct answers for all operations`() {
        val problems = generator.generateProblems(100, MathOperation.MIXED, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            val expectedAnswer =
                when (problem.operation) {
                    MathOperation.ADDITION -> problem.num1 + problem.num2
                    MathOperation.SUBTRACTION -> problem.num1 - problem.num2
                    MathOperation.MULTIPLICATION -> problem.num1 * problem.num2
                    MathOperation.DIVISION -> problem.num1 / problem.num2
                    MathOperation.MIXED -> throw IllegalStateException("Should not have MIXED operation in results")
                }
            assertEquals(
                "Problem ${problem.num1} ${problem.operation.symbol} ${problem.num2} has incorrect answer",
                expectedAnswer,
                problem.correctAnswer,
            )
        }
    }
}
