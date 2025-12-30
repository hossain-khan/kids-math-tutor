package dev.hossain.mathtutor.domain.model.goals

import android.os.Parcelable
import dev.hossain.mathtutor.domain.model.MathOperation
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Sealed class representing a component of a goal.
 * A goal can have multiple components, each representing a set of practice sessions.
 *
 * Components can be:
 * - OperationBased: Practice a specific math operation (e.g., "Addition 2x" = 2 sessions of addition)
 * - CustomChallengeBased: Practice a custom challenge created by the parent (e.g., "Worksheet 1" 3x)
 */
@Serializable
sealed class GoalComponent : Parcelable {
    /**
     * The number of sessions to complete for this component.
     * Each session typically contains 10 problems.
     */
    abstract val sessionCount: Int

    /**
     * Returns a human-readable description of this component.
     */
    abstract fun getDescription(): String

    /**
     * Operation-based goal component.
     * Child practices a specific math operation for the specified number of sessions.
     *
     * @property operation The math operation to practice (Addition, Subtraction, etc.)
     * @property sessionCount Number of sessions (each session = 10 problems)
     */
    @Parcelize
    @Serializable
    data class OperationBased(
        val operation: MathOperation,
        override val sessionCount: Int,
    ) : GoalComponent() {
        override fun getDescription(): String = "${operation.displayName} ($sessionCount x Sessions)"
    }

    /**
     * Custom challenge-based goal component.
     * Child practices a custom challenge created by the parent.
     *
     * @property challengeId The ID of the custom challenge
     * @property challengeTitle The title of the custom challenge (for display)
     * @property sessionCount Number of times to complete the challenge
     */
    @Parcelize
    @Serializable
    data class CustomChallengeBased(
        val challengeId: String,
        val challengeTitle: String,
        override val sessionCount: Int,
    ) : GoalComponent() {
        override fun getDescription(): String = "$challengeTitle ($sessionCount x Sessions)"
    }
}
