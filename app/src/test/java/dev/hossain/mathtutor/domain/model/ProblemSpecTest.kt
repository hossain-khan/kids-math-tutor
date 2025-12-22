package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProblemSpecTest {
    @Test
    fun `creates addition problem spec correctly`() {
        val spec =
            ProblemSpec(
                operand1 = 5,
                operand2 = 3,
                operation = MathOperation.ADDITION,
            )

        assertThat(spec.operand1).isEqualTo(5)
        assertThat(spec.operand2).isEqualTo(3)
        assertThat(spec.operation).isEqualTo(MathOperation.ADDITION)
    }

    @Test
    fun `creates subtraction problem spec correctly`() {
        val spec =
            ProblemSpec(
                operand1 = 10,
                operand2 = 4,
                operation = MathOperation.SUBTRACTION,
            )

        assertThat(spec.operand1).isEqualTo(10)
        assertThat(spec.operand2).isEqualTo(4)
        assertThat(spec.operation).isEqualTo(MathOperation.SUBTRACTION)
    }

    @Test
    fun `creates multiplication problem spec correctly`() {
        val spec =
            ProblemSpec(
                operand1 = 7,
                operand2 = 8,
                operation = MathOperation.MULTIPLICATION,
            )

        assertThat(spec.operand1).isEqualTo(7)
        assertThat(spec.operand2).isEqualTo(8)
        assertThat(spec.operation).isEqualTo(MathOperation.MULTIPLICATION)
    }

    @Test
    fun `creates division problem spec correctly`() {
        val spec =
            ProblemSpec(
                operand1 = 20,
                operand2 = 5,
                operation = MathOperation.DIVISION,
            )

        assertThat(spec.operand1).isEqualTo(20)
        assertThat(spec.operand2).isEqualTo(5)
        assertThat(spec.operation).isEqualTo(MathOperation.DIVISION)
    }

    @Test
    fun `handles negative operands`() {
        val spec =
            ProblemSpec(
                operand1 = -5,
                operand2 = 3,
                operation = MathOperation.ADDITION,
            )

        assertThat(spec.operand1).isEqualTo(-5)
        assertThat(spec.operand2).isEqualTo(3)
    }

    @Test
    fun `handles zero operands`() {
        val spec =
            ProblemSpec(
                operand1 = 0,
                operand2 = 5,
                operation = MathOperation.ADDITION,
            )

        assertThat(spec.operand1).isEqualTo(0)
        assertThat(spec.operand2).isEqualTo(5)
    }
}
