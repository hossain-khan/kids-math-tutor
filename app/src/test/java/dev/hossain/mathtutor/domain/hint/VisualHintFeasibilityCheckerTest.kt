package dev.hossain.mathtutor.domain.hint

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import org.junit.Test

/**
 * Unit tests for [VisualHintFeasibilityChecker].
 *
 * Verifies that the feasibility checker correctly identifies which math problems
 * can be reasonably represented with visual dot hints.
 */
class VisualHintFeasibilityCheckerTest {
    // ============== ADDITION TESTS ==============

    @Test
    fun `addition with small numbers is feasible`() {
        val problem = MathProblem(num1 = 3, num2 = 2, operation = MathOperation.ADDITION, correctAnswer = 5)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `addition at maximum threshold is feasible`() {
        val problem = MathProblem(num1 = 20, num2 = 20, operation = MathOperation.ADDITION, correctAnswer = 40)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `addition exceeding threshold is not feasible`() {
        val problem = MathProblem(num1 = 54, num2 = 43, operation = MathOperation.ADDITION, correctAnswer = 97)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isFalse()
    }

    @Test
    fun `addition with one operand exceeding limit is not feasible`() {
        val problem = MathProblem(num1 = 21, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 26)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isFalse()
    }

    @Test
    fun `addition with zero is feasible`() {
        val problem = MathProblem(num1 = 0, num2 = 5, operation = MathOperation.ADDITION, correctAnswer = 5)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    // ============== SUBTRACTION TESTS ==============

    @Test
    fun `subtraction with small numbers is feasible`() {
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.SUBTRACTION, correctAnswer = 2)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `subtraction at maximum minuend is feasible`() {
        val problem = MathProblem(num1 = 20, num2 = 5, operation = MathOperation.SUBTRACTION, correctAnswer = 15)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `subtraction exceeding minuend limit is not feasible`() {
        val problem = MathProblem(num1 = 72, num2 = 8, operation = MathOperation.SUBTRACTION, correctAnswer = 64)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isFalse()
    }

    @Test
    fun `subtraction with large subtrahend but small minuend is feasible`() {
        // Subtrahend doesn't matter, only minuend
        val problem = MathProblem(num1 = 10, num2 = 100, operation = MathOperation.SUBTRACTION, correctAnswer = -90)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    // ============== MULTIPLICATION TESTS ==============

    @Test
    fun `multiplication with small numbers is feasible`() {
        val problem = MathProblem(num1 = 3, num2 = 4, operation = MathOperation.MULTIPLICATION, correctAnswer = 12)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `multiplication at maximum threshold is feasible`() {
        val problem = MathProblem(num1 = 9, num2 = 9, operation = MathOperation.MULTIPLICATION, correctAnswer = 81)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `multiplication 8 times 5 is feasible`() {
        // The reported bug case: should show 8 groups of 5 dots
        val problem = MathProblem(num1 = 8, num2 = 5, operation = MathOperation.MULTIPLICATION, correctAnswer = 40)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `multiplication exceeding limit is not feasible`() {
        val problem = MathProblem(num1 = 14, num2 = 11, operation = MathOperation.MULTIPLICATION, correctAnswer = 154)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isFalse()
    }

    @Test
    fun `multiplication with one operand exceeding limit is not feasible`() {
        val problem = MathProblem(num1 = 10, num2 = 5, operation = MathOperation.MULTIPLICATION, correctAnswer = 50)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isFalse()
    }

    // ============== DIVISION TESTS ==============

    @Test
    fun `division with small numbers is feasible`() {
        val problem = MathProblem(num1 = 12, num2 = 3, operation = MathOperation.DIVISION, correctAnswer = 4)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `division at maximum threshold is feasible`() {
        val problem = MathProblem(num1 = 100, num2 = 10, operation = MathOperation.DIVISION, correctAnswer = 10)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `division 72 divided by 8 is feasible`() {
        // The reported case: 9 groups of 8 dots
        val problem = MathProblem(num1 = 72, num2 = 8, operation = MathOperation.DIVISION, correctAnswer = 9)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `division exceeding dividend limit is not feasible`() {
        val problem = MathProblem(num1 = 101, num2 = 5, operation = MathOperation.DIVISION, correctAnswer = 20)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isFalse()
    }

    @Test
    fun `division exceeding divisor limit is not feasible`() {
        val problem = MathProblem(num1 = 50, num2 = 11, operation = MathOperation.DIVISION, correctAnswer = 4)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isFalse()
    }

    // ============== MIXED OPERATION TESTS ==============

    @Test
    fun `mixed operation is never feasible`() {
        val problem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.MIXED, correctAnswer = 0)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isFalse()
    }

    // ============== EDGE CASES ==============

    @Test
    fun `problem with zero dividend in division`() {
        val problem = MathProblem(num1 = 0, num2 = 5, operation = MathOperation.DIVISION, correctAnswer = 0)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `problem with one as multiplier`() {
        val problem = MathProblem(num1 = 1, num2 = 5, operation = MathOperation.MULTIPLICATION, correctAnswer = 5)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }

    @Test
    fun `addition with identical operands at limit`() {
        val problem = MathProblem(num1 = 20, num2 = 20, operation = MathOperation.ADDITION, correctAnswer = 40)
        assertThat(VisualHintFeasibilityChecker.isFeasible(problem)).isTrue()
    }
}
