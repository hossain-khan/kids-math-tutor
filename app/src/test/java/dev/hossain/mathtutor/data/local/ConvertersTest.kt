package dev.hossain.mathtutor.data.local

import dev.hossain.mathtutor.domain.model.BadgeCategory
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
    fun `fromBadgeCategory converts GETTING_STARTED to string`() {
        val result = converters.fromBadgeCategory(BadgeCategory.GETTING_STARTED)
        assertEquals("GETTING_STARTED", result)
    }

    @Test
    fun `fromBadgeCategory converts VOLUME to string`() {
        val result = converters.fromBadgeCategory(BadgeCategory.VOLUME)
        assertEquals("VOLUME", result)
    }

    @Test
    fun `fromBadgeCategory converts OPERATION_MASTERY to string`() {
        val result = converters.fromBadgeCategory(BadgeCategory.OPERATION_MASTERY)
        assertEquals("OPERATION_MASTERY", result)
    }

    @Test
    fun `fromBadgeCategory converts SPEED_ACCURACY to string`() {
        val result = converters.fromBadgeCategory(BadgeCategory.SPEED_ACCURACY)
        assertEquals("SPEED_ACCURACY", result)
    }

    @Test
    fun `fromBadgeCategory converts STREAK to string`() {
        val result = converters.fromBadgeCategory(BadgeCategory.STREAK)
        assertEquals("STREAK", result)
    }

    @Test
    fun `toBadgeCategory converts string to GETTING_STARTED`() {
        val result = converters.toBadgeCategory("GETTING_STARTED")
        assertEquals(BadgeCategory.GETTING_STARTED, result)
    }

    @Test
    fun `toBadgeCategory converts string to VOLUME`() {
        val result = converters.toBadgeCategory("VOLUME")
        assertEquals(BadgeCategory.VOLUME, result)
    }

    @Test
    fun `toBadgeCategory converts string to OPERATION_MASTERY`() {
        val result = converters.toBadgeCategory("OPERATION_MASTERY")
        assertEquals(BadgeCategory.OPERATION_MASTERY, result)
    }

    @Test
    fun `toBadgeCategory converts string to SPEED_ACCURACY`() {
        val result = converters.toBadgeCategory("SPEED_ACCURACY")
        assertEquals(BadgeCategory.SPEED_ACCURACY, result)
    }

    @Test
    fun `toBadgeCategory converts string to STREAK`() {
        val result = converters.toBadgeCategory("STREAK")
        assertEquals(BadgeCategory.STREAK, result)
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
    fun `round trip conversion preserves BadgeCategory`() {
        val original = BadgeCategory.VOLUME
        val converted = converters.toBadgeCategory(converters.fromBadgeCategory(original))
        assertEquals(original, converted)
    }

    @Test
    fun `round trip conversion preserves Instant`() {
        // Use ofEpochMilli to create an Instant with only millisecond precision
        // to match the precision preserved by the converters
        val original = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val converted = converters.toInstant(converters.fromInstant(original))
        assertEquals(original, converted)
    }
}
