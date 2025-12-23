package dev.hossain.mathtutor.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.UUID

/**
 * Represents a custom challenge created by parents for their children.
 *
 * A custom challenge can be created through two methods:
 * - Generated: Rule-based problem generation
 * - Explicit: Manually specified problems
 *
 * Parents can easily create custom challenges using the Math Pup Worksheet Creator web app:
 * `https://math-worksheet.gohk.xyz/`
 *
 * The web app provides a child-friendly interface to generate challenge JSON that can be
 * imported into the app via QR code or deep link.
 *
 * @property id Unique identifier for the challenge
 * @property title The title of the challenge
 * @property subtitle Optional subtitle or description
 * @property type The type of challenge (GENERATED or EXPLICIT)
 * @property problems List of math problems in this challenge
 * @property createdAt Timestamp when the challenge was created
 * @property isArchived Whether the challenge is archived
 * @property practiceHistory List of practice sessions for this challenge
 */
@Parcelize
data class CustomChallenge(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String? = null,
    val type: ChallengeType,
    val problems: List<MathProblem>,
    val createdAt: Instant = Instant.now(),
    val isArchived: Boolean = false,
    val practiceHistory: List<ChallengePracticeSession> = emptyList(),
) : Parcelable {
    /**
     * Returns the total number of times this challenge has been practiced.
     *
     * @return Count of completed practice sessions
     */
    fun getTotalPracticeSessions(): Int = practiceHistory.count { it.isComplete() }

    /**
     * Returns the average accuracy across all completed practice sessions.
     *
     * @return Average accuracy as a percentage (0-100), or 0 if no sessions
     */
    fun getAverageAccuracy(): Float {
        val completedSessions = practiceHistory.filter { it.isComplete() }
        if (completedSessions.isEmpty()) return 0f
        return completedSessions.map { it.getAccuracy() }.average().toFloat()
    }

    /**
     * Returns the total number of problems in this challenge.
     *
     * @return Count of problems
     */
    fun getProblemCount(): Int = problems.size
}
