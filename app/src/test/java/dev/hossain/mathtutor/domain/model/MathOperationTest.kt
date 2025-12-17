package dev.hossain.mathtutor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MathOperationTest {
    @Test
    fun `calculate addition returns correct sum`() {
        val result = MathOperation.ADDITION.calculate(3, 5)
        assertEquals(8, result)
    }

    @Test
    fun `calculate subtraction returns correct difference`() {
        val result = MathOperation.SUBTRACTION.calculate(10, 4)
        assertEquals(6, result)
    }

    @Test
    fun `calculate multiplication returns correct product`() {
        val result = MathOperation.MULTIPLICATION.calculate(7, 3)
        assertEquals(21, result)
    }

    @Test
    fun `calculate division returns correct quotient`() {
        val result = MathOperation.DIVISION.calculate(20, 5)
        assertEquals(4, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculate division by zero throws exception`() {
        MathOperation.DIVISION.calculate(10, 0)
    }

    @Test
    fun `operation symbols are correct`() {
        assertEquals("+", MathOperation.ADDITION.symbol)
        assertEquals("-", MathOperation.SUBTRACTION.symbol)
        assertEquals("×", MathOperation.MULTIPLICATION.symbol)
        assertEquals("÷", MathOperation.DIVISION.symbol)
        assertEquals("?", MathOperation.MIXED.symbol)
    }

    @Test
    fun `operation display names are correct`() {
        assertEquals("Addition", MathOperation.ADDITION.displayName)
        assertEquals("Subtraction", MathOperation.SUBTRACTION.displayName)
        assertEquals("Multiplication", MathOperation.MULTIPLICATION.displayName)
        assertEquals("Division", MathOperation.DIVISION.displayName)
        assertEquals("Mix It Up", MathOperation.MIXED.displayName)
    }

    @Test(expected = IllegalStateException::class)
    fun `calculate mixed operation throws exception`() {
        MathOperation.MIXED.calculate(5, 3)
    }
}
