package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MathOperationTest {
    @Test
    fun `calculate addition returns correct sum`() {
        val result = MathOperation.ADDITION.calculate(3, 5)
        assertThat(result).isEqualTo(8)
    }

    @Test
    fun `calculate subtraction returns correct difference`() {
        val result = MathOperation.SUBTRACTION.calculate(10, 4)
        assertThat(result).isEqualTo(6)
    }

    @Test
    fun `calculate multiplication returns correct product`() {
        val result = MathOperation.MULTIPLICATION.calculate(7, 3)
        assertThat(result).isEqualTo(21)
    }

    @Test
    fun `calculate division returns correct quotient`() {
        val result = MathOperation.DIVISION.calculate(20, 5)
        assertThat(result).isEqualTo(4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculate division by zero throws exception`() {
        MathOperation.DIVISION.calculate(10, 0)
    }

    @Test
    fun `operation symbols are correct`() {
        assertThat(MathOperation.ADDITION.symbol).isEqualTo("+")
        assertThat(MathOperation.SUBTRACTION.symbol).isEqualTo("-")
        assertThat(MathOperation.MULTIPLICATION.symbol).isEqualTo("×")
        assertThat(MathOperation.DIVISION.symbol).isEqualTo("÷")
        assertThat(MathOperation.MIXED.symbol).isEqualTo("?")
    }

    @Test
    fun `operation display names are correct`() {
        assertThat(MathOperation.ADDITION.displayName).isEqualTo("Addition")
        assertThat(MathOperation.SUBTRACTION.displayName).isEqualTo("Subtraction")
        assertThat(MathOperation.MULTIPLICATION.displayName).isEqualTo("Multiplication")
        assertThat(MathOperation.DIVISION.displayName).isEqualTo("Division")
        assertThat(MathOperation.MIXED.displayName).isEqualTo("Mix It Up")
    }

    @Test(expected = IllegalStateException::class)
    fun `calculate mixed operation throws exception`() {
        MathOperation.MIXED.calculate(5, 3)
    }
}
