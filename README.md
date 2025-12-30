# Kids Math Pup Tutor 🐶

A fun, free math learning app for K-2 children. No ads. No data collection. Just learning.

[![Android CI](https://github.com/hossain-khan/kids-math-tutor/actions/workflows/android.yml/badge.svg)](https://github.com/hossain-khan/kids-math-tutor/actions/workflows/android.yml)

## Features

| Learning                                        | Games                            | Tools                                  |
|-------------------------------------------------|----------------------------------|----------------------------------------|
| Addition, Subtraction, Multiplication, Division | 🏎️ Math Race (60-sec challenge) | 👨‍👩‍👧 Custom worksheets for parents |
| Grade-appropriate difficulty (K-2)              | 🧩 Memory Match (4×4 cards)      | 📋 27 pre-built templates              |
| Instant feedback with animations                | 🏆 27 Achievement Badges         | 📊 Progress tracking & stats           |
| 🎯 **Smart Learning Goals** (custom & templates) | 🔐 Game blocking (earn unlocks)   | 📈 Goal analytics & completion tracking |

### Smart Learning Goals

Create **personalized learning paths** for children with milestone-based progress tracking:

**For Parents:**
- 📝 **Goal Templates**: Pre-built math focus areas (Addition Master, Times Table Wizard, etc.)
- 🎯 **Custom Goals**: Create goals with 3-5 milestones (e.g., "100 addition problems by Friday")
- 📊 **Progress Dashboard**: Real-time milestones, accuracy tracking, game access controls
- 🎮 **Smart Game Blocking**: Unlock games by completing goals (motivates focused practice)
- 📊 **Session Resumption**: Pause and resume mid-session with progress saved

**For Children:**
- 🏆 **Progress Banner**: See active goal with milestone progress on home screen
- ✨ **Achievement Celebration**: Special animations and badges when completing goals
- 🔓 **Unlocked Games**: New games become playable as milestones are completed
- 📈 **Math Practice Integration**: Track progress while practicing custom worksheets

**Key Capabilities:**
- Create up to 10 concurrent goals with flexible timelines
- 3-5 customizable milestones per goal with accuracy targets
- Persistent session state with resume capability
- Goal completion history and analytics
- Material 3 design with adaptive layouts
- Full accessibility support (WCAG 2.1 AA)

**Accessibility**: TalkBack support, high contrast mode, dynamic text sizing (WCAG 2.1 AA)

## Tech Stack

|                  |                                                     |
|------------------|-----------------------------------------------------|
| **UI**           | Jetpack Compose + Material 3                        |
| **Architecture** | [Circuit](https://github.com/slackhq/circuit) (UDF) |
| **DI**           | [Metro](https://zacsweers.github.io/metro/)         |
| **Storage**      | Room + DataStore                                    |
| **Audio**        | Media3 ExoPlayer                                    |

## Quick Start

```bash
git clone https://github.com/hossain-khan/kids-math-tutor.git
cd kids-math-tutor
./gradlew installDebug
```

**Requirements**: Android Studio Ladybug+, JDK 17+, SDK 34 (min 28)

## Development

```bash
./gradlew formatKotlin    # Format code (run before commits)
./gradlew test            # Run tests
./gradlew assembleDebug   # Build debug APK
```

## Documentation

| Development                                          | Google Play                                                           |
|------------------------------------------------------|-----------------------------------------------------------------------|
| [CHANGELOG](CHANGELOG.md)                            | [Store Listing](project-resources/google-play/GOOGLE-PLAY.md)         |
| [RELEASE](RELEASE.md)                                | [Privacy Policy](project-resources/google-play/PRIVACY-POLICY.md)     |
| [ACCESSIBILITY](ACCESSIBILITY.md)                    | [Terms of Service](project-resources/google-play/TERMS-OF-SERVICE.md) |
| [ANALYTICS](project-resources/tech-doc/ANALYTICS.md) | [Release Notes](project-resources/google-play/RELEASE-NOTES.md)       |

## Privacy

- ✅ All data stored locally on device
- ✅ Optional analytics (opt-out in Settings)
- ✅ COPPA compliant
- ❌ No ads
- ❌ No in-app purchases
- ❌ No personal data collection

## License

[MIT](LICENSE)

