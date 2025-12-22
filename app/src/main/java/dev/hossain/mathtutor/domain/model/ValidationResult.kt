package dev.hossain.mathtutor.domain.model

/**
 * Represents the result of validating a [ChallengeImportSpec].
 *
 * This sealed class provides two possible outcomes:
 * - [Success]: Validation passed with no errors
 * - [Error]: Validation failed with field-specific error messages
 */
sealed class ValidationResult {
    /**
     * Indicates that validation was successful.
     */
    data object Success : ValidationResult()

    /**
     * Indicates that validation failed with specific field errors.
     *
     * @property fieldErrors Map of field names to error messages
     *   Example: mapOf("title" to "Title is required", "problemCount" to "Must be between 1-50")
     */
    data class Error(
        val fieldErrors: Map<String, String>,
    ) : ValidationResult()
}
