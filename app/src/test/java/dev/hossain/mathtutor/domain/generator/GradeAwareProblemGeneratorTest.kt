package dev.hossain.mathtutor.domain.generator

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Test

class GradeAwareProblemGeneratorTest {
    private val generator = GradeAwareProblemGenerator()

    // ==================== Basic Tests ====================

    @Test
    fun `generateProblems returns correct count`() {
        val problems = generator.generateProblems(10, MathOperation.ADDITION, GradeLevel.GRADE_1)
        assertThat(problems.size).isEqualTo(10)
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
        assertThat(problems.size, ids.size).isEqualTo("All problem IDs should be unique")
    }

    // ==================== Kindergarten Addition Tests ====================

    @Test
    fun `Kindergarten addition uses numbers 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertThat("num1 should be 1-10, got: ${problem.num1}", problem.num1 in 1..10).isTrue()
            assertThat("num2 should be 1-10, got: ${problem.num2}", problem.num2 in 1..10).isTrue()
        }
    }

    @Test
    fun `Kindergarten addition results are 2-18`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertThat(
                "Answer should be 2-18, got: ${problem.correctAnswer} from ${problem.num1} + ${problem.num2}",
                problem.correctAnswer in 2..18,
            ).isTrue()
        }
    }

    @Test
    fun `Kindergarten addition problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.ADDITION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 + problem.num2
            assertThat(
                expectedAnswer,
                problem.correctAnswer,
            ).isEqualTo("Problem ${problem.num1} + ${problem.num2} has incorrect answer")
        }
    }

    // ==================== Kindergarten Subtraction Tests ====================

    @Test
    fun `Kindergarten subtraction uses numbers 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertThat("num1 should be 1-10, got: ${problem.num1}", problem.num1 in 1..10).isTrue()
            assertThat("num2 should be 1-10, got: ${problem.num2}", problem.num2 in 1..10).isTrue()
        }
    }

    @Test
    fun `Kindergarten subtraction results are 0-9`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertThat(
                "Answer should be 0-9, got: ${problem.correctAnswer} from ${problem.num1} - ${problem.num2}",
                problem.correctAnswer in 0..9,
            ).isTrue()
        }
    }

    @Test
    fun `Kindergarten subtraction never produces negative results`() {
        val problems = generator.generateProblems(200, MathOperation.SUBTRACTION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertThat(
                "Answer should not be negative: ${problem.num1} - ${problem.num2} = ${problem.correctAnswer}",
                problem.correctAnswer >= 0,
            ).isTrue()
            assertThat(
                "First number should be >= second number: ${problem.num1} >= ${problem.num2}",
                problem.num1 >= problem.num2,
            ).isTrue()
        }
    }

    @Test
    fun `Kindergarten subtraction problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.SUBTRACTION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 - problem.num2
            assertThat(
                expectedAnswer,
                problem.correctAnswer,
            ).isEqualTo("Problem ${problem.num1} - ${problem.num2} has incorrect answer")
        }
    }

    // ==================== Kindergarten Multiplication/Division Fallback Tests ====================

    @Test
    fun `Kindergarten multiplication falls back to addition`() {
        val problems = generator.generateProblems(50, MathOperation.MULTIPLICATION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            // Should be addition problems with K ranges
            assertThat(MathOperation.ADDITION, problem.operation).isEqualTo("Should fall back to ADDITION")
            assertThat("num1 should be 1-10", problem.num1 in 1..10).isTrue()
            assertThat("num2 should be 1-10", problem.num2 in 1..10).isTrue()
            assertThat(problem.num1 + problem.num2, problem.correctAnswer).isEqualTo("Should have correct addition answer")
        }
    }

    @Test
    fun `Kindergarten division falls back to subtraction`() {
        val problems = generator.generateProblems(50, MathOperation.DIVISION, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            // Should be subtraction problems with K ranges
            assertThat(MathOperation.SUBTRACTION, problem.operation).isEqualTo("Should fall back to SUBTRACTION")
            assertThat("num1 should be 1-10", problem.num1 in 1..10).isTrue()
            assertThat("num2 should be 1-10", problem.num2 in 1..10).isTrue()
            assertThat("Should not produce negative", problem.correctAnswer >= 0).isTrue()
        }
    }

    // ==================== Grade 1 Addition Tests ====================

    @Test
    fun `Grade 1 addition uses numbers 1-20`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertThat("num1 should be 1-20, got: ${problem.num1}", problem.num1 in 1..20).isTrue()
            assertThat("num2 should be 1-20, got: ${problem.num2}", problem.num2 in 1..20).isTrue()
        }
    }

    @Test
    fun `Grade 1 addition results are 2-40`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertThat(
                "Answer should be 2-40, got: ${problem.correctAnswer} from ${problem.num1} + ${problem.num2}",
                problem.correctAnswer in 2..40,
            ).isTrue()
        }
    }

    @Test
    fun `Grade 1 addition problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.ADDITION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 + problem.num2
            assertThat(
                expectedAnswer,
                problem.correctAnswer,
            ).isEqualTo("Problem ${problem.num1} + ${problem.num2} has incorrect answer")
        }
    }

    // ==================== Grade 1 Subtraction Tests ====================

    @Test
    fun `Grade 1 subtraction uses numbers 1-20`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertThat("num1 should be 1-20, got: ${problem.num1}", problem.num1 in 1..20).isTrue()
            assertThat("num2 should be 1-20, got: ${problem.num2}", problem.num2 in 1..20).isTrue()
        }
    }

    @Test
    fun `Grade 1 subtraction results are 0-19`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertThat(
                "Answer should be 0-19, got: ${problem.correctAnswer} from ${problem.num1} - ${problem.num2}",
                problem.correctAnswer in 0..19,
            ).isTrue()
        }
    }

    @Test
    fun `Grade 1 subtraction never produces negative results`() {
        val problems = generator.generateProblems(200, MathOperation.SUBTRACTION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertThat(
                "Answer should not be negative: ${problem.num1} - ${problem.num2} = ${problem.correctAnswer}",
                problem.correctAnswer >= 0,
            ).isTrue()
        }
    }

    // ==================== Grade 1 Multiplication Tests ====================

    @Test
    fun `Grade 1 multiplication only uses x2 x5 x10 tables`() {
        val problems = generator.generateProblems(100, MathOperation.MULTIPLICATION, GradeLevel.GRADE_1)

        val allowedMultipliers = setOf(2, 5, 10)
        problems.forEach { problem ->
            assertThat(
                "Second number should be 2, 5, or 10, got: ${problem.num2}",
                problem.num2 in allowedMultipliers,
            ).isTrue()
        }
    }

    @Test
    fun `Grade 1 multiplication first operand is 1-10`() {
        val problems = generator.generateProblems(100, MathOperation.MULTIPLICATION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertThat(
                "First operand should be 1-10, got: ${problem.num1}",
                problem.num1 in 1..10,
            ).isTrue()
        }
    }

    @Test
    fun `Grade 1 multiplication problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.MULTIPLICATION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 * problem.num2
            assertThat(
                expectedAnswer,
                problem.correctAnswer,
            ).isEqualTo("Problem ${problem.num1} × ${problem.num2} has incorrect answer")
        }
    }

    // ==================== Grade 1 Division Fallback Tests ====================

    @Test
    fun `Grade 1 division falls back to subtraction`() {
        val problems = generator.generateProblems(50, MathOperation.DIVISION, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            // Should be subtraction problems with Grade 1 ranges
            assertThat(MathOperation.SUBTRACTION, problem.operation).isEqualTo("Should fall back to SUBTRACTION")
            assertThat("num1 should be 1-20", problem.num1 in 1..20).isTrue()
            assertThat("num2 should be 1-20", problem.num2 in 1..20).isTrue()
            assertThat("Should not produce negative", problem.correctAnswer >= 0).isTrue()
        }
    }

    // ==================== Grade 2 Addition Tests ====================

    @Test
    fun `Grade 2 addition uses numbers 1-100`() {
        val problems = generator.generateProblems(100, MathOperation.ADDITION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertThat("num1 should be 1-100, got: ${problem.num1}", problem.num1 in 1..100).isTrue()
            assertThat("num2 should be 1-100, got: ${problem.num2}", problem.num2 in 1..100).isTrue()
        }
    }

    @Test
    fun `Grade 2 addition generates variety across full range`() {
        val problems = generator.generateProblems(200, MathOperation.ADDITION, GradeLevel.GRADE_2)

        // Check that we're generating numbers across the full range, not just small numbers
        val num1Values = problems.map { it.num1 }
        val num2Values = problems.map { it.num2 }

        // At least some numbers should be > 50
        assertThat("Should have some num1 > 50", num1Values.any { it > 50 }).isTrue()
        assertThat("Should have some num2 > 50", num2Values.any { it > 50 }).isTrue()

        // Should have variety (at least 30 different values out of 200 problems)
        assertThat("Should have variety in num1", num1Values.toSet().isTrue().size >= 30)
        assertThat("Should have variety in num2", num2Values.toSet().isTrue().size >= 30)
    }

    @Test
    fun `Grade 2 addition problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.ADDITION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 + problem.num2
            assertThat(
                expectedAnswer,
                problem.correctAnswer,
            ).isEqualTo("Problem ${problem.num1} + ${problem.num2} has incorrect answer")
        }
    }

    // ==================== Grade 2 Subtraction Tests ====================

    @Test
    fun `Grade 2 subtraction uses numbers 1-100`() {
        val problems = generator.generateProblems(100, MathOperation.SUBTRACTION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertThat("num1 should be 1-100, got: ${problem.num1}", problem.num1 in 1..100).isTrue()
            assertThat("num2 should be 1-100, got: ${problem.num2}", problem.num2 in 1..100).isTrue()
        }
    }

    @Test
    fun `Grade 2 subtraction never produces negative results`() {
        val problems = generator.generateProblems(200, MathOperation.SUBTRACTION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertThat(
                "Answer should not be negative: ${problem.num1} - ${problem.num2} = ${problem.correctAnswer}",
                problem.correctAnswer >= 0,
            ).isTrue()
        }
    }

    @Test
    fun `Grade 2 subtraction problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.SUBTRACTION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 - problem.num2
            assertThat(
                expectedAnswer,
                problem.correctAnswer,
            ).isEqualTo("Problem ${problem.num1} - ${problem.num2} has incorrect answer")
        }
    }

    // ==================== Grade 2 Multiplication Tests ====================

    @Test
    fun `Grade 2 multiplication uses tables 2-10`() {
        val problems = generator.generateProblems(100, MathOperation.MULTIPLICATION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertThat(
                "Multiplier should be 2-10, got: ${problem.num2}",
                problem.num2 in 2..10,
            ).isTrue()
        }
    }

    @Test
    fun `Grade 2 multiplication first operand is 1-12`() {
        val problems = generator.generateProblems(100, MathOperation.MULTIPLICATION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertThat(
                "First operand should be 1-12, got: ${problem.num1}",
                problem.num1 in 1..12,
            ).isTrue()
        }
    }

    @Test
    fun `Grade 2 multiplication problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.MULTIPLICATION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 * problem.num2
            assertThat(
                expectedAnswer,
                problem.correctAnswer,
            ).isEqualTo("Problem ${problem.num1} × ${problem.num2} has incorrect answer")
        }
    }

    // ==================== Grade 2 Division Tests ====================

    @Test
    fun `Grade 2 division uses divisors 2-10`() {
        val problems = generator.generateProblems(100, MathOperation.DIVISION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            assertThat(
                "Divisor should be 2-10, got: ${problem.num2}",
                problem.num2 in 2..10,
            ).isTrue()
        }
    }

    @Test
    fun `Grade 2 division always divides evenly`() {
        val problems = generator.generateProblems(100, MathOperation.DIVISION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            val remainder = problem.num1 % problem.num2
            assertThat(
                0,
                remainder,
            ).isEqualTo("Division should have no remainder: ${problem.num1} ÷ ${problem.num2}")
        }
    }

    @Test
    fun `Grade 2 division problems have correct answers`() {
        val problems = generator.generateProblems(50, MathOperation.DIVISION, GradeLevel.GRADE_2)

        problems.forEach { problem ->
            val expectedAnswer = problem.num1 / problem.num2
            assertThat(
                expectedAnswer,
                problem.correctAnswer,
            ).isEqualTo("Problem ${problem.num1} ÷ ${problem.num2} has incorrect answer")
            // Also verify the multiplication fact
            assertThat(
                problem.num1,
                problem.correctAnswer * problem.num2,
            ).isEqualTo("Quotient × Divisor should equal Dividend")
        }
    }

    // ==================== Mixed Mode Tests ====================

    @Test
    fun `Kindergarten mixed produces only addition and subtraction`() {
        val problems = generator.generateProblems(100, MathOperation.MIXED, GradeLevel.KINDERGARTEN)

        problems.forEach { problem ->
            assertThat(
                "Kindergarten mixed should only have ADD or SUB, got: ${problem.operation}",
                problem.operation in listOf(MathOperation.ADDITION, MathOperation.SUBTRACTION).isTrue(),
            )
        }

        // Should have both operations
        val hasAddition = problems.any { it.operation == MathOperation.ADDITION }
        val hasSubtraction = problems.any { it.operation == MathOperation.SUBTRACTION }
        assertThat("Should have at least one addition problem", hasAddition).isTrue()
        assertThat("Should have at least one subtraction problem", hasSubtraction).isTrue()
    }

    @Test
    fun `Grade 1 mixed produces addition subtraction and multiplication`() {
        val problems = generator.generateProblems(100, MathOperation.MIXED, GradeLevel.GRADE_1)

        problems.forEach { problem ->
            assertThat(
                "Grade 1 mixed should only have ADD, SUB, or MUL, got: ${problem.operation}",
                problem.operation in
                    listOf(
                        MathOperation.ADDITION,
                        MathOperation.SUBTRACTION,
                        MathOperation.MULTIPLICATION,
                    ).isTrue(),
            )
        }

        // Should have all three operations
        val hasAddition = problems.any { it.operation == MathOperation.ADDITION }
        val hasSubtraction = problems.any { it.operation == MathOperation.SUBTRACTION }
        val hasMultiplication = problems.any { it.operation == MathOperation.MULTIPLICATION }
        assertThat("Should have at least one addition problem", hasAddition).isTrue()
        assertThat("Should have at least one subtraction problem", hasSubtraction).isTrue()
        assertThat("Should have at least one multiplication problem", hasMultiplication).isTrue()
    }

    @Test
    fun `Grade 2 mixed produces all four operations`() {
        val problems = generator.generateProblems(200, MathOperation.MIXED, GradeLevel.GRADE_2)

        // Should have all four operations
        val hasAddition = problems.any { it.operation == MathOperation.ADDITION }
        val hasSubtraction = problems.any { it.operation == MathOperation.SUBTRACTION }
        val hasMultiplication = problems.any { it.operation == MathOperation.MULTIPLICATION }
        val hasDivision = problems.any { it.operation == MathOperation.DIVISION }

        assertThat("Should have at least one addition problem", hasAddition).isTrue()
        assertThat("Should have at least one subtraction problem", hasSubtraction).isTrue()
        assertThat("Should have at least one multiplication problem", hasMultiplication).isTrue()
        assertThat("Should have at least one division problem", hasDivision).isTrue()
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
            assertThat(
                expectedAnswer,
                problem.correctAnswer,
            ).isEqualTo("Problem ${problem.num1} ${problem.operation.symbol} ${problem.num2} has incorrect answer")
        }
    }
}
