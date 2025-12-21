# Analytics Manual Testing Guide

**Purpose**: Verify analytics events are being tracked correctly using Firebase DebugView  
**Version**: 1.0  
**Last Updated**: December 21, 2025

---

## Setup

### 1. Enable Firebase DebugView

Enable debug mode for your test device:

```bash
adb shell setprop debug.firebase.analytics.app dev.hossain.mathtutor
```

### 2. Open Firebase Console

1. Navigate to [Firebase Console](https://console.firebase.google.com/)
2. Select the Kids Math Pup Tutor project
3. Go to **Analytics → DebugView**

### 3. Clear App Data (Optional)

For a fresh start:

```bash
adb shell pm clear dev.hossain.mathtutor
```

---

## Testing Checklist

### Onboarding Flow

Start with a fresh app install or cleared data.

- [ ] **Launch App**
  - Expected: `screen_view` event with `screen_name: "Onboarding"`
  - Expected: `onboarding_started` event

- [ ] **Tap "Get Started"**
  - Expected: `screen_view` event with `screen_name: "Grade Selection"`

- [ ] **Select Grade Level (e.g., Grade 1)**
  - Expected: `grade_selected` event
  - Verify parameter: `grade_level: "GRADE_1"`

- [ ] **Tap "Continue"**
  - Expected: `screen_view` event with `screen_name: "Name Entry"`

- [ ] **Enter Name and Tap "Continue"**
  - Expected: `name_entered` event
  - Expected: `onboarding_completed` event
  - Expected: `screen_view` event with `screen_name: "Home"`

### Home Screen

- [ ] **Home Screen Loads**
  - Expected: `screen_view` event with `screen_name: "Home"`
  - Check User Properties tab:
    - `grade_level` should be set
    - `has_completed_onboarding` should be "true"
    - `total_problems_solved` should be visible
    - `current_streak` should be visible
    - `total_badges_unlocked` should be visible

### Practice Flow

- [ ] **Tap "Start Practice"**
  - Expected: `screen_view` event with `screen_name: "Operation Selector"`

- [ ] **Select "Addition"**
  - Expected: `operation_selected` event
  - Verify parameter: `operation_type: "ADDITION"`
  - Expected: `screen_view` event with `screen_name: "Math Practice"`

- [ ] **Practice Session Starts**
  - Expected: `practice_session_started` event
  - Verify parameters: `operation_type`, `problem_count`

- [ ] **Answer Problem Correctly**
  - Expected: `problem_correct` event
  - Verify parameter: `solve_time` (in seconds)

- [ ] **Answer Problem Incorrectly**
  - Expected: `problem_incorrect` event

- [ ] **Complete All Problems**
  - Expected: `screen_view` event with `screen_name: "Practice Results"`

- [ ] **Results Screen Loads**
  - Expected: `practice_session_completed` event
  - Verify parameters:
    - `operation_type`
    - `problem_count`
    - `correct_answers`
    - `accuracy` (percentage)
    - `session_duration` (seconds)
  - Check User Properties:
    - `total_problems_solved` should have increased

### Badge System

- [ ] **Navigate to Badges (from Home)**
  - Expected: `screen_view` event with `screen_name: "Badges"`
  - Expected: `badges_viewed` event

- [ ] **Unlock a Badge** (e.g., complete 10 problems)
  - Expected: `badge_unlocked` event
  - Verify parameters:
    - `badge_id` (e.g., "first_problem")
    - `badge_name` (e.g., "First Problem")
    - `badge_category` (e.g., "GETTING_STARTED")
  - Check User Properties:
    - `total_badges_unlocked` should have increased

### Game System

- [ ] **Navigate to Games**
  - Expected: `screen_view` event with `screen_name: "Game Selection"`

- [ ] **Play Math Race**
  - Expected: `screen_view` event with `screen_name: "Math Race"`

- [ ] **Start Game**
  - Expected: `game_started` event
  - Verify parameter: `game_id: "MATH_RACE"`

- [ ] **Complete Game**
  - Expected: `game_completed` event
  - Verify parameters:
    - `game_id: "MATH_RACE"`
    - `game_score` (number of problems)
    - `game_duration` (seconds)
    - `accuracy` (percentage)

- [ ] **New High Score**
  - Expected: `game_high_score` event (if score > previous best)
  - Verify parameters:
    - `game_id`
    - `game_score`
    - `is_new_record: true`

### Settings

- [ ] **Navigate to Settings**
  - Expected: `screen_view` event with `screen_name: "Settings"`

- [ ] **Navigate to Audio & Haptic Settings**
  - Expected: `screen_view` event with `screen_name: "Audio & Haptic Settings"`

- [ ] **Toggle Audio**
  - Expected: `audio_toggled` or `settings_changed` event
  - Verify parameter: `setting_name`, `setting_value`

- [ ] **Toggle Haptics**
  - Expected: `haptics_toggled` or `settings_changed` event

- [ ] **Toggle Analytics OFF**
  - Expected: Events STOP being logged
  - Try navigating to other screens - no `screen_view` events should appear

- [ ] **Toggle Analytics ON**
  - Expected: `settings_changed` event
  - Expected: Events resume (verify with a navigation action)

### Statistics Screen

- [ ] **Navigate to Stats**
  - Expected: `screen_view` event with `screen_name: "Stats"`

---

## Verification Tips

### In DebugView

1. **Events Tab**: Shows real-time events as they occur
2. **User Properties Tab**: Shows current user property values
3. **Device Selection**: Make sure your test device is selected in the dropdown

### Event Timing

- Events appear within **1-5 seconds** in DebugView
- If events don't appear, check:
  - Debug mode is enabled (run adb command again)
  - Device is connected to internet
  - Analytics is enabled in Settings → Privacy

### Common Issues

**No events appearing:**
```bash
# Verify debug mode
adb shell getprop debug.firebase.analytics.app
# Should output: dev.hossain.mathtutor

# Re-enable if needed
adb shell setprop debug.firebase.analytics.app dev.hossain.mathtutor
```

**Analytics disabled:**
- Check Settings → Privacy → Analytics toggle
- Must be ON for events to be tracked

**Wrong device selected:**
- Use the device dropdown in DebugView to select your test device

---

## Expected Counts

After complete testing, you should see approximately:

- **Screen Views**: 13+ (one per unique screen visited)
- **Custom Events**: 15-25 (depending on actions performed)
- **User Properties**: 6 (grade_level, has_completed_onboarding, total_problems_solved, current_streak, total_badges_unlocked, games_unlocked)

---

## Disable DebugView

When done testing:

```bash
adb shell setprop debug.firebase.analytics.app .none.
```

---

## Production Verification

### After 24-48 Hours

1. Go to Firebase Console → Analytics → **Events**
2. Check that events appear in the Events report
3. Go to **User Properties** to verify properties are set
4. Go to **Dashboards** to see aggregated data

**Note**: Production reports have a 24-48 hour delay. Use DebugView for real-time verification.

---

## Test Results Template

Use this template to record your test results:

```
Test Date: _______________
Tester: _______________
App Version: _______________
Device: _______________

Onboarding Flow:
- [ ] onboarding_started ✓ / ✗
- [ ] grade_selected ✓ / ✗
- [ ] onboarding_completed ✓ / ✗

Practice Flow:
- [ ] practice_session_started ✓ / ✗
- [ ] problem_correct ✓ / ✗
- [ ] problem_incorrect ✓ / ✗
- [ ] practice_session_completed ✓ / ✗

Badge System:
- [ ] badges_viewed ✓ / ✗
- [ ] badge_unlocked ✓ / ✗

Game System:
- [ ] game_started ✓ / ✗
- [ ] game_completed ✓ / ✗
- [ ] game_high_score ✓ / ✗

Settings:
- [ ] audio_toggled ✓ / ✗
- [ ] haptics_toggled ✓ / ✗
- [ ] Analytics OFF (events stop) ✓ / ✗
- [ ] Analytics ON (events resume) ✓ / ✗

User Properties Verified:
- [ ] grade_level ✓ / ✗
- [ ] has_completed_onboarding ✓ / ✗
- [ ] total_problems_solved ✓ / ✗
- [ ] current_streak ✓ / ✗
- [ ] total_badges_unlocked ✓ / ✗
- [ ] games_unlocked ✓ / ✗

Notes:
_______________________________________________
_______________________________________________
```

---

## Resources

- [Firebase DebugView Documentation](https://firebase.google.com/docs/analytics/debugview)
- [Analytics Implementation Guide](ANALYTICS.md)
- [Firebase Console](https://console.firebase.google.com/)
