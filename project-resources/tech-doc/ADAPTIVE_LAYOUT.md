# Adaptive Layout Implementation Plan

This document provides a comprehensive implementation plan for making Kids Math Pup Tutor tablet and adaptive layout friendly, following best practices from the [Reply sample](https://github.com/android/compose-samples/tree/main/Reply) in Google's Compose Samples.

## Table of Contents

1. [Overview](#overview)
2. [Current State Analysis](#current-state-analysis)
3. [Adaptive Layout Best Practices from Reply](#adaptive-layout-best-practices-from-reply)
4. [Implementation Phases](#implementation-phases)
5. [Detailed Implementation Guide](#detailed-implementation-guide)
6. [Dependencies Required](#dependencies-required)
7. [Testing Strategy](#testing-strategy)

---

## Overview

The goal is to make the Kids Math Pup Tutor app responsive across multiple form factors:
- **Phones** (compact): Single-pane layout with bottom navigation
- **Tablets** (medium): Navigation rail with optimized layouts
- **Large tablets/Desktop** (expanded): Permanent navigation drawer with dual-pane layouts
- **Foldables**: Adapt to book posture and table-top modes

### Target Window Size Classes

| Class | Width | Navigation | Content |
|-------|-------|------------|---------|
| Compact | < 600dp | Bottom Navigation Bar | Single pane |
| Medium | 600dp - 840dp | Navigation Rail | Single pane (may expand) |
| Expanded | > 840dp | Permanent Navigation Drawer | Dual pane |

---

## Current State Analysis

### Existing Screens (13 total)

1. **OnboardingScreen** - Welcome flow
2. **GradeSelectionScreen** - Grade selection
3. **NameEntryScreen** - Name input
4. **HomeScreen** - Main hub
5. **OperationSelectorScreen** - Operation selection
6. **MathPracticeScreen** - Active practice
7. **ResultsScreen** - Session results
8. **StatsScreen** - Statistics
9. **BadgesScreen** - Achievement badges
10. **SettingsScreen** - App settings
11. **AudioHapticSettingsScreen** - Audio/haptic preferences
12. **GameSelectionScreen** - Mini-game selection
13. **MathRaceScreen** - Math race game

### Current Navigation Structure

- Uses Circuit Navigator for navigation
- Single-pane layouts only
- No responsive navigation components
- Back button in TopAppBar for navigation

### Current Dependencies

- Compose BOM: 2025.12.01
- Material 3: Standard (no adaptive extensions)
- No WindowSizeClass support
- No WindowLayoutInfo/FoldingFeature support

---

## Adaptive Layout Best Practices from Reply

### Key Components Used in Reply Sample

#### 1. WindowSizeClass

```kotlin
// In MainActivity.kt
val windowSize = calculateWindowSizeClass(this)
```

Used to determine device category (Compact, Medium, Expanded) and adapt UI accordingly.

#### 2. NavigationSuiteType

Reply uses `NavigationSuiteScaffoldLayout` with adaptive navigation:
- `NavigationBar` for compact devices
- `NavigationRail` for medium devices
- `PermanentNavigationDrawer` for expanded devices

#### 3. ContentType

```kotlin
enum class ReplyContentType {
    SINGLE_PANE,
    DUAL_PANE,
}
```

Determines whether to show single or dual-pane layouts based on window size.

#### 4. DevicePosture (Foldable Support)

```kotlin
sealed interface DevicePosture {
    object NormalPosture : DevicePosture
    data class BookPosture(val hingePosition: Rect) : DevicePosture
    data class Separating(val hingePosition: Rect, var orientation: FoldingFeature.Orientation) : DevicePosture
}
```

Used to detect foldable device states and adapt layouts.

#### 5. TwoPane Layout

```kotlin
// Using Material 3 Adaptive TwoPane (replaces deprecated accompanist-adaptive)
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole

ListDetailPaneScaffold(
    listPane = { /* List content */ },
    detailPane = { /* Detail content */ },
)

// Or using supportingPane for three-pane layout
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold

SupportingPaneScaffold(
    mainPane = { /* Main content */ },
    supportingPane = { /* Supporting content */ },
)
```

For master-detail layouts on larger screens. Note: The Reply sample uses `com.google.accompanist:accompanist-adaptive` but this library is deprecated. The official Material 3 Adaptive library (`androidx.compose.material3.adaptive:adaptive-layout`) provides `ListDetailPaneScaffold` and `SupportingPaneScaffold` as replacements.

---

## Implementation Phases

### Phase 1: Foundation - Window Size & Navigation Infrastructure

**Description**: Add core adaptive layout dependencies and create window size utilities.

**Tasks**:
1. Add Material 3 Adaptive dependencies to `gradle/libs.versions.toml`
2. Add Window library for foldable support
3. Create `ui/utils/WindowStateUtils.kt` with DevicePosture and window utilities
4. Create `ui/utils/AdaptiveLayoutTypes.kt` for NavigationType and ContentType enums
5. Update `MainActivity.kt` to calculate WindowSizeClass and DisplayFeatures

**Files to Create/Modify**:
- `gradle/libs.versions.toml` - Add new dependencies
- `app/build.gradle.kts` - Add dependency declarations
- `app/src/main/java/dev/hossain/mathtutor/ui/utils/WindowStateUtils.kt` - NEW
- `app/src/main/java/dev/hossain/mathtutor/ui/utils/AdaptiveLayoutTypes.kt` - NEW
- `app/src/main/java/dev/hossain/mathtutor/MainActivity.kt` - Add window calculations

**Dependencies Required**:
```toml
[versions]
androidx-window = "1.5.1"

[libraries]
# Material 3 Adaptive Components (includes TwoPane/ListDetail support)
androidx-compose-material3-adaptive = { module = "androidx.compose.material3.adaptive:adaptive" }
androidx-compose-material3-adaptive-layout = { module = "androidx.compose.material3.adaptive:adaptive-layout" }
androidx-compose-material3-adaptive-navigation = { module = "androidx.compose.material3.adaptive:adaptive-navigation" }
androidx-compose-material3-adaptive-navigationSuite = { module = "androidx.compose.material3:material3-adaptive-navigation-suite" }
androidx-compose-materialWindow = { module = "androidx.compose.material3:material3-window-size-class" }
androidx-window = { module = "androidx.window:window", version.ref = "androidx-window" }
```

**Estimated Effort**: 2-3 hours

---

### Phase 2: Navigation Wrapper Component

**Description**: Create an adaptive navigation wrapper that automatically switches between BottomNav, NavigationRail, and NavigationDrawer.

**Tasks**:
1. Create `ui/navigation/AdaptiveNavigationWrapper.kt` - Main wrapper component
2. Create `ui/navigation/AppNavigationRail.kt` - Navigation rail implementation
3. Create `ui/navigation/AppBottomNavigationBar.kt` - Bottom navigation implementation
4. Create `ui/navigation/AppNavigationDrawer.kt` - Navigation drawer implementations (Modal and Permanent)
5. Create `ui/navigation/TopLevelDestination.kt` - Define top-level navigation destinations
6. Integrate with Circuit's Navigator for routing

**Top-Level Destinations for Kids Math Pup Tutor**:
- **Home** (House icon) - HomeScreen
- **Practice** (Edit/Pencil icon) - OperationSelectorScreen
- **Games** (Gamepad icon) - GameSelectionScreen
- **Stats** (Chart icon) - StatsScreen
- **Settings** (Gear icon) - SettingsScreen

**Files to Create**:
- `app/src/main/java/dev/hossain/mathtutor/ui/navigation/TopLevelDestination.kt` - NEW
- `app/src/main/java/dev/hossain/mathtutor/ui/navigation/AdaptiveNavigationWrapper.kt` - NEW
- `app/src/main/java/dev/hossain/mathtutor/ui/navigation/AppNavigationRail.kt` - NEW
- `app/src/main/java/dev/hossain/mathtutor/ui/navigation/AppBottomNavigationBar.kt` - NEW
- `app/src/main/java/dev/hossain/mathtutor/ui/navigation/AppNavigationDrawer.kt` - NEW

**Estimated Effort**: 4-6 hours

---

### Phase 3: App Shell with Adaptive Navigation

**Description**: Create main app shell that wraps all screens with adaptive navigation.

**Tasks**:
1. Create `ui/AppShell.kt` - Root composable with adaptive navigation
2. Update `MainActivity.kt` to use AppShell instead of direct Circuit content
3. Handle navigation state synchronization between adaptive nav and Circuit
4. Add Circuit screen awareness to highlight current destination
5. Handle back navigation properly with drawer state

**Key Implementation Details**:
- Wrap `NavigableCircuitContent` with `AdaptiveNavigationWrapper`
- Track current screen type to determine navigation highlighting
- Handle drawer gestures and back press for modal drawer

**Files to Modify**:
- `app/src/main/java/dev/hossain/mathtutor/ui/AppShell.kt` - NEW
- `app/src/main/java/dev/hossain/mathtutor/MainActivity.kt` - Update to use AppShell

**Estimated Effort**: 4-5 hours

---

### Phase 4: HomeScreen Adaptive Layout

**Description**: Update HomeScreen to leverage larger screen real estate effectively.

**Tasks**:
1. Create responsive grid layout for Quick Stats and Badges sections
2. On tablets, show stats and badges side-by-side
3. Optimize action buttons layout for wider screens
4. Add tablet-optimized preview functions

**Layout Strategy**:
- **Compact**: Current vertical scrolling layout
- **Medium/Expanded**: 2-column grid for cards, wider action buttons

**Files to Modify**:
- `app/src/main/java/dev/hossain/mathtutor/ui/home/HomeUi.kt`

**Estimated Effort**: 2-3 hours

---

### Phase 5: MathPracticeScreen Adaptive Layout

**Description**: Optimize math practice for larger screens.

**Tasks**:
1. Create dual-pane layout for tablets showing problem and history side-by-side
2. Optimize NumberPad size for tablets (larger buttons)
3. Center content better on wider screens
4. Add landscape mode optimizations

**Layout Strategy**:
- **Compact Portrait**: Current stacked layout
- **Compact Landscape**: Side-by-side problem and answer area
- **Medium/Expanded**: Problem on left, answer area/history on right

**Files to Modify**:
- `app/src/main/java/dev/hossain/mathtutor/ui/mathpractice/MathPracticeUi.kt`
- `app/src/main/java/dev/hossain/mathtutor/ui/component/NumberPad.kt`
- `app/src/main/java/dev/hossain/mathtutor/ui/component/AnswerField.kt`

**Estimated Effort**: 3-4 hours

---

### Phase 6: StatsScreen Adaptive Layout

**Description**: Create responsive statistics display.

**Tasks**:
1. Use FlowRow/LazyVerticalGrid for stats cards
2. Show more details inline on tablets
3. Create responsive operation stats grid

**Layout Strategy**:
- **Compact**: Single column list
- **Medium**: 2-column grid
- **Expanded**: 3-column grid with more detail visible

**Files to Modify**:
- `app/src/main/java/dev/hossain/mathtutor/ui/stats/StatsUi.kt`

**Estimated Effort**: 2-3 hours

---

### Phase 7: BadgesScreen Adaptive Layout

**Description**: Optimize badges grid for larger screens.

**Tasks**:
1. Use adaptive grid columns based on screen width
2. Show more badges per row on tablets
3. Larger badge icons on tablets
4. Consider list+detail layout for expanded screens

**Layout Strategy**:
- **Compact**: 3 badges per row
- **Medium**: 4-5 badges per row
- **Expanded**: 6+ badges per row, or list+detail with badge details panel

**Files to Modify**:
- `app/src/main/java/dev/hossain/mathtutor/ui/badges/BadgesUi.kt`
- `app/src/main/java/dev/hossain/mathtutor/ui/component/BadgeGrid.kt`

**Estimated Effort**: 2-3 hours

---

### Phase 8: GameSelectionScreen & MathRaceScreen Adaptive Layout

**Description**: Optimize game screens for tablets.

**Tasks**:
1. GameSelectionScreen: Responsive grid for game cards
2. MathRaceScreen: Center game content, optimize timer/score display

**Layout Strategy**:
- **Compact**: Current single-column layout
- **Medium/Expanded**: 2+ game cards per row

**Files to Modify**:
- `app/src/main/java/dev/hossain/mathtutor/ui/games/GameSelectionUi.kt`
- `app/src/main/java/dev/hossain/mathtutor/ui/mathrace/MathRaceUi.kt`
- `app/src/main/java/dev/hossain/mathtutor/ui/mathrace/MathRaceGameScreen.kt`
- `app/src/main/java/dev/hossain/mathtutor/ui/mathrace/MathRaceStartScreen.kt`
- `app/src/main/java/dev/hossain/mathtutor/ui/mathrace/MathRaceResultsScreen.kt`

**Estimated Effort**: 3-4 hours

---

### Phase 9: SettingsScreen Adaptive Layout

**Description**: Optimize settings for larger screens.

**Tasks**:
1. Use wider max-width for settings content
2. Consider dual-pane for settings + audio/haptic settings on expanded
3. Group related settings visually

**Layout Strategy**:
- **Compact**: Current single column
- **Medium/Expanded**: Centered with max-width, possibly dual-pane

**Files to Modify**:
- `app/src/main/java/dev/hossain/mathtutor/ui/settings/SettingsUi.kt`
- `app/src/main/java/dev/hossain/mathtutor/ui/settings/AudioHapticSettingsUi.kt`

**Estimated Effort**: 2-3 hours

---

### Phase 10: OnboardingScreen Adaptive Layout

**Description**: Optimize onboarding for tablets.

**Tasks**:
1. Larger images and text on tablets
2. Center content with max-width
3. Optimize page indicator spacing
4. Possibly show multiple pages visible on expanded screens

**Layout Strategy**:
- **Compact**: Current pager layout
- **Medium/Expanded**: Centered with max-width, larger images

**Files to Modify**:
- `app/src/main/java/dev/hossain/mathtutor/ui/onboarding/OnboardingUi.kt`
- `app/src/main/java/dev/hossain/mathtutor/ui/onboarding/GradeSelectionUi.kt`
- `app/src/main/java/dev/hossain/mathtutor/ui/onboarding/NameEntryUi.kt`

**Estimated Effort**: 2-3 hours

---

### Phase 11: ResultsScreen Adaptive Layout

**Description**: Optimize results display for tablets.

**Tasks**:
1. Use wider layout for results summary
2. Show more problem review items per row on tablets
3. Optimize confetti animation for larger screens

**Layout Strategy**:
- **Compact**: Current vertical layout
- **Medium/Expanded**: 2-column problem review, wider summary card

**Files to Modify**:
- `app/src/main/java/dev/hossain/mathtutor/ui/practiceresults/ResultsUi.kt`

**Estimated Effort**: 2 hours

---

### Phase 12: Foldable Device Support

**Description**: Add specific support for foldable devices.

**Tasks**:
1. Detect book posture and table-top posture
2. Adapt layouts to avoid hinge area
3. Use TwoPane component for foldable book mode
4. Test with emulator foldable profiles

**Key Considerations**:
- Avoid placing critical content over the hinge
- Use different layouts for different postures
- Support half-fold table-top mode for practice

**Files to Modify**:
- `app/src/main/java/dev/hossain/mathtutor/ui/utils/WindowStateUtils.kt`
- `app/src/main/java/dev/hossain/mathtutor/MainActivity.kt`
- Various UI files for foldable-specific layouts

**Estimated Effort**: 3-4 hours

---

### Phase 13: Testing & Polish

**Description**: Final testing and refinements.

**Tasks**:
1. Add Compose preview functions for tablet sizes
2. Test on various device sizes (phone, tablet, foldable)
3. Test landscape orientations
4. Fix any responsive layout issues
5. Update documentation

**Testing Devices/Emulators**:
- Pixel phone (compact)
- Pixel Tablet (expanded)
- Galaxy Fold (foldable)
- Various landscape modes

**Files to Create/Modify**:
- All UI files (add tablet previews)
- `NAVIGATION.md` (update with new navigation architecture)
- `CHANGELOG.md` (document changes)

**Estimated Effort**: 4-5 hours

---

## Dependencies Required

Add to `gradle/libs.versions.toml`:

```toml
[versions]
androidx-window = "1.5.1"

[libraries]
# Material 3 Adaptive Components (includes TwoPane support, replacing deprecated accompanist-adaptive)
androidx-compose-material3-adaptive = { module = "androidx.compose.material3.adaptive:adaptive" }
androidx-compose-material3-adaptive-layout = { module = "androidx.compose.material3.adaptive:adaptive-layout" }
androidx-compose-material3-adaptive-navigation = { module = "androidx.compose.material3.adaptive:adaptive-navigation" }
androidx-compose-material3-adaptive-navigationSuite = { module = "androidx.compose.material3:material3-adaptive-navigation-suite" }

# Window Size Class
androidx-compose-materialWindow = { module = "androidx.compose.material3:material3-window-size-class" }

# Window library for foldable support
androidx-window = { module = "androidx.window:window", version.ref = "androidx-window" }
```

Add to `app/build.gradle.kts`:

```kotlin
dependencies {
    // Adaptive layouts (Material 3 Adaptive includes TwoPane support)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigationSuite)
    implementation(libs.androidx.compose.materialWindow)
    implementation(libs.androidx.window)
}
```

**Note**: The deprecated `accompanist-adaptive` library is NOT included here. TwoPane functionality has been migrated to `androidx.compose.material3.adaptive:adaptive-layout` as part of the official Material 3 adaptive libraries.

---

## Testing Strategy

### Preview Annotations

Add tablet preview functions to each screen:

```kotlin
@Preview(showBackground = true, widthDp = 700, heightDp = 500, name = "Tablet Landscape")
@Preview(showBackground = true, widthDp = 500, heightDp = 700, name = "Tablet Portrait")
@Preview(showBackground = true, widthDp = 1100, heightDp = 600, name = "Desktop")
@Composable
fun ScreenTabletPreview() {
    KidsMathTutorAppTheme {
        ScreenUi(state = SampleState)
    }
}
```

### Emulator Testing

Use Android Studio's resizable emulator to test:
1. Phone (6" - 411dp x 891dp)
2. Foldable (7.6" unfolded - 884dp x 754dp)
3. Tablet (10" - 800dp x 1280dp)
4. Desktop (various sizes)

### Test Scenarios

1. **Orientation changes**: Rotate device, ensure smooth transitions
2. **Window resizing**: On desktop/ChromeOS, resize window
3. **Navigation**: Verify correct nav component shows for each size
4. **Content**: Verify content adapts appropriately
5. **Foldable**: Test fold/unfold transitions

---

## Summary Timeline

| Phase | Description | Effort |
|-------|-------------|--------|
| Phase 1 | Foundation - Dependencies & Utils | 2-3 hours |
| Phase 2 | Navigation Wrapper | 4-6 hours |
| Phase 3 | App Shell Integration | 4-5 hours |
| Phase 4 | HomeScreen Adaptive | 2-3 hours |
| Phase 5 | MathPracticeScreen Adaptive | 3-4 hours |
| Phase 6 | StatsScreen Adaptive | 2-3 hours |
| Phase 7 | BadgesScreen Adaptive | 2-3 hours |
| Phase 8 | Games Screens Adaptive | 3-4 hours |
| Phase 9 | SettingsScreen Adaptive | 2-3 hours |
| Phase 10 | OnboardingScreen Adaptive | 2-3 hours |
| Phase 11 | ResultsScreen Adaptive | 2 hours |
| Phase 12 | Foldable Support | 3-4 hours |
| Phase 13 | Testing & Polish | 4-5 hours |
| **Total** | | **~36-49 hours** |

---

## Notes for Engineers/AI Agents

### Key Principles

1. **Progressive Enhancement**: Start with phone layout, enhance for larger screens
2. **Material 3 Compliance**: Use Material 3 adaptive components
3. **Theme-Aware**: Never hardcode colors, use MaterialTheme.colorScheme
4. **Accessibility**: Maintain TalkBack support across all layouts
5. **Performance**: Avoid layout thrashing during orientation changes

### Common Patterns

```kotlin
// Get window size class in composable
val windowSizeClass = calculateWindowSizeClass(LocalContext.current as Activity)

// Determine layout type
val contentType = when (windowSizeClass.widthSizeClass) {
    WindowWidthSizeClass.Compact -> ContentType.SINGLE_PANE
    WindowWidthSizeClass.Medium -> ContentType.SINGLE_PANE  // or DUAL_PANE based on screen
    WindowWidthSizeClass.Expanded -> ContentType.DUAL_PANE
    else -> ContentType.SINGLE_PANE
}

// Use in composable
if (contentType == ContentType.DUAL_PANE) {
    TwoPane(first = { ... }, second = { ... })
} else {
    SinglePaneContent()
}
```

### Circuit Integration Considerations

- Circuit's Navigator operates independently from Material navigation components
- Need to synchronize visual highlighting with actual Circuit screen state
- Consider creating a custom Circuit decorator for adaptive navigation
- May need to expose current screen type from backstack for navigation highlighting

---

## Related Documentation

- [Reply Sample README](https://github.com/android/compose-samples/blob/main/Reply/README.md)
- [Material 3 Adaptive Design Overview](https://m3.material.io/foundations/adaptive-design/overview)
- [WindowSizeClass Documentation](https://developer.android.com/reference/kotlin/androidx/compose/material3/windowsizeclass/WindowSizeClass)
- [Accompanist Adaptive](https://google.github.io/accompanist/adaptive/)
- [Circuit Documentation](https://slackhq.github.io/circuit/)
