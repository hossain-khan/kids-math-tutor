# Navigation Architecture

This document describes all Circuit screens in the Kids Math Pup Tutor app and their navigation relationships.

## Overview

The app uses [Circuit](https://slackhq.github.io/circuit/) for navigation with a Unidirectional Data Flow (UDF) architecture. All screens implement the `Screen` interface and are navigated using `Navigator.goTo()` or `Navigator.resetRoot()`.

## Screen Inventory

The app contains **13 Circuit screens** organized by feature:

| # | Screen | Location | Type | Description |
|---|--------|----------|------|-------------|
| 1 | `OnboardingScreen` | `ui/onboarding/` | `data object` | Welcome screen for new users |
| 2 | `GradeSelectionScreen` | `ui/onboarding/` | `data class` | Grade level selection (K-2) |
| 3 | `NameEntryScreen` | `ui/onboarding/` | `data class` | Child's name input |
| 4 | `HomeScreen` | `ui/home/` | `data object` | Main hub with activity options |
| 5 | `OperationSelectorScreen` | `ui/operationselector/` | `data object` | Math operation selection |
| 6 | `MathPracticeScreen` | `ui/mathpractice/` | `data class` | Active math problem solving |
| 7 | `ResultsScreen` | `ui/practiceresults/` | `data class` | Practice session results |
| 8 | `StatsScreen` | `ui/stats/` | `data object` | User statistics overview |
| 9 | `BadgesScreen` | `ui/badges/` | `data object` | Achievement badges display |
| 10 | `SettingsScreen` | `ui/settings/` | `data object` | App settings |
| 11 | `AudioHapticSettingsScreen` | `ui/settings/` | `data object` | Sound & haptic preferences |
| 12 | `GameSelectionScreen` | `ui/games/` | `data object` | Mini-game selection |
| 13 | `MathRaceScreen` | `ui/mathrace/` | `data object` | Math race mini-game |
| 14 | `ImportChallengeScreen` | `ui/importchallenge/` | `data class` | Import custom challenges via QR/JSON |
| 15 | `ParentChallengesScreen` | `ui/parentchallenges/` | `data object` | Manage custom parent challenges |

## Navigation Graph

### Entry Points

The app has two entry points defined in `MainActivity.kt`:

- **New users**: `OnboardingScreen` (when `isOnboardingCompleted = false`)
- **Returning users**: `HomeScreen` (when `isOnboardingCompleted = true`)

### Visual Navigation Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           ONBOARDING FLOW                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  OnboardingScreen ──goTo──► GradeSelectionScreen ──goTo──► NameEntryScreen  │
│                                                                    │        │
│                                                              resetRoot      │
│                                                                    │        │
│                                                                    ▼        │
├─────────────────────────────────────────────────────────────────────────────┤
│                              MAIN APP FLOW                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│                              HomeScreen                                     │
│                                  │                                          │
│           ┌──────────┬──────────┼──────────┬──────────┐                     │
│           │          │          │          │          │                     │
│         goTo       goTo       goTo       goTo       goTo                    │
│           │          │          │          │          │                     │
│           ▼          ▼          ▼          ▼          ▼                     │
│    ┌──────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌─────────────┐            │
│    │Operation │ │ Stats  │ │ Badges │ │Settings│ │    Game     │            │
│    │ Selector │ │ Screen │ │ Screen │ │ Screen │ │  Selection  │            │
│    └────┬─────┘ └────────┘ └────────┘ └───┬────┘ └──────┬──────┘            │
│         │                            ┌────┴────┐        │                   │
│    ┌────┴────┐                       │         │      goTo                  │
│    │         │                     goTo      goTo       │                   │
│  goTo      goTo                      │         │        ▼                   │
│    │         │                       ▼         ▼   ┌──────────┐             │
│    ▼         ▼               ┌─────────────┐ ┌───────────┐│ MathRace │      │
│ ┌──────┐ ┌────────┐          │AudioHaptic │ │   Grade   ││  Screen  │       │
│ │ Math │ │ Stats  │          │  Settings  │ │ Selection │└──────────┘       │
│ │Pract.│ │ Screen │          └─────────────┘ └───────────┘                  │
│ └──┬───┘ └────────┘                                                         │
│    │                                                                        │
│  goTo                                                                       │
│    │                                                                        │
│    ▼                                                                        │
│ ┌────────┐                                                                  │
│ │Results │                                                                  │
│ │ Screen │                                                                  │
│ └───┬────┘                                                                  │
│     │                                                                       │
│ resetRoot ──────────────────────────► HomeScreen                            │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                      CUSTOM CHALLENGES FLOW                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  MainActivity (share intent)                                                │
│           │                                                                 │
│         Initial                                                             │
│           │                                                                 │
│           ▼                                                                 │
│  ImportChallengeScreen ──goTo──► ParentChallengesScreen                     │
│           │                                                                 │
│           └──────────────► pop back ──────────────► HomeScreen              │
│                                                                             │
│  Or from HomeScreen: HomeScreen ──goTo──► ParentChallengesScreen            │
│                                                   │                         │
│                                                 pop back                     │
│                                                   │                         │
│                                                   ▼                         │
│                                               HomeScreen                    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Navigation Details

### Navigation Methods

| Method | Usage | Effect |
|--------|-------|--------|
| `navigator.goTo(screen)` | Forward navigation | Pushes screen onto backstack |
| `navigator.resetRoot(screen)` | Reset navigation | Clears backstack, sets new root |
| `navigator.pop()` | Back navigation | Pops current screen from backstack |

### Screen-by-Screen Navigation

| Screen | Navigated From | Method | Navigates To |
|--------|---------------|--------|--------------|
| `OnboardingScreen` | MainActivity (root) | Initial | `GradeSelectionScreen` |
| `GradeSelectionScreen` | OnboardingScreen, SettingsScreen | `goTo` | `NameEntryScreen` (onboarding) or back via pop (settings) |
| `NameEntryScreen` | GradeSelectionScreen | `goTo` | `HomeScreen` |
| `HomeScreen` | NameEntryScreen, ResultsScreen, ImportChallengeScreen | `resetRoot` | Multiple screens |
| `OperationSelectorScreen` | HomeScreen | `goTo` | `MathPracticeScreen`, `StatsScreen` |
| `MathPracticeScreen` | OperationSelectorScreen | `goTo` | `ResultsScreen` |
| `ResultsScreen` | MathPracticeScreen | `goTo` | `HomeScreen` |
| `StatsScreen` | HomeScreen, OperationSelectorScreen | `goTo` | — |
| `BadgesScreen` | HomeScreen | `goTo` | — |
| `SettingsScreen` | HomeScreen | `goTo` | `AudioHapticSettingsScreen`, `GradeSelectionScreen` |
| `AudioHapticSettingsScreen` | SettingsScreen | `goTo` | — |
| `GameSelectionScreen` | HomeScreen | `goTo` | `MathRaceScreen` |
| `MathRaceScreen` | GameSelectionScreen | `goTo` | — |
| `ImportChallengeScreen` | MainActivity (share intent), HomeScreen | `goTo` or Initial | `ParentChallengesScreen` or back |
| `ParentChallengesScreen` | HomeScreen, ImportChallengeScreen | `goTo` | back via pop |

## Screen Types

### Data Object Screens
Screens without parameters use `data object`:
```kotlin
data object HomeScreen : Screen {
    data class State(...) : CircuitUiState
    sealed interface Event : CircuitUiEvent { ... }
}
```

### Data Class Screens
Screens with parameters use `data class`:
```kotlin
@Parcelize
data class MathPracticeScreen(
    val problemCount: Int = 10,
    val operation: MathOperation = MathOperation.ADDITION
) : Screen {
    data class State(...) : CircuitUiState
    sealed interface Event : CircuitUiEvent { ... }
}
```

## Adding New Screens

To add a new navigable screen:

1. **Create the screen** in the appropriate `ui/` subdirectory
2. **Define Screen, State, and Events** following Circuit patterns
3. **Create Presenter** with `@CircuitInject` annotation
4. **Create UI composable** with `@CircuitInject` annotation
5. **Add navigation** from an existing screen using `navigator.goTo(NewScreen)`
6. **Update this document** with the new screen

See [copilot-instructions.md](/.github/copilot-instructions.md) for detailed implementation patterns.
