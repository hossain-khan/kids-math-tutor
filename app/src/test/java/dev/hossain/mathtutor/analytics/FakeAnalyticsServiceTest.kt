package dev.hossain.mathtutor.analytics

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [FakeAnalyticsService].
 */
class FakeAnalyticsServiceTest {
    private lateinit var analyticsService: FakeAnalyticsService

    @Before
    fun setup() {
        analyticsService = FakeAnalyticsService()
    }

    @Test
    fun `logScreenView records screen view event`() {
        analyticsService.logScreenView(
            screenName = "Test Screen",
            screenClass = "TestScreen",
            parameters = mapOf("key" to "value"),
        )

        assertThat(analyticsService.screenViews).hasSize(1)
        val screenView = analyticsService.screenViews.first()
        assertThat(screenView.screenName).isEqualTo("Test Screen")
        assertThat(screenView.screenClass).isEqualTo("TestScreen")
        assertThat(screenView.parameters["key"]).isEqualTo("value")
    }

    @Test
    fun `logScreenView records multiple screen views`() {
        analyticsService.logScreenView("Screen 1", "Screen1")
        analyticsService.logScreenView("Screen 2", "Screen2")
        analyticsService.logScreenView("Screen 3", "Screen3")

        assertThat(analyticsService.screenViews).hasSize(3)
        assertThat(analyticsService.screenViews[0].screenName).isEqualTo("Screen 1")
        assertThat(analyticsService.screenViews[1].screenName).isEqualTo("Screen 2")
        assertThat(analyticsService.screenViews[2].screenName).isEqualTo("Screen 3")
    }

    @Test
    fun `getScreenViewsForScreen filters by screen name`() {
        analyticsService.logScreenView("Home", "HomeScreen")
        analyticsService.logScreenView("Settings", "SettingsScreen")
        analyticsService.logScreenView("Home", "HomeScreen")

        val homeViews = analyticsService.getScreenViewsForScreen("Home")
        assertThat(homeViews).hasSize(2)
        assertThat(homeViews.all { it.screenName == "Home" }).isTrue()
    }

    @Test
    fun `logEvent records custom event`() {
        analyticsService.logEvent(
            AnalyticsEvent.BADGE_UNLOCKED,
            mapOf(AnalyticsParam.BADGE_ID to "first_problem"),
        )

        assertThat(analyticsService.events).hasSize(1)
        val event = analyticsService.events.first()
        assertThat(event.eventName).isEqualTo(AnalyticsEvent.BADGE_UNLOCKED)
        assertThat(event.parameters[AnalyticsParam.BADGE_ID]).isEqualTo("first_problem")
    }

    @Test
    fun `logEvent records multiple events`() {
        analyticsService.logEvent(AnalyticsEvent.PROBLEM_CORRECT, emptyMap())
        analyticsService.logEvent(AnalyticsEvent.PROBLEM_INCORRECT, emptyMap())
        analyticsService.logEvent(AnalyticsEvent.BADGE_UNLOCKED, emptyMap())

        assertThat(analyticsService.events).hasSize(3)
    }

    @Test
    fun `getEventsWithName filters by event name`() {
        analyticsService.logEvent(AnalyticsEvent.PROBLEM_CORRECT, emptyMap())
        analyticsService.logEvent(AnalyticsEvent.PROBLEM_INCORRECT, emptyMap())
        analyticsService.logEvent(AnalyticsEvent.PROBLEM_CORRECT, emptyMap())

        val correctEvents = analyticsService.getEventsWithName(AnalyticsEvent.PROBLEM_CORRECT)
        assertThat(correctEvents).hasSize(2)
        assertThat(correctEvents.all { it.eventName == AnalyticsEvent.PROBLEM_CORRECT }).isTrue()
    }

    @Test
    fun `setUserProperty records user property`() {
        analyticsService.setUserProperty(
            UserProperty.GRADE_LEVEL,
            "KINDERGARTEN",
        )

        assertThat(analyticsService.userProperties).hasSize(1)
        val property = analyticsService.userProperties.first()
        assertThat(property.propertyName).isEqualTo(UserProperty.GRADE_LEVEL)
        assertThat(property.value).isEqualTo("KINDERGARTEN")
    }

    @Test
    fun `setUserProperty records multiple properties`() {
        analyticsService.setUserProperty(UserProperty.GRADE_LEVEL, "KINDERGARTEN")
        analyticsService.setUserProperty(UserProperty.TOTAL_PROBLEMS_SOLVED, "100")
        analyticsService.setUserProperty(UserProperty.CURRENT_STREAK, "5")

        assertThat(analyticsService.userProperties).hasSize(3)
    }

    @Test
    fun `logError records error event`() {
        val exception = RuntimeException("Test error")
        analyticsService.logError(
            error = exception,
            context = "Test context",
            isFatal = false,
        )

        assertThat(analyticsService.errors).hasSize(1)
        val error = analyticsService.errors.first()
        assertThat(error.error).isEqualTo(exception)
        assertThat(error.context).isEqualTo("Test context")
        assertThat(error.isFatal).isFalse()
    }

    @Test
    fun `logError records fatal error`() {
        val exception = RuntimeException("Fatal error")
        analyticsService.logError(
            error = exception,
            context = "Fatal context",
            isFatal = true,
        )

        assertThat(analyticsService.errors).hasSize(1)
        val error = analyticsService.errors.first()
        assertThat(error.isFatal).isTrue()
    }

    @Test
    fun `setAnalyticsEnabled changes enabled state`() {
        assertThat(analyticsService.analyticsEnabled).isTrue()

        analyticsService.setAnalyticsEnabled(false)
        assertThat(analyticsService.analyticsEnabled).isFalse()

        analyticsService.setAnalyticsEnabled(true)
        assertThat(analyticsService.analyticsEnabled).isTrue()
    }

    @Test
    fun `clear removes all recorded events`() {
        analyticsService.logScreenView("Screen", "Screen")
        analyticsService.logEvent("event", emptyMap())
        analyticsService.setUserProperty("property", "value")
        analyticsService.logError(RuntimeException(), "context")

        assertThat(analyticsService.screenViews).isNotEmpty()
        assertThat(analyticsService.events).isNotEmpty()
        assertThat(analyticsService.userProperties).isNotEmpty()
        assertThat(analyticsService.errors).isNotEmpty()

        analyticsService.clear()

        assertThat(analyticsService.screenViews).isEmpty()
        assertThat(analyticsService.events).isEmpty()
        assertThat(analyticsService.userProperties).isEmpty()
        assertThat(analyticsService.errors).isEmpty()
        assertThat(analyticsService.analyticsEnabled).isTrue()
    }

    @Test
    fun `getLastScreenView returns most recent screen view`() {
        analyticsService.logScreenView("Screen 1", "Screen1")
        analyticsService.logScreenView("Screen 2", "Screen2")

        val lastView = analyticsService.getLastScreenView()
        assertThat(lastView?.screenName).isEqualTo("Screen 2")
    }

    @Test
    fun `getLastScreenView returns null when no screen views`() {
        assertThat(analyticsService.getLastScreenView()).isNull()
    }

    @Test
    fun `getLastEvent returns most recent event`() {
        analyticsService.logEvent("event1", emptyMap())
        analyticsService.logEvent("event2", emptyMap())

        val lastEvent = analyticsService.getLastEvent()
        assertThat(lastEvent?.eventName).isEqualTo("event2")
    }

    @Test
    fun `getLastEvent returns null when no events`() {
        assertThat(analyticsService.getLastEvent()).isNull()
    }

    @Test
    fun `getLastUserProperty returns most recent property`() {
        analyticsService.setUserProperty("prop1", "value1")
        analyticsService.setUserProperty("prop2", "value2")

        val lastProperty = analyticsService.getLastUserProperty()
        assertThat(lastProperty?.propertyName).isEqualTo("prop2")
        assertThat(lastProperty?.value).isEqualTo("value2")
    }

    @Test
    fun `getLastUserProperty returns null when no properties`() {
        assertThat(analyticsService.getLastUserProperty()).isNull()
    }

    @Test
    fun `getLastError returns most recent error`() {
        analyticsService.logError(RuntimeException("Error 1"), "Context 1")
        analyticsService.logError(RuntimeException("Error 2"), "Context 2")

        val lastError = analyticsService.getLastError()
        assertThat(lastError?.error?.message).isEqualTo("Error 2")
        assertThat(lastError?.context).isEqualTo("Context 2")
    }

    @Test
    fun `getLastError returns null when no errors`() {
        assertThat(analyticsService.getLastError()).isNull()
    }

    @Test
    fun `event parameters support multiple types`() {
        analyticsService.logEvent(
            "test_event",
            mapOf(
                "string_param" to "value",
                "int_param" to 42,
                "long_param" to 100L,
                "float_param" to 3.14f,
                "double_param" to 2.718,
                "boolean_param" to true,
            ),
        )

        val event = analyticsService.getLastEvent()!!
        assertThat(event.parameters["string_param"]).isEqualTo("value")
        assertThat(event.parameters["int_param"]).isEqualTo(42)
        assertThat(event.parameters["long_param"]).isEqualTo(100L)
        assertThat(event.parameters["float_param"]).isEqualTo(3.14f)
        assertThat(event.parameters["double_param"]).isEqualTo(2.718)
        assertThat(event.parameters["boolean_param"]).isEqualTo(true)
    }
}
