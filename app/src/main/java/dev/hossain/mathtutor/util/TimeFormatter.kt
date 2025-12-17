package dev.hossain.mathtutor.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Utility object for formatting timestamps into human-readable relative time strings.
 */
object TimeFormatter {
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

    /**
     * Formats an [Instant] timestamp into a relative time string.
     *
     * Format examples:
     * - "Today, 2:45 PM"
     * - "Yesterday, 10:30 AM"
     * - "2 days ago"
     * - "Dec 15, 3:20 PM" (for older dates)
     *
     * @param timestamp The instant to format
     * @param now The current time (defaults to Instant.now(), useful for testing)
     * @return Formatted relative time string
     */
    fun formatRelativeTime(
        timestamp: Instant,
        now: Instant = Instant.now(),
    ): String {
        val zoneId = ZoneId.systemDefault()
        val timestampDateTime = LocalDateTime.ofInstant(timestamp, zoneId)
        val nowDateTime = LocalDateTime.ofInstant(now, zoneId)

        val duration = Duration.between(timestamp, now)
        val daysBetween = duration.toDays()

        return when {
            // Today
            timestampDateTime.toLocalDate() == nowDateTime.toLocalDate() -> {
                "Today, ${timestampDateTime.format(timeFormatter)}"
            }

            // Yesterday
            daysBetween == 1L -> {
                "Yesterday, ${timestampDateTime.format(timeFormatter)}"
            }

            // Within last week (2-6 days ago)
            daysBetween in 2..6 -> {
                "$daysBetween days ago"
            }

            // Older - show full date
            else -> {
                timestampDateTime.format(dateTimeFormatter)
            }
        }
    }
}
