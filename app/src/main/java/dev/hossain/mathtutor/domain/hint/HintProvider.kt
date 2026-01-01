package dev.hossain.mathtutor.domain.hint

import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Provides operation-specific hints to guide children toward solving math problems
 * without directly giving away the answer.
 *
 * Hints are categorized by level:
 * - Level 1: Initial gentle nudge
 * - Level 2: More specific guidance
 */
interface HintProvider {
    /**
     * Gets the first-level hint for a problem.
     * This is a gentle nudge to guide thinking.
     *
     * @param problem The math problem to provide a hint for
     * @return A hint message (max 60 characters for readability)
     */
    fun getFirstHint(problem: MathProblem): String

    /**
     * Gets the second-level hint for a problem.
     * This provides more specific guidance.
     *
     * @param problem The math problem to provide a hint for
     * @return A more specific hint message
     */
    fun getSecondHint(problem: MathProblem): String
}

/**
 * Default implementation of HintProvider with encouragement-based hints.
 * Provides warm, supportive guidance that helps children problem-solve.
 */
@ContributesBinding(AppScope::class)
@Inject
class DefaultHintProvider : HintProvider {
    override fun getFirstHint(problem: MathProblem): String =
        when (problem.operation) {
            MathOperation.ADDITION -> {
                "Try counting up from ${problem.num1}"
            }

            MathOperation.SUBTRACTION -> {
                "How many are left if you take away ${problem.num2}?"
            }

            MathOperation.MULTIPLICATION -> {
                "You have ${problem.num1} groups of ${problem.num2}"
            }

            MathOperation.DIVISION -> {
                "Share ${problem.num1} equally among ${problem.num2}"
            }

            MathOperation.MIXED -> {
                ""
            }
        }

    override fun getSecondHint(problem: MathProblem): String =
        when (problem.operation) {
            MathOperation.ADDITION -> {
                "${problem.num1}... ${problem.num1 + 1}, ${problem.num1 + 2}, count on!"
            }

            MathOperation.SUBTRACTION -> {
                "Start with ${problem.num1}, count back ${problem.num2}"
            }

            MathOperation.MULTIPLICATION -> {
                "Add ${problem.num2} + ${problem.num2}... ${problem.num1} times"
            }

            MathOperation.DIVISION -> {
                "Each gets the same amount. How many each?"
            }

            MathOperation.MIXED -> {
                ""
            }
        }
}
