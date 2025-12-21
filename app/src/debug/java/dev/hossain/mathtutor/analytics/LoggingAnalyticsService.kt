package dev.hossain.mathtutor.analytics

import timber.log.Timber

/**
 * Debug implementation of [AnalyticsService] that only logs to console.
 * Useful for development when you don't want to send data to Firebase.
 *
 * This implementation logs all analytics calls using Timber with a consistent
 * format and emoji prefix for easy identification in logcat.
 *
 * To use this instead of Firebase in debug builds, you can create a debug-specific
 * DI module that provides this binding instead of [FirebaseAnalyticsService].
 *
 * Example logcat output:
 * ```
 * D/Analytics: 📊 Screen View: Math Practice (class: MathPracticeScreen) { problem_count=10, operation_type=ADDITION }
 * D/Analytics: 📊 Event: problem_correct { operation_type=ADDITION, solve_time=3.5 }
 * D/Analytics: 📊 User Property: grade_level = KINDERGARTEN
 * ```
 */
class LoggingAnalyticsService : AnalyticsService {
    override fun logScreenView(
        screenName: String,
        screenClass: String,
        parameters: Map<String, Any>,
    ) {
        Timber.tag("Analytics").d(
            "📊 Screen View: $screenName (class: $screenClass) ${formatParams(parameters)}",
        )
    }

    override fun logEvent(
        eventName: String,
        parameters: Map<String, Any>,
    ) {
        Timber.tag("Analytics").d(
            "📊 Event: $eventName ${formatParams(parameters)}",
        )
    }

    override fun setUserProperty(
        propertyName: String,
        value: String,
    ) {
        Timber.tag("Analytics").d("📊 User Property: $propertyName = $value")
    }

    override fun logError(
        error: Throwable,
        context: String,
        isFatal: Boolean,
    ) {
        Timber.tag("Analytics").e(
            error,
            "📊 Error: $context (fatal=$isFatal)",
        )
    }

    override fun setAnalyticsEnabled(enabled: Boolean) {
        Timber.tag("Analytics").d("📊 Analytics Enabled: $enabled")
    }

    /**
     * Formats parameter map for readable logging.
     */
    private fun formatParams(parameters: Map<String, Any>): String {
        if (parameters.isEmpty()) return ""
        return parameters.entries.joinToString(
            prefix = "{ ",
            postfix = " }",
            separator = ", ",
        ) { "${it.key}=${it.value}" }
    }
}
