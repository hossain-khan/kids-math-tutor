package dev.hossain.mathtutor.domain.work

import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.ui.component.WorkBreakdownStep
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Provides step-by-step work breakdown for math problems.
 *
 * Generates operation-specific solution steps to teach children
 * the process of solving each type of problem.
 */
interface WorkProvider {
    /**
     * Gets the step-by-step solution breakdown for a problem.
     *
     * @param problem The math problem to solve
     * @return List of steps showing how to solve the problem
     */
    fun getWorkBreakdown(problem: MathProblem): List<WorkBreakdownStep>
}

/**
 * Default implementation of WorkProvider with operation-specific breakdowns.
 *
 * Uses memoization to cache work breakdowns, avoiding regeneration for the same problem.
 */
@ContributesBinding(AppScope::class)
@Inject
class DefaultWorkProvider : WorkProvider {
    private val workBreakdownCache = mutableMapOf<String, List<WorkBreakdownStep>>()

    private fun getCacheKey(problem: MathProblem): String = "${problem.operation}_${problem.num1}_${problem.num2}"

    override fun getWorkBreakdown(problem: MathProblem): List<WorkBreakdownStep> {
        val cacheKey = getCacheKey(problem)
        return workBreakdownCache.getOrPut(cacheKey) {
            generateWorkBreakdown(problem)
        }
    }

    private fun generateWorkBreakdown(problem: MathProblem): List<WorkBreakdownStep> =
        when (problem.operation) {
            MathOperation.ADDITION -> getAdditionWork(problem)
            MathOperation.SUBTRACTION -> getSubtractionWork(problem)
            MathOperation.MULTIPLICATION -> getMultiplicationWork(problem)
            MathOperation.DIVISION -> getDivisionWork(problem)
            MathOperation.MIXED -> emptyList()
        }

    private fun getAdditionWork(problem: MathProblem): List<WorkBreakdownStep> {
        val num1 = problem.num1
        val num2 = problem.num2
        val result = num1 + num2

        return listOf(
            WorkBreakdownStep(
                emoji = "🎯",
                description = "Start with $num1",
            ),
            WorkBreakdownStep(
                emoji = "➕",
                description = "Add $num2 more",
            ),
            WorkBreakdownStep(
                emoji = "💡",
                description = "Count on: $num1... ${(num1 + 1)}, ${(num1 + 2)}${if (num2 > 2) ", ..." else ""}",
            ),
            WorkBreakdownStep(
                emoji = "✅",
                description = "We counted $num2 more, so $num1 + $num2 = $result",
            ),
        )
    }

    private fun getSubtractionWork(problem: MathProblem): List<WorkBreakdownStep> {
        val num1 = problem.num1
        val num2 = problem.num2
        val result = num1 - num2

        return listOf(
            WorkBreakdownStep(
                emoji = "🎯",
                description = "Start with $num1",
            ),
            WorkBreakdownStep(
                emoji = "➖",
                description = "Take away $num2",
            ),
            WorkBreakdownStep(
                emoji = "💭",
                description = "Count back: $num1... ${(num1 - 1)}, ${(num1 - 2)}${if (num2 > 2) ", ..." else ""}",
            ),
            WorkBreakdownStep(
                emoji = "✅",
                description = "We counted back $num2, so $num1 - $num2 = $result",
            ),
        )
    }

    private fun getMultiplicationWork(problem: MathProblem): List<WorkBreakdownStep> {
        val num1 = problem.num1
        val num2 = problem.num2
        val result = num1 * num2

        return listOf(
            WorkBreakdownStep(
                emoji = "📦",
                description = "We have $num1 groups",
            ),
            WorkBreakdownStep(
                emoji = "🎁",
                description = "Each group has $num2 items",
            ),
            WorkBreakdownStep(
                emoji = "➕",
                description = "Add $num2 + $num2 + ... ($num1 times)",
            ),
            WorkBreakdownStep(
                emoji = "✅",
                description = "$num1 × $num2 means $num1 groups of $num2 = $result",
            ),
        )
    }

    private fun getDivisionWork(problem: MathProblem): List<WorkBreakdownStep> {
        val num1 = problem.num1
        val num2 = problem.num2
        val result = num1 / num2

        return listOf(
            WorkBreakdownStep(
                emoji = "🎁",
                description = "We have $num1 items to share",
            ),
            WorkBreakdownStep(
                emoji = "👥",
                description = "Share equally among $num2 friends",
            ),
            WorkBreakdownStep(
                emoji = "📊",
                description = "Each friend gets the same amount",
            ),
            WorkBreakdownStep(
                emoji = "✅",
                description = "$num1 ÷ $num2 = $result (each friend gets $result)",
            ),
        )
    }
}
