package dev.hossain.mathtutor.domain.model

/**
 * Represents the type of custom challenge creation method.
 *
 * - [GENERATED]: Rule-based generation of problems using operation, count, and number range
 * - [EXPLICIT]: Manually specified problems provided as a list
 */
enum class ChallengeType {
    GENERATED,
    EXPLICIT,
}
