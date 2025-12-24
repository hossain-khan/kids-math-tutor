# Project Overview

**Kids Math Pup Tutor** 🐶 is an educational Android app for K-2 children to practice basic math skills. Built with Jetpack Compose, it features a clean, child-friendly interface with instant feedback and encouraging results.

The app follows Circuit UDF (Unidirectional Data Flow) architecture for predictable state management and Metro for dependency injection, providing a robust foundation for the math practice experience.

## Project Structure

```
kids-math-tutor/
├── app/
│   └── src/
│       └── main/java/dev/hossain/mathtutor/
│           ├── KidsMathTutorApp.kt     # Main Application class
│           ├── MainActivity.kt         # Main Activity with Circuit
│           ├── domain/                 # Domain layer
│           │   ├── model/              # Domain models (MathProblem, MathOperation, etc.)
│           │   └── generator/          # Problem generators
│           ├── ui/                     # Feature-based UI organization
│           │   ├── onboarding/         # Onboarding screen
│           │   ├── mathpractice/       # Math practice screen (Screen, Presenter, UI)
│           │   ├── practiceresults/    # Results screen (Screen, Presenter, UI)
│           │   ├── component/          # Reusable UI components (NumberPad, AnswerField)
│           │   └── theme/              # Compose theme configuration
│           ├── circuit/                # Legacy - being migrated to ui/
│           │   └── overlay/            # Circuit overlays (AppInfo)
│           ├── data/                   # Data layer (UserPreferences)
│           ├── di/                     # Metro dependency injection
│           └── work/                   # WorkManager workers (sample)
└── gradle/
    └── libs.versions.toml              # Centralized dependency versions
```

## Architecture Patterns

### Circuit UDF (Unidirectional Data Flow)

1. **Use `@CircuitInject` annotation** for screens and presenters
2. **Screens** are composable functions that render UI
3. **Presenters** handle business logic and state management
4. **Events flow up, state flows down**

Example:
```kotlin
// Screen definition with State and Events
@Parcelize
data class MathPracticeScreen(
    val problemCount: Int = 10
) : Screen {
    data class State(
        val currentProblem: MathProblem?,
        val currentAnswer: String,
        val eventSink: (Event) -> Unit
    ) : CircuitUiState
    
    sealed interface Event : CircuitUiEvent {
        data class NumberClicked(val number: Int) : Event
        data object CheckAnswer : Event
    }
}

// Presenter with business logic
@CircuitInject(MathPracticeScreen::class, AppScope::class)
@Composable
fun MathPracticePresenter(
    @Assisted screen: MathPracticeScreen,
    @Assisted navigator: Navigator,
    problemGenerator: ProblemGenerator
): MathPracticeScreen.State {
    // State management and event handling
}

// UI composition
@CircuitInject(MathPracticeScreen::class, AppScope::class)
@Composable
fun MathPracticeUi(
    state: MathPracticeScreen.State,
    modifier: Modifier = Modifier
) {
    // UI rendering based on state
}
```

### Metro Dependency Injection

1. **Use `@ContributesBinding`** for interface implementations
2. **Use `@Inject` constructor injection** for dependencies
3. **Scopes**: `@ApplicationContext`, `@ActivityKey`, `@WorkerKey`
4. **Multibindings**: Use for activity and worker factories

Example:
```kotlin
// Define interface
interface ProblemGenerator {
    fun generateProblems(count: Int, operation: MathOperation): List<MathProblem>
}

// Implementation with Metro DI
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SimpleProblemGenerator constructor() : ProblemGenerator {
    override fun generateProblems(count: Int, operation: MathOperation): List<MathProblem> {
        // Generate random math problems
    }
}

// Usage with @AssistedInject for presenters
@AssistedInject
class MathPracticePresenter constructor(
    @Assisted private val screen: MathPracticeScreen,
    @Assisted private val navigator: Navigator,
    private val problemGenerator: ProblemGenerator // Injected dependency
) : Presenter<MathPracticeScreen.State>
```

## Code Style

### Kotlin Guidelines

- **Follow [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html)**
- **Formatting**: Enforced by Kotlinter plugin (ktlint)
- **Naming Conventions**:
  - Classes: `PascalCase`
  - Functions/Properties: `camelCase`
  - Constants: `SCREAMING_SNAKE_CASE`
  - Composables: `PascalCase` (like classes)

### Material 3 / Material You Guidelines

**All UI components MUST be Material 3 compatible:**

1. **Use Material 3 Components**:
   - Use `androidx.compose.material3.*` (NOT `material` or `material2`)
   - Components: `Button`, `Card`, `TextField`, `TopAppBar`, `ListItem`, etc.

2. **Theme-Aware Colors**:
   - **NEVER use hardcoded colors** (e.g., `Color(0xFF4CAF50)`, `Color.Red`)
   - Always use `MaterialTheme.colorScheme.*`:
     - `primary`, `onPrimary` - Main brand colors
     - `primaryContainer`, `onPrimaryContainer` - Filled components
     - `secondary`, `tertiary` - Accent colors
     - `surface`, `onSurface` - Backgrounds
     - `error`, `onError` - Error states

3. **Typography**:
   - Use `MaterialTheme.typography.*` for all text
   - Available: `displayLarge`, `headlineMedium`, `titleLarge`, `bodyMedium`, `labelSmall`, etc.

**Example - Correct**:
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    )
) {
    Text(
        text = "Hello",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}
```

**Example - Incorrect** ❌:
```kotlin
Card(colors = CardDefaults.cardColors(containerColor = Color.Blue)) {
    Text(text = "Hello", color = Color.White)
}
```

## Development Workflow

### Before Committing

**ALWAYS run these commands before committing:**

```bash
# 1. Format Kotlin code (auto-fixes style issues)
./gradlew formatKotlin

# 2. Build to ensure no compilation errors
./gradlew assembleDebug
```

### macOS Development Notes

- **Do NOT use `timeout` command in terminal operations on macOS** - it's not available by default. Use `gtimeout` (from GNU coreutils) or run gradle commands directly without timeout wrapping. Gradle tasks will complete naturally.

### Changelog Maintenance

**REQUIRED**: Always update `CHANGELOG.md` when making changes following [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) guidelines:

1. **Format**: Follow Keep a Changelog format
2. **Versioning**: Use [Semantic Versioning](https://semver.org/spec/v2.0.0.html) (MAJOR.MINOR.PATCH)
   - MAJOR: Incompatible API changes
   - MINOR: Add functionality in a backward compatible manner
   - PATCH: Backward compatible bug fixes
3. **Sections**: Use appropriate change types:
   - `Added` for new features
   - `Changed` for changes in existing functionality
   - `Deprecated` for soon-to-be removed features
   - `Removed` for now removed features
   - `Fixed` for any bug fixes
   - `Security` in case of vulnerabilities
4. **Unreleased Section**: Add all changes to `[Unreleased]` section first
5. **Avoid Duplicate Section Headers**: 
   - **CRITICAL**: Before adding a new section header (e.g., `### Added`, `### Changed`, `### Fixed`), **always check if that section already exists** in the `[Unreleased]` section
   - If the section header already exists, **add your entry to the existing section** rather than creating a duplicate header
   - Only create a new section header if it doesn't already exist in `[Unreleased]`
   - Example of CORRECT approach:
     ```markdown
     ## [Unreleased]
     
     ### Added
     - Existing feature A
     - NEW: Your new feature B  ← Add here, don't create another ### Added
     
     ### Fixed
     - Existing bug fix
     ```
   - Example of INCORRECT approach (DO NOT DO THIS):
     ```markdown
     ## [Unreleased]
     
     ### Added
     - Existing feature A
     
     ### Added  ← WRONG: Duplicate header
     - Your new feature B
     ```
6. **Release Process**: When releasing, move `[Unreleased]` changes to a new version section with date
7. **Format Example**:
   ```markdown
   ## [Unreleased]
   
   ### Added
   - New feature description
   
   ### Fixed
   - Bug fix description
   
   ## [1.0.1] - 2025-10-03
   
   ### Fixed
   - Previous bug fix
   ```
8. **Guidelines**:
   - Write for humans, not machines
   - Each version should have an entry
   - Group similar types of changes together
   - Use ISO 8601 date format (YYYY-MM-DD)
   - Link versions at bottom of file
   - Keep entries concise but descriptive
   - Don't dump git commit logs

**Example Workflow**:
```bash
# 1. Make code changes
# 2. Update CHANGELOG.md under [Unreleased] section
# 3. Format code
./gradlew formatKotlin
# 4. Run tests
./gradlew test
# 5. Commit with descriptive message
git commit -m "Add feature X

- Updated CHANGELOG.md with new feature"
```

### Common Gradle Tasks

```bash
# Build the project
./gradlew build

# Clean build
./gradlew clean build

# Check code formatting (doesn't modify files)
./gradlew lintKotlin

# Run specific module commands
./gradlew :app:formatKotlin
./gradlew :app:assembleDebug
```

### Release Process

**IMPORTANT**: The `main` branch is protected. All changes must be made in new git branch via pull requests.

Follow this workflow for creating a new release:

1. **Create Release Branch**:
   ```bash
   git checkout main
   git pull
   git checkout -b release/X.Y.Z
   ```

2. **Update Version Numbers**:
   - Update `versionCode` and `versionName` in `app/build.gradle.kts`
   - Example: `versionCode = 4` and `versionName = "1.0.3"`

3. **Update CHANGELOG.md**:
   - Move all `[Unreleased]` changes to new version section `[X.Y.Z] - YYYY-MM-DD`
   - Add empty `[Unreleased]` section at top
   - Update version comparison links at bottom:
     ```markdown
     [unreleased]: https://github.com/hossain-khan/kids-math-pup-tutor/compare/X.Y.Z...HEAD
     [X.Y.Z]: https://github.com/hossain-khan/kids-math-pup-tutor/compare/X.Y.Z-1...X.Y.Z
     ```

4. **Commit and Push Release Branch**:
   ```bash
   git add app/build.gradle.kts CHANGELOG.md
   git commit -m "chore: Prepare release X.Y.Z"
   git push -u origin release/X.Y.Z
   ```

5. **Create Release Pull Request**:
   - Create PR from `release/X.Y.Z` to `main`
   - Title: "Release X.Y.Z"
   - Include changelog summary in PR description
   - Request review and merge

6. **Create and Push Tag** (after PR is merged):
   ```bash
   git checkout main
   git pull
   git tag -a X.Y.Z -m "Release X.Y.Z - Brief Description

   - Major change 1
   - Major change 2
   - Major change 3"
   git push origin X.Y.Z
   ```

7. **Create GitHub Release**:
   - Go to GitHub Releases page
   - Click "Draft a new release"
   - Select tag `X.Y.Z`
   - Title: "Release X.Y.Z"
   - Copy relevant section from CHANGELOG.md
   - Publish release

**Version Numbering** (Semantic Versioning):
- `MAJOR.MINOR.PATCH` (e.g., 1.0.3)
- MAJOR: Breaking changes or major new features
- MINOR: New features, backward compatible
- PATCH: Bug fixes, backward compatible
- **Default**: Always increment MINOR version for regular releases (e.g., 1.2.0 → 1.3.0)
- Only use PATCH for critical hotfixes between regular releases

**Tag Format**: Use plain version number (e.g., `1.0.3`), not `v1.0.3`

## Testing Guidelines

- **Unit Tests**: Required for repositories and business logic
- **Test Coverage**: Aim for success cases, error cases, and edge cases
- **Coroutine Testing**: Use `kotlinx-coroutines-test` with `runTest`
- **Circuit Testing**: Use `circuit-test` library with `FakeNavigator`

## Dependencies Management

All dependency versions are centralized in `gradle/libs.versions.toml`:

**Major Dependencies**:
- Kotlin: 2.2.21
- Circuit: 0.31.0
- Metro: 0.9.0
- Compose BOM: 2025.12.00
- WorkManager: 2.11.0
- DataStore Preferences: 1.2.0
- Firebase BOM: 34.7.0
- Timber: 5.0.1

## Common Patterns

### Adding a New Circuit Screen

1. **Create feature package** under `ui/` (e.g., `ui/newfeature/`)
2. **Create Screen definition** with State and Event sealed classes:
   ```kotlin
   @Parcelize
   data class NewFeatureScreen() : Screen {
       data class State(..., val eventSink: (Event) -> Unit) : CircuitUiState
       sealed interface Event : CircuitUiEvent { ... }
   }
   ```
3. **Create Presenter** with `@AssistedInject` and `@CircuitInject`:
   ```kotlin
   @AssistedInject
   class NewFeaturePresenter constructor(
       @Assisted private val screen: NewFeatureScreen,
       @Assisted private val navigator: Navigator
   ) : Presenter<NewFeatureScreen.State>
   ```
4. **Create UI composable** with `@CircuitInject`:
   ```kotlin
   @CircuitInject(NewFeatureScreen::class, AppScope::class)
   @Composable
   fun NewFeatureUi(state: NewFeatureScreen.State, modifier: Modifier = Modifier)
   ```
5. **Navigate** using `Navigator.goTo(NewFeatureScreen())` or `Navigator.resetRoot(NewFeatureScreen())`

### Adding a WorkManager Worker

1. Use `@AssistedInject` for constructor injection
2. Add `@WorkerKey` annotation
3. Implement `CoroutineWorker` or `Worker`
4. Schedule work using `WorkManager`

## Resources

- [Circuit Documentation](https://slackhq.github.io/circuit/)
- [Metro Documentation](https://zacsweers.github.io/metro/)
- [Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material 3 Design System](https://m3.material.io/)
- [Material 3 Compose Components](https://developer.android.com/jetpack/compose/designsystems/material3)
- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)

## Notes for AI Assistants

- Always suggest running `formatKotlin` before commits
- Follow Material 3 design system strictly
- Use theme colors, never hardcode colors
- Prefer constructor injection over field injection
- Follow existing code structure and patterns
- Keep code concise and readable
