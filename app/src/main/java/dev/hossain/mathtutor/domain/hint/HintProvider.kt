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
 *
 * Uses memoization to cache hints, avoiding regeneration for the same problem.
 */
@ContributesBinding(AppScope::class)
@Inject
class DefaultHintProvider : HintProvider {
    private val firstHintCache = mutableMapOf<String, String>()
    private val secondHintCache = mutableMapOf<String, String>()

    private fun getCacheKey(problem: MathProblem): String = "${problem.operation}_${problem.num1}_${problem.num2}"

    override fun getFirstHint(problem: MathProblem): String {
        val cacheKey = getCacheKey(problem)
        return firstHintCache.getOrPut(cacheKey) {
            generateFirstHint(problem)
        }
    }

    private fun generateFirstHint(problem: MathProblem): String =
        when (problem.operation) {
            MathOperation.ADDITION -> {
                "You're doing great! Try counting up from ${problem.num1} 🎯"
            }

            MathOperation.SUBTRACTION -> {
                "Great question! Start with ${problem.num1}, then take away ${problem.num2} 💭"
            }

            MathOperation.MULTIPLICATION -> {
                "Nice try! You have ${problem.num1} groups with ${problem.num2} in each 📦"
            }

            MathOperation.DIVISION -> {
                "You're thinking right! Share ${problem.num1} equally with ${problem.num2} friends 🎁"
            }

            MathOperation.MIXED -> {
                "You've got this!"
            }
        }

    override fun getSecondHint(problem: MathProblem): String {
        val cacheKey = getCacheKey(problem)
        return secondHintCache.getOrPut(cacheKey) {
            generateSecondHint(problem)
        }
    }

    private fun generateSecondHint(problem: MathProblem): String =
        when (problem.operation) {
            MathOperation.ADDITION -> {
                "Start at ${problem.num1}, then: ${problem.num1 + 1}, ${problem.num1 + 2}... keep counting! 💡"
            }

            MathOperation.SUBTRACTION -> {
                "Start with ${problem.num1}, then take away ${problem.num2}. Count what's left! 💭"
            }

            MathOperation.MULTIPLICATION -> {
                "Add this ${problem.num1} times: ${problem.num2} + ${problem.num2} + ... 🔢"
            }

            MathOperation.DIVISION -> {
                "Give each friend 1 at a time until they all have the same amount 📊"
            }

            MathOperation.MIXED -> {
                "You can do it!"
            }
        }
}
