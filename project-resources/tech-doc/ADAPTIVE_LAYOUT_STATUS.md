# Adaptive Layout Implementation Status

**Last Updated**: December 29, 2025  
**Current Version**: 1.19.0  
**Current Implementation Status**: 🟨 IN PROGRESS (Phases 1-3 Complete, Phases 4-13 Pending)  
**Target**: Make app responsive across phones (compact), tablets (medium), and large tablets/desktop (expanded)

## Overview

This document tracks the implementation status of adaptive layouts for all 21 screens in Kids Math Pup Tutor.

**Current State**:
- ✅ **Foundation Complete** (Phase 1): Window size utilities, device posture detection, and adaptive layout types implemented
- ✅ **Navigation Infrastructure Complete** (Phases 2-3): `AdaptiveNavigationWrapper` handles bottom nav/rail/drawer switching for all 21 screens
- 🟨 **Partial Screen Adaptation In Progress**: 7 screens have started adaptive work (MAX_CONTENT_WIDTH centering, landscape support)
- ❌ **Screen Content Adaptation Incomplete**: Remaining 14 screens + full completion of 7 in-progress screens needed

**What's Working**:
- Navigation properly adapts (bottom nav on phones, rail on tablets, drawer on large tablets)
- 7 screens have MAX_CONTENT_WIDTH centering for tablet readability (basic tablet support)
- MathPracticeScreen has landscape side-by-side layout (most advanced screen)
- Tablet preview functions available for testing
- Window size utilities and device posture detection complete

**What's Still Needed**:
- Responsive grids: HomeScreen, StatsScreen, BadgesScreen, GameSelectionScreen card columns
- Full dual-pane layouts: Not yet implemented anywhere
- Landscape optimizations: Games and onboarding need landscape support
- ParentSettingsScreen and other new screens need tablet layouts
- Full responsive implementation (not just centering)

### Status Legend

| Symbol | Status | Description |
|--------|--------|-------------|
| ⬜ | Not Started | No adaptive layout work begun |
| 🟨 | In Progress | Work started but not complete |
| 🟩 | Implemented | Adaptive layout complete and tested |
| ⏸️ | Blocked | Waiting on dependencies or other work |

---

## Implementation Roadmap Summary

**Total Screens**: 21  
**Navigation Fully Adaptive**: 21/21 ✅  
**Content with Adaptive Work Started**: 7/21 🟨  
**Content Fully Adaptive**: 0/21 ❌  
**No Adaptive Work Yet**: 14/21 ⬜

**Completed Work** (Phases 1-3): ~6-8 hours ✅
- ✅ Material 3 Adaptive dependencies added
- ✅ `AdaptiveNavigationWrapper` created (bottom nav → rail → drawer)
- ✅ Window size utilities: `WindowStateUtils.kt`, `AdaptiveLayoutTypes.kt`
- ✅ Device posture detection for foldables
- ✅ Navigation type detection (compact/medium/expanded)
- ✅ 7 screens with MAX_CONTENT_WIDTH centering + MathPracticeScreen landscape layout

**In Progress** (Partial Screen Work): Completed foundation, awaiting content expansion
- 🟨 HomeScreen: MAX_CONTENT_WIDTH centering implemented
- 🟨 MathPracticeScreen: Landscape side-by-side layout + MAX_CONTENT_WIDTH centering (most complete)
- 🟨 StatsScreen: MAX_CONTENT_WIDTH centering
- 🟨 BadgesScreen: MAX_CONTENT_WIDTH centering  
- 🟨 GameSelectionScreen: MAX_CONTENT_WIDTH centering
- 🟨 SettingsScreen: MAX_CONTENT_WIDTH centering
- 🟨 ResultsScreen: MAX_CONTENT_WIDTH centering

**Remaining Work** (Phases 4-13 completion + new screens): ~20-33 hours  
**Prerequisites**: ✅ All complete (Phases 1-3 done)

---

## Screen-by-Screen Status

### Onboarding Flow

| # | Screen | Status | Notes | Phase |
|---|--------|--------|-------|-------|
| 1 | `OnboardingScreen` | ⬜ Not Started | Largest content - needs tablet centering and sizing | Phase 10 |
| 2 | `GradeSelectionScreen` | ⬜ Not Started | Grade selector UI needs scaling | Phase 10 |
| 3 | `NameEntryScreen` | ⬜ Not Started | Simple text field, low priority | Phase 10 |

**Gaps**: No tablet-optimized image sizing, no max-width content centering

---

### Main App Screens

| # | Screen | Status | Notes | Phase |
|---|--------|--------|-------|-------|
| 4 | `HomeScreen` | 🟨 In Progress | MAX_CONTENT_WIDTH (840dp) centering implemented, needs responsive grid for cards | Phase 4 |
| 5 | `OperationSelectorScreen` | ⬜ Not Started | Simple button grid, candidate for early implementation, **no MAX_CONTENT_WIDTH yet** | Phase 4 |

**Gaps**: OperationSelectorScreen and HomeScreen need responsive card grids (1 col → 2-3 cols)

---

### Practice Screens

| # | Screen | Status | Notes | Phase |
|---|--------|--------|-------|-------|
| 6 | `MathPracticeScreen` | 🟨 In Progress | Landscape side-by-side layout (Row with left problem/feedback, right answer input) + MAX_CONTENT_WIDTH (500dp) centering implemented - **Most Complete Screen** | Phase 5 |
| 7 | `ResultsScreen` | 🟨 In Progress | MAX_CONTENT_WIDTH (700dp) centering implemented, needs dual-pane enhancement | Phase 11 |

**Gaps**: ResultsScreen needs full dual-pane (results + problem review side-by-side)

---

### Statistics & Achievements

| # | Screen | Status | Notes | Phase |
|---|--------|--------|-------|-------|
| 8 | `StatsScreen` | 🟨 In Progress | MAX_CONTENT_WIDTH centering implemented, needs responsive card grid | Phase 6 |
| 9 | `AccuracyDetailsScreen` | ⬜ Not Started | Child of StatsScreen, inherits layout needs | Phase 6 |
| 10 | `BadgesScreen` | 🟨 In Progress | MAX_CONTENT_WIDTH centering implemented, needs responsive column grid | Phase 7 |

**Gaps**: Responsive grids not fully implemented, no list+detail for badge inspector

---

### Settings Screens

| # | Screen | Status | Notes | Phase |
|---|--------|--------|-------|-------|
| 11 | `SettingsScreen` | 🟨 In Progress | MAX_CONTENT_WIDTH centering implemented, done for Phase 9 | Phase 9 |
| 12 | `AudioHapticSettingsScreen` | ⬜ Not Started | Child of SettingsScreen, could be dual-pane on expanded | Phase 9 |
| 13 | `ParentSettingsScreen` | ⬜ Not Started | **NEW in v1.19.0** - Dialog-heavy, needs tablet optimization | Phase 9 |

**Gaps**: Dual-pane settings layout not implemented

---

### Game Screens

| # | Screen | Status | Notes | Phase |
|---|--------|--------|-------|-------|
| 14 | `GameSelectionScreen` | 🟨 In Progress | MAX_CONTENT_WIDTH centering implemented, needs responsive card grid | Phase 8 |
| 15 | `MathRaceScreen` | ⬜ Not Started | Game content needs landscape optimization | Phase 8 |
| 16 | `NumberSequenceScreen` | ⬜ Not Started | Game content needs landscape optimization | Phase 8 |
| 17 | `MemoryMatchScreen` | ⬜ Not Started | Game grid needs to expand for tablets | Phase 8 |

**Gaps**: Responsive game card grids, landscape game optimizations not done

---

### Custom Challenges

| # | Screen | Status | Notes | Phase |
|---|--------|--------|-------|-------|
| 18 | `ImportChallengeScreen` | ⬜ Not Started | **NEW in v1.17.0** - JSON input UI could be wider on tablets | Phase 2-3 |
| 19 | `ParentChallengesScreen` | ⬜ Not Started | **NEW in v1.16.0** - Challenge list could show multiple cards per row | Phase 2-3 |

**Gaps**: No responsive challenge lists, no list+detail layout

---

### Developer/Debug Screens

| # | Screen | Status | Notes | Phase |
|---|--------|--------|-------|-------|
| 20 | `DeveloperPortalScreen` | ⬜ Not Started | Debug-only, low priority | Phase 2-3 |
| 21 | `ColorPaletteViewerScreen` | ⬜ Not Started | Debug-only, low priority | Phase 2-3 |

**Gaps**: Debug screens don't need full adaptive treatment

---

## Priority Implementation Order

### Tier 1 - High Priority (Finish In-Progress Content)
1. **HomeScreen** (Phase 4 continuation) - Complete responsive grids/cards (beyond MAX_CONTENT_WIDTH centering)
2. **MathPracticeScreen** (Phase 5 continuation) - Expand tablet layout beyond current landscape split (e.g., richer tablet UX)
3. **StatsScreen** (Phase 6 continuation) - Add responsive stat card grids (1 → 2-3 cols) beyond centering
4. **BadgesScreen** (Phase 7 continuation) - Implement responsive badge grid columns (3 → 4-5 → 6+)

**Why first**: These screens already have partial adaptive work and are heavily used.

### Tier 2 - Medium Priority (Complete Remaining Core Screens)
5. **GameSelectionScreen** (Phase 8 continuation) - Responsive grid for game cards (beyond centering)
6. **Settings + Audio/Haptic** (Phase 9) - Dual-pane settings on expanded + tablet spacing
7. **ResultsScreen** (Phase 11 continuation) - Dual-pane results + review layout (beyond centering)

### Tier 3 - Medium-Low Priority (Cover Remaining Screens)
8. **OperationSelectorScreen** - Responsive grid layout
9. **AccuracyDetailsScreen** - Responsive layout consistent with Stats
10. **Games** (MathRaceScreen, NumberSequenceScreen, MemoryMatchScreen) - Landscape/tablet optimizations
11. **Custom Challenges** (ImportChallengeScreen, ParentChallengesScreen) - Responsive lists / max-width / grids
12. **ParentSettingsScreen** - Tablet-friendly layout for dialogs and content density

### Tier 4 - Lower Priority (Polish & Edge Cases)
13. **Foldable posture support** (Phase 12) - Use `DevicePosture` (book/separating) to avoid hinge overlap
14. **Debug screens** (DeveloperPortalScreen, ColorPaletteViewerScreen) - Optional tablet tuning
15. **Testing & documentation** (Phase 13) - Tablet previews, emulator coverage, docs updates

---

## Critical Dependencies

### Phase 1 - COMPLETE ✅
- ✅ Material 3 Adaptive libraries added to `gradle/libs.versions.toml`
  - ✅ `androidx-window: 1.5.1`
  - ✅ `androidx-compose-material3-adaptive`
  - ✅ `androidx-compose-material3-adaptive-layout`
  - ✅ `androidx-compose-material3-adaptive-navigation`
  - ✅ `androidx-compose-material3-adaptive-navigationSuite`
  - ✅ `androidx-compose-materialWindow`

- ✅ Utility files created
  - ✅ `ui/utils/WindowStateUtils.kt` - Device posture detection, foldable state checks
  - ✅ `ui/utils/AdaptiveLayoutTypes.kt` - `NavigationType`, `ContentType`, `NavigationContentPosition` enums

### Phase 2-3 - COMPLETE ✅
- ✅ `AdaptiveNavigationWrapper.kt` - Switches between bottom nav/rail/drawer based on window size
- ✅ Navigation infrastructure working and integrated with Circuit Navigator
- ✅ Support for detecting current screen and highlighting correct destination
- ✅ Proper window insets handling to avoid double padding

### Phases 4-13 - READY TO START
- ✅ All foundation work complete
- ✅ Ready for individual screen content adaptations
- Dependencies: Each phase depends only on Phases 1-3 being complete (which they are)

---

## What's Already Implemented vs. What's Pending

### Navigation Layer - DONE ✅

The **navigation infrastructure is fully adaptive** and working on all 21 screens:

```
Compact (< 600dp)        Medium (600-840dp)      Expanded (> 840dp)
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│   Content       │     │Nav│   Content    │     │Nav│    Content       │
│   Content       │     │Rail   Content    │     │Drawer    Content     │
│   Content       │     │   │   Content    │     │   │    Content       │
├─────────────────┤     │   │   Content    │     │   │    Content       │
│ Bottom Nav Bar  │     └──────────────────┘     └─────────────────────┘
└─────────────────┘
```

**Implemented Components**:
- `AdaptiveNavigationWrapper.kt` - Main wrapper that detects window size and renders appropriate nav
- `AppBottomNavigation()` - Bottom bar for phones (< 600dp)
- `AppNavigationRail()` - Side rail for tablets (600-840dp)
- `AppPermanentNavigationDrawer()` - Permanent sidebar for large tablets/desktop (> 840dp)
- `WindowStateUtils.kt` - Device posture detection for foldables (BookPosture, Separating, NormalPosture)
- `AdaptiveLayoutTypes.kt` - Enums for NavigationType, ContentType, NavigationContentPosition

### Screen Content - PARTIAL ✅/❌

**7 Screens with MAX_CONTENT_WIDTH Centering** (Basic tablet support, not full responsive grids):
1. ✅ **HomeScreen** - MAX_CONTENT_WIDTH (840dp) - content centered on wide screens
2. ✅ **MathPracticeScreen** - MAX_CONTENT_WIDTH (500dp) + **landscape side-by-side layout** - Most advanced implementation
3. ✅ **StatsScreen** - MAX_CONTENT_WIDTH (840dp) - content centered on wide screens
4. ✅ **BadgesScreen** - MAX_CONTENT_WIDTH (840dp) - content centered on wide screens
5. ✅ **GameSelectionScreen** - MAX_CONTENT_WIDTH (700dp) - content centered on wide screens
6. ✅ **SettingsScreen** - MAX_CONTENT_WIDTH (600dp) - content centered on wide screens
7. ✅ **ResultsScreen** - MAX_CONTENT_WIDTH (700dp) - content centered on wide screens

**What MAX_CONTENT_WIDTH Does** (Current Implementation):
- Prevents content from stretching to full screen width on tablets (good for readability)
- Centers content horizontally on wider screens
- Improves tablet UX by constraining content width
- **Does NOT include**: Responsive grids, dual-pane layouts, or responsive columns

**What These 7 Screens Still Need** (For Full Responsive Implementation):
- Responsive grids: Change column count (1 col phone → 2-3 cols tablet)
- Dual-pane layouts: Side-by-side detail views on expanded screens
- Landscape optimization: Special layouts for landscape orientation
- Tablet-specific component sizing: Larger buttons, more spacing on tablets
- MathPracticeScreen specifically needs: Enhanced tablet UX beyond current landscape layout

**14 Screens with No Adaptive Work**:
- **Onboarding Flow** (3): OnboardingScreen, GradeSelectionScreen, NameEntryScreen
- **Other Main Screens** (1): OperationSelectorScreen, AccuracyDetailsScreen (2 total)
- **Games** (3): MathRaceScreen, NumberSequenceScreen, MemoryMatchScreen
- **Custom Challenges** (2): ImportChallengeScreen, ParentChallengesScreen
- **Settings Sub-screens** (2): AudioHapticSettingsScreen, ParentSettingsScreen
- **Debug Screens** (2): DeveloperPortalScreen, ColorPaletteViewerScreen

**Current User Experience**:
```
Phone (current)           Tablet (current)          Tablet (desired)
┌──────────────┐         ┌──────────────────┐      ┌──────────────────┐
│ Content      │         │ Content          │      │ Content   │ More │
│ Content      │         │ Content          │      │ Content   │Content
│ Content      │         │ Content          │      │ Content   │      │
│              │         │ (centered,       │      │ (responsive)     │
│              │         │  max-width)      │      │ (dual-pane,      │
└──────────────┘         └──────────────────┘      │  grids)          │
                                                    └──────────────────┘
```

---

## Known Gaps & Missing Features

### Navigation
- [x] **Adaptive navigation wrapper** - Bottom nav/rail/drawer switching via `AdaptiveNavigationWrapper`
- [x] **Current screen highlighting** - Destination highlighting synced with Circuit backstack
- [x] **Foldable posture detection utilities** - `WindowStateUtils` provides `DevicePosture` (not yet applied to layouts)
- [ ] **Hinge-aware layouts** - No UI avoids hinge/separating fold features yet
- [ ] **Consistent landscape content layouts** - Only MathPractice has a landscape-specific layout

### Layouts
- [x] **Max-width content centering (partial)** - Implemented on 7 screens via `MAX_CONTENT_WIDTH` + `widthIn(max = ...)`
- [ ] **Max-width/centering on remaining screens** - 14 screens still full-width on tablets
- [ ] **Responsive grids** - Key screens still single-column (no 1 → 2/3 column behavior based on width)
- [ ] **Dual-pane layouts** - No list+detail / side-by-side detail panes on expanded screens
- [ ] **Tablet-sized components** - No tablet-specific sizing for key interactive components (e.g., NumberPad)

### Content
- [ ] **Tablet-optimized onboarding visuals** - Images/content sizing not adjusted for tablets
- [ ] **Games tablet/landscape UX** - MathRace/NumberSequence/MemoryMatch are not optimized for large/landscape screens
- [ ] **Settings dual-pane** - Settings + sub-screens not shown side-by-side on expanded screens
- [ ] **Badges list+detail** - Badge detail remains modal-based; no detail pane on expanded screens

### Testing
- [x] **Tablet previews (partial)** - Some screens include tablet-sized previews (e.g., Home/Stats)
- [ ] **Tablet previews for all screens** - Add preview coverage for remaining screens
- [ ] **Foldable testing** - No emulator/device testing of book/separating posture UX
- [ ] **Landscape rotation testing** - Orientation transitions not validated across all screens

---

## Implementation Checklist

### Prerequisites - COMPLETE ✅
- ✅ Review ADAPTIVE_LAYOUT.md implementation plan
- ✅ Review Reply sample from Google Compose Samples
- ✅ Gather team approval on Tier 1 priorities

### Phase 1: Dependencies & Utilities - COMPLETE ✅
- ✅ Update `gradle/libs.versions.toml`
- ✅ Update `app/build.gradle.kts`
- ✅ Create `WindowStateUtils.kt`
- ✅ Create `AdaptiveLayoutTypes.kt`
- ✅ Window size detection working via `currentWindowAdaptiveInfo()`
- ✅ Device posture detection for foldables

### Phase 2: Navigation Infrastructure - COMPLETE ✅
- ✅ Create `TopLevelDestination.kt`
- ✅ Create `AdaptiveNavigationWrapper.kt`
- ✅ `AppBottomNavigation()` - Bottom nav for compact
- ✅ `AppNavigationRail()` - Rail for medium
- ✅ `AppPermanentNavigationDrawer()` - Drawer for expanded
- ✅ Integrate with Circuit Navigator
- ✅ Navigation switching at different screen sizes working

### Phase 3: App Shell - COMPLETE ✅
- ✅ `AdaptiveNavigationWrapper` ready for use in screens
- ✅ Synchronize Circuit backstack with nav highlighting
- ✅ Proper window insets handling
- ✅ Responsive navigation infrastructure tested

### Phases 4-13: Individual Screen Adaptations - READY TO START
- [ ] Follow same pattern for each screen
- [ ] Add tablet preview functions
- [ ] Test on compact, medium, and expanded sizes
- [ ] Test landscape orientations
- [ ] Update NAVIGATION.md with new navigation structure

### Testing & Release
- [ ] Run full pre-commit check with adaptive layouts
- [ ] Test on emulators: phone, tablet, foldable
- [ ] Test all orientation transitions
- [ ] Performance testing for layout thrashing
- [ ] Accessibility testing (TalkBack on all layouts)
- [ ] Update CHANGELOG.md
- [ ] Create GitHub issues for any remaining gaps

---

## GitHub Issues to Create

Foundation phases are complete. Create these issues to continue with screen content adaptation:

### Issue 1: HomeScreen Responsive Layout (NEXT)
**Title**: `[Feature] Make HomeScreen Responsive for Tablets - Phase 4`  
**Description**: Add side-by-side stats and badges layout for tablets  
**Labels**: `enhancement`, `ui`, `high-priority`, `adaptive-layout`
**Estimated Effort**: 2-3 hours  
**Details**:
- On compact (phones): Keep current vertical scrolling layout
- On medium/expanded (tablets): Show stats cards and badges side-by-side
- Use responsive grid for action buttons
- Add tablet-optimized preview functions

### Issue 2: MathPracticeScreen Dual-Pane
**Title**: `[Feature] Add Dual-Pane Layout to MathPracticeScreen - Phase 5`  
**Description**: Show problem and history side-by-side on tablets  
**Labels**: `enhancement`, `ui`, `high-priority`, `adaptive-layout`
**Estimated Effort**: 3-4 hours  
**Details**:
- On compact: Keep current stacked layout
- On medium: Side-by-side problem and answer area
- On expanded: Problem on left, answer area/history on right
- Tablet-sized NumberPad buttons
- Support landscape orientation

### Issue 3: Stats/Badges Responsive Grids
**Title**: `[Feature] Make Stats and Badges Responsive - Phases 6-7`  
**Description**: Implement adaptive column grids for statistics and badges  
**Labels**: `enhancement`, `ui`, `adaptive-layout`
**Estimated Effort**: 4-6 hours  
**Details**:
- StatsScreen: 1 col → 2-3 cols based on screen size
- BadgesScreen: 3 → 4-5 → 6+ badges per row
- Operation stats grid responsive layout
- List+detail layout for badge inspector on expanded

### Issue 4: Games Responsive Layouts
**Title**: `[Feature] Optimize Game Screens for Tablets - Phase 8`  
**Description**: Make game selection and game screens responsive  
**Labels**: `enhancement`, `ui`, `adaptive-layout`
**Estimated Effort**: 3-4 hours  
**Depends On**: Issue 1  
**Details**:
- GameSelectionScreen: Multiple game cards per row
- Game content centered on larger screens
- Landscape optimizations for games
- NumberSequenceScreen, MemoryMatchScreen grids

### Issue 5: Settings Tablet Optimization
**Title**: `[Feature] Optimize Settings for Tablets - Phase 9`  
**Description**: Add max-width and dual-pane layout for settings screens  
**Labels**: `enhancement`, `ui`, `adaptive-layout`
**Estimated Effort**: 2-3 hours  
**Details**:
- Max-width constraints for settings content on expanded
- Dual-pane: Main settings + Audio/Haptic on expanded screens
- ParentSettingsScreen tablet optimization
- Better spacing and padding for tablet screens

### Issue 6: Remaining Screens
**Title**: `[Feature] Adaptive Layout for Onboarding, Results, and Challenge Screens - Phases 10-11`  
**Description**: Complete adaptive layout work for remaining screens  
**Labels**: `enhancement`, `ui`, `adaptive-layout`
**Estimated Effort**: 4-5 hours  
**Details**:
- OnboardingScreen: Tablet-optimized image sizing and centering
- GradeSelectionScreen, NameEntryScreen: Responsive layouts
- ResultsScreen: Dual-pane results + problem review
- ImportChallengeScreen, ParentChallengesScreen responsive lists
- DeveloperPortalScreen, ColorPaletteViewerScreen debug optimizations

### Issue 7: Foldable Device Support
**Title**: `[Feature] Add Foldable Device Support - Phase 12`  
**Description**: Detect device posture and adapt layouts for foldable phones  
**Labels**: `enhancement`, `ui`, `accessibility`, `adaptive-layout`
**Estimated Effort**: 3-4 hours  
**Details**:
- Use `WindowStateUtils.getDevicePosture()` to detect book/separating postures
- Avoid placing critical content over hinge
- TwoPane component for book mode
- Test with Galaxy Fold emulator profile

### Issue 8: Adaptive Layout Testing & Polish
**Title**: `[Feature] Testing, Documentation, and Polish - Phase 13`  
**Description**: Add tablet previews, test all sizes, update docs  
**Labels**: `enhancement`, `testing`, `documentation`, `adaptive-layout`
**Estimated Effort**: 4-5 hours  
**Details**:
- Add @Preview functions for tablet sizes (700dp, 1100dp widths)
- Test on emulators: phone, tablet, foldable
- Landscape orientation testing
- Performance testing for layout thrashing
- Accessibility testing (TalkBack)
- Update NAVIGATION.md with adaptive structure
- Update CHANGELOG.md with adaptive layout changes

---

## Recommendations for Next Steps

1. **Foundation work is complete!** ✅ Phases 1-3 are done and working
2. **Next Priority**: Start with Tier 1 screens (Phases 4-5)
   - Phase 4: HomeScreen - Sidebar stats/badges layout for tablets
   - Phase 5: MathPracticeScreen - Dual-pane problem + history layout
3. **Create GitHub issues** for Phases 4-13 using the templates in this document
4. **Assign Phase 4** as the first screen adaptation work
5. **Plan implementation timeline** based on team capacity (30-41 hours remaining for all 21 screens)
6. **Consider phased rollout**:
   - Release v1.20.0 with Phases 4-5 (home + practice optimized)
   - Release v1.21.0 with Phases 6-11 (stats, badges, games, settings)
   - Release v1.22.0 with Phases 12-13 (foldable support + polish)

**Key Insight**: The hardest part (setting up adaptive navigation infrastructure) is already done! Remaining work is straightforward screen-by-screen content adaptation.

---

## Related Documentation

- [ADAPTIVE_LAYOUT.md](./ADAPTIVE_LAYOUT.md) - Detailed implementation plan
- [NAVIGATION.md](./NAVIGATION.md) - Current navigation architecture
- [Reply Sample](https://github.com/android/compose-samples/tree/main/Reply) - Reference implementation
- [Material 3 Adaptive Design](https://m3.material.io/foundations/adaptive-design/overview) - Design guidelines
