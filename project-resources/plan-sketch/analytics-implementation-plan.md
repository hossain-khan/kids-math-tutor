# Analytics Implementation Plan

**Created**: December 20, 2025  
**Status**: 🔴 Not Started  
**Priority**: Medium  
**Estimated Duration**: 3-4 days

---

## Overview

This plan outlines the implementation of an analytics system for the Kids Math Pup Tutor app to track user engagement, screen views, and key activities. The implementation will follow a **clean architecture pattern** with an abstraction layer to avoid direct Firebase Analytics dependencies throughout the codebase.

**Key Principles**:
- **Abstraction**: Use interfaces to decouple analytics implementation from business logic
- **Circuit Integration**: Leverage Circuit's `ImpressionEffect` and `LaunchedImpressionEffect` for screen tracking
- **Privacy-First**: Only collect non-PII data, respect user consent
- **Testability**: Easy to mock and test without Firebase dependencies
- **Flexibility**: Easy to switch or add multiple analytics providers

---

## Goals

1. ✅ Track screen views (all 13 Circuit screens)
2. ✅ Track key user actions (button clicks, problem completion, badge unlocks)
3. ✅ Track engagement metrics (session duration, problem solve time)
4. ✅ Track game performance (Math Race scores, attempts)
5. ✅ Abstract Firebase Analytics behind interface
6. ✅ Make analytics testable with fake implementations
7. ✅ Integrate seamlessly with Circuit UDF architecture

---

## Architecture Design

### Layer Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                       Presentation Layer                        │
│  (Circuit Presenters, UI Composables)                           │
│  - Use AnalyticsService interface                               │
│  - Track impressions via ImpressionEffect                       │
│  - Track events via eventSink                                   │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Depends on Interface
┌──────────────────────────▼──────────────────────────────────────┐
│                       Domain Layer                              │
│  AnalyticsService interface                                     │
│  - logScreenView(screenName: String)                            │
│  - logEvent(eventName: String, params: Map<String, Any>)        │
│  - setUserProperty(name: String, value: String)                 │
│  - logError(error: Throwable, context: String)                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Implemented by
┌──────────────────────────▼──────────────────────────────────────┐
│                       Data Layer                                │
│  FirebaseAnalyticsService (Implementation)                      │
│  - Firebase Analytics SDK                                       │
│  - Event name mapping                                           │
│  - Parameter validation                                         │
└─────────────────────────────────────────────────────────────────┘
```

### Alternative Implementations

```
AnalyticsService (Interface)
     │
     ├──► FirebaseAnalyticsService (Production)
     ├──► LoggingAnalyticsService (Debug/Testing)
     └──► NoOpAnalyticsService (Testing)
```

---

## Implementation Steps

### Step 1: Define Analytics Service Interface (Day 1 Morning)

**Location**: `app/src/main/java/dev/hossain/mathtutor/analytics/AnalyticsService.kt`

```kotlin
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
        parameters: Map<String, Any> = emptyMap()
    )
    
    /**
     * Logs a custom event with optional parameters.
     * 
     * @param eventName Name of the event (use predefined constants from [AnalyticsEvent])
     * @param parameters Event parameters (use [AnalyticsParam] for keys)
     */
    fun logEvent(eventName: String, parameters: Map<String, Any> = emptyMap())
    
    /**
     * Sets a user property for analytics segmentation.
     * User properties persist across sessions.
     * 
     * @param propertyName Name of the property (use [UserProperty] constants)
     * @param value Property value
     */
    fun setUserProperty(propertyName: String, value: String)
    
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
        isFatal: Boolean = false
    )
    
    /**
     * Sets whether analytics collection is enabled.
     * Respects user privacy preferences.
     */
    fun setAnalyticsEnabled(enabled: Boolean)
}
```

**Constants File**: `app/src/main/java/dev/hossain/mathtutor/analytics/AnalyticsConstants.kt`

```kotlin
package dev.hossain.mathtutor.analytics

/**
 * Predefined analytics event names.
 * Following Firebase Analytics naming conventions: lowercase with underscores.
 */
object AnalyticsEvent {
    // Screen events (automatically logged)
    const val SCREEN_VIEW = "screen_view"
    
    // Onboarding events
    const val ONBOARDING_STARTED = "onboarding_started"
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    const val GRADE_SELECTED = "grade_selected"
    const val NAME_ENTERED = "name_entered"
    
    // Practice events
    const val PRACTICE_SESSION_STARTED = "practice_session_started"
    const val PRACTICE_SESSION_COMPLETED = "practice_session_completed"
    const val PROBLEM_ANSWERED = "problem_answered"
    const val PROBLEM_CORRECT = "problem_correct"
    const val PROBLEM_INCORRECT = "problem_incorrect"
    
    // Operation selection
    const val OPERATION_SELECTED = "operation_selected"
    const val MIXED_OPERATIONS_SELECTED = "mixed_operations_selected"
    
    // Badge events
    const val BADGE_UNLOCKED = "badge_unlocked"
    const val BADGES_VIEWED = "badges_viewed"
    
    // Game events
    const val GAME_STARTED = "game_started"
    const val GAME_COMPLETED = "game_completed"
    const val GAME_HIGH_SCORE = "game_high_score"
    
    // Settings events
    const val SETTINGS_CHANGED = "settings_changed"
    const val AUDIO_TOGGLED = "audio_toggled"
    const val HAPTICS_TOGGLED = "haptics_toggled"
    
    // Error events
    const val ERROR_OCCURRED = "error_occurred"
}

/**
 * Predefined analytics parameter keys.
 */
object AnalyticsParam {
    // Screen parameters
    const val SCREEN_NAME = "screen_name"
    const val SCREEN_CLASS = "screen_class"
    
    // User parameters
    const val GRADE_LEVEL = "grade_level"
    const val USER_NAME = "user_name"
    
    // Practice parameters
    const val OPERATION_TYPE = "operation_type"
    const val PROBLEM_COUNT = "problem_count"
    const val CORRECT_ANSWERS = "correct_answers"
    const val ACCURACY = "accuracy"
    const val SESSION_DURATION = "session_duration"
    const val SOLVE_TIME = "solve_time"
    
    // Badge parameters
    const val BADGE_ID = "badge_id"
    const val BADGE_NAME = "badge_name"
    const val BADGE_CATEGORY = "badge_category"
    
    // Game parameters
    const val GAME_ID = "game_id"
    const val GAME_SCORE = "game_score"
    const val GAME_DURATION = "game_duration"
    const val IS_NEW_RECORD = "is_new_record"
    
    // Settings parameters
    const val SETTING_NAME = "setting_name"
    const val SETTING_VALUE = "setting_value"
    
    // Error parameters
    const val ERROR_MESSAGE = "error_message"
    const val ERROR_CONTEXT = "error_context"
    const val IS_FATAL = "is_fatal"
}

/**
 * User property keys for analytics segmentation.
 */
object UserProperty {
    const val GRADE_LEVEL = "grade_level"
    const val HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
    const val TOTAL_PROBLEMS_SOLVED = "total_problems_solved"
    const val CURRENT_STREAK = "current_streak"
    const val TOTAL_BADGES_UNLOCKED = "total_badges_unlocked"
    const val GAMES_UNLOCKED = "games_unlocked"
}
```

---

### Step 2: Implement Firebase Analytics Service (Day 1 Afternoon)

**Location**: `app/src/main/java/dev/hossain/mathtutor/analytics/FirebaseAnalyticsService.kt`

```kotlin
package dev.hossain.mathtutor.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dev.hossain.mathtutor.di.ApplicationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import timber.log.Timber

/**
 * Firebase Analytics implementation of [AnalyticsService].
 * 
 * This is the production implementation that sends analytics data to Firebase.
 * Uses Metro DI with [ContributesBinding] to automatically provide this implementation
 * when [AnalyticsService] is injected.
 * 
 * @param context Application context for Firebase initialization
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class FirebaseAnalyticsService constructor(
    @ApplicationContext private val context: Context
) : AnalyticsService {
    
    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }
    
    override fun logScreenView(
        screenName: String,
        screenClass: String,
        parameters: Map<String, Any>
    ) {
        Timber.d("Analytics: Screen view - $screenName")
        
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass.ifEmpty { screenName })
            parameters.forEach { (key, value) ->
                putParameter(key, value)
            }
        }
        
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
    
    override fun logEvent(eventName: String, parameters: Map<String, Any>) {
        Timber.d("Analytics: Event - $eventName with ${parameters.size} parameters")
        
        val bundle = Bundle().apply {
            parameters.forEach { (key, value) ->
                putParameter(key, value)
            }
        }
        
        firebaseAnalytics.logEvent(eventName, bundle)
    }
    
    override fun setUserProperty(propertyName: String, value: String) {
        Timber.d("Analytics: User property - $propertyName = $value")
        firebaseAnalytics.setUserProperty(propertyName, value)
    }
    
    override fun logError(error: Throwable, context: String, isFatal: Boolean) {
        Timber.e(error, "Analytics: Error - $context (fatal=$isFatal)")
        
        logEvent(
            AnalyticsEvent.ERROR_OCCURRED,
            mapOf(
                AnalyticsParam.ERROR_MESSAGE to (error.message ?: "Unknown error"),
                AnalyticsParam.ERROR_CONTEXT to context,
                AnalyticsParam.IS_FATAL to isFatal
            )
        )
    }
    
    override fun setAnalyticsEnabled(enabled: Boolean) {
        Timber.d("Analytics: Collection enabled = $enabled")
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }
    
    /**
     * Helper to add parameter to Bundle based on type.
     */
    private fun Bundle.putParameter(key: String, value: Any) {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Double -> putDouble(key, value)
            is Boolean -> putBoolean(key, value)
            else -> putString(key, value.toString())
        }
    }
}
```

---

### Step 3: Create Test/Debug Implementations (Day 2 Morning)

**Location**: `app/src/test/java/dev/hossain/mathtutor/analytics/FakeAnalyticsService.kt`

```kotlin
package dev.hossain.mathtutor.analytics

/**
 * Fake implementation of [AnalyticsService] for testing.
 * Records all analytics calls for verification in tests.
 */
class FakeAnalyticsService : AnalyticsService {
    data class ScreenViewEvent(
        val screenName: String,
        val screenClass: String,
        val parameters: Map<String, Any>
    )
    
    data class LoggedEvent(
        val eventName: String,
        val parameters: Map<String, Any>
    )
    
    data class UserPropertySet(
        val propertyName: String,
        val value: String
    )
    
    data class ErrorLogged(
        val error: Throwable,
        val context: String,
        val isFatal: Boolean
    )
    
    // Recorded events for verification
    val screenViews = mutableListOf<ScreenViewEvent>()
    val events = mutableListOf<LoggedEvent>()
    val userProperties = mutableListOf<UserPropertySet>()
    val errors = mutableListOf<ErrorLogged>()
    var analyticsEnabled = true
    
    override fun logScreenView(
        screenName: String,
        screenClass: String,
        parameters: Map<String, Any>
    ) {
        screenViews.add(ScreenViewEvent(screenName, screenClass, parameters))
    }
    
    override fun logEvent(eventName: String, parameters: Map<String, Any>) {
        events.add(LoggedEvent(eventName, parameters))
    }
    
    override fun setUserProperty(propertyName: String, value: String) {
        userProperties.add(UserPropertySet(propertyName, value))
    }
    
    override fun logError(error: Throwable, context: String, isFatal: Boolean) {
        errors.add(ErrorLogged(error, context, isFatal))
    }
    
    override fun setAnalyticsEnabled(enabled: Boolean) {
        analyticsEnabled = enabled
    }
    
    // Helper methods for testing
    fun clear() {
        screenViews.clear()
        events.clear()
        userProperties.clear()
        errors.clear()
        analyticsEnabled = true
    }
    
    fun getScreenViewsForScreen(screenName: String): List<ScreenViewEvent> {
        return screenViews.filter { it.screenName == screenName }
    }
    
    fun getEventsWithName(eventName: String): List<LoggedEvent> {
        return events.filter { it.eventName == eventName }
    }
}
```

**Location**: `app/src/debug/java/dev/hossain/mathtutor/analytics/LoggingAnalyticsService.kt`

```kotlin
package dev.hossain.mathtutor.analytics

import timber.log.Timber

/**
 * Debug implementation of [AnalyticsService] that only logs to console.
 * Useful for development when you don't want to send data to Firebase.
 * 
 * To use this instead of Firebase in debug builds, create a module that
 * provides this binding instead of FirebaseAnalyticsService.
 */
class LoggingAnalyticsService : AnalyticsService {
    override fun logScreenView(
        screenName: String,
        screenClass: String,
        parameters: Map<String, Any>
    ) {
        Timber.tag("Analytics").d(
            "📊 Screen View: $screenName (class: $screenClass) ${formatParams(parameters)}"
        )
    }
    
    override fun logEvent(eventName: String, parameters: Map<String, Any>) {
        Timber.tag("Analytics").d(
            "📊 Event: $eventName ${formatParams(parameters)}"
        )
    }
    
    override fun setUserProperty(propertyName: String, value: String) {
        Timber.tag("Analytics").d("📊 User Property: $propertyName = $value")
    }
    
    override fun logError(error: Throwable, context: String, isFatal: Boolean) {
        Timber.tag("Analytics").e(
            error,
            "📊 Error: $context (fatal=$isFatal)"
        )
    }
    
    override fun setAnalyticsEnabled(enabled: Boolean) {
        Timber.tag("Analytics").d("📊 Analytics Enabled: $enabled")
    }
    
    private fun formatParams(parameters: Map<String, Any>): String {
        if (parameters.isEmpty()) return ""
        return parameters.entries.joinToString(
            prefix = "{ ",
            postfix = " }",
            separator = ", "
        ) { "${it.key}=${it.value}" }
    }
}
```

---

### Step 4: Integrate with Circuit Screens (Day 2 Afternoon - Day 3)

**Use Circuit's `LaunchedImpressionEffect` for screen tracking**

Example in `MathPracticePresenter.kt`:

```kotlin
@AssistedInject
class MathPracticePresenter constructor(
    @Assisted private val screen: MathPracticeScreen,
    @Assisted private val navigator: Navigator,
    private val problemGenerator: ProblemGenerator,
    private val sessionRepository: SessionRepository,
    private val analyticsService: AnalyticsService // <-- Inject
) : Presenter<MathPracticeScreen.State> {
    
    @Composable
    override fun present(): MathPracticeScreen.State {
        // Track screen view using Circuit's LaunchedImpressionEffect
        LaunchedImpressionEffect {
            analyticsService.logScreenView(
                screenName = "Math Practice",
                screenClass = MathPracticeScreen::class.java.name,
                parameters = mapOf(
                    AnalyticsParam.PROBLEM_COUNT to screen.problemCount,
                    AnalyticsParam.OPERATION_TYPE to screen.operation.name
                )
            )
        }
        
        // ... existing presenter logic
        
        return MathPracticeScreen.State(
            // ... state
        ) { event ->
            when (event) {
                is MathPracticeScreen.Event.CheckAnswer -> {
                    val isCorrect = checkAnswer()
                    
                    // Log analytics event
                    analyticsService.logEvent(
                        if (isCorrect) AnalyticsEvent.PROBLEM_CORRECT 
                        else AnalyticsEvent.PROBLEM_INCORRECT,
                        mapOf(
                            AnalyticsParam.OPERATION_TYPE to currentProblem.operation.name,
                            AnalyticsParam.SOLVE_TIME to problemSolveTime
                        )
                    )
                    
                    // ... rest of logic
                }
            }
        }
    }
}
```

**Apply to all 13 screens**:

| Screen | Screen Name | Key Events to Track |
|--------|-------------|---------------------|
| `OnboardingScreen` | "Onboarding" | `onboarding_started` |
| `GradeSelectionScreen` | "Grade Selection" | `grade_selected` |
| `NameEntryScreen` | "Name Entry" | `name_entered`, `onboarding_completed` |
| `HomeScreen` | "Home" | Screen view only |
| `OperationSelectorScreen` | "Operation Selector" | `operation_selected` |
| `MathPracticeScreen` | "Math Practice" | `practice_session_started`, `problem_answered` |
| `ResultsScreen` | "Practice Results" | `practice_session_completed` |
| `StatsScreen` | "Stats" | Screen view only |
| `BadgesScreen` | "Badges" | `badges_viewed` |
| `SettingsScreen` | "Settings" | `settings_changed` |
| `AudioHapticSettingsScreen` | "Audio & Haptic Settings" | `audio_toggled`, `haptics_toggled` |
| `GameSelectionScreen` | "Game Selection" | Screen view only |
| `MathRaceScreen` | "Math Race" | `game_started`, `game_completed` |

---

### Step 5: Track Key User Actions (Day 3)

**Badge Unlocks** in `BadgeRepositoryImpl.kt`:

```kotlin
override suspend fun unlockBadge(badgeId: String) {
    // ... existing logic
    
    val badge = badgeDao.getBadge(badgeId).first()
    badge?.let {
        analyticsService.logEvent(
            AnalyticsEvent.BADGE_UNLOCKED,
            mapOf(
                AnalyticsParam.BADGE_ID to badgeId,
                AnalyticsParam.BADGE_NAME to it.name,
                AnalyticsParam.BADGE_CATEGORY to it.category.name
            )
        )
    }
}
```

**Session Completion** in `SessionRepositoryImpl.kt`:

```kotlin
override suspend fun saveSession(session: PracticeSession): Long {
    val id = sessionDao.insertSession(session.toEntity())
    
    // Log analytics
    analyticsService.logEvent(
        AnalyticsEvent.PRACTICE_SESSION_COMPLETED,
        mapOf(
            AnalyticsParam.OPERATION_TYPE to session.operation.name,
            AnalyticsParam.PROBLEM_COUNT to session.problemsAttempted,
            AnalyticsParam.CORRECT_ANSWERS to session.correctAnswers,
            AnalyticsParam.ACCURACY to session.accuracy,
            AnalyticsParam.SESSION_DURATION to session.duration.inWholeSeconds
        )
    )
    
    return id
}
```

**Game Completion** in `MathRacePresenter.kt`:

```kotlin
// When game ends
analyticsService.logEvent(
    AnalyticsEvent.GAME_COMPLETED,
    mapOf(
        AnalyticsParam.GAME_ID to Game.MATH_RACE.id,
        AnalyticsParam.GAME_SCORE to finalScore,
        AnalyticsParam.CORRECT_ANSWERS to correctAnswers,
        AnalyticsParam.ACCURACY to accuracy,
        AnalyticsParam.GAME_DURATION to 60,
        AnalyticsParam.IS_NEW_RECORD to isNewRecord
    )
)

if (isNewRecord) {
    analyticsService.logEvent(
        AnalyticsEvent.GAME_HIGH_SCORE,
        mapOf(
            AnalyticsParam.GAME_ID to Game.MATH_RACE.id,
            AnalyticsParam.GAME_SCORE to finalScore
        )
    )
}
```

---

### Step 6: Track User Properties (Day 3)

**Update user properties when profile changes** in `UserProfileRepositoryImpl.kt`:

```kotlin
override suspend fun saveProfile(profile: UserProfile) {
    // ... existing logic
    
    // Update analytics user properties
    analyticsService.setUserProperty(
        UserProperty.GRADE_LEVEL,
        profile.gradeLevel.name
    )
    analyticsService.setUserProperty(
        UserProperty.HAS_COMPLETED_ONBOARDING,
        profile.hasCompletedOnboarding.toString()
    )
}
```

**Update totals periodically** (e.g., in `HomePresenter.kt`):

```kotlin
LaunchedEffect(Unit) {
    sessionRepository.getOverallStats().collect { stats ->
        analyticsService.setUserProperty(
            UserProperty.TOTAL_PROBLEMS_SOLVED,
            stats.totalProblems.toString()
        )
    }
}
```

---

### Step 7: Add Dependencies (Day 4)

**Update `gradle/libs.versions.toml`**:

```toml
[versions]
# ... existing versions
firebase-bom = "34.7.0"

[libraries]
# ... existing libraries
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebase-bom" }
firebase-analytics = { module = "com.google.firebase:firebase-analytics" }
```

**Update `app/build.gradle.kts`**:

```kotlin
dependencies {
    // ... existing dependencies
    
    // Firebase Analytics
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
}
```

**Update CircuitX dependencies** (if not already present):

```toml
[libraries]
circuit-effects = { module = "com.slack.circuit:circuitx-effects", version.ref = "circuit" }
```

```kotlin
dependencies {
    implementation(libs.circuit.effects)
}
```

---

### Step 8: Testing (Day 4)

**Unit Test Example** - `AnalyticsServiceTest.kt`:

```kotlin
class AnalyticsServiceTest {
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
            parameters = mapOf("key" to "value")
        )
        
        assertThat(analyticsService.screenViews).hasSize(1)
        val screenView = analyticsService.screenViews.first()
        assertThat(screenView.screenName).isEqualTo("Test Screen")
        assertThat(screenView.parameters["key"]).isEqualTo("value")
    }
    
    @Test
    fun `logEvent records custom event`() {
        analyticsService.logEvent(
            AnalyticsEvent.BADGE_UNLOCKED,
            mapOf(AnalyticsParam.BADGE_ID to "first_problem")
        )
        
        val events = analyticsService.getEventsWithName(AnalyticsEvent.BADGE_UNLOCKED)
        assertThat(events).hasSize(1)
        assertThat(events.first().parameters[AnalyticsParam.BADGE_ID])
            .isEqualTo("first_problem")
    }
    
    @Test
    fun `setAnalyticsEnabled changes enabled state`() {
        analyticsService.setAnalyticsEnabled(false)
        assertThat(analyticsService.analyticsEnabled).isFalse()
        
        analyticsService.setAnalyticsEnabled(true)
        assertThat(analyticsService.analyticsEnabled).isTrue()
    }
}
```

**Presenter Test Example**:

```kotlin
@Test
fun `presenter logs screen view on composition`() = runTest {
    val analyticsService = FakeAnalyticsService()
    val presenter = MathPracticePresenter(
        screen = MathPracticeScreen(),
        navigator = FakeNavigator(),
        problemGenerator = FakeProblemGenerator(),
        sessionRepository = FakeSessionRepository(),
        analyticsService = analyticsService
    )
    
    // Present the screen
    val state = presenter.present()
    
    // Verify screen view was logged
    assertThat(analyticsService.screenViews).hasSize(1)
    assertThat(analyticsService.screenViews.first().screenName)
        .isEqualTo("Math Practice")
}
```

---

## Privacy Considerations

### User Consent

Add analytics consent preference to `UserPreferencesRepository`:

```kotlin
data class UserPreferences(
    // ... existing preferences
    val analyticsEnabled: Boolean = true // Opt-out by default
)
```

Add toggle in `SettingsScreen`:

```kotlin
SwitchRow(
    label = "Analytics",
    description = "Help improve the app by sharing usage data",
    checked = preferences.analyticsEnabled,
    onCheckedChange = { enabled ->
        analyticsService.setAnalyticsEnabled(enabled)
        // Save preference
    }
)
```

### Data Collected

**Screen Views**:
- Screen name
- Screen parameters (problem count, operation type)
- Timestamp (automatic)

**User Actions**:
- Problem attempts (correct/incorrect, solve time)
- Badge unlocks (badge name, category)
- Session completions (duration, accuracy)
- Game sessions (score, duration)

**User Properties**:
- Grade level
- Total problems solved
- Current streak
- Total badges unlocked

**NOT Collected** (Privacy-safe):
- ❌ User's entered name
- ❌ Exact locations
- ❌ Device identifiers beyond Firebase anonymous ID
- ❌ Any personally identifiable information (PII)

---

## Benefits

### For Development
- ✅ Understand which screens users visit most
- ✅ Identify drop-off points in user journey
- ✅ Track feature adoption (games, badges, operations)
- ✅ Monitor crash/error rates
- ✅ A/B test different UX approaches

### For Product Decisions
- ✅ Which math operations are most popular?
- ✅ What grade level are most users?
- ✅ How long are typical practice sessions?
- ✅ Which badges motivate users most?
- ✅ Are games increasing engagement?

### For Users
- ✅ Better app through data-driven improvements
- ✅ Bug fixes prioritized by impact
- ✅ Features users actually want
- ✅ Privacy-respecting (opt-out available)

---

## Migration Checklist

### Day 1
- [x] Define `AnalyticsService` interface
- [x] Define `AnalyticsEvent`, `AnalyticsParam`, `UserProperty` constants
- [x] Implement `FirebaseAnalyticsService`
- [x] Add Metro DI binding

### Day 2
- [x] Create `FakeAnalyticsService` for testing
- [x] Create `LoggingAnalyticsService` for debug
- [x] Add `LaunchedImpressionEffect` to first 3 screens
- [x] Test screen view tracking

### Day 3
- [x] Add screen tracking to remaining 10 screens
- [x] Track badge unlock events
- [x] Track session completion events
- [x] Track game events
- [x] Update user properties on profile changes

### Day 4
- [x] Add Firebase Analytics dependencies
- [x] Write unit tests for analytics service
- [x] Write presenter tests with analytics
- [x] Add analytics consent toggle to settings
- [x] Test end-to-end analytics flow
- [x] Update documentation

---

## Testing Strategy

### Unit Tests
- ✅ Test `AnalyticsService` interface with fake
- ✅ Test event parameter mapping
- ✅ Test user property updates
- ✅ Test analytics enabled/disabled state

### Integration Tests
- ✅ Test screen view tracking in Circuit presenters
- ✅ Test event logging from repositories
- ✅ Test user property updates flow

### Manual Testing
- ✅ Verify events appear in Firebase Console (debug view)
- ✅ Check screen flow tracking
- ✅ Validate parameter data accuracy
- ✅ Test opt-out functionality

---

## Firebase Console Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select Kids Math Pup Tutor project
3. Navigate to **Analytics** → **Events**
4. Enable **DebugView** for testing:
   - `adb shell setprop debug.firebase.analytics.app dev.hossain.mathtutor`
5. View real-time events in DebugView
6. Create custom dashboard with key metrics:
   - Daily active users
   - Screen views by screen
   - Session duration
   - Problem completion rate
   - Badge unlock rate
   - Game play frequency

---

## Future Enhancements

### Phase 2 (Future)
- A/B testing framework integration
- Remote Config for dynamic feature flags
- Crash analytics with Crashlytics (already added)
- Performance monitoring with Firebase Performance
- User cohort analysis
- Funnel analysis for onboarding flow
- Custom audience creation for targeted features

### Advanced Analytics
- Predictive analytics (user churn prediction)
- Machine learning insights
- Automated anomaly detection
- Cross-platform analytics (if iOS version developed)

---

## Definition of Done

- ✅ `AnalyticsService` interface defined
- ✅ `FirebaseAnalyticsService` implemented with Metro DI
- ✅ `FakeAnalyticsService` created for testing
- ✅ All 13 screens track screen views
- ✅ Key events tracked (badges, sessions, games)
- ✅ User properties tracked (grade, totals, streak)
- ✅ Unit tests passing
- ✅ Integration tests passing
- ✅ Analytics consent toggle in settings
- ✅ Privacy policy updated (if applicable)
- ✅ Firebase Console dashboard configured
- ✅ Code reviewed and formatted (`./gradlew formatKotlin`)
- ✅ Documentation updated

---

## Resources

- [Circuit Effects Documentation](https://slackhq.github.io/circuit/circuitx/effects/)
- [Firebase Analytics Documentation](https://firebase.google.com/docs/analytics)
- [Firebase Analytics Best Practices](https://firebase.google.com/docs/analytics/best-practices)
- [Metro Dependency Injection](https://zacsweers.github.io/metro/)
- [Android Privacy Guidelines](https://developer.android.com/privacy)

---

*Document created: December 20, 2025*  
*Status: 🔴 Not Started*  
*Estimated completion: 3-4 days*
