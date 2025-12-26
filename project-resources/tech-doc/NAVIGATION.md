# Navigation Architecture

This document describes all Circuit screens in the Kids Math Pup Tutor app and their navigation relationships.

## Overview

The app uses [Circuit](https://slackhq.github.io/circuit/) for navigation with a Unidirectional Data Flow (UDF) architecture. All screens implement the `Screen` interface and are navigated using `Navigator.goTo()` or `Navigator.resetRoot()`.

## Screen Inventory

The app contains **20 Circuit screens** organized by feature:

| # | Screen | Location | Type | Description |
|---|--------|----------|------|-------------|
| 1 | `OnboardingScreen` | `ui/onboarding/` | `data object` | Welcome screen for new users |
| 2 | `GradeSelectionScreen` | `ui/onboarding/` | `data class` | Grade level selection (K-2) |
| 3 | `NameEntryScreen` | `ui/onboarding/` | `data class` | Child's name input |
| 4 | `HomeScreen` | `ui/home/` | `data object` | Main hub with activity options |
| 5 | `OperationSelectorScreen` | `ui/operationselector/` | `data object` | Math operation selection (Practice) |
| 6 | `MathPracticeScreen` | `ui/mathpractice/` | `data class` | Active math problem solving |
| 7 | `ResultsScreen` | `ui/practiceresults/` | `data class` | Practice session results |
| 8 | `StatsScreen` | `ui/stats/` | `data object` | User statistics overview |
| 9 | `AccuracyDetailsScreen` | `ui/stats/accuracydetails/` | `data object` | Daily accuracy breakdown |
| 10 | `BadgesScreen` | `ui/badges/` | `data object` | Achievement badges display |
| 11 | `SettingsScreen` | `ui/settings/` | `data object` | App settings |
| 12 | `AudioHapticSettingsScreen` | `ui/settings/` | `data object` | Sound & haptic preferences |
| 13 | `GameSelectionScreen` | `ui/games/` | `data object` | Mini-game selection |
| 14 | `MathRaceScreen` | `ui/mathrace/` | `data class` | Math race mini-game |
| 15 | `NumberSequenceScreen` | `ui/numbersequence/` | `data class` | Number sequence mini-game |
| 16 | `MemoryMatchScreen` | `ui/memorymatch/` | `data class` | Memory match mini-game |
| 17 | `ImportChallengeScreen` | `ui/importchallenge/` | `data class` | Import custom challenges via QR/JSON |
| 18 | `ParentChallengesScreen` | `ui/parentchallenges/` | `data object` | Manage custom parent challenges |
| 19 | `DeveloperPortalScreen` | `ui/devportal/` | `data object` | Developer tools & testing |
| 20 | `ColorPaletteViewerScreen` | `ui/devportal/` | `data object` | Color palette visualization (debug) |

## Navigation Graph

### Entry Points

The app has two entry points defined in `MainActivity.kt`:

- **New users**: `OnboardingScreen` (when `isOnboardingCompleted = false`)
- **Returning users**: `HomeScreen` (when `isOnboardingCompleted = true`)

### Top-Level Navigation Structure

The app has **4 main feature areas** accessible from `HomeScreen`:

1. **Practice** (`OperationSelectorScreen`) - Complete math practice with operation selection
2. **Stats** (`StatsScreen`) - View performance statistics and accuracy details
3. **Badges** (`BadgesScreen`) - View achievement badges
4. **Settings** (`SettingsScreen`) - App configuration and preferences

Additionally:
- **Games** (`GameSelectionScreen`) - Access mini-games (Math Race, Number Sequence, Memory Match)
- **Custom Challenges** (`ParentChallengesScreen`) - Manage imported custom challenges

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
│                           MAIN APP FLOW                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│                              HomeScreen                                     │
│                 ┌────────────────┼────────────────┐                         │
│           ┌─────┴──────┐    ┌────┴─────┐    ┌────┴─────┐                  │
│           │            │    │          │    │          │                  │
│         goTo         goTo  goTo       goTo  goTo       goTo                │
│           │            │    │          │    │          │                  │
│           ▼            ▼    ▼          ▼    ▼          ▼                  │
│   ┌──────────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌──────────────┐      │
│   │  Operation   │ │ Stats  │ │ Badges │ │Settings│ │    Games     │      │
│   │  Selector    │ │ Screen │ │ Screen │ │ Screen │ │  Selection   │      │
│   │ (Practice)   │ └────┬───┘ └────────┘ └───┬────┘ └──────┬───────┘      │
│   └────┬─────────┘      │                  ┌──┴──┐        │               │
│        │                │                  │     │        │               │
│      goTo             goTo               goTo   goTo    goTo              │
│        │                │                  │     │        │               │
│        ▼                ▼            ┌─────▼──┐ │        ▼               │
│   ┌─────────┐   ┌─────────────────┐│AudioHap.│ │   ┌──────────┐          │
│   │  Math   │   │   Accuracy      ││Settings │ │   │ MathRace │          │
│   │Practice │   │   Details       │└─────────┘ │   │  Screen  │          │
│   └────┬────┘   │   Screen        │            │   └──────────┘          │
│        │        └─────────────────┘            │                         │
│      goTo                                    goTo                        │
│        │                                       │                         │
│        ▼                                       ▼                         │
│   ┌─────────┐                          ┌──────────────┐                  │
│   │ Results │                          │GradeSelection│                  │
│   │ Screen  │                          │    Screen    │                  │
│   └────┬────┘                          └──────────────┘                  │
│        │                                                                 │
│   resetRoot ────────────────────► HomeScreen                             │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                   CUSTOM CHALLENGES & GAMES FLOW                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  HomeScreen ──goTo──► GameSelectionScreen                               │
│                            │                                            │
│                          goTo (for each mini-game)                      │
│                            │                                            │
│                    ┌───────┼────────┐                                   │
│                    ▼       ▼        ▼                                   │
│            ┌────────────┐ ┌──────────────┐ ┌──────────────┐             │
│            │ MathRace   │ │   Number     │ │   Memory     │             │
│            │  Screen    │ │  Sequence    │ │    Match     │             │
│            │            │ │   Screen     │ │    Screen    │             │
│            └────────────┘ └──────────────┘ └──────────────┘             │
│                                                                          │
│  HomeScreen ──goTo──► ParentChallengesScreen                            │
│                            │                                            │
│                          pop back                                        │
│                            │                                            │
│                            ▼                                            │
│                       HomeScreen                                        │
│                                                                          │
│  (Share Intent) ──► ImportChallengeScreen                               │
│                            │                                            │
│                          goTo                                           │
│                            │                                            │
│                            ▼                                            │
│                   ParentChallengesScreen                                │
│                                                                          │
│  (Deeplink URL) ──► ImportChallengeScreen                               │
│                    mathpup://import?json=...                           │
│                            │                                            │
│                          goTo                                           │
│                            │                                            │
│                            ▼                                            │
│                   ParentChallengesScreen                                │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

## Deep Linking for Challenge Import

The app supports Android deep links for one-click challenge import from the web-based worksheet creator. This enables seamless integration between the web app and mobile app.

### Deep Link Scheme

**Format**: `mathpup://import?json=<url-encoded-json>`

**Example**:
```
mathpup://import?json=%7B%22type%22%3A%22explicit%22%2C%22title%22%3A%22Addition%20Practice%22%2C%22problems%22%3A%5B%7B%22operand1%22%3A5%2C%22operand2%22%3A3%2C%22operation%22%3A%22addition%22%7D%5D%7D
```

### How It Works

#### Android Implementation

1. **Intent Filter Registration** (`AndroidManifest.xml`):
   ```xml
   <intent-filter>
       <action android:name="android.intent.action.VIEW" />
       <category android:name="android.intent.category.DEFAULT" />
       <category android:name="android.intent.category.BROWSABLE" />
       <data
           android:scheme="mathpup"
           android:host="import" />
   </intent-filter>
   ```

2. **Deep Link Handling** (`MainActivity.kt`):
   - Checks for incoming deep link intent in `onCreate()` and `onNewIntent()`
   - Extracts JSON from URI query parameter using `DeeplinkHandler.extractJsonFromDeeplink()`
   - Navigates to `ImportChallengeScreen` with the challenge data
   - Falls back to share intent or normal app start if no deep link

3. **DeeplinkHandler Utility** (`deeplink/DeeplinkHandler.kt`):
   - `extractJsonFromDeeplink(uri: Uri?): String?` - Safely decodes URL-encoded JSON from deep link
   - `generateDeeplink(jsonData: String): String` - Encodes challenge JSON as deep link URI
   - Handles error cases gracefully with Timber logging

#### Web App Integration

The web-based worksheet creator provides one-click import buttons for Android users:

1. **Device Detection**: `isLikelyAndroidDevice()` detects Android via user agent
2. **Deep Link Generation**: `generateDeeplink(jsonData)` creates the `mathpup://` URI
3. **Import Triggers**:
   - "Open in Math Pup App" button on Result page (Android only)
   - "📱 Open in App" buttons on template cards (Android only)
4. **User Experience**:
   - Clicking button generates deep link and sets `window.location.href`
   - If app is installed: Opens app and navigates to `ImportChallengeScreen`
   - If app is not installed: Deep link fails gracefully, user can copy JSON manually

### Data Flow

```
Web App (Result.tsx)
    │
    ├─► isLikelyAndroidDevice() → Check if Android
    │
    ├─► generateDeeplink(challengeJson)
    │   └─► URL encode challenge JSON
    │
    └─► window.location.href = deeplink
        │
        ├─► [App Installed] → mathpup:// intent received
        │   │
        │   └─► MainActivity.handleDeeplink()
        │       │
        │       └─► DeeplinkHandler.extractJsonFromDeeplink()
        │           │
        │           └─► Navigator.goTo(ImportChallengeScreen)
        │
        └─► [App Not Installed] → Deep link fails gracefully
            └─► User can still copy JSON or use other methods
```

### Technical Details

**URL Encoding**: JSON is encoded using `URLEncoder` (Android) and `encodeURIComponent` (JavaScript) to safely transmit special characters in URI query parameters.

**Error Handling**:
- Invalid scheme/host: Returns null
- Missing JSON parameter: Returns null
- Malformed JSON: Logged to Timber, empty string returned
- Circular references: Caught during JSON.stringify, empty string returned

**Security Considerations**:
- Only accepts challenges from `mathpup://import` scheme
- JSON is validated by `ImportChallengeScreen` against challenge schema
- No sensitive data exposed in URI (challenge data only)

### Usage Examples

#### From Web App Result Page
User creates a challenge in the worksheet creator → Clicks "Open in Math Pup App" → App opens and loads challenge automatically

#### From Template Cards
User selects a template → Clicks "📱 Open in App" → Challenge data is passed via deep link → Loaded in app

#### Manual Deep Link Testing
```
adb shell am start -W \
  -a android.intent.action.VIEW \
  -d "mathpup://import?json=%7B%22type%22%3A%22generated%22%2C%22title%22%3A%22Test%22%7D" \
  dev.hossain.mathtutor
```

└─────────────────────────────────────────────────────────────────────────┘
```

## Navigation Details

### Navigation Methods

| Method | Usage | Effect |
|--------|-------|--------|
| `navigator.goTo(screen)` | Forward navigation | Pushes screen onto backstack |
| `navigator.resetRoot(screen)` | Reset navigation | Clears backstack, sets new root |
| `navigator.pop()` | Back navigation | Pops current screen from backstack |

### Top-Level Screen Colors & Theming

Each top-level feature area has a designated vibrant color for consistent visual branding across the app.

#### Color Assignments

| Feature | Screen | Color Token | Purpose |
|---------|--------|-------------|---------|
| **Practice** | `OperationSelectorScreen` | `primaryContainer` | Primary math practice operations |
| **Stats** | `StatsScreen` | `secondaryContainer` | Performance tracking & analytics |
| **Badges** | `BadgesScreen` | `tertiaryContainer` | Achievement & reward recognition |
| **Settings** | `SettingsScreen` | `inversePrimary` | Configuration & preferences |
| **Games** | `GameSelectionScreen` | `primaryContainer` | Game selection & entertainment |

#### Child Screens Color Inheritance

Child screens navigate under their parent and should use the same color scheme:

- **Practice Tree** → Use `primaryContainer`
  - `MathPracticeScreen` - Problem solving interface
  - `ResultsScreen` - Session results and feedback

- **Stats Tree** → Use `secondaryContainer`
  - `AccuracyDetailsScreen` - Daily accuracy breakdown

- **Settings Tree** → Use `inversePrimary`
  - `AudioHapticSettingsScreen` - Audio & haptic configuration

- **Games Tree** → Use `primaryContainer`
  - `MathRaceScreen` - Speed-based game
  - `NumberSequenceScreen` - Pattern recognition game
  - `MemoryMatchScreen` - Memory-based game

#### Implementation Pattern

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUi(state: ScreenScreen.State, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen Title") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        // ... rest of scaffold
    )
}
```

### Screen-by-Screen Navigation

| Screen | Navigated From | Method | Navigates To |
|--------|---------------|--------|--------------|
| `OnboardingScreen` | MainActivity (root) | Initial | `GradeSelectionScreen` |
| `GradeSelectionScreen` | OnboardingScreen, SettingsScreen | `goTo` | `NameEntryScreen` (onboarding) or back via pop (settings) |
| `NameEntryScreen` | GradeSelectionScreen | `goTo` | `HomeScreen` |
| `HomeScreen` | NameEntryScreen, ResultsScreen, ImportChallengeScreen | `resetRoot` | Multiple screens |
| `OperationSelectorScreen` | HomeScreen | `goTo` | `MathPracticeScreen` | primaryContainer |
| `MathPracticeScreen` | OperationSelectorScreen | `goTo` | `ResultsScreen` | primaryContainer |
| `ResultsScreen` | MathPracticeScreen | `goTo` | `HomeScreen` | primaryContainer |
| `StatsScreen` | HomeScreen | `goTo` | `AccuracyDetailsScreen` | secondaryContainer |
| `AccuracyDetailsScreen` | StatsScreen | `goTo` | — | secondaryContainer |
| `BadgesScreen` | HomeScreen | `goTo` | — | tertiaryContainer |
| `SettingsScreen` | HomeScreen | `goTo` | `AudioHapticSettingsScreen`, `GradeSelectionScreen` | inversePrimary |
| `AudioHapticSettingsScreen` | SettingsScreen | `goTo` | — | inversePrimary |
| `GameSelectionScreen` | HomeScreen | `goTo` | Game screens | primaryContainer |
| `MathRaceScreen` | GameSelectionScreen | `goTo` | — | primaryContainer |
| `NumberSequenceScreen` | GameSelectionScreen | `goTo` | — | primaryContainer |
| `MemoryMatchScreen` | GameSelectionScreen | `goTo` | — | primaryContainer |
| `ImportChallengeScreen` | MainActivity (share intent), HomeScreen | `goTo` or Initial | `ParentChallengesScreen` | — |
| `ParentChallengesScreen` | HomeScreen, ImportChallengeScreen | `goTo` | back via pop | — |
| `DeveloperPortalScreen` | Debug menu | `goTo` | — | — |

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
5. **Apply TopAppBar styling** with appropriate color based on feature hierarchy:
   - If top-level feature: Use dedicated color from the Top-Level Screen Colors table
   - If child screen: Use same color as parent feature
6. **Add navigation** from an existing screen using `navigator.goTo(NewScreen)`
7. **Update this document** with the new screen, color assignment, and navigation flow

See [copilot-instructions.md](/.github/copilot-instructions.md) for detailed implementation patterns.
