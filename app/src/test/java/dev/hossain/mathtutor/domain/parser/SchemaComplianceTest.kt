package dev.hossain.mathtutor.domain.parser

import com.google.common.truth.Truth.assertThat
import dev.hossain.mathtutor.domain.model.ChallengeImportSpec
import dev.hossain.mathtutor.domain.model.MathOperation
import org.junit.Before
import org.junit.Test

/**
 * Tests to verify Android app compliance with the official JSON schema at:
 * https://math-worksheet.gohk.xyz/challenge-schema.json
 *
 * These tests use the exact examples from the schema to ensure perfect compliance.
 */
class SchemaComplianceTest {
    private lateinit var parser: ChallengeJsonParser

    @Before
    fun setup() {
        parser = DefaultChallengeJsonParser()
    }

    @Test
    fun `schema example 1 - generated challenge with type field`() {
        // This is the exact example from the schema
        val json =
            """
            {
              "type": "generated",
              "title": "Addition Practice 1-20",
              "subtitle": "Master basic addition skills",
              "operation": "addition",
              "problemCount": 10,
              "numberRange": {
                "min": 1,
                "max": 20
              }
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrThrow() as ChallengeImportSpec.Generated
        assertThat(spec.title).isEqualTo("Addition Practice 1-20")
        assertThat(spec.subtitle).isEqualTo("Master basic addition skills")
        assertThat(spec.operation).isEqualTo(MathOperation.ADDITION)
        assertThat(spec.problemCount).isEqualTo(10)
        assertThat(spec.numberRange.min).isEqualTo(1)
        assertThat(spec.numberRange.max).isEqualTo(20)
    }

    @Test
    fun `schema example 2 - explicit challenge with type field`() {
        // This is the exact example from the schema
        val json =
            """
            {
              "type": "explicit",
              "title": "Mixed Math Practice",
              "subtitle": "Custom problems",
              "problems": [
                {
                  "operand1": 5,
                  "operand2": 3,
                  "operation": "addition"
                },
                {
                  "operand1": 12,
                  "operand2": 4,
                  "operation": "division"
                },
                {
                  "operand1": 8,
                  "operand2": 2,
                  "operation": "subtraction"
                },
                {
                  "operand1": 6,
                  "operand2": 7,
                  "operation": "multiplication"
                }
              ]
            }
            """.trimIndent()

        val result = parser.parseFromText(json)

        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrThrow() as ChallengeImportSpec.Explicit
        assertThat(spec.title).isEqualTo("Mixed Math Practice")
        assertThat(spec.subtitle).isEqualTo("Custom problems")
        assertThat(spec.problems).hasSize(4)

        // Verify all problems
        assertThat(spec.problems[0].operand1).isEqualTo(5)
        assertThat(spec.problems[0].operand2).isEqualTo(3)
        assertThat(spec.problems[0].operation).isEqualTo(MathOperation.ADDITION)

        assertThat(spec.problems[1].operand1).isEqualTo(12)
        assertThat(spec.problems[1].operand2).isEqualTo(4)
        assertThat(spec.problems[1].operation).isEqualTo(MathOperation.DIVISION)

        assertThat(spec.problems[2].operand1).isEqualTo(8)
        assertThat(spec.problems[2].operand2).isEqualTo(2)
        assertThat(spec.problems[2].operation).isEqualTo(MathOperation.SUBTRACTION)

        assertThat(spec.problems[3].operand1).isEqualTo(6)
        assertThat(spec.problems[3].operand2).isEqualTo(7)
        assertThat(spec.problems[3].operation).isEqualTo(MathOperation.MULTIPLICATION)
    }

    @Test
    fun `schema compliance - all operations are lowercase strings`() {
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

    @Test
    fun `schema compliance - boundary values for generated challenges`() {
        val json =
            """
            {
              "type": "generated",
              "title": "x",
              "operation": "addition",
              "problemCount": 1,
              "numberRange": {"min": 0, "max": 9999}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `schema compliance - boundary values for explicit challenges`() {
        val json =
            """
            {
              "type": "explicit",
              "title": "x",
              "problems": [
                {"operand1": 0, "operand2": 9999, "operation": "addition"}
              ]
            }
            """.trimIndent()

        val result = parser.parseFromText(json)
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `schema compliance - maximum title length`() {
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
    fun `schema compliance - maximum subtitle length`() {
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

    @Test
    fun `schema compliance - maximum problem count`() {
        val json =
            """
            {
              "type": "generated",
              "title": "Test",
              "operation": "addition",
              "problemCount": 50,
              "numberRange": {"min": 1, "max": 10}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `schema compliance - maximum problems array size`() {
        val problems =
            (1..50).joinToString(",") {
                """{"operand1": 5, "operand2": 3, "operation": "addition"}"""
            }
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
    fun `schema compliance - subtitle is optional`() {
        val json =
            """
            {
              "type": "generated",
              "title": "Test",
              "operation": "addition",
              "problemCount": 5,
              "numberRange": {"min": 1, "max": 10}
            }
            """.trimIndent()

        val result = parser.parseFromText(json)
        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrThrow() as ChallengeImportSpec.Generated
        assertThat(spec.subtitle).isNull()
    }
}
