package dev.hossain.mathtutor.deeplink

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.URLEncoder

/**
 * Unit tests for [DeeplinkHandler].
 *
 * Tests deeplink encoding/decoding for importing challenges
 * from the webapp to the Android app.
 */
@RunWith(RobolectricTestRunner::class)
class DeeplinkHandlerTest {
    @Test
    fun extractJsonFromDeeplink_validDeeplink_extractsJson() {
        // Given
        val testJson = """{"type":"explicit","title":"Test Challenge"}"""
        val encodedJson = URLEncoder.encode(testJson, "UTF-8")
        val uri = Uri.parse("mathpup://import?json=$encodedJson")

        // When
        val result = DeeplinkHandler.extractJsonFromDeeplink(uri)

        // Then
        assertThat(result).isEqualTo(testJson)
    }

    @Test
    fun extractJsonFromDeeplink_nullUri_returnsNull() {
        // When
        val result = DeeplinkHandler.extractJsonFromDeeplink(null)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun extractJsonFromDeeplink_invalidScheme_returnsNull() {
        // Given
        val uri = Uri.parse("https://example.com/import?json=test")

        // When
        val result = DeeplinkHandler.extractJsonFromDeeplink(uri)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun extractJsonFromDeeplink_invalidHost_returnsNull() {
        // Given
        val uri = Uri.parse("mathpup://challenge?json=test")

        // When
        val result = DeeplinkHandler.extractJsonFromDeeplink(uri)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun extractJsonFromDeeplink_missingJsonParam_returnsNull() {
        // Given
        val uri = Uri.parse("mathpup://import")

        // When
        val result = DeeplinkHandler.extractJsonFromDeeplink(uri)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun extractJsonFromDeeplink_emptyJsonParam_returnsNull() {
        // Given
        val uri = Uri.parse("mathpup://import?json=")

        // When
        val result = DeeplinkHandler.extractJsonFromDeeplink(uri)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun generateDeeplink_validJson_createsValidUri() {
        // Given
        val testJson = """{"type":"explicit","title":"Test"}"""

        // When
        val deeplink = DeeplinkHandler.generateDeeplink(testJson)

        // Then
        assertThat(deeplink).startsWith("mathpup://import?json=")
        // Verify it can be parsed back
        val uri = Uri.parse(deeplink)
        val extracted = DeeplinkHandler.extractJsonFromDeeplink(uri)
        assertThat(extracted).isEqualTo(testJson)
    }

    @Test
    fun generateDeeplink_complexJson_encodesCorrectly() {
        // Given
        val testJson =
            """
            {
              "type": "generated",
              "title": "Addition Practice",
              "operation": "addition",
              "problemCount": 10,
              "numberRange": {"min": 1, "max": 10}
            }
            """.trimIndent()

        // When
        val deeplink = DeeplinkHandler.generateDeeplink(testJson)

        // Then
        assertThat(deeplink).isNotEmpty()
        val uri = Uri.parse(deeplink)
        val extracted = DeeplinkHandler.extractJsonFromDeeplink(uri)
        assertThat(extracted).isEqualTo(testJson)
    }

    @Test
    fun generateDeeplink_jsonWithSpecialCharacters_encodesCorrectly() {
        // Given
        val testJson = """{"title":"Math & Science: \"Addition\"","emoji":"🎉"}"""

        // When
        val deeplink = DeeplinkHandler.generateDeeplink(testJson)

        // Then
        assertThat(deeplink).isNotEmpty()
        val uri = Uri.parse(deeplink)
        val extracted = DeeplinkHandler.extractJsonFromDeeplink(uri)
        assertThat(extracted).isEqualTo(testJson)
    }

    @Test
    fun roundTripEncoding_multipleJsons_preservesContent() {
        // Given
        val testCases =
            listOf(
                """{"type":"explicit"}""",
                """{"type":"generated","problemCount":15}""",
                """{"title":"Complex: & < > \" '"}""",
            )

        for (testJson in testCases) {
            // When
            val deeplink = DeeplinkHandler.generateDeeplink(testJson)
            val uri = Uri.parse(deeplink)
            val extracted = DeeplinkHandler.extractJsonFromDeeplink(uri)

            // Then
            assertThat(extracted).isEqualTo(testJson)
        }
    }

    @Test
    fun deeplinkFormat_followsExpectedPattern() {
        // Given
        val testJson = """{"type":"test"}"""

        // When
        val deeplink = DeeplinkHandler.generateDeeplink(testJson)

        // Then
        assertThat(deeplink).startsWith("mathpup://import?json=")
        assertThat(deeplink).contains("?json=")
    }
}
