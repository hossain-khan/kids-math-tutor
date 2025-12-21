package dev.hossain.mathtutor.analytics

/**
 * Analytics service interface for tracking user interactions and events.
 *
 * This abstraction allows the app to remain independent of specific analytics
 * providers (e.g., Firebase Analytics). Implementations can be swapped without
 * affecting the rest of the codebase.
 */
interface AnalyticsService {
    /**
     * Logs a screen view event when a user navigates to a screen.
     *
     * @param screenName Name of the screen (should match Screen class simple name)
     * @param screenClass Fully qualified class name of the screen
     * @param parameters Additional context about the screen
     */
    fun logScreenView(
        screenName: String,
        screenClass: String = "",
        parameters: Map<String, Any> = emptyMap(),
    )

    /**
     * Logs a custom event with optional parameters.
     *
     * @param eventName Name of the event (use predefined constants from [AnalyticsEvent])
     * @param parameters Event parameters (use [AnalyticsParam] for keys)
     */
    fun logEvent(
        eventName: String,
        parameters: Map<String, Any> = emptyMap(),
    )

    /**
     * Sets a user property for analytics segmentation.
     * User properties persist across sessions.
     *
     * @param propertyName Name of the property (use [UserProperty] constants)
     * @param value Property value
     */
    fun setUserProperty(
        propertyName: String,
        value: String,
    )

    /**
     * Logs an error/exception event for crash analytics.
     *
     * @param error The throwable/exception to log
     * @param context Description of where/why the error occurred
     * @param isFatal Whether the error caused app termination
     */
    fun logError(
        error: Throwable,
        context: String,
        isFatal: Boolean = false,
    )

    /**
     * Sets whether analytics collection is enabled.
     * Respects user privacy preferences.
     */
    fun setAnalyticsEnabled(enabled: Boolean)
}
