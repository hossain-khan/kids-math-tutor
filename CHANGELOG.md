# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Material Icons Extended library for access to extended Material Design icons
- Onboarding screen drawable resources (4 images)
- Onboarding screen with Circuit UDF architecture showing 4-page welcome flow
- DataStore Preferences library for persistent storage
- UserPreferencesRepository for storing user preferences (onboarding completion status)
- First-time user experience that shows onboarding once and remembers completion
- DynaPuff Google Font as the primary app font for playful, kid-friendly typography
- Dev container support for consistent development environment
  - Java 21 base image with Android SDK setup
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
- Fixed onboarding navigation to properly navigate to InboxScreen after completion
