package dev.hossain.mathtutor.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NumberRangeTest {
    @Test
    fun `creates valid range with min less than max`() {
        val range = NumberRange(min = 1, max = 10)
        assertThat(range.min).isEqualTo(1)
        assertThat(range.max).isEqualTo(10)
    }

    @Test
    fun `creates valid range with min equal to max`() {
        val range = NumberRange(min = 5, max = 5)
        assertThat(range.min).isEqualTo(5)
        assertThat(range.max).isEqualTo(5)
    }

    @Test
    fun `throws exception when min is greater than max`() {
        val exception =
            runCatching {
                NumberRange(min = 10, max = 5)
            }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception?.message).contains("Minimum value")
        assertThat(exception?.message).contains("must be less than or equal to maximum value")
    }

    @Test
    fun `handles negative numbers correctly`() {
        val range = NumberRange(min = -10, max = 10)
        assertThat(range.min).isEqualTo(-10)
        assertThat(range.max).isEqualTo(10)
    }

    @Test
    fun `handles large numbers correctly`() {
        val range = NumberRange(min = 1, max = 999)
        assertThat(range.min).isEqualTo(1)
        assertThat(range.max).isEqualTo(999)
    }
}
