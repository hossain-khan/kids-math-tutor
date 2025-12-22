# Kids Math Pup Tutor 🐶

A fun, free math learning app for K-2 children. No ads. No data collection. Just learning.

[![Android CI](https://github.com/hossain-khan/kids-math-tutor/actions/workflows/android-ci.yml/badge.svg)](https://github.com/hossain-khan/kids-math-tutor/actions/workflows/android-ci.yml)

## Features

| Learning | Games | Motivation |
|----------|-------|------------|
| Addition, Subtraction, Multiplication, Division | 🏎️ Math Race (60-sec challenge) | 🏆 23 Achievement Badges |
| Grade-appropriate difficulty (K-2) | 🧩 Memory Match (4×4 cards) | 🔥 Daily Streaks |
| Instant feedback with animations | 📊 Personal best tracking | 📈 Progress Stats |

**Accessibility**: TalkBack support, high contrast mode, dynamic text sizing (WCAG 2.1 AA)

## Tech Stack

| | |
|---|---|
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | [Circuit](https://github.com/slackhq/circuit) (UDF) |
| **DI** | [Metro](https://zacsweers.github.io/metro/) |
| **Storage** | Room + DataStore |
| **Audio** | Media3 ExoPlayer |

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

| Development | Google Play |
|-------------|-------------|
| [CHANGELOG](CHANGELOG.md) | [Store Listing](project-resources/google-play/GOOGLE-PLAY.md) |
| [RELEASE](RELEASE.md) | [Privacy Policy](project-resources/google-play/PRIVACY-POLICY.md) |
| [ACCESSIBILITY](ACCESSIBILITY.md) | [Terms of Service](project-resources/google-play/TERMS-OF-SERVICE.md) |
| [ANALYTICS](project-resources/tech-doc/ANALYTICS.md) | [Release Notes](project-resources/google-play/RELEASE-NOTES.md) |

## Privacy

- ✅ All data stored locally on device
- ✅ Optional analytics (opt-out in Settings)
- ✅ COPPA compliant
- ❌ No ads
- ❌ No in-app purchases
- ❌ No personal data collection

## License

[MIT](LICENSE)

