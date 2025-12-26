package dev.hossain.mathtutor.deeplink

import android.net.Uri
import timber.log.Timber
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Utilities for handling deeplinks for importing challenges.
 *
 * Format: `mathpup://import?json=<url-encoded-json>`
 *
 * Example:
 * ```
 * mathpup://import?json=%7B%22type%22%3A%22explicit%22%2C...%7D
 * ```
 */
object DeeplinkHandler {
    private const val SCHEME = "mathpup"
    private const val HOST = "import"
    private const val JSON_PARAM = "json"

    /**
     * Extracts the JSON challenge data from a deeplink URI.
     *
     * @param uri The deeplink URI to parse
     * @return The decoded JSON string if valid, null otherwise
     */
    fun extractJsonFromDeeplink(uri: Uri?): String? {
        if (uri == null) {
            Timber.w("DeeplinkHandler: URI is null")
            return null
        }

        // Verify scheme and host
        if (uri.scheme != SCHEME || uri.host != HOST) {
            Timber.w("DeeplinkHandler: Invalid scheme or host. Scheme: ${uri.scheme}, Host: ${uri.host}")
            return null
        }

        // Extract and decode the JSON parameter
        val encodedJson = uri.getQueryParameter(JSON_PARAM)
        if (encodedJson.isNullOrBlank()) {
            Timber.w("DeeplinkHandler: Missing or empty 'json' query parameter")
            return null
        }

        return try {
            val decoded = URLDecoder.decode(encodedJson, "UTF-8")
            Timber.d("DeeplinkHandler: Successfully decoded JSON from deeplink")
            decoded
        } catch (e: Exception) {
            Timber.e(e, "DeeplinkHandler: Failed to decode JSON from deeplink")
            null
        }
    }

    /**
     * Generates a deeplink URL for importing a challenge.
     *
     * @param jsonData The challenge JSON to encode
     * @return The deeplink URL string
     */
    fun generateDeeplink(jsonData: String): String =
        try {
            val encodedJson = URLEncoder.encode(jsonData, "UTF-8")
            "mathpup://import?json=$encodedJson"
        } catch (e: Exception) {
            Timber.e(e, "DeeplinkHandler: Failed to generate deeplink")
            ""
        }
}
