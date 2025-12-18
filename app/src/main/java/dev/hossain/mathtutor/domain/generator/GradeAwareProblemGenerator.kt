package dev.hossain.mathtutor.domain.generator

import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import timber.log.Timber
import kotlin.random.Random

/**
 * Grade-aware implementation of [ProblemGenerator] that generates problems
 * appropriate for each grade level (K, 1, 2).
 *
 * Grade specifications:
 * - **Kindergarten**: Numbers 1-10, addition/subtraction only
 *   - Addition: 1-10 + 1-10, results 2-18
 *   - Subtraction: 1-10 - 1-10, results 0-9, no negatives
 * - **Grade 1**: Numbers 1-20, limited multiplication
 *   - Addition: 1-20 + 1-20, results 2-40
 *   - Subtraction: 1-20 - 1-20, results 0-19
 *   - Multiplication: Only ×2, ×5, ×10 tables (operand 1-10)
 * - **Grade 2**: Numbers 1-100, all operations
 *   - Addition: 1-100 + 1-100
 *   - Subtraction: 1-100 - 1-100
 *   - Multiplication: Tables 2-10 (operand 1-12)
 *   - Division: Derived from multiplication facts (always divides evenly)
 */
class GradeAwareProblemGenerator : ProblemGenerator {
    override fun generateProblems(
        count: Int,
        operation: MathOperation,
        gradeLevel: GradeLevel,
    ): List<MathProblem> {
        require(count > 0) { "Count must be positive, got: $count" }

        return (1..count).map {
            generateSingleProblem(operation, gradeLevel)
        }
    }

    private fun generateSingleProblem(
        operation: MathOperation,
        gradeLevel: GradeLevel,
    ): MathProblem =
        when (operation) {
            MathOperation.ADDITION -> generateAddition(gradeLevel)
            MathOperation.SUBTRACTION -> generateSubtraction(gradeLevel)
            MathOperation.MULTIPLICATION -> generateMultiplication(gradeLevel)
            MathOperation.DIVISION -> generateDivision(gradeLevel)
            MathOperation.MIXED -> generateMixed(gradeLevel)
        }

    /**
     * Generates an addition problem appropriate for the grade level.
     *
     * - K: 1-10 + 1-10 = 2-18 (ensures result doesn't exceed 18)
     * - Grade 1: 1-20 + 1-20 = 2-40
     * - Grade 2: 1-100 + 1-100
     */
    private fun generateAddition(gradeLevel: GradeLevel): MathProblem {
        val (min, max) =
            when (gradeLevel) {
                GradeLevel.KINDERGARTEN -> 1 to 10
                GradeLevel.GRADE_1 -> 1 to 20
                GradeLevel.GRADE_2 -> 1 to 100
            }

        // For Kindergarten, constrain the sum to not exceed 18
        val num1 = Random.nextInt(min, max + 1)
        val num2 =
            if (gradeLevel == GradeLevel.KINDERGARTEN) {
                // Ensure num1 + num2 <= 18
                val maxNum2 = minOf(max, 18 - num1)
                Random.nextInt(min, maxNum2 + 1)
            } else {
                Random.nextInt(min, max + 1)
            }

        val answer = num1 + num2

        return MathProblem(
            num1 = num1,
            num2 = num2,
            operation = MathOperation.ADDITION,
            correctAnswer = answer,
        )
    }

    /**
     * Generates a subtraction problem appropriate for the grade level.
     * Always ensures num1 >= num2 to avoid negative results.
     *
     * - K: 1-10 - 1-10 = 0-9
     * - Grade 1: 1-20 - 1-20 = 0-19
     * - Grade 2: 1-100 - 1-100
     */
    private fun generateSubtraction(gradeLevel: GradeLevel): MathProblem {
        val (min, max) =
            when (gradeLevel) {
                GradeLevel.KINDERGARTEN -> 1 to 10
                GradeLevel.GRADE_1 -> 1 to 20
                GradeLevel.GRADE_2 -> 1 to 100
            }

        val num1 = Random.nextInt(min, max + 1)
        // Ensure num2 <= num1 to prevent negative results
        val num2 = Random.nextInt(min, num1 + 1)
        val answer = num1 - num2

        return MathProblem(
            num1 = num1,
            num2 = num2,
            operation = MathOperation.SUBTRACTION,
            correctAnswer = answer,
        )
    }

    /**
     * Generates a multiplication problem appropriate for the grade level.
     *
     * - K: Not available, falls back to addition
     * - Grade 1: Only ×2, ×5, ×10 tables (first operand 1-10)
     * - Grade 2: Tables 2-10 (first operand 1-12)
     */
    private fun generateMultiplication(gradeLevel: GradeLevel): MathProblem =
        when (gradeLevel) {
            GradeLevel.KINDERGARTEN -> {
                Timber.d("Multiplication not available for Kindergarten, using addition")
                generateAddition(gradeLevel)
            }

            GradeLevel.GRADE_1 -> {
                // Only ×2, ×5, ×10 tables
                val allowedMultipliers = listOf(2, 5, 10)
                val multiplier = allowedMultipliers.random()
                val num1 = Random.nextInt(1, 11) // 1-10
                val answer = num1 * multiplier

                MathProblem(
                    num1 = num1,
                    num2 = multiplier,
                    operation = MathOperation.MULTIPLICATION,
                    correctAnswer = answer,
                )
            }

            GradeLevel.GRADE_2 -> {
                // Full multiplication tables 2-10
                val multiplier = Random.nextInt(2, 11) // 2-10
                val num1 = Random.nextInt(1, 13) // 1-12
                val answer = num1 * multiplier

                MathProblem(
                    num1 = num1,
                    num2 = multiplier,
                    operation = MathOperation.MULTIPLICATION,
                    correctAnswer = answer,
                )
            }
        }

    /**
     * Generates a division problem appropriate for the grade level.
     * Division is created from multiplication facts to ensure even division.
     *
     * - K: Not available, falls back to subtraction
     * - Grade 1: Not available, falls back to subtraction
     * - Grade 2: Derived from multiplication facts (2-10 tables)
     */
    private fun generateDivision(gradeLevel: GradeLevel): MathProblem =
        when (gradeLevel) {
            GradeLevel.KINDERGARTEN, GradeLevel.GRADE_1 -> {
                Timber.d("Division not available for ${gradeLevel.displayName}, using subtraction")
                generateSubtraction(gradeLevel)
            }

            GradeLevel.GRADE_2 -> {
                // Create division from multiplication facts
                val divisor = Random.nextInt(2, 11) // 2-10
                val quotient = Random.nextInt(1, 13) // 1-12
                val dividend = divisor * quotient

                MathProblem(
                    num1 = dividend,
                    num2 = divisor,
                    operation = MathOperation.DIVISION,
                    correctAnswer = quotient,
                )
            }
        }

    /**
     * Generates a mixed problem (randomly chooses between available operations for the grade).
     *
     * - K: Addition or Subtraction (50/50)
     * - Grade 1: Addition, Subtraction, or Multiplication (33/33/33)
     * - Grade 2: All four operations (25/25/25/25)
     */
    private fun generateMixed(gradeLevel: GradeLevel): MathProblem {
        val availableOperations =
            when (gradeLevel) {
                GradeLevel.KINDERGARTEN -> {
                    listOf(
                        MathOperation.ADDITION,
                        MathOperation.SUBTRACTION,
                    )
                }

                GradeLevel.GRADE_1 -> {
                    listOf(
                        MathOperation.ADDITION,
                        MathOperation.SUBTRACTION,
                        MathOperation.MULTIPLICATION,
                    )
                }

                GradeLevel.GRADE_2 -> {
                    listOf(
                        MathOperation.ADDITION,
                        MathOperation.SUBTRACTION,
                        MathOperation.MULTIPLICATION,
                        MathOperation.DIVISION,
                    )
                }
            }

        val operation = availableOperations.random()
        return generateSingleProblem(operation, gradeLevel)
    }
}
