# Phase 3 Manual Testing Guide

**Version**: 1.0.0  
**Date**: December 18, 2024  
**Target**: Physical Android Device (API 28+)  
**Duration**: ~2 hours

---

## Pre-Testing Setup

### Requirements
- [ ] Physical Android device (API 28 or higher)
- [ ] USB debugging enabled
- [ ] Debug APK installed: `app/build/outputs/apk/debug/app-debug.apk`
- [ ] Device has sufficient storage
- [ ] Device has stable date/time settings

### Installation
```bash
# Build and install debug APK
./gradlew installDebug

# Or manually install
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Test Data Preparation
- [ ] Clear app data before testing: Settings → Apps → Math Pup Tutor → Clear Data
- [ ] Note: Some tests require practicing on consecutive days (plan accordingly)

---

## Test Session 1: First-Time User Experience

### Objective
Verify app behavior for brand new users with no data.

### Test Steps

#### 1.1 Initial Launch
- [ ] Launch app for first time
- [ ] Verify onboarding screen displays
- [ ] **Expected**: Welcome screen with app introduction
- [ ] **Pass/Fail**: ____

#### 1.2 Complete Onboarding
- [ ] Tap "Get Started" or "Skip"
- [ ] Verify navigation to Home Screen
- [ ] **Expected**: Home screen displays with:
  - Welcome message
  - Empty/zero streak card
  - Zero stats (0 problems, 0% accuracy)
  - No badges shown
  - "Start Practice" button visible
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: Attach home screen for first-time user

#### 1.3 Check Badges Screen (Empty)
- [ ] From Home, navigate to Badges screen (if accessible)
- [ ] **Expected**: 
  - "0 of 15 Badges Unlocked" or similar
  - All 15 badges shown as locked (🔒)
  - Badges organized by category
  - No unlock dates shown
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: Attach badges screen showing all locked

#### 1.4 First Practice Session
- [ ] Tap "Start Practice"
- [ ] Select Addition (easy mode)
- [ ] Complete 10 problems correctly
- [ ] **Expected after completion**:
  - Badge unlock dialog appears (First Steps, Math Rookie, Perfect 10)
  - Can dismiss each badge sequentially
  - Navigate to results screen
  - Streak updated to "1 Day"
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: Badge unlock dialog
- [ ] **Notes**: Number of badges unlocked: ____

---

## Test Session 2: Badge System

### Objective
Test badge unlock mechanics, display, and animations.

### Test Steps

#### 2.1 Badge Unlock Dialog
- [ ] Complete a practice session
- [ ] Observe badge unlock dialog (if badges unlocked)
- [ ] **Verify**:
  - Large badge icon displayed (emoji)
  - Badge name shown
  - Description shown
  - "Awesome!" or similar button to dismiss
  - Animation smooth (scale/bounce)
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: Badge unlock dialog

#### 2.2 Multiple Badge Unlocks
- [ ] Continue practicing to unlock multiple badges in one session
  - Suggestion: Complete 25 total problems to unlock Math Explorer
- [ ] **Verify**:
  - Multiple badges show sequentially (one at a time)
  - Can navigate through all unlocked badges
  - Final badge dismisses and shows results
- [ ] **Pass/Fail**: ____
- [ ] **Notes**: Number of sequential badges shown: ____

#### 2.3 Badge Screen Display
- [ ] Navigate to Badges screen
- [ ] **Verify unlocked badges**:
  - Full color icon
  - Checkmark (✓) indicator
  - Badge name visible
  - Background: `primaryContainer` color
- [ ] **Verify locked badges**:
  - Grayscale/dimmed icon
  - Lock icon (🔒) overlay
  - Progress indicator if applicable (e.g., "15/25")
  - Background: `surfaceVariant` color
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: Badges screen with mixed locked/unlocked

#### 2.4 Badge Categories
- [ ] Scroll through badges screen
- [ ] **Verify all 5 categories shown**:
  - Getting Started (3 badges)
  - Volume (4 badges)
  - Operation Mastery (3 badges)
  - Speed & Accuracy (3 badges)
  - Streak (2 badges)
- [ ] **Pass/Fail**: ____
- [ ] **Notes**: Any category missing? ____

#### 2.5 Badge Progress Indicators
- [ ] Check locked badges for progress indicators
- [ ] **Expected**:
  - Volume badges show "X/25", "X/50", etc.
  - Operation badges show counts
  - Streak badges show current streak
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: Badge with progress indicator

#### 2.6 Badge Detail Modal (if implemented)
- [ ] Tap on a badge
- [ ] **Verify modal displays**:
  - Large badge icon
  - Full description
  - Requirement details
  - Unlock date (if unlocked) OR progress (if locked)
  - Close button
- [ ] **Pass/Fail**: ____
- [ ] **Notes**: ____

---

## Test Session 3: Streak Tracking

### Objective
Verify daily streak mechanics across multiple days.

### Test Steps

#### 3.1 Initial Streak Creation
- [ ] Complete first practice session
- [ ] Return to home screen
- [ ] **Verify streak card**:
  - Shows "1 Day!" or "1 Day Streak"
  - Fire emoji 🔥 displayed
  - Encouraging message shown
  - Weekly calendar (if implemented) shows today checked
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: Streak card showing 1-day streak

#### 3.2 Same-Day Multiple Practice
- [ ] Complete another practice session same day
- [ ] Return to home screen
- [ ] **Verify streak unchanged**:
  - Still shows "1 Day"
  - Total days practiced: 1
  - Longest streak: 1
- [ ] **Pass/Fail**: ____
- [ ] **Notes**: Did streak incorrectly increment? ____

#### 3.3 Consecutive Day Practice
⚠️ **Note**: This test requires practicing on consecutive days

- [ ] **Day 1**: Complete practice session
- [ ] **Day 2**: Complete practice session
- [ ] **Verify after Day 2**:
  - Streak shows "2 Days" or "2 Day Streak"
  - Weekly calendar shows 2 days checked
  - Longest streak: 2
- [ ] **Day 3**: Complete practice session
- [ ] **Verify after Day 3**:
  - Streak shows "3 Days"
  - Longest streak: 3
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: Streak card showing 3+ day streak

#### 3.4 Streak At Risk
⚠️ **Note**: Practice one day, then check the next day WITHOUT practicing

- [ ] Practice on Day 1
- [ ] On Day 2, open app but DON'T practice
- [ ] **Verify streak card**:
  - Shows urgent message: "Practice today to keep your streak!"
  - Streak is still "alive" (at risk but not broken yet)
  - Fire emoji may change color or show warning
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: At-risk streak warning

#### 3.5 Streak Recovery
- [ ] On same Day 2 from 3.4, complete a practice session
- [ ] **Verify**:
  - Streak increments (e.g., 1 → 2)
  - Warning message disappears
  - Normal fire emoji returns
- [ ] **Pass/Fail**: ____

#### 3.6 Streak Reset After Gap
⚠️ **Note**: Practice one day, skip 2+ days, then practice again

- [ ] Practice to build a streak (e.g., 3 days)
- [ ] Skip 2-3 days (don't open app)
- [ ] Practice again
- [ ] **Verify**:
  - Current streak resets to "1 Day"
  - Longest streak preserved (shows previous max)
  - Total days practiced increments correctly
- [ ] **Pass/Fail**: ____
- [ ] **Notes**: Current: ____, Longest: ____, Total: ____

#### 3.7 Weekly Calendar Visualization
- [ ] Check home screen streak card
- [ ] **Verify weekly calendar** (if implemented):
  - Shows Su Mo Tu We Th Fr Sa
  - Checkmarks (✓) for practiced days
  - Current week displayed
  - Correct days marked
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: Weekly calendar with multiple days checked

---

## Test Session 4: Home Dashboard

### Objective
Verify home screen displays all widgets correctly and navigation works.

### Test Steps

#### 4.1 Home Screen Layout
- [ ] Open home screen after some practice
- [ ] **Verify all sections present**:
  - Welcome message at top
  - Streak card (prominent)
  - Quick stats card
  - Latest badges section (3 most recent)
  - "Start Practice" button (large, primary)
  - "View Full Stats" link (if user has sessions)
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: Complete home screen layout

#### 4.2 Welcome Message
- [ ] Check welcome message
- [ ] **Verify**:
  - Shows personalized greeting (if userName set)
  - OR shows "Welcome back!" or similar
  - Typography: Large, readable
  - Color: Theme-aware
- [ ] **Pass/Fail**: ____

#### 4.3 Quick Stats Card
- [ ] Check quick stats display
- [ ] **Verify**:
  - Shows total problems solved
  - Shows overall accuracy percentage
  - Format: Compact (e.g., "247 problems • 78% ✓")
  - Updates after new practice sessions
- [ ] **Pass/Fail**: ____
- [ ] **Notes**: Stats shown: ____

#### 4.4 Latest Badges Section
- [ ] Check latest badges display
- [ ] **Verify**:
  - Shows 3 most recently unlocked badges
  - Each badge: Large emoji (48dp), name
  - "View All Badges" link/button present
  - Tapping link navigates to badges screen
- [ ] **Pass/Fail**: ____
- [ ] **Screenshot**: Latest badges section

#### 4.5 Navigation - Start Practice
- [ ] Tap "Start Practice" button
- [ ] **Verify**:
  - Navigates to Operation Selector screen
  - Can select operation (Addition, Subtraction, Mixed)
  - Can start practice session
  - Back button returns to home
- [ ] **Pass/Fail**: ____

#### 4.6 Navigation - View Stats
- [ ] Tap "View Full Stats" (if visible)
- [ ] **Verify**:
  - Navigates to Stats screen
  - Shows detailed statistics
  - Back button returns to home
- [ ] **Pass/Fail**: ____

#### 4.7 Navigation - View Badges
- [ ] Tap "View All Badges"
- [ ] **Verify**:
  - Navigates to Badges screen
  - Shows all badges by category
  - Back button returns to home
- [ ] **Pass/Fail**: ____

#### 4.8 Home Screen Refresh
- [ ] Complete a practice session
- [ ] Return to home screen
- [ ] **Verify all data updates**:
  - Streak increments (if consecutive day)
  - Stats update (problems, accuracy)
  - New badges appear if unlocked
- [ ] **Pass/Fail**: ____

---

## Test Session 5: Performance Testing

### Objective
Measure and verify app performance meets requirements.

### Test Steps

#### 5.1 Home Screen Load Time
- [ ] Close app completely
- [ ] Launch app (returning user)
- [ ] **Measure** time from tap to home screen fully loaded
- [ ] **Requirement**: < 1 second
- [ ] **Actual time**: _____ ms
- [ ] **Pass/Fail**: ____

#### 5.2 Badges Screen with Many Badges
- [ ] Unlock at least 5-7 badges
- [ ] Navigate to badges screen
- [ ] **Verify**:
  - Smooth scrolling
  - No lag when rendering badges
  - All icons display correctly
  - No crashes
- [ ] **Pass/Fail**: ____
- [ ] **Notes**: Any performance issues? ____

#### 5.3 Badge Checking Speed
- [ ] Complete a practice session
- [ ] **Observe** time from last problem to badge dialog/results
- [ ] **Verify**:
  - Badge checking happens quickly (< 500ms)
  - No noticeable delay
  - Smooth transition
- [ ] **Actual delay**: _____ ms (approximate)
- [ ] **Pass/Fail**: ____

#### 5.4 Database Query Performance
- [ ] Complete 10+ practice sessions (varies problems)
- [ ] Navigate between screens (Home → Stats → Badges)
- [ ] **Verify**:
  - No lag when loading data
  - Smooth transitions
  - Stats calculate quickly
- [ ] **Pass/Fail**: ____
- [ ] **Notes**: Any slowdowns observed? ____

#### 5.5 Memory Usage
- [ ] Use Android Studio Profiler or device monitoring
- [ ] Monitor memory while using app for 10 minutes
- [ ] **Verify**:
  - No memory leaks
  - Memory usage stable
  - No increasing trend
- [ ] **Pass/Fail**: ____
- [ ] **Peak memory**: _____ MB

---

## Test Session 6: Navigation Flows

### Objective
Verify all navigation paths work correctly.

### Test Steps

#### 6.1 Full Navigation Cycle
- [ ] Launch app (returning user)
- [ ] **Navigate**: Home → Start Practice → Operation Selector
- [ ] **Navigate**: Operation Selector → Practice Session
- [ ] Complete session
- [ ] **Navigate**: Practice → Badge Dialog (if unlocked) → Results
- [ ] **Navigate**: Results → Home (Try Again button)
- [ ] **Navigate**: Home → Badges → Back to Home
- [ ] **Navigate**: Home → Stats → Back to Home
- [ ] **Verify**: All transitions smooth, no crashes
- [ ] **Pass/Fail**: ____

#### 6.2 Back Button Behavior
- [ ] Test back button at each screen:
  - Badges screen → Home
  - Stats screen → Home
  - Operation Selector → Home
  - Practice session → Operation Selector
  - Results → Home
- [ ] **Verify**: Back navigation intuitive and correct
- [ ] **Pass/Fail**: ____

#### 6.3 Deep Link / State Restoration
- [ ] Navigate to Badges screen
- [ ] Put app in background
- [ ] Wait 30 seconds
- [ ] Return to app
- [ ] **Verify**: Badges screen restored correctly
- [ ] **Pass/Fail**: ____

---

## Test Session 7: Edge Cases & Error Handling

### Objective
Test boundary conditions and error scenarios.

### Test Steps

#### 7.1 Zero State Handling
- [ ] Clear all app data
- [ ] Launch app
- [ ] **Verify**: No crashes, appropriate empty states shown
- [ ] **Pass/Fail**: ____

#### 7.2 Rapid Navigation
- [ ] Quickly tap between screens multiple times
- [ ] **Verify**: No crashes, no UI glitches
- [ ] **Pass/Fail**: ____

#### 7.3 Device Rotation
- [ ] Rotate device on each screen:
  - Home
  - Badges
  - Stats
  - Practice
  - Results
- [ ] **Verify**: State preserved, no crashes, layout correct
- [ ] **Pass/Fail**: ____

#### 7.4 Low Memory Scenario
- [ ] Open many apps to consume memory
- [ ] Return to Math Pup Tutor
- [ ] **Verify**: App recovers gracefully, data not lost
- [ ] **Pass/Fail**: ____

#### 7.5 Date/Time Changes
- [ ] Complete practice (note streak)
- [ ] Change device date forward 2 days
- [ ] Open app
- [ ] **Expected**: Streak should reset
- [ ] **Actual**: ____
- [ ] **Pass/Fail**: ____
- [ ] ⚠️ **Remember to reset device time**

---

## Test Session 8: Real Child Testing (Optional)

### Objective
Validate usability with target audience (K-2 children).

### Test Steps

#### 8.1 Badge System Understanding
- [ ] Ask child: "What are the badges for?"
- [ ] Observe if child explores badges naturally
- [ ] **Notes**: ____

#### 8.2 Streak Motivation
- [ ] Show streak after practice
- [ ] Ask child: "Do you want to keep your streak going?"
- [ ] **Notes on motivation level**: ____

#### 8.3 Home Screen Intuitiveness
- [ ] Let child explore home screen
- [ ] **Observe**:
  - Can they find "Start Practice"?
  - Do they understand streak card?
  - Are badges appealing?
- [ ] **Notes**: ____

#### 8.4 Badge Unlock Excitement
- [ ] Let child unlock a badge
- [ ] **Observe reaction**:
  - Excitement level
  - Understands achievement
  - Wants to unlock more
- [ ] **Notes**: ____

---

## Bug Tracking

### Bugs Found During Testing

| ID | Session | Description | Severity | Screenshot | Status |
|----|---------|-------------|----------|------------|--------|
| 1  |         |             |          |            |        |
| 2  |         |             |          |            |        |
| 3  |         |             |          |            |        |

**Severity Levels**:
- **Critical**: App crashes, data loss
- **High**: Feature doesn't work, major UI issues
- **Medium**: Minor functional issues, cosmetic problems
- **Low**: Polish items, nice-to-haves

---

## Performance Metrics Summary

| Metric | Target | Actual | Pass/Fail |
|--------|--------|--------|-----------|
| Home screen load time | < 1s | _____ ms | ____ |
| Badge screen load | Smooth | _____ | ____ |
| Badge check speed | < 500ms | _____ ms | ____ |
| DB query speed | Fast | _____ | ____ |
| Memory usage | Stable | _____ MB | ____ |

---

## Final Checklist

### Before Sign-Off
- [ ] All test sessions completed
- [ ] All screenshots captured and saved
- [ ] All bugs documented with severity
- [ ] Performance metrics recorded
- [ ] Child testing completed (if applicable)
- [ ] No critical or high severity bugs unresolved

### Sign-Off
- **Tester Name**: _________________
- **Date**: _________________
- **Device**: _________________
- **Android Version**: _________________
- **App Version**: 1.0.0 (versionCode 4)
- **Overall Result**: PASS / FAIL
- **Comments**: 
  _________________________________________
  _________________________________________

---

## Appendix: Test Data Reset

### Clear App Data
```bash
adb shell pm clear dev.hossain.mathtutor
```

### Force Stop App
```bash
adb shell am force-stop dev.hossain.mathtutor
```

### Uninstall App
```bash
adb uninstall dev.hossain.mathtutor
```

---

*Manual Testing Guide Version 1.0.0*  
*Generated: December 18, 2024*  
*Phase: 3-8 Testing & Bug Fixes*
