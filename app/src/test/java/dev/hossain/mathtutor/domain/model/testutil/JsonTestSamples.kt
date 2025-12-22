package dev.hossain.mathtutor.domain.model.testutil

/**
 * Collection of JSON test samples for custom challenge testing.
 *
 * Provides various JSON samples for testing parser behavior, validation,
 * and edge case handling.
 */
object JsonTestSamples {
    /**
     * Valid JSON for a generated challenge with addition operation.
     */
    const val VALID_GENERATED_JSON =
        """
        {
          "type": "generated",
          "title": "Addition Practice",
          "subtitle": "Numbers 1-20",
          "operation": "addition",
          "problemCount": 10,
          "numberRange": {"min": 1, "max": 20}
        }
        """

    /**
     * Valid JSON for a generated challenge without subtitle.
     */
    const val VALID_GENERATED_NO_SUBTITLE =
        """
        {
          "type": "generated",
          "title": "Quick Math",
          "operation": "multiplication",
          "problemCount": 5,
          "numberRange": {"min": 2, "max": 10}
        }
        """

    /**
     * Valid JSON for an explicit challenge with specific problems.
     */
    const val VALID_EXPLICIT_JSON =
        """
        {
          "type": "explicit",
          "title": "Mixed Practice",
          "subtitle": "Hand-picked problems",
          "problems": [
            {"operand1": 5, "operand2": 3, "operation": "addition"},
            {"operand1": 8, "operand2": 4, "operation": "division"},
            {"operand1": 10, "operand2": 2, "operation": "subtraction"}
          ]
        }
        """

    /**
     * Valid JSON for explicit challenge with clean division problems.
     */
    const val VALID_EXPLICIT_DIVISION =
        """
        {
          "type": "explicit",
          "title": "Division Practice",
          "problems": [
            {"operand1": 10, "operand2": 2, "operation": "division"},
            {"operand1": 15, "operand2": 3, "operation": "division"},
            {"operand1": 20, "operand2": 4, "operation": "division"}
          ]
        }
        """

    /**
     * Invalid JSON with missing required field (title).
     */
    const val INVALID_JSON_MISSING_TITLE =
        """
        {
          "type": "generated",
          "operation": "addition",
          "problemCount": 10,
          "numberRange": {"min": 1, "max": 20}
        }
        """

    /**
     * Invalid JSON with empty title.
     */
    const val INVALID_JSON_EMPTY_TITLE =
        """
        {
          "type": "generated",
          "title": "",
          "operation": "addition",
          "problemCount": 10,
          "numberRange": {"min": 1, "max": 20}
        }
        """

    /**
     * Invalid JSON with problem count out of range.
     */
    const val INVALID_JSON_PROBLEM_COUNT =
        """
        {
          "type": "generated",
          "title": "Too Many Problems",
          "operation": "addition",
          "problemCount": 100,
          "numberRange": {"min": 1, "max": 10}
        }
        """

    /**
     * Invalid JSON with invalid number range.
     */
    const val INVALID_JSON_NUMBER_RANGE =
        """
        {
          "type": "generated",
          "title": "Bad Range",
          "operation": "addition",
          "problemCount": 10,
          "numberRange": {"min": 20, "max": 10}
        }
        """

    /**
     * Invalid JSON with division by zero.
     */
    const val INVALID_EXPLICIT_DIVISION_BY_ZERO =
        """
        {
          "type": "explicit",
          "title": "Division by Zero",
          "problems": [
            {"operand1": 10, "operand2": 0, "operation": "division"}
          ]
        }
        """

    /**
     * Invalid JSON with non-whole division result.
     */
    const val INVALID_EXPLICIT_NON_WHOLE_DIVISION =
        """
        {
          "type": "explicit",
          "title": "Non-whole Division",
          "problems": [
            {"operand1": 7, "operand2": 2, "operation": "division"}
          ]
        }
        """

    /**
     * Invalid JSON with empty problems list.
     */
    const val INVALID_EXPLICIT_EMPTY_PROBLEMS =
        """
        {
          "type": "explicit",
          "title": "No Problems",
          "problems": []
        }
        """

    /**
     * Malformed JSON with syntax error.
     */
    const val MALFORMED_JSON =
        """
        {
          "type": "generated",
          "title": "Malformed",
          "operation": "addition"
          "problemCount": 10
        }
        """

    /**
     * Email content with embedded JSON challenge.
     */
    const val EMAIL_WITH_EMBEDDED_JSON =
        """
        Hi there!
        
        Here's the math challenge for Emma:
        
        {
          "type": "generated",
          "title": "Emma's Challenge",
          "subtitle": "Practice for the week",
          "operation": "addition",
          "problemCount": 5,
          "numberRange": {"min": 1, "max": 10}
        }
        
        Let me know how it goes!
        
        Best regards,
        Mom
        """

    /**
     * Text with multiple JSON objects (should select first valid one).
     */
    const val TEXT_WITH_MULTIPLE_JSON =
        """
        Here are two challenges:
        
        First challenge:
        {
          "type": "generated",
          "title": "First Challenge",
          "operation": "addition",
          "problemCount": 5,
          "numberRange": {"min": 1, "max": 10}
        }
        
        Second challenge:
        {
          "type": "generated",
          "title": "Second Challenge",
          "operation": "subtraction",
          "problemCount": 10,
          "numberRange": {"min": 1, "max": 20}
        }
        """

    /**
     * JSON with large numbers that would cause overflow.
     */
    const val JSON_WITH_OVERFLOW_RISK =
        """
        {
          "type": "explicit",
          "title": "Overflow Risk",
          "problems": [
            {"operand1": 2000000000, "operand2": 2000000000, "operation": "addition"}
          ]
        }
        """

    /**
     * JSON with title exceeding maximum length.
     */
    const val INVALID_JSON_TITLE_TOO_LONG =
        """
        {
          "type": "generated",
          "title": "This is an extremely long title that exceeds the maximum allowed length of one hundred characters for challenge titles",
          "operation": "addition",
          "problemCount": 10,
          "numberRange": {"min": 1, "max": 10}
        }
        """

    /**
     * JSON with subtitle exceeding maximum length.
     */
    const val INVALID_JSON_SUBTITLE_TOO_LONG =
        """
        {
          "type": "generated",
          "title": "Valid Title",
          "subtitle": "This is an extremely long subtitle that exceeds the maximum allowed length of one hundred and fifty characters for challenge subtitles and should be rejected",
          "operation": "addition",
          "problemCount": 10,
          "numberRange": {"min": 1, "max": 10}
        }
        """

    /**
     * JSON with MIXED operation (not supported).
     */
    const val INVALID_JSON_MIXED_OPERATION =
        """
        {
          "type": "generated",
          "title": "Mixed Operation",
          "operation": "mixed",
          "problemCount": 10,
          "numberRange": {"min": 1, "max": 10}
        }
        """

    /**
     * Valid JSON with maximum allowed problems.
     */
    const val VALID_JSON_MAX_PROBLEMS =
        """
        {
          "type": "generated",
          "title": "Maximum Problems",
          "operation": "addition",
          "problemCount": 50,
          "numberRange": {"min": 1, "max": 10}
        }
        """

    /**
     * Invalid JSON with too many problems (over limit).
     * Note: Not const because it uses dynamic list generation.
     */
    val INVALID_EXPLICIT_TOO_MANY_PROBLEMS: String
        get() =
            """
            {
              "type": "explicit",
              "title": "Too Many",
              "problems": [${
                List(51) { """{"operand1": 1, "operand2": 1, "operation": "addition"}""" }.joinToString(",")
            }]
            }
            """
}
