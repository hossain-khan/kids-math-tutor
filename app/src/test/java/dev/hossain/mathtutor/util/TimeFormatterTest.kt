package dev.hossain.mathtutor.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for [TimeFormatter].
 *
 * Tests relative timestamp formatting for various time ranges.
 */
class TimeFormatterTest {
    @Test
    fun formatRelativeTime_today_returnsCorrectFormat() {
        // Given
        val now = Instant.parse("2025-12-17T14:30:00Z")
        val timestamp = Instant.parse("2025-12-17T10:45:00Z")

        // When
        val result = TimeFormatter.formatRelativeTime(timestamp, now)

        // Then - Format should be "Today, HH:MM AM/PM"
        assert(result.startsWith("Today,"))
    }

    @Test
    fun formatRelativeTime_yesterday_returnsCorrectFormat() {
        // Given
        val now = Instant.parse("2025-12-17T14:30:00Z")
        val timestamp = Instant.parse("2025-12-16T10:45:00Z")

        // When
        val result = TimeFormatter.formatRelativeTime(timestamp, now)

        // Then
        assert(result.startsWith("Yesterday,"))
    }

    @Test
    fun formatRelativeTime_twoDaysAgo_returnsCorrectFormat() {
        // Given
        val now = Instant.parse("2025-12-17T14:30:00Z")
        val timestamp = now.minus(2, ChronoUnit.DAYS)

        // When
        val result = TimeFormatter.formatRelativeTime(timestamp, now)

        // Then
        assertThat(result).isEqualTo("2 days ago")
    }

    @Test
    fun formatRelativeTime_threeDaysAgo_returnsCorrectFormat() {
        // Given
        val now = Instant.parse("2025-12-17T14:30:00Z")
        val timestamp = now.minus(3, ChronoUnit.DAYS)

        // When
        val result = TimeFormatter.formatRelativeTime(timestamp, now)

        // Then
        assertThat(result).isEqualTo("3 days ago")
    }

    @Test
    fun formatRelativeTime_sixDaysAgo_returnsCorrectFormat() {
        // Given
        val now = Instant.parse("2025-12-17T14:30:00Z")
        val timestamp = now.minus(6, ChronoUnit.DAYS)

        // When
        val result = TimeFormatter.formatRelativeTime(timestamp, now)

        // Then
        assertThat(result).isEqualTo("6 days ago")
    }

    @Test
    fun formatRelativeTime_oneWeekAgo_returnsFullDate() {
        // Given
        val now = Instant.parse("2025-12-17T14:30:00Z")
        val timestamp = now.minus(7, ChronoUnit.DAYS)

        // When
        val result = TimeFormatter.formatRelativeTime(timestamp, now)

        // Then - Should return full date format like "Dec 10, 2:30 PM"
        assert(result.contains("Dec"))
        assert(result.contains(","))
    }

    @Test
    fun formatRelativeTime_sameInstant_returnsToday() {
        // Given
        val now = Instant.parse("2025-12-17T14:30:00Z")
        val timestamp = now

        // When
        val result = TimeFormatter.formatRelativeTime(timestamp, now)

        // Then
        assert(result.startsWith("Today,"))
    }
}
