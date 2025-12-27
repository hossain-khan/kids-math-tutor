package dev.hossain.mathtutor.domain.parser

import dev.hossain.mathtutor.domain.model.ChallengeImportSpec
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.ValidationResult
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Parser for custom challenge JSON specifications.
 *
 * Handles parsing and validation of JSON challenge import specifications
 * supporting both generated (rule-based) and explicit (problem list) formats.
 *
 * Challenge JSON can be created using the Math Pup Worksheet Creator web app:
 * `https://math-worksheet.gohk.xyz/`
 *
 * The JSON schema is available at:
 * `https://math-worksheet.gohk.xyz/challenge-schema.json`
 *
 * @see ChallengeImportSpec for the parsed specification model
 */
interface ChallengeJsonParser {
    /**
     * Parses a JSON string into a [ChallengeImportSpec].
     *
     * @param text The JSON string to parse
     * @return Result containing the parsed spec or an exception
     */
    fun parseFromText(text: String): Result<ChallengeImportSpec>

    /**
     * Attempts to find and extract the first valid JSON object in the given text.
     *
     * @param text Text that may contain embedded JSON
     * @return The extracted JSON string, or null if no valid JSON found
     */
    fun findJsonInText(text: String): String?
}

/**
 * Default implementation of [ChallengeJsonParser].
 *
 * Uses kotlinx.serialization for JSON parsing with validation rules
 * to ensure challenge specifications meet all requirements.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DefaultChallengeJsonParser
    constructor() : ChallengeJsonParser {
        private val json: Json =
            Json {
                ignoreUnknownKeys = true
                isLenient = false
                coerceInputValues = true
                classDiscriminator = "type"
            }

        override fun parseFromText(text: String): Result<ChallengeImportSpec> =
            runCatching {
                // Try to find JSON in the text first
                val jsonText = findJsonInText(text) ?: text.trim()

                // Try to parse the JSON
                val spec =
                    try {
                        json.decodeFromString<ChallengeImportSpec>(jsonText)
                    } catch (e: SerializationException) {
                        // Check if this is the missing discriminator error
                        if (e.message?.contains("discriminator") == true) {
                            // Try to infer the type from the JSON structure
                            inferAndParseType(jsonText)
                        } else {
                            throw e
                        }
                    }

                // Validate the parsed spec
                when (val validationResult = validateSpec(spec)) {
                    is ValidationResult.Success -> {
                        spec
                    }

                    is ValidationResult.Error -> {
                        throw ValidationException(validationResult.fieldErrors)
                    }
                }
            }

        /**
         * Attempts to infer the challenge type from JSON structure and parse accordingly.
         * Falls back to throwing a helpful error if type cannot be inferred.
         */
        private fun inferAndParseType(jsonText: String): ChallengeImportSpec {
            // Parse as JsonElement to inspect structure
            val jsonElement = json.parseToJsonElement(jsonText)
            if (jsonElement !is JsonObject) {
                throw IllegalArgumentException(
                    "Invalid JSON: Root must be an object. " +
                        "Please add a 'type' field with value 'generated' or 'explicit'.",
                )
            }

            // Try to infer type from structure
            val inferredType =
                when {
                    jsonElement.containsKey("problems") -> {
                        "explicit"
                    }

                    jsonElement.containsKey("operation") && jsonElement.containsKey("problemCount") -> {
                        "generated"
                    }

                    else -> {
                        throw IllegalArgumentException(
                            "Cannot determine challenge type. JSON must include a 'type' field with value 'generated' or 'explicit'. " +
                                "Generated challenges need: operation, problemCount, numberRange. " +
                                "Explicit challenges need: problems array.",
                        )
                    }
                }

            // Add the type field and re-parse
            val mutableMap: MutableMap<String, kotlinx.serialization.json.JsonElement> = jsonElement.toMutableMap()
            mutableMap["type"] = JsonPrimitive(inferredType)
            val fixedJson = JsonObject(mutableMap).toString()

            return json.decodeFromString<ChallengeImportSpec>(fixedJson)
        }

        override fun findJsonInText(text: String): String? {
            val trimmedText = text.trim()

            // Try to find JSON object boundaries by attempting to parse from each '{' found
            var searchIndex = 0
            while (true) {
                val startIndex = trimmedText.indexOf('{', searchIndex)
                if (startIndex == -1) return null

                // Try to extract a complete JSON object from this position
                val jsonCandidate = extractJsonObject(trimmedText, startIndex)

                if (jsonCandidate != null) {
                    // Validate it's parseable JSON
                    try {
                        json.parseToJsonElement(jsonCandidate)
                        return jsonCandidate
                    } catch (e: SerializationException) {
                        // Not valid JSON, try next '{'
                    }
                }

                // Move past this '{' and try the next one
                searchIndex = startIndex + 1
            }
        }

        /**
         * Attempts to extract a complete JSON object starting from the given index.
         * Returns null if no complete object can be extracted.
         */
        private fun extractJsonObject(
            text: String,
            startIndex: Int,
        ): String? {
            var braceCount = 0
            var inString = false
            var escaped = false

            for (i in startIndex until text.length) {
                val char = text[i]

                when {
                    escaped -> {
                        escaped = false
                    }

                    char == '\\' && inString -> {
                        escaped = true
                    }

                    char == '"' -> {
                        inString = !inString
                    }

                    !inString && char == '{' -> {
                        braceCount++
                    }

                    !inString && char == '}' -> {
                        braceCount--
                        if (braceCount == 0) {
                            // Found complete object
                            return text.substring(startIndex, i + 1)
                        }
                    }
                }
            }

            return null
        }

        /**
         * Validates a parsed [ChallengeImportSpec] against business rules.
         *
         * @param spec The specification to validate
         * @return [ValidationResult] indicating success or field-specific errors
         */
        private fun validateSpec(spec: ChallengeImportSpec): ValidationResult {
            val errors = mutableMapOf<String, String>()

            // Validate title
            when {
                spec.title.isBlank() -> errors["title"] = "Title is required"
                spec.title.length > 100 -> errors["title"] = "Title must not exceed 100 characters"
            }

            // Validate subtitle if present
            spec.subtitle?.let { subtitle ->
                if (subtitle.length > 150) {
                    errors["subtitle"] = "Subtitle must not exceed 150 characters"
                }
            }

            // Validate type-specific fields
            when (spec) {
                is ChallengeImportSpec.Generated -> validateGeneratedSpec(spec, errors)
                is ChallengeImportSpec.Explicit -> validateExplicitSpec(spec, errors)
            }

            return if (errors.isEmpty()) {
                ValidationResult.Success
            } else {
                ValidationResult.Error(errors)
            }
        }

        /**
         * Validates a [ChallengeImportSpec.Generated] specification.
         */
        private fun validateGeneratedSpec(
            spec: ChallengeImportSpec.Generated,
            errors: MutableMap<String, String>,
        ) {
            // Validate problem count
            if (spec.problemCount !in 1..50) {
                errors["problemCount"] = "Problem count must be between 1 and 50"
            }

            // Validate number range
            val range = spec.numberRange
            when {
                range.min < 0 -> errors["numberRange.min"] = "Minimum value must be at least 0"
                range.max > 9999 -> errors["numberRange.max"] = "Maximum value must not exceed 9999"
                range.min >= range.max -> errors["numberRange"] = "Minimum must be less than maximum"
            }

            // Validate operation
            if (spec.operation == MathOperation.MIXED) {
                errors["operation"] = "MIXED operation is not supported for custom challenges"
            }

            // Check for potential integer overflow
            try {
                checkOverflow(range.max, range.max, spec.operation)
            } catch (e: ArithmeticException) {
                errors["numberRange"] = "Number range would cause integer overflow for ${spec.operation.displayName}"
            }
        }

        /**
         * Validates a [ChallengeImportSpec.Explicit] specification.
         */
        private fun validateExplicitSpec(
            spec: ChallengeImportSpec.Explicit,
            errors: MutableMap<String, String>,
        ) {
            // Validate problems list
            if (spec.problems.isEmpty()) {
                errors["problems"] = "At least one problem is required"
            }

            if (spec.problems.size > 50) {
                errors["problems"] = "Cannot have more than 50 problems"
            }

            // Validate individual problems
            spec.problems.forEachIndexed { index, problem ->
                // Check for division by zero
                if (problem.operation == MathOperation.DIVISION && problem.operand2 == 0) {
                    errors["problems[$index]"] = "Division by zero is not allowed"
                }

                // Check for non-whole division results
                if (problem.operation == MathOperation.DIVISION &&
                    problem.operand2 != 0 &&
                    problem.operand1 % problem.operand2 != 0
                ) {
                    errors["problems[$index]"] =
                        "Division must result in whole numbers (${problem.operand1} ÷ ${problem.operand2} = ${problem.operand1.toDouble() / problem.operand2})"
                }

                // Check for overflow
                try {
                    checkOverflow(problem.operand1, problem.operand2, problem.operation)
                } catch (e: ArithmeticException) {
                    errors["problems[$index]"] =
                        "Problem would cause integer overflow: ${problem.operand1} ${problem.operation.symbol} ${problem.operand2}"
                }

                // Validate operation
                if (problem.operation == MathOperation.MIXED) {
                    errors["problems[$index].operation"] = "MIXED operation is not supported for explicit problems"
                }
            }
        }

        /**
         * Checks if an operation would cause integer overflow.
         *
         * @throws ArithmeticException if overflow would occur
         */
        private fun checkOverflow(
            operand1: Int,
            operand2: Int,
            operation: MathOperation,
        ) {
            when (operation) {
                MathOperation.ADDITION -> {
                    val result = operand1.toLong() + operand2.toLong()
                    if (result > Int.MAX_VALUE || result < Int.MIN_VALUE) {
                        throw ArithmeticException("Addition overflow")
                    }
                }

                MathOperation.SUBTRACTION -> {
                    val result = operand1.toLong() - operand2.toLong()
                    if (result > Int.MAX_VALUE || result < Int.MIN_VALUE) {
                        throw ArithmeticException("Subtraction overflow")
                    }
                }

                MathOperation.MULTIPLICATION -> {
                    val result = operand1.toLong() * operand2.toLong()
                    if (result > Int.MAX_VALUE || result < Int.MIN_VALUE) {
                        throw ArithmeticException("Multiplication overflow")
                    }
                }

                MathOperation.DIVISION -> {
                    // Division doesn't overflow in the same way, but check for division by zero
                    if (operand2 == 0) {
                        throw ArithmeticException("Division by zero")
                    }
                }

                MathOperation.MIXED -> {
                    // MIXED should not be used
                }
            }
        }
    }

/**
 * Exception thrown when validation fails.
 *
 * @property fieldErrors Map of field names to error messages
 */
class ValidationException(
    val fieldErrors: Map<String, String>,
) : Exception("Validation failed: ${fieldErrors.values.joinToString(", ")}")
