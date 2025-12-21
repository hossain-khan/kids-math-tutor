# Analytics Documentation

**Version**: 1.0  
**Last Updated**: December 21, 2025  
**Status**: ✅ Implemented

---

## Overview

Kids Math Pup Tutor uses Firebase Analytics to track user engagement and improve the learning experience. The analytics system is built with a **privacy-first approach** and follows **clean architecture principles** with an abstraction layer that decouples the app from any specific analytics provider.

### Key Principles

- **Privacy-First**: Only non-PII data is collected
- **User Control**: Users can opt-out via Settings → Privacy
- **Abstraction**: Interface-based design for easy testing and provider swapping
- **Circuit Integration**: Seamless integration with Circuit UDF architecture
- **Testability**: Comprehensive fake implementations for unit tests

---

## Architecture

### Layer Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                       Presentation Layer                        │
│  (Circuit Presenters, UI Composables)                           │
│  - Use AnalyticsService interface                               │
│  - Track impressions via LaunchedImpressionEffect               │
│  - Track events via eventSink                                   │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Depends on Interface
┌──────────────────────────▼──────────────────────────────────────┐
│                       Analytics Interface                       │
│  AnalyticsService                                               │
│  - logScreenView(screenName, screenClass, parameters)           │
│  - logEvent(eventName, parameters)                              │
│  - setUserProperty(propertyName, value)                         │
│  - logError(error, context, isFatal)                            │
│  - setAnalyticsEnabled(enabled)                                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Implemented by
┌──────────────────────────▼──────────────────────────────────────┐
│                       Data Layer                                │
│  FirebaseAnalyticsService (Production)                          │
│  LoggingAnalyticsService (Debug)                                │
│  FakeAnalyticsService (Testing)                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation Classes

- **`AnalyticsService`** (`analytics/AnalyticsService.kt`)
  - Interface defining analytics contract
  - Keeps business logic independent of Firebase

- **`FirebaseAnalyticsService`** (`analytics/FirebaseAnalyticsService.kt`)
  - Production implementation using Firebase Analytics SDK
  - Observes user preferences for opt-out support
  - Bound via Metro DI with `@ContributesBinding`

- **`LoggingAnalyticsService`** (`debug/analytics/LoggingAnalyticsService.kt`)
  - Debug implementation that logs to console
  - Useful for local development without Firebase

- **`FakeAnalyticsService`** (`test/analytics/FakeAnalyticsService.kt`)
  - Test implementation that records events for verification
  - Used in unit tests to assert analytics calls

---

## What We Track

### Screen Views (13 Screens)

All Circuit screens automatically track screen views using `LaunchedImpressionEffect`:

1. **Onboarding Flow**
   - `OnboardingScreen` - Welcome screens
   - `GradeSelectionScreen` - Grade level selection
   - `NameEntryScreen` - Optional name entry

2. **Main App Screens**
   - `HomeScreen` - Main dashboard
   - `OperationSelectorScreen` - Math operation selection
   - `MathPracticeScreen` - Practice session
   - `PracticeResultsScreen` - Session results

3. **Progress Screens**
   - `StatsScreen` - Statistics and history
   - `BadgesScreen` - Badge collection

4. **Settings Screens**
   - `SettingsScreen` - Main settings
   - `AudioHapticSettingsScreen` - Audio/haptic preferences

5. **Game Screens**
   - `GameSelectionScreen` - Game hub
   - `MathRaceScreen` - Math Race mini-game

**Parameters Tracked**:
- `screen_name` - Human-readable screen name
- `screen_class` - Fully qualified class name
- Custom parameters (e.g., operation type, problem count)

### Events Tracked

#### Onboarding Events
- `onboarding_started` - User begins onboarding
- `onboarding_completed` - User finishes or skips onboarding
- `grade_selected` - User selects grade level
  - Params: `grade_level` (KINDERGARTEN, GRADE_1, GRADE_2)
- `name_entered` - User enters name
  - Params: `skipped` (true/false)

#### Practice Events
- `practice_session_started` - Practice session begins
  - Params: `operation_type`, `problem_count`
- `practice_session_completed` - Practice session finishes
  - Params: `operation_type`, `problem_count`, `correct_answers`, `accuracy`, `session_duration`
- `problem_answered` - Problem submitted
- `problem_correct` - Correct answer given
  - Params: `solve_time` (seconds)
- `problem_incorrect` - Incorrect answer given
- `operation_selected` - User picks operation
  - Params: `operation_type` (ADDITION, SUBTRACTION, etc.)

#### Badge Events
- `badge_unlocked` - Badge earned
  - Params: `badge_id`, `badge_name`, `badge_category`
- `badges_viewed` - User views badge screen

#### Game Events
- `game_started` - Game begins
  - Params: `game_id`
- `game_completed` - Game finishes
  - Params: `game_id`, `game_score`, `game_duration`, `accuracy`
- `game_high_score` - New personal best
  - Params: `game_id`, `game_score`, `is_new_record`

#### Settings Events
- `settings_changed` - Setting modified
  - Params: `setting_name`, `setting_value`
- `audio_toggled` - Audio enabled/disabled
- `haptics_toggled` - Haptics enabled/disabled

#### Error Events
- `error_occurred` - App error logged
  - Params: `error_message`, `error_context`, `is_fatal`

### User Properties

User properties persist across sessions and enable audience segmentation:

- `grade_level` - Current grade level (KINDERGARTEN, GRADE_1, GRADE_2)
- `has_completed_onboarding` - Onboarding status (true/false)
- `total_problems_solved` - Cumulative problems answered
- `current_streak` - Current daily practice streak (days)
- `total_badges_unlocked` - Number of badges earned
- `games_unlocked` - Number of games unlocked

**Updated By**:
- `HomePresenter` - Updates aggregate properties on app open
- `UserProfileRepositoryImpl` - Updates grade_level and onboarding status
- `SessionRepositoryImpl` - Updates total_problems_solved after session
- `BadgeRepositoryImpl` - Updates total_badges_unlocked after unlock

---

## How to Add New Analytics

### 1. Add Event or Parameter Constants

Update `analytics/AnalyticsConstants.kt`:

```kotlin
object AnalyticsEvent {
    const val MY_NEW_EVENT = "my_new_event"
}

object AnalyticsParam {
    const val MY_PARAM = "my_param"
}
```

### 2. Track Screen View

In your Circuit UI composable:

```kotlin
@CircuitInject(MyScreen::class, AppScope::class)
@Composable
fun MyScreenUi(state: MyScreen.State, modifier: Modifier = Modifier) {
    // Track screen view automatically when screen appears
    LaunchedImpressionEffect(state.analyticsService) {
        state.analyticsService.logScreenView(
            screenName = "My Screen",
            screenClass = MyScreen::class.qualifiedName ?: "MyScreen",
            parameters = mapOf(
                AnalyticsParam.MY_PARAM to state.myValue
            )
        )
    }
    
    // UI content...
}
```

### 3. Track Event

In your presenter or repository:

```kotlin
@AssistedInject
class MyPresenter constructor(
    @Assisted private val screen: MyScreen,
    @Assisted private val navigator: Navigator,
    private val analyticsService: AnalyticsService,
) : Presenter<MyScreen.State> {
    
    fun handleButtonClick() {
        // Track event
        analyticsService.logEvent(
            AnalyticsEvent.MY_NEW_EVENT,
            mapOf(
                AnalyticsParam.MY_PARAM to "my_value",
                AnalyticsParam.COUNT to 42
            )
        )
        
        // Business logic...
    }
}
```

### 4. Track User Property

Update long-lived user attributes:

```kotlin
analyticsService.setUserProperty(
    UserProperty.MY_PROPERTY,
    "my_value"
)
```

### 5. Add Tests

In your presenter or repository test:

```kotlin
@Test
fun `button click logs analytics event`() = runTest {
    val fakeAnalytics = FakeAnalyticsService()
    val presenter = MyPresenter(
        screen = MyScreen(),
        navigator = FakeNavigator(),
        analyticsService = fakeAnalytics
    )
    
    // Trigger action
    presenter.handleButtonClick()
    
    // Verify analytics
    val events = fakeAnalytics.getEventsWithName(AnalyticsEvent.MY_NEW_EVENT)
    assertThat(events).hasSize(1)
    assertThat(events.first().parameters[AnalyticsParam.MY_PARAM])
        .isEqualTo("my_value")
}
```

---

## Testing Analytics Locally

### Enable Firebase DebugView

1. Enable debug mode for your device:
   ```bash
   adb shell setprop debug.firebase.analytics.app dev.hossain.mathtutor
   ```

2. Open Firebase Console → Analytics → DebugView

3. Clear app data (optional for fresh start):
   ```bash
   adb shell pm clear dev.hossain.mathtutor
   ```

### Verify Events

1. Perform actions in the app
2. Check DebugView in real-time (events appear within seconds)
3. Verify event names, parameters, and user properties

### Disable Debug Mode

```bash
adb shell setprop debug.firebase.analytics.app .none.
```

---

## Firebase Console Access

### Dashboard Setup

**Custom Dashboard: "Kids Math Tutor - User Engagement"**

1. Navigate to: Firebase Console → Analytics → Custom Dashboards
2. Create dashboard with cards:
   - Daily Active Users (DAU)
   - Screen Views by Screen Name (Bar chart)
   - Practice Sessions Completed (Line chart)
   - Badge Unlocks (Total count)
   - Game Sessions (Count by game_id)
   - Average Session Duration
   - User Retention (Cohort analysis)

### Conversion Funnels

**Funnel 1: Onboarding Completion**
1. `onboarding_started`
2. `grade_selected`
3. `name_entered`
4. `onboarding_completed`

**Funnel 2: Practice Flow**
1. `screen_view: Operation Selector`
2. `operation_selected`
3. `practice_session_started`
4. `practice_session_completed`

### User Audiences

1. **Active Learners**: 10+ practice sessions
2. **Badge Collectors**: 5+ badges unlocked
3. **Game Players**: Completed a game
4. **Kindergarten Users**: `grade_level == KINDERGARTEN`
5. **Streak Champions**: `current_streak >= 7`

---

## Privacy & User Control

### What We DO Collect
- Screen navigation patterns
- Feature usage (practice, badges, games)
- Performance metrics (accuracy, time)
- App errors and crashes
- Device information (OS version, device model)

### What We DO NOT Collect
- User names (stored locally only)
- Exact locations
- Personally Identifiable Information (PII)
- Contact information
- Photos or files

### User Control

Users can opt-out of analytics tracking:
1. Navigate to **Settings → Privacy**
2. Toggle **Analytics** switch to OFF
3. All analytics collection stops immediately

**Implementation**:
- `UserPreferencesRepository.isAnalyticsEnabled` Flow
- `FirebaseAnalyticsService` observes preference
- Calls `FirebaseAnalytics.setAnalyticsCollectionEnabled()`

---

## Testing Best Practices

### Unit Tests

Always verify analytics in unit tests:

```kotlin
@Test
fun `action logs expected analytics`() = runTest {
    val fakeAnalytics = FakeAnalyticsService()
    val subject = MyClass(fakeAnalytics)
    
    subject.performAction()
    
    // Verify screen view
    assertThat(fakeAnalytics.screenViews).hasSize(1)
    assertThat(fakeAnalytics.screenViews.first().screenName)
        .isEqualTo("Expected Screen")
    
    // Verify event
    val events = fakeAnalytics.getEventsWithName(AnalyticsEvent.MY_EVENT)
    assertThat(events).hasSize(1)
    assertThat(events.first().parameters[AnalyticsParam.MY_PARAM])
        .isEqualTo("expected_value")
}
```

### Manual Testing Checklist

See "Testing Analytics Locally" section above for DebugView setup.

**Onboarding Flow**:
- [ ] `onboarding_started` fires on app first launch
- [ ] `grade_selected` fires with correct grade_level
- [ ] `onboarding_completed` fires after name entry

**Practice Flow**:
- [ ] `practice_session_started` fires with operation_type
- [ ] `problem_correct` fires with solve_time
- [ ] `practice_session_completed` fires with accuracy

**User Properties**:
- [ ] Verify in DebugView → User Properties tab
- [ ] Check: grade_level, total_problems_solved, current_streak

---

## Troubleshooting

### Events Not Appearing in DebugView

1. **Check debug mode is enabled**:
   ```bash
   adb shell getprop debug.firebase.analytics.app
   ```
   Should return: `dev.hossain.mathtutor`

2. **Check internet connection**: DebugView requires network

3. **Check analytics opt-in**: Verify Settings → Privacy → Analytics is ON

4. **Check Firebase configuration**: Ensure `google-services.json` is present

### Events Not in Production Reports

- Events can take **24-48 hours** to appear in production reports
- DebugView shows real-time data, but Reports are delayed
- Check Events tab (not DebugView) after 24 hours

### User Properties Not Updating

- Properties update when set via `setUserProperty()`
- Changes may take a few minutes to appear in DebugView
- Verify property name matches `UserProperty` constants

---

## Resources

- [Firebase Analytics Documentation](https://firebase.google.com/docs/analytics)
- [Firebase Console](https://console.firebase.google.com/)
- [Circuit Testing Guide](https://slackhq.github.io/circuit/testing/)
- [Analytics Implementation Plan](../plan-sketch/analytics-implementation-plan.md)

---

## Changelog

### Version 1.0 (December 21, 2025)
- Initial analytics system documentation
- Comprehensive tracking for 13 screens
- 20+ events tracked across all features
- 6 user properties for segmentation
- Privacy-first design with opt-out toggle
- Testing guide for DebugView and unit tests
