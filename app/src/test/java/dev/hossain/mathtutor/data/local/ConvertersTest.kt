package dev.hossain.mathtutor.data.local

import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun `fromMathOperation converts ADDITION to string`() {
        val result = converters.fromMathOperation(MathOperation.ADDITION)
        assertEquals("ADDITION", result)
    }

    @Test
    fun `fromMathOperation converts SUBTRACTION to string`() {
        val result = converters.fromMathOperation(MathOperation.SUBTRACTION)
        assertEquals("SUBTRACTION", result)
    }

    @Test
    fun `fromMathOperation converts MULTIPLICATION to string`() {
        val result = converters.fromMathOperation(MathOperation.MULTIPLICATION)
        assertEquals("MULTIPLICATION", result)
    }

    @Test
    fun `fromMathOperation converts DIVISION to string`() {
        val result = converters.fromMathOperation(MathOperation.DIVISION)
        assertEquals("DIVISION", result)
    }

    @Test
    fun `toMathOperation converts string to ADDITION`() {
        val result = converters.toMathOperation("ADDITION")
        assertEquals(MathOperation.ADDITION, result)
    }

    @Test
    fun `toMathOperation converts string to SUBTRACTION`() {
        val result = converters.toMathOperation("SUBTRACTION")
        assertEquals(MathOperation.SUBTRACTION, result)
    }

    @Test
    fun `toMathOperation converts string to MULTIPLICATION`() {
        val result = converters.toMathOperation("MULTIPLICATION")
        assertEquals(MathOperation.MULTIPLICATION, result)
    }

    @Test
    fun `toMathOperation converts string to DIVISION`() {
        val result = converters.toMathOperation("DIVISION")
        assertEquals(MathOperation.DIVISION, result)
    }

    @Test
    fun `fromInstant converts Instant to epoch milliseconds`() {
        val instant = Instant.ofEpochMilli(1234567890L)
        val result = converters.fromInstant(instant)
        assertEquals(1234567890L, result)
    }

    @Test
    fun `fromInstant returns null for null input`() {
        val result = converters.fromInstant(null)
        assertNull(result)
    }

    @Test
    fun `toInstant converts epoch milliseconds to Instant`() {
        val result = converters.toInstant(1234567890L)
        assertEquals(Instant.ofEpochMilli(1234567890L), result)
    }

    @Test
    fun `toInstant returns null for null input`() {
        val result = converters.toInstant(null)
        assertNull(result)
    }

    @Test
    fun `round trip conversion preserves MathOperation`() {
        val original = MathOperation.ADDITION
        val converted = converters.toMathOperation(converters.fromMathOperation(original))
        assertEquals(original, converted)
    }

    @Test
    fun `round trip conversion preserves Instant`() {
        val original = Instant.now()
        val converted = converters.toInstant(converters.fromInstant(original))
        assertEquals(original, converted)
    }
}
