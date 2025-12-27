package dev.hossain.mathtutor.domain.parser

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.ChallengeImportSpec
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.NumberRange
import dev.hossain.mathtutor.domain.model.ProblemSpec
import org.junit.Before
import org.junit.Test

class ChallengeJsonParserTest {
    private lateinit var parser: ChallengeJsonParser

    @Before
    fun setup() {
        parser = DefaultChallengeJsonParser()
    }

    // ==================== Valid JSON Parsing Tests ====================

    @Test
    fun `parseFromText with valid generated schema succeeds`() {
        val json =
            """
            {
              "type": "generated",
              "title": "Addition Practice",
              "subtitle": "Focus on carrying over",
              "operation": "addition",
              "problemCount": 10,
              "numberRange": {"min": 10, "max": 99}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrThrow()
        assertThat(spec).isInstanceOf(ChallengeImportSpec.Generated::class.java)

        val generated = spec as ChallengeImportSpec.Generated
        assertThat(generated.title).isEqualTo("Addition Practice")
        assertThat(generated.subtitle).isEqualTo("Focus on carrying over")
        assertThat(generated.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(generated.problemCount).isEqualTo(10)
        assertThat(generated.numberRange.min).isEqualTo(10)
        assertThat(generated.numberRange.max).isEqualTo(99)
    }

    @Test
    fun `parseFromText with valid explicit schema succeeds`() {
        val json =
            """
            {
              "type": "explicit",
              "title": "Emma's Challenges",
              "subtitle": "Mixed practice problems",
              "problems": [
                {"operand1": 15, "operand2": 3, "operation": "division"},
                {"operand1": 8, "operand2": 4, "operation": "multiplication"}
              ]
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrThrow()
        assertThat(spec).isInstanceOf(ChallengeImportSpec.Explicit::class.java)

        val explicit = spec as ChallengeImportSpec.Explicit
        assertThat(explicit.title).isEqualTo("Emma's Challenges")
        assertThat(explicit.subtitle).isEqualTo("Mixed practice problems")
        assertThat(explicit.problems).hasSize(2)
        assertThat(explicit.problems[0].operand1).isEqualTo(15)
        assertThat(explicit.problems[0].operand2).isEqualTo(3)
        assertThat(explicit.problems[0].operation).isEqualTo(MathOperation.DIVISION)
        assertThat(explicit.problems[1].operand1).isEqualTo(8)
        assertThat(explicit.problems[1].operand2).isEqualTo(4)
        assertThat(explicit.problems[1].operation).isEqualTo(MathOperation.MULTIPLICATION)
    }

    @Test
    fun `parseFromText with null subtitle succeeds`() {
        val json =
            """
            {
              "type": "generated",
              "title": "Quick Practice",
              "operation": "subtraction",
              "problemCount": 5,
              "numberRange": {"min": 1, "max": 20}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrThrow() as ChallengeImportSpec.Generated
        assertThat(spec.subtitle).isNull()
    }

    @Test
    fun `parseFromText with all operations succeeds`() {
        val operations = listOf("addition", "subtraction", "multiplication", "division")

        operations.forEach { op ->
            val json =
                """
                {
                  "type": "generated",
                  "title": "Test",
                  "operation": "$op",
                  "problemCount": 5,
                  "numberRange": {"min": 1, "max": 10}
                }
                """.trimIndent()

            val result = parser.parseFromText(json)
            assertThat(result.isSuccess).isTrue()
        }
    }

    // ==================== Invalid JSON Tests ====================

    @Test
    fun `parseFromText with malformed JSON fails`() {
        val json = """{"type": "generated", "title": "Test"""

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `parseFromText with unknown type fails`() {
        val json =
            """
            {
              "type": "unknown",
              "title": "Test"
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `parseFromText with missing type field auto-detects generated type`() {
        // This is the exact scenario from the user's error - JSON without "type" field
        val json =
            """
            {
              "title": "Add to 5",
              "subtitle": "Kindergarten - Adding numbers up to 5",
              "operation": "addition",
              "problemCount": 10,
              "numberRange": {"min": 0, "max": 5}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        // Should succeed by auto-detecting as "generated" type
        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrThrow()
        assertThat(spec).isInstanceOf(ChallengeImportSpec.Generated::class.java)

        val generated = spec as ChallengeImportSpec.Generated
        assertThat(generated.title).isEqualTo("Add to 5")
        assertThat(generated.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(generated.problemCount).isEqualTo(10)
    }

    @Test
    fun `parseFromText with missing type field auto-detects explicit type`() {
        val json =
            """
            {
              "title": "Mixed Practice",
              "subtitle": "Various problems",
              "problems": [
                {"operand1": 5, "operand2": 3, "operation": "addition"},
                {"operand1": 9, "operand2": 5, "operation": "addition"}
              ]
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        // Should succeed by auto-detecting as "explicit" type
        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrThrow()
        assertThat(spec).isInstanceOf(ChallengeImportSpec.Explicit::class.java)

        val explicit = spec as ChallengeImportSpec.Explicit
        assertThat(explicit.title).isEqualTo("Mixed Practice")
        assertThat(explicit.problems).hasSize(2)
    }

    @Test
    fun `parseFromText with missing type and ambiguous structure fails with helpful error`() {
        val json =
            """
            {
              "title": "Test"
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception?.message).contains("Cannot determine challenge type")
        assertThat(exception?.message).contains("'type' field")
    }

    // ==================== Validation Tests ====================

    @Test
    fun `parseFromText with empty title fails`() {
        val json =
            """
            {
              "type": "generated",
              "title": "",
              "operation": "addition",
              "problemCount": 5,
              "numberRange": {"min": 1, "max": 10}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors).containsKey("title")
        assertThat(exception.fieldErrors["title"]).isEqualTo("Title is required")
    }

    @Test
    fun `parseFromText with title exceeding 100 characters fails`() {
        val longTitle = "a".repeat(101)
        val json =
            """
            {
              "type": "generated",
              "title": "$longTitle",
              "operation": "addition",
              "problemCount": 5,
              "numberRange": {"min": 1, "max": 10}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors).containsKey("title")
        assertThat(exception.fieldErrors["title"]).contains("100 characters")
    }

    @Test
    fun `parseFromText with subtitle exceeding 150 characters fails`() {
        val longSubtitle = "a".repeat(151)
        val json =
            """
            {
              "type": "generated",
              "title": "Test",
              "subtitle": "$longSubtitle",
              "operation": "addition",
              "problemCount": 5,
              "numberRange": {"min": 1, "max": 10}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors).containsKey("subtitle")
        assertThat(exception.fieldErrors["subtitle"]).contains("150 characters")
    }

    @Test
    fun `parseFromText with problemCount less than 1 fails`() {
        val json =
            """
            {
              "type": "generated",
              "title": "Test",
              "operation": "addition",
              "problemCount": 0,
              "numberRange": {"min": 1, "max": 10}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors).containsKey("problemCount")
    }

    @Test
    fun `parseFromText with problemCount greater than 50 fails`() {
        val json =
            """
            {
              "type": "generated",
              "title": "Test",
              "operation": "addition",
              "problemCount": 51,
              "numberRange": {"min": 1, "max": 10}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors).containsKey("problemCount")
    }

    @Test
    fun `parseFromText with min less than 0 fails`() {
        val json =
            """
            {
              "type": "generated",
              "title": "Test",
              "operation": "addition",
              "problemCount": 5,
              "numberRange": {"min": -1, "max": 10}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors).containsKey("numberRange.min")
    }

    @Test
    fun `parseFromText with max greater than 9999 fails`() {
        val json =
            """
            {
              "type": "generated",
              "title": "Test",
              "operation": "addition",
              "problemCount": 5,
              "numberRange": {"min": 1, "max": 10000}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors).containsKey("numberRange.max")
    }

    @Test
    fun `parseFromText with min greater than or equal to max fails`() {
        val json =
            """
            {
              "type": "generated",
              "title": "Test",
              "operation": "addition",
              "problemCount": 5,
              "numberRange": {"min": 10, "max": 10}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors).containsKey("numberRange")
    }

    @Test
    fun `parseFromText with overflow risk fails`() {
        val json =
            """
            {
              "type": "generated",
              "title": "Test",
              "operation": "multiplication",
              "problemCount": 5,
              "numberRange": {"min": 50000, "max": 50000}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        // Should fail because max > 9999
        assertThat(exception!!.fieldErrors).containsKey("numberRange.max")
    }

    @Test
    fun `parseFromText with empty problems list fails`() {
        val json =
            """
            {
              "type": "explicit",
              "title": "Test",
              "problems": []
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors).containsKey("problems")
    }

    @Test
    fun `parseFromText with more than 50 problems fails`() {
        val problems = (1..51).joinToString(",") { """{"operand1": 5, "operand2": 3, "operation": "addition"}""" }
        val json =
            """
            {
              "type": "explicit",
              "title": "Test",
              "problems": [$problems]
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors).containsKey("problems")
    }

    @Test
    fun `parseFromText with division by zero fails`() {
        val json =
            """
            {
              "type": "explicit",
              "title": "Test",
              "problems": [
                {"operand1": 10, "operand2": 0, "operation": "division"}
              ]
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors.keys.any { it.startsWith("problems[") }).isTrue()
    }

    @Test
    fun `parseFromText with non-whole division result fails`() {
        val json =
            """
            {
              "type": "explicit",
              "title": "Test",
              "problems": [
                {"operand1": 10, "operand2": 3, "operation": "division"}
              ]
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors.keys.any { it.startsWith("problems[") }).isTrue()
        assertThat(exception.fieldErrors.values.any { it.contains("whole numbers") }).isTrue()
    }

    @Test
    fun `parseFromText with problem causing overflow fails`() {
        val json =
            """
            {
              "type": "explicit",
              "title": "Test",
              "problems": [
                {"operand1": 2000000000, "operand2": 2, "operation": "multiplication"}
              ]
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as? ValidationException
        assertThat(exception).isNotNull()
        assertThat(exception!!.fieldErrors.keys.any { it.startsWith("problems[") }).isTrue()
        assertThat(exception.fieldErrors.values.any { it.contains("overflow") }).isTrue()
    }

    // ==================== JSON Detection Tests ====================

    @Test
    fun `findJsonInText extracts JSON from plain text`() {
        val text =
            """
            Here's a challenge for you:
            {
              "type": "generated",
              "title": "Addition Practice",
              "operation": "addition",
              "problemCount": 10,
              "numberRange": {"min": 10, "max": 99}
            }
            Let me know if you need help!
            """.trimIndent()

        val json = parser.findJsonInText(text)

        assertThat(json).isNotNull()
        assertThat(json).contains("Addition Practice")
        assertThat(json).contains("generated")
    }

    @Test
    fun `findJsonInText returns null when no JSON present`() {
        val text = "This is just plain text without any JSON"

        val json = parser.findJsonInText(text)

        assertThat(json).isNull()
    }

    @Test
    fun `findJsonInText extracts first JSON when multiple present`() {
        val text =
            """
            First: {"type": "generated", "title": "First"}
            Second: {"type": "generated", "title": "Second"}
            """.trimIndent()

        val json = parser.findJsonInText(text)

        assertThat(json).isNotNull()
        assertThat(json).contains("First")
        assertThat(json).doesNotContain("Second")
    }

    @Test
    fun `findJsonInText handles nested JSON objects`() {
        val text =
            """
            Check this out:
            {
              "type": "generated",
              "title": "Test",
              "operation": "addition",
              "problemCount": 10,
              "numberRange": {"min": 10, "max": 99}
            }
            """.trimIndent()

        val json = parser.findJsonInText(text)

        assertThat(json).isNotNull()
        assertThat(json).contains("numberRange")
        assertThat(json).contains("min")
    }

    @Test
    fun `findJsonInText handles JSON with strings containing braces`() {
        val text =
            """
            Message: "Check {this} out"
            {
              "type": "generated",
              "title": "Test with {braces}",
              "operation": "addition",
              "problemCount": 10,
              "numberRange": {"min": 10, "max": 99}
            }
            """.trimIndent()

        val json = parser.findJsonInText(text)

        assertThat(json).isNotNull()
        assertThat(json).contains("Test with {braces}")
    }

    @Test
    fun `parseFromText uses findJsonInText for embedded JSON`() {
        val text =
            """
            Hi! I created this challenge for you:
            
            {
              "type": "generated",
              "title": "Addition Practice",
              "operation": "addition",
              "problemCount": 10,
              "numberRange": {"min": 10, "max": 99}
            }
            
            Hope you enjoy it!
            """.trimIndent()

        val result = parser.parseFromText(text)

        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrThrow() as ChallengeImportSpec.Generated
        assertThat(spec.title).isEqualTo("Addition Practice")
    }

    // ==================== Edge Cases ====================

    @Test
    fun `parseFromText with boundary values succeeds`() {
        val json =
            """
            {
              "type": "generated",
              "title": "Test",
              "operation": "addition",
              "problemCount": 1,
              "numberRange": {"min": 0, "max": 9999}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `parseFromText with exactly 50 problems succeeds`() {
        val problems = (1..50).joinToString(",") { """{"operand1": 5, "operand2": 3, "operation": "addition"}""" }
        val json =
            """
            {
              "type": "explicit",
              "title": "Test",
              "problems": [$problems]
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `parseFromText with title exactly 100 characters succeeds`() {
        val title = "a".repeat(100)
        val json =
            """
            {
              "type": "generated",
              "title": "$title",
              "operation": "addition",
              "problemCount": 5,
              "numberRange": {"min": 1, "max": 10}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `parseFromText with subtitle exactly 150 characters succeeds`() {
        val subtitle = "a".repeat(150)
        val json =
            """
            {
              "type": "generated",
              "title": "Test",
              "subtitle": "$subtitle",
              "operation": "addition",
              "problemCount": 5,
              "numberRange": {"min": 1, "max": 10}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isSuccess).isTrue()
    }
}
