package dev.hossain.mathtutor.domain.model

/**
 * Represents the grade levels supported by the app.
 * Each grade level has specific number ranges for different math operations.
 *
 * @property displayName The human-readable name of the grade level
 */
enum class GradeLevel(
    val displayName: String,
) {
    KINDERGARTEN("Kindergarten"),
    GRADE_1("Grade 1"),
    GRADE_2("Grade 2"),
    ;

    /**
     * Returns the appropriate number range for the given operation based on this grade level.
     * Ranges are designed to be age-appropriate and progressively challenging.
     *
     * @param operation The math operation to get the range for
     * @return Pair of (min, max) inclusive numbers for the operation
     */
    fun getNumberRange(operation: MathOperation): Pair<Int, Int> =
        when (this) {
            KINDERGARTEN -> {
                when (operation) {
                    MathOperation.ADDITION -> 1 to 5
                    MathOperation.SUBTRACTION -> 1 to 5
                    MathOperation.MULTIPLICATION -> 1 to 2
                    MathOperation.DIVISION -> 1 to 2
                    MathOperation.MIXED -> 1 to 5
                }
            }

            GRADE_1 -> {
                when (operation) {
                    MathOperation.ADDITION -> 1 to 10
                    MathOperation.SUBTRACTION -> 1 to 10
                    MathOperation.MULTIPLICATION -> 1 to 5
                    MathOperation.DIVISION -> 1 to 5
                    MathOperation.MIXED -> 1 to 10
                }
            }

            GRADE_2 -> {
                when (operation) {
                    MathOperation.ADDITION -> 1 to 20
                    MathOperation.SUBTRACTION -> 1 to 20
                    MathOperation.MULTIPLICATION -> 1 to 10
                    MathOperation.DIVISION -> 1 to 10
                    MathOperation.MIXED -> 1 to 20
                }
            }
        }
}
