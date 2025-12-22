package dev.hossain.mathtutor.domain.model

/**
 * Sealed class representing the specification for importing a custom challenge.
 *
 * This class defines two ways to create custom challenges:
 * - [Generated]: Rule-based generation using operation, count, and number range
 * - [Explicit]: Manual specification of individual problems
 */
sealed class ChallengeImportSpec {
    abstract val title: String
    abstract val subtitle: String?

    /**
     * Specification for generating problems using rules.
     *
     * @property title The title of the challenge
     * @property subtitle Optional subtitle or description
     * @property operation The math operation to use
     * @property problemCount Number of problems to generate
     * @property numberRange Range of numbers to use for problem generation
     */
    data class Generated(
        override val title: String,
        override val subtitle: String?,
        val operation: MathOperation,
        val problemCount: Int,
        val numberRange: NumberRange,
    ) : ChallengeImportSpec()

    /**
     * Specification for explicitly defined problems.
     *
     * @property title The title of the challenge
     * @property subtitle Optional subtitle or description
     * @property problems List of explicitly defined problem specifications
     */
    data class Explicit(
        override val title: String,
        override val subtitle: String?,
        val problems: List<ProblemSpec>,
    ) : ChallengeImportSpec()
}
