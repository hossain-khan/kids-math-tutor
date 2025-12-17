package dev.hossain.mathtutor.domain.generator

import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.random.Random

/**
 * Simple implementation of [ProblemGenerator] for Phase 1-2 MVP.
 *
 * Currently supports:
 * - Addition problems with numbers in range 1-10
 * - Subtraction problems with numbers in range 1-10 (no negative results)
 * - Mixed mode (random addition or subtraction)
 *
 * Future phases will expand to support more operations and difficulty levels.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SimpleProblemGenerator constructor() : ProblemGenerator {
    override fun generateProblems(
        count: Int,
        operation: MathOperation,
    ): List<MathProblem> {
        require(count > 0) { "Count must be positive, got: $count" }

        return (1..count).map {
            generateSingleProblem(operation)
        }
    }

    private fun generateSingleProblem(operation: MathOperation): MathProblem =
        when (operation) {
            MathOperation.ADDITION -> generateAddition()

            MathOperation.SUBTRACTION -> generateSubtraction()

            MathOperation.MIXED -> generateMixedProblem()

            else -> throw IllegalArgumentException(
                "Only ADDITION, SUBTRACTION, and MIXED are supported. Requested: $operation",
            )
        }

    /**
     * Generates an addition problem with numbers in range 1-10.
     *
     * @return A math problem with random numbers between 1 and 10
     */
    private fun generateAddition(): MathProblem {
        val num1 = Random.nextInt(1, 11) // 1-10 inclusive
        val num2 = Random.nextInt(1, 11) // 1-10 inclusive
        val answer = num1 + num2

        return MathProblem(
            num1 = num1,
            num2 = num2,
            operation = MathOperation.ADDITION,
            correctAnswer = answer,
        )
    }

    /**
     * Generates a subtraction problem with numbers in range 1-10.
     * Ensures the larger number is always first to prevent negative results.
     *
     * @return A math problem with random numbers between 1 and 10, no negative answers
     */
    private fun generateSubtraction(): MathProblem {
        val num1 = Random.nextInt(1, 11) // 1-10 inclusive
        val num2 = Random.nextInt(1, num1 + 1) // Ensure num2 <= num1 to avoid negatives
        val answer = num1 - num2

        return MathProblem(
            num1 = num1,
            num2 = num2,
            operation = MathOperation.SUBTRACTION,
            correctAnswer = answer,
        )
    }

    /**
     * Generates a mixed problem (randomly chooses between addition or subtraction).
     * Uses a 50/50 split between operations.
     *
     * @return A math problem that is either addition or subtraction
     */
    private fun generateMixedProblem(): MathProblem {
        val operation =
            if (Random.nextBoolean()) {
                MathOperation.ADDITION
            } else {
                MathOperation.SUBTRACTION
            }

        return when (operation) {
            MathOperation.ADDITION -> generateAddition()
            MathOperation.SUBTRACTION -> generateSubtraction()
            else -> throw IllegalStateException("Mixed only supports ADD/SUB")
        }
    }
}
