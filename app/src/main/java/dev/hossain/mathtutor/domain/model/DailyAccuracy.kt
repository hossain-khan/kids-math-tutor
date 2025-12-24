package dev.hossain.mathtutor.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * Represents accuracy statistics for a single day of practice.
 *
 * Aggregates all practice sessions completed on a specific date to show
 * daily performance trends.
 *
 * @property date The date for this daily summary
 * @property sessionCount Number of practice sessions completed on this date
 * @property totalProblems Total number of problems attempted across all sessions
 * @property correctAnswers Total number of correct answers across all sessions
 * @property accuracy Overall accuracy percentage for the day (0-100)
 */
@Parcelize
data class DailyAccuracy(
    val date: LocalDate,
    val sessionCount: Int,
    val totalProblems: Int,
    val correctAnswers: Int,
    val accuracy: Float,
) : Parcelable {
    /**
     * Returns the star rating (1-5) based on accuracy.
     *
     * @return Star rating from 1 to 5
     */
    fun getStarRating(): Int =
        when {
            accuracy >= 90 -> 5
            accuracy >= 80 -> 4
            accuracy >= 70 -> 3
            accuracy >= 60 -> 2
            else -> 1
        }
}
