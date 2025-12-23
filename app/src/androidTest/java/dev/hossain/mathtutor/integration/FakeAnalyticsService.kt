package dev.hossain.mathtutor.integration

import dev.hossain.mathtutor.analytics.AnalyticsService

/**
 * Fake implementation of AnalyticsService for testing.
 * Records analytics calls without sending them to actual analytics backend.
 */
class FakeAnalyticsService : AnalyticsService {
    val loggedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
    val loggedScreenViews = mutableListOf<Triple<String, String, Map<String, Any>>>()
    val setUserProperties = mutableMapOf<String, String>()
    val loggedErrors = mutableListOf<Triple<Throwable, String, Boolean>>()
    private var _analyticsEnabled: Boolean = true
    val analyticsEnabled: Boolean get() = _analyticsEnabled

    override fun logScreenView(
        screenName: String,
        screenClass: String,
        parameters: Map<String, Any>,
    ) {
        loggedScreenViews.add(Triple(screenName, screenClass, parameters))
    }

    override fun logEvent(
        eventName: String,
        parameters: Map<String, Any>,
    ) {
        loggedEvents.add(eventName to parameters)
    }

    override fun setUserProperty(
        propertyName: String,
        value: String,
    ) {
        setUserProperties[propertyName] = value
    }

    override fun logError(
        error: Throwable,
        context: String,
        isFatal: Boolean,
    ) {
        loggedErrors.add(Triple(error, context, isFatal))
    }

    override fun setAnalyticsEnabled(enabled: Boolean) {
        _analyticsEnabled = enabled
    }

    fun reset() {
        loggedEvents.clear()
        loggedScreenViews.clear()
        setUserProperties.clear()
        loggedErrors.clear()
        _analyticsEnabled = true
    }
}
