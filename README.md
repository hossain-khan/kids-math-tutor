# Kids Math Pup Tutor 🐶

A comprehensive K-2 math learning app that makes practice fun and accessible for all children.

## ✨ Features

### 🎯 Core Learning
- **Grade-appropriate problems** for Kindergarten through Grade 2
- **Adaptive difficulty** that adjusts to student performance
- **Four operations**: Addition, Subtraction, Multiplication, Division
- **Personalized feedback** with encouraging messages

### � Mini-Games
- **Math Race** - 60-second challenge to solve as many problems as possible (unlocks at 50 problems)
- **Memory Match** - 4×4 card matching game pairing problems with answers (unlocks at 100 problems)
- **Game Hub** - Central location to discover and play mini-games
- **Personal Best Tracking** - High scores and fastest times for each game

### �🎵 Sensory Feedback
- **Audio system** with 7 distinct sound effects and background music
- **Haptic feedback** with 5 vibration patterns for different interactions
- **Lifecycle-aware** audio management (auto-pauses on background)

### ♿ Accessibility (WCAG 2.1 Level AA)
- **TalkBack support** with spoken math operations ("3 plus 5 equals")
- **High contrast mode** for improved visibility
- **Dynamic text sizing** that respects system font settings
- **Touch target compliance** (minimum 48dp, NumberPad exceeds at 64dp)
- **Semantic navigation** with proper heading structure

### 📊 Progress Tracking
- **Daily streaks** to encourage consistent practice
- **Badge system** with unlockable achievements
- **Performance analytics** with accuracy tracking
- **Session history** to monitor improvement

### ⚙️ Customization
- **User profiles** with name and grade level
- **Audio & Haptic settings** to control sensory feedback
- **Volume control** for sound effects and music
- **Accessibility preferences** for high contrast and large text

## 🏗️ Built With

- ⚡️ [Circuit](https://github.com/slackhq/circuit) - UI architecture with Unidirectional Data Flow
- 🏗️ [Metro](https://zacsweers.github.io/metro/) - Dependency Injection
- 🎨 [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI
- 📱 Material Design 3 - Google's latest design system
- 🎵 [Media3 ExoPlayer](https://developer.android.com/guide/topics/media/media3) - Audio playback
- 💾 [Room](https://developer.android.com/training/data-storage/room) - Local database
- 📦 [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - Preferences storage

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or later
- JDK 17 or later
- Android SDK 34 (minimum SDK 28)

### Build and Run

1. Clone the repository
   ```bash
   git clone https://github.com/hossain-khan/kids-math-tutor.git
   cd kids-math-tutor
   ```

2. Open the project in Android Studio

3. Sync Gradle and build the project
   ```bash
   ./gradlew build
   ```

4. Run the app
   ```bash
   ./gradlew installDebug
   ```

### Development

- **Format code**: `./gradlew formatKotlin`
- **Run tests**: `./gradlew test`
- **Run linter**: `./gradlew lintKotlin`

## 📚 Documentation

- [ACCESSIBILITY.md](ACCESSIBILITY.md) - Comprehensive accessibility guide
- [CHANGELOG.md](CHANGELOG.md) - Detailed version history
- [RELEASE.md](RELEASE.md) - Release process guide

## 🏛️ Architecture

This app follows the **Circuit UDF (Unidirectional Data Flow)** architecture pattern with **Metro** for dependency injection.

### Key Architectural Patterns
- **Circuit Screens**: Composable UI with Presenter for business logic
- **Repository Pattern**: Clean separation of data sources
- **Use Cases**: Encapsulated business logic for complex operations
- **State Management**: Unidirectional data flow with immutable states
- **Dependency Injection**: Constructor injection with Metro

### Project Structure
```
app/src/main/java/dev/hossain/mathtutor/
├── domain/          # Business logic, models, use cases
├── data/            # Repositories, database, preferences
├── ui/              # Circuit screens, presenters, UI components
├── audio/           # Audio service implementation
├── haptic/          # Haptic feedback service
├── di/              # Metro dependency injection
└── util/            # Utility functions
```

## GitHub Actions

This project includes automated workflows:
- **CI builds** on pull requests and main branch
- **Android Lint** checks for code quality
- **Release builds** - Currently uses debug keystore (see builds are signed but not for production)

Note: For production releases with proper signing, you'll need to:
1. Generate a production keystore
2. Configure GitHub secrets (KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS)
3. The workflows will automatically use production keystore once secrets are set

