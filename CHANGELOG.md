# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Operation Selector Screen (Phase 2-4)
  - Created `OperationSelectorScreen` Circuit screen with State and Events
  - Created `OperationSelectorPresenter` with SessionRepository integration
  - Created `OperationSelectorUi` with Material 3 design
  - Created reusable `OperationCard` component for displaying operation options
  - Three operation cards: Addition, Subtraction, and Mix It Up
  - Stats button enabled/disabled based on session history
  - Integration with Circuit architecture using @CircuitInject annotations
  - Updated MainActivity to use OperationSelectorScreen as initial screen after onboarding
  - Updated OnboardingScreen to navigate to OperationSelectorScreen
- Repository Layer implementation for session data management (Phase 2-2)
  - Created `SessionRepository` interface with all data operation methods
  - Created `SessionStats` domain model with star rating calculation (1-5 stars based on accuracy)
  - Created `SessionMapper` for converting between domain and data layer objects
  - Created `SessionRepositoryImpl` with Metro DI integration using `@ContributesBinding`
  - Implemented Flow-based reactive data streams for all repository methods
  - Combined stats calculation using Flow.combine for overall statistics
  - Operation-specific stats grouping for detailed analytics
  - Comprehensive unit tests for SessionStats (7 test cases)
  - Comprehensive unit tests for SessionMapper (5 test cases covering all scenarios)
  - Comprehensive unit tests for SessionRepositoryImpl (13 test cases, >85% coverage)
  - All repository methods tested: saveSession, stats calculations, Flow emissions, operation grouping
- Room Database integration for local data persistence (Phase 2-1)
  - Room dependencies added to gradle/libs.versions.toml (Room 2.6.1, Coroutines Test 1.9.0)
  - Created `PracticeSessionEntity` for storing practice session statistics
  - Created `SessionDao` with Flow-based query methods for reactive data access
  - Created `Converters` for Room type conversion (MathOperation enum and Instant timestamp)
  - Created `MathDatabase` with version 1 schema
  - Implemented Metro DI module for database and DAO provision
  - Comprehensive unit tests for Converters (11 test cases)
  - Comprehensive instrumented tests for SessionDao (16 test cases)
  - All DAO methods support Flow returns for reactive updates
  - Database operations: insert, query by operation, aggregate stats, date filtering

### Changed
- Updated `MathPracticeScreen` to accept `operation` parameter for flexible operation selection
- Updated `MathPracticePresenter` to use operation parameter from screen
- Upgraded Room from 2.6.1 to 2.7.1 to fix KSP "unexpected jvm signature V" error with Kotlin 2.2
- Updated KSP version from 2.3.3 to 2.2.21-2.0.4 to align with Kotlin 2.2.21
- Simplified DatabaseModule to use direct @Provides methods instead of provider interfaces
- Updated `getTodaySessions()` to accept timezone-aware timestamp parameters for correct date filtering
- Enhanced test comment for `allowMainThreadQueries()` to emphasize production prohibition

### Fixed
- Removed incorrect `testImplementation` dependency for Room testing (only works with androidTest)
- Fixed timezone issues in `getTodaySessions()` query and test to work correctly across timezones
- Added Room schema export location configuration to build.gradle.kts
- Fixed Room 2.6.1 KSP compatibility issue by upgrading to Room 2.7.1 (resolves JVM signature V error)

## [1.0.0] - 2025-12-16

### Added
- **Phase 1 MVP: Complete! 🎉** All 8 phases completed
  - User testing completed with manual verification on device
  - Complete flow tested: Onboarding → Practice (10 problems) → Results → Try Again
  - Dark mode verified working correctly
  - All functionality working as expected
- Phase 1 MVP: Testing & Polish
  - Comprehensive test suite with 66 unit tests passing (domain models, generator, presenters)
  - 13 instrumented UI tests for components (NumberPad, AnswerField)
  - Removed example test file (ExampleUnitTest.kt)
  - Zero lint warnings - clean code quality
  - Material 3 compliance verified: all screens use MaterialTheme colors (no hardcoded colors except intentional onboarding palette)
  - Accessibility compliance: all touch targets meet 48dp minimum (NumberPad 64dp, buttons 48-56dp)
  - Dark mode support: screens use theme-aware colors
  - Code formatted with ktlint
- Phase 1 MVP: Complete navigation integration with Circuit Navigator
  - Full navigation flow: Onboarding → Math Practice → Results → Try Again
  - Circuit Navigator `resetRoot()` for replacing navigation stack (Onboarding → Practice, Results → Practice)
  - Circuit Navigator `goTo()` for forward navigation with data passing (Practice → Results with problems and answers)
  - Circuit Navigator `pop()` for back navigation in all screens
  - Back button functionality in MathPracticeScreen and ResultsScreen top app bars
  - Gesture navigation support via GestureNavigationDecorationFactory
  - All navigation flows verified and documented
- Phase 1 MVP: Results Screen with session summary and review
  - `ResultsScreen` Circuit screen for displaying practice session results
  - `ResultsPresenter` calculating accuracy percentage and managing navigation events
  - `ResultsUi` with Material 3 design showing summary statistics
  - Circular accuracy percentage display with visual appeal
  - Statistics breakdown (Correct, Total, Incorrect) with color coding
  - Problem review list with user answers vs correct answers
  - Color-coded problem cards (green for correct, red for incorrect)
  - Try Again button to start new practice session
  - Comprehensive unit tests (13 test cases) for accuracy calculations
- Phase 1 MVP: Session tracking and navigation flow
  - MathPracticeScreen now tracks user answers throughout session
  - Automatic navigation to Results screen after completing all problems
  - Navigator integration for seamless screen transitions
  - Answer collection with null handling for skipped problems
- Phase 1 MVP: Parcelable domain models for navigation
  - `MathProblem` now implements Parcelable for Circuit navigation
  - Support for passing complex data between screens
- Phase 1 MVP: Math Practice Screen with Circuit UDF architecture
  - `MathPracticeScreen` Circuit screen for interactive math practice sessions
  - `MathPracticePresenter` managing state with problem progression and answer validation
  - `MathPracticeUi` integrating NumberPad and AnswerField components
  - Progress indicator showing current problem number and visual progress bar
  - Real-time feedback with ✓ Correct! and ✗ Try again messages
  - Action buttons (Clear and Check/Next) with conditional enabling
  - Auto-advance logic after correct answers
  - Comprehensive unit tests (13 test cases) for presenter logic
  - Compose preview functions for different states (initial, correct, incorrect)
- Phase 1 MVP: Reusable UI components for math practice interface
  - `NumberPad` composable with 0-9 buttons in 2x5 grid layout
  - Child-friendly 64dp button size with Material 3 theming
  - Accessibility support with content descriptions for screen readers (e.g., "Number 1", "Number 2")
  - `AnswerField` composable as read-only text field for displaying user input
  - Centered text with "?" placeholder and large typography for visibility
  - Accessibility label "Your Answer" for screen reader users
  - Compose preview functions for both light and dark themes
  - Instrumented UI tests (13 test cases) for both components covering rendering, interaction, and accessibility
- Phase 1 MVP: Problem generator for creating math exercises
  - `ProblemGenerator` interface for generating math problems
  - `SimpleProblemGenerator` implementation with Metro DI integration
  - Addition problem generation with numbers in range 1-10
  - Comprehensive unit tests with 13 test cases covering edge cases
  - Validation for unsupported operations (throws clear exceptions)
- Phase 1 MVP: Domain models for math practice system
  - `MathOperation` enum with ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION support
  - `MathProblem` data class with problem generation and answer validation
  - `PracticeSession` data class with progress tracking and accuracy calculation
  - `SessionAnswer` data class for storing user answers with metadata
  - Comprehensive unit tests for all domain models (>80% coverage)
- Material Icons Extended library for access to extended Material Design icons
- Onboarding screen drawable resources (4 images)
- Onboarding screen with Circuit UDF architecture showing 4-page welcome flow
- DataStore Preferences library for persistent storage
- UserPreferencesRepository for storing user preferences (onboarding completion status)
- First-time user experience that shows onboarding once and remembers completion
- DynaPuff Google Font as the primary app font for playful, kid-friendly typography
- Dev container support for consistent development environment
  - Java 21 base image with Android SDK setup

### Changed
- Updated landing screen from example InboxScreen to MathPracticeScreen
- OnboardingScreen now navigates to MathPracticeScreen after completion
- Improved code clarity in MathPracticePresenter with explicit number-to-string conversion
- Enhanced button spacing in MathPracticeUi following Material 3 design guidelines (8dp between icon and text)

### Removed
- Example Circuit screens (ExampleInboxScreen, ExampleEmailDetailsScreen)
- Example data classes (ExampleEmailRepository, ExampleAppVersionService, ExampleEmailValidator)
  - Automatic installation of Android Command Line Tools (version 11076708)
  - Pre-configured VS Code extensions (Kotlin, Gradle, Java, GitHub Copilot, IntelliJ IDEA keybindings)
  - Post-create script that installs Android Platform 35 and Build Tools 35.0.0
  - ADB support with privileged mode for physical device connection
  - Environment variables for ANDROID_HOME and ANDROID_SDK_ROOT

### Changed
- Onboarding screen now uses vibrant color palettes extracted from each page's image
- Enhanced onboarding UI with rounded cards, colorful backgrounds, and themed buttons
- Page indicators now match each page's accent color for better visual cohesion
- Updated onboarding page 2 background to softer rose beige (#E7D5CA) for better contrast

### Fixed
- Reserved space for skip button on all onboarding pages for consistent layout positioning
- Applied proper system bars insets for edge-to-edge display on onboarding screen
- Fixed onboarding navigation to properly navigate to MathPracticeScreen after completion

[unreleased]: https://github.com/hossain-khan/kids-math-pup-tutor/compare/1.0.0...HEAD
[1.0.0]: https://github.com/hossain-khan/kids-math-pup-tutor/releases/tag/1.0.0
