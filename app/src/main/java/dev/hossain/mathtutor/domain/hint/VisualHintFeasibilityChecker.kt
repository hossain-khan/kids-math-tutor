package dev.hossain.mathtutor.domain.hint

import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem

/**
 * Determines if a math problem is suitable for visual hint representation.
 *
 * **Rationale:**
 * Visual hints using dot visualization work well for simple, small numbers
 * that K-2 children can easily comprehend. However, for larger numbers,
 * displaying too many dots becomes impractical and confusing:
 *
 * - **54 + 43**: Showing 97 individual dots is overwhelming
 * - **14 × 11**: Creating 14 groups of 11 dots (154 total) is impractical
 * - **72 ÷ 8**: Showing 9 groups of 8 dots is okay but approaching limits
 *
 * This checker ensures the "Show Visually" option only appears when visual
 * hints will be educationally useful and not cognitively overwhelming.
 *
 * **Thresholds (Based on Grade 1-2 Standards):**
 *
 * | Operation | Constraint | Reasoning |
 * |-----------|-----------|-----------|
 * | Addition | Both operands ≤ 20 | Sum ≤ 40, manageable dot count |
 * | Subtraction | Minuend ≤ 20 | Minuend determines dot count |
 * | Multiplication | Both operands ≤ 9 | Max 9×9 = 81 dots across groups |
 * | Division | Dividend ≤ 100, Divisor ≤ 10 | Max 10 groups of ~10 dots |
 *
 * Children using visual hints benefit most when:
 * ✓ They can count dots without overwhelming visual complexity
 * ✓ The representation stays on one screen without excessive scrolling
 * ✓ Each group is visually distinct and manageable
 * ✓ Animation completes in reasonable time (800ms base + stagger)
 */
object VisualHintFeasibilityChecker {
    /**
     * Maximum value for operands in addition (affects total dot count).
     * Addition shows two dot groups side-by-side with animation.
     */
    private const val MAX_ADDITION_OPERAND = 20

    /**
     * Maximum value for the minuend in subtraction.
     * Subtraction shows dots that fade, so count is primarily minuend.
     */
    private const val MAX_SUBTRACTION_MINUEND = 20

    /**
     * Maximum multiplier and multiplicand.
     * Limits grid to reasonable size (e.g., 9×9, not 14×11).
     * Higher limits would create too many groups/dots for portrait mode.
     */
    private const val MAX_MULTIPLICATION_OPERAND = 9

    /**
     * Maximum dividend for division.
     * Controls total dot count across groups.
     */
    private const val MAX_DIVISION_DIVIDEND = 100

    /**
     * Maximum divisor for division.
     * Controls number of groups displayed.
     */
    private const val MAX_DIVISION_DIVISOR = 10

    /**
     * Checks if a problem is suitable for visual hint display.
     *
     * Returns `false` for problems that would create:
     * - More than ~40-50 dots total (Addition/Subtraction)
     * - More than 9 groups (Multiplication/Division)
     * - Visual representations that don't fit on screen
     *
     * @param problem The math problem to check
     * @return `true` if visual hint is feasible, `false` otherwise
     *
     * **Examples:**
     * - `5 + 3` → true (8 dots total)
     * - `20 + 20` → true (40 dots, at limit but manageable)
     * - `54 + 43` → false (97 dots, too many)
     * - `8 × 5` → true (8 groups of 5 dots)
     * - `14 × 11` → false (14 groups of 11, exceeds limits)
     * - `72 ÷ 8` → true (9 groups of 8 dots)
     * - `100 ÷ 10` → true (10 groups of 10 dots, at limit)
     */
    fun isFeasible(problem: MathProblem): Boolean =
        when (problem.operation) {
            MathOperation.ADDITION -> {
                problem.num1 <= MAX_ADDITION_OPERAND && problem.num2 <= MAX_ADDITION_OPERAND
            }

            MathOperation.SUBTRACTION -> {
                problem.num1 <= MAX_SUBTRACTION_MINUEND
            }

            MathOperation.MULTIPLICATION -> {
                problem.num1 <= MAX_MULTIPLICATION_OPERAND &&
                    problem.num2 <= MAX_MULTIPLICATION_OPERAND
            }

            MathOperation.DIVISION -> {
                problem.num1 <= MAX_DIVISION_DIVIDEND && problem.num2 <= MAX_DIVISION_DIVISOR
            }

            MathOperation.MIXED -> {
                false
            } // Mixed operations too complex for visual hints
        }
}
