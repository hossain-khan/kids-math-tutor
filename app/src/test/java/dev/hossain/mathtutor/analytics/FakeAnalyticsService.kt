package dev.hossain.mathtutor.analytics

/**
 * Fake implementation of [AnalyticsService] for testing.
 * Records all analytics calls for verification in tests.
 *
 * This implementation maintains lists of all logged events that can be
 * queried and asserted in unit tests. It's useful for verifying that
 * the correct analytics events are being tracked without actually sending
 * data to Firebase.
 *
 * Example usage in tests:
 * ```
 * val analyticsService = FakeAnalyticsService()
 * val presenter = MyPresenter(analyticsService, ...)
 *
 * // Trigger some action
 * presenter.present()
 *
 * // Verify analytics were logged
 * assertThat(analyticsService.screenViews).hasSize(1)
 * assertThat(analyticsService.screenViews.first().screenName).isEqualTo("My Screen")
 * ```
 */
class FakeAnalyticsService : AnalyticsService {
    /**
     * Represents a screen view event.
     */
    data class ScreenViewEvent(
        val screenName: String,
        val screenClass: String,
        val parameters: Map<String, Any>,
    )

    /**
     * Represents a logged event.
     */
    data class LoggedEvent(
        val eventName: String,
        val parameters: Map<String, Any>,
    )

    /**
     * Represents a user property change.
     */
    data class UserPropertySet(
        val propertyName: String,
        val value: String,
    )

    /**
     * Represents an error logged.
     */
    data class ErrorLogged(
        val error: Throwable,
        val context: String,
        val isFatal: Boolean,
    )

    // Recorded events for verification
    val screenViews = mutableListOf<ScreenViewEvent>()
    val events = mutableListOf<LoggedEvent>()
    val userProperties = mutableListOf<UserPropertySet>()
    val errors = mutableListOf<ErrorLogged>()
    
    private var _analyticsEnabled = true
    val analyticsEnabled: Boolean get() = _analyticsEnabled

    override fun logScreenView(
        screenName: String,
        screenClass: String,
        parameters: Map<String, Any>,
    ) {
        screenViews.add(ScreenViewEvent(screenName, screenClass, parameters))
    }

    override fun logEvent(
        eventName: String,
        parameters: Map<String, Any>,
    ) {
        events.add(LoggedEvent(eventName, parameters))
    }

    override fun setUserProperty(
        propertyName: String,
        value: String,
    ) {
        userProperties.add(UserPropertySet(propertyName, value))
    }

    override fun logError(
        error: Throwable,
        context: String,
        isFatal: Boolean,
    ) {
        errors.add(ErrorLogged(error, context, isFatal))
    }

    override fun setAnalyticsEnabled(enabled: Boolean) {
        _analyticsEnabled = enabled
    }

    /**
     * Clears all recorded events. Useful for resetting state between tests.
     */
    fun clear() {
        screenViews.clear()
        events.clear()
        userProperties.clear()
        errors.clear()
        _analyticsEnabled = true
    }

    /**
     * Gets all screen views for a specific screen name.
     */
    fun getScreenViewsForScreen(screenName: String): List<ScreenViewEvent> = screenViews.filter { it.screenName == screenName }

    /**
     * Gets all events with a specific event name.
     */
    fun getEventsWithName(eventName: String): List<LoggedEvent> = events.filter { it.eventName == eventName }

    /**
     * Gets the most recent screen view event, or null if none recorded.
     */
    fun getLastScreenView(): ScreenViewEvent? = screenViews.lastOrNull()

    /**
     * Gets the most recent event, or null if none recorded.
     */
    fun getLastEvent(): LoggedEvent? = events.lastOrNull()

    /**
     * Gets the most recent user property set, or null if none recorded.
     */
    fun getLastUserProperty(): UserPropertySet? = userProperties.lastOrNull()

    /**
     * Gets the most recent error logged, or null if none recorded.
     */
    fun getLastError(): ErrorLogged? = errors.lastOrNull()
}
