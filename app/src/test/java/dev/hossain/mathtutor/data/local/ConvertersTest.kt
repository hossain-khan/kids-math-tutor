package dev.hossain.mathtutor.data.local

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.BadgeCategory
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Test
import java.time.Instant

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun `fromMathOperation converts ADDITION to string`() {
        val result = converters.fromMathOperation(MathOperation.ADDITION)
        assertThat(result).isEqualTo("ADDITION")
    }

    @Test
    fun `fromMathOperation converts SUBTRACTION to string`() {
        val result = converters.fromMathOperation(MathOperation.SUBTRACTION)
        assertThat(result).isEqualTo("SUBTRACTION")
    }

    @Test
    fun `fromMathOperation converts MULTIPLICATION to string`() {
        val result = converters.fromMathOperation(MathOperation.MULTIPLICATION)
        assertThat(result).isEqualTo("MULTIPLICATION")
    }

    @Test
    fun `fromMathOperation converts DIVISION to string`() {
        val result = converters.fromMathOperation(MathOperation.DIVISION)
        assertThat(result).isEqualTo("DIVISION")
    }

    @Test
    fun `toMathOperation converts string to ADDITION`() {
        val result = converters.toMathOperation("ADDITION")
        assertThat(result).isEqualTo(MathOperation.ADDITION)
    }

    @Test
    fun `toMathOperation converts string to SUBTRACTION`() {
        val result = converters.toMathOperation("SUBTRACTION")
        assertThat(result).isEqualTo(MathOperation.SUBTRACTION)
    }

    @Test
    fun `toMathOperation converts string to MULTIPLICATION`() {
        val result = converters.toMathOperation("MULTIPLICATION")
        assertThat(result).isEqualTo(MathOperation.MULTIPLICATION)
    }

    @Test
    fun `toMathOperation converts string to DIVISION`() {
        val result = converters.toMathOperation("DIVISION")
        assertThat(result).isEqualTo(MathOperation.DIVISION)
    }

    @Test
    fun `fromBadgeCategory converts GETTING_STARTED to string`() {
        val result = converters.fromBadgeCategory(BadgeCategory.GETTING_STARTED)
        assertThat(result).isEqualTo("GETTING_STARTED")
    }

    @Test
    fun `fromBadgeCategory converts VOLUME to string`() {
        val result = converters.fromBadgeCategory(BadgeCategory.VOLUME)
        assertThat(result).isEqualTo("VOLUME")
    }

    @Test
    fun `fromBadgeCategory converts OPERATION_MASTERY to string`() {
        val result = converters.fromBadgeCategory(BadgeCategory.OPERATION_MASTERY)
        assertThat(result).isEqualTo("OPERATION_MASTERY")
    }

    @Test
    fun `fromBadgeCategory converts SPEED_ACCURACY to string`() {
        val result = converters.fromBadgeCategory(BadgeCategory.SPEED_ACCURACY)
        assertThat(result).isEqualTo("SPEED_ACCURACY")
    }

    @Test
    fun `fromBadgeCategory converts STREAK to string`() {
        val result = converters.fromBadgeCategory(BadgeCategory.STREAK)
        assertThat(result).isEqualTo("STREAK")
    }

    @Test
    fun `toBadgeCategory converts string to GETTING_STARTED`() {
        val result = converters.toBadgeCategory("GETTING_STARTED")
        assertThat(result).isEqualTo(BadgeCategory.GETTING_STARTED)
    }

    @Test
    fun `toBadgeCategory converts string to VOLUME`() {
        val result = converters.toBadgeCategory("VOLUME")
        assertThat(result).isEqualTo(BadgeCategory.VOLUME)
    }

    @Test
    fun `toBadgeCategory converts string to OPERATION_MASTERY`() {
        val result = converters.toBadgeCategory("OPERATION_MASTERY")
        assertThat(result).isEqualTo(BadgeCategory.OPERATION_MASTERY)
    }

    @Test
    fun `toBadgeCategory converts string to SPEED_ACCURACY`() {
        val result = converters.toBadgeCategory("SPEED_ACCURACY")
        assertThat(result).isEqualTo(BadgeCategory.SPEED_ACCURACY)
    }

    @Test
    fun `toBadgeCategory converts string to STREAK`() {
        val result = converters.toBadgeCategory("STREAK")
        assertThat(result).isEqualTo(BadgeCategory.STREAK)
    }

    @Test
    fun `fromInstant converts Instant to epoch milliseconds`() {
        val instant = Instant.ofEpochMilli(1234567890L)
        val result = converters.fromInstant(instant)
        assertThat(result).isEqualTo(1234567890L)
    }

    @Test
    fun `fromInstant returns null for null input`() {
        val result = converters.fromInstant(null)
        assertThat(result).isNull()
    }

    @Test
    fun `toInstant converts epoch milliseconds to Instant`() {
        val result = converters.toInstant(1234567890L)
        assertThat(result).isEqualTo(Instant.ofEpochMilli(1234567890L))
    }

    @Test
    fun `toInstant returns null for null input`() {
        val result = converters.toInstant(null)
        assertThat(result).isNull()
    }

    @Test
    fun `round trip conversion preserves MathOperation`() {
        val original = MathOperation.ADDITION
        val converted = converters.toMathOperation(converters.fromMathOperation(original))
        assertThat(converted).isEqualTo(original)
    }

    @Test
    fun `round trip conversion preserves BadgeCategory`() {
        val original = BadgeCategory.VOLUME
        val converted = converters.toBadgeCategory(converters.fromBadgeCategory(original))
        assertThat(converted).isEqualTo(original)
    }

    @Test
    fun `round trip conversion preserves Instant`() {
        // Use ofEpochMilli to create an Instant with only millisecond precision
        // to match the precision preserved by the converters
        val original = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val converted = converters.toInstant(converters.fromInstant(original))
        assertThat(converted).isEqualTo(original)
    }
}
