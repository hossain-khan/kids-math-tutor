# Firebase Test Lab Robo Test Script

This directory contains the Firebase Test Lab Robo test script for the Math Pup Tutor Android app.

## Overview

The Robo test script (`robo_script.json`) provides guided instructions for Firebase Test Lab's Robo crawler to systematically test the app's key features and user flows. Robo scripts help ensure consistent, reproducible test coverage across different devices and Android versions.

## What is Firebase Test Lab Robo Testing?

Firebase Test Lab's Robo test is an automated testing tool that simulates user interactions with your app. When you provide a Robo script, you can:

- Guide Robo through specific user journeys
- Test critical app flows systematically
- Ensure edge cases are covered
- Get consistent test results across test runs
- Identify crashes, ANRs, and UI issues

Learn more: [Firebase Test Lab - Robo Scripts Documentation](https://firebase.google.com/docs/test-lab/android/run-robo-scripts)

## Test Coverage

The `robo_script.json` file covers the following app features and user flows:

### 1. **Onboarding Flow** (First Launch Experience)
   - Welcome screens (4 onboarding pages)
   - Grade selection (Kindergarten, Grade 1, Grade 2)
   - Name entry
   - Completion and transition to home screen

### 2. **Home Screen**
   - Main dashboard display
   - Welcome message
   - Streak calendar
   - Quick stats card
   - Latest badges section
   - Navigation buttons

### 3. **Math Practice Flow**
   - Operation selection screen
   - Math operation cards:
     - Addition
     - Subtraction
     - Multiplication (Grade 1+)
     - Division (Grade 2+)
     - Mix It Up (all grades)
   - Practice session:
     - Problem display
     - Number pad interaction
     - Answer submission
     - Feedback animations
     - Progress tracking
   - Results screen

### 4. **Games Section**
   - Game selection screen
   - Available games:
     - Math Race (60-second challenge)
     - Number Sequence
     - Memory Match
   - Game unlock status
   - Trial play functionality

### 5. **Progress Tracking**
   - Stats screen (session history, accuracy)
   - Badges screen (achievement viewing)

### 6. **Settings**
   - Settings menu access
   - Audio/haptic preferences
   - Accessibility options
   - Profile management

## Running the Robo Test

### Using Firebase Console

1. **Build your APK:**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Go to [Firebase Console](https://console.firebase.google.com/)**

3. **Navigate to Test Lab:**
   - Select your project
   - Go to "Test Lab" in the left sidebar
   - Click "Run a test"

4. **Upload APK and Robo Script:**
   - Select "Robo test"
   - Upload your APK file (`app/build/outputs/apk/debug/app-debug.apk`)
   - Under "Advanced options" → "Test script"
   - Upload `robo_script.json` from this directory

5. **Configure Test:**
   - Select devices (recommended: mix of physical devices and emulators)
   - Select Android versions (API 28-35 for this app)
   - Set test timeout (default: 5 minutes, recommended: 10 minutes for full script)

6. **Run Test:**
   - Click "Start test"
   - Monitor progress in Firebase Console
   - Review results, logs, screenshots, and videos

### Using gcloud CLI

```bash
# Authenticate with Google Cloud
gcloud auth login

# Set your project
gcloud config set project YOUR_PROJECT_ID

# Build the APK
./gradlew assembleDebug

# Run the Robo test with script
gcloud firebase test android run \
  --type robo \
  --app app/build/outputs/apk/debug/app-debug.apk \
  --robo-script project-resources/robo-test/robo_script.json \
  --device model=Pixel2,version=28,locale=en,orientation=portrait \
  --timeout 10m
```

### Recommended Test Matrix

For comprehensive coverage, test on:

| Device Type | Model | Android Version | Orientation |
|-------------|-------|-----------------|-------------|
| Phone | Pixel 2 | API 28 (9.0) | Portrait |
| Phone | Pixel 4 | API 30 (11.0) | Portrait |
| Phone | Pixel 6 | API 33 (13.0) | Portrait |
| Tablet | Nexus 9 | API 28 (9.0) | Landscape |

## Script Structure

The Robo script uses Firebase Test Lab's JSON format with the following event types:

### Event Types Used

1. **LAUNCH_ACTIVITY**: Starts the app
2. **VIEW_TEXT**: Verifies text is visible on screen
3. **VIEW_TEXT_THEN_CLICK**: Finds text and clicks it
4. **VIEW_CONTENT_DESCRIPTION_THEN_CLICK**: Finds element by accessibility label and clicks
5. **VIEW_ID_THEN_CLICK**: Finds element by resource ID and clicks (optional)
6. **PRESS_BACK**: Simulates back button press
7. **DELAYED_MESSAGE_CLICK**: Waits for specified milliseconds (for animations/loading)

### Optional vs Required Steps

- **optional: false** - Critical steps that must succeed for the test to be valid
- **optional: true** - Steps that may not always be available (e.g., features that require data, conditional UI)

## Maintenance

### When to Update the Script

Update `robo_script.json` when:

1. **New Features Added**: Add test steps for new screens or functionality
2. **UI Text Changes**: Update text strings if button labels or screen titles change
3. **Navigation Changes**: Modify flow if screen transitions change
4. **Bug Fixes**: Add specific test cases for regression testing

### Testing the Script

Before committing changes to the Robo script:

1. Run a test with the updated script in Firebase Test Lab
2. Review the test video to ensure all steps execute as expected
3. Check that optional steps handle missing UI elements gracefully
4. Verify timing delays are appropriate for animations

## Troubleshooting

### Common Issues

1. **Step fails: "Text not found"**
   - Verify the text string matches exactly (case-sensitive)
   - Check if the text is visible on screen (not scrolled off)
   - Ensure the step isn't running too quickly (add delay before it)

2. **Step times out**
   - Increase `delayTime` before the step
   - Check if the previous step needs more time to complete
   - Verify the element exists in the current app version

3. **Navigation issues**
   - Ensure PRESS_BACK steps match your navigation stack
   - Verify the target screen is reachable from the current state
   - Check for blocking dialogs or overlays

4. **Test is too short/long**
   - Adjust delay times to match actual UI response times
   - Remove or mark as optional steps that aren't critical
   - Add steps to cover untested areas

## Additional Resources

- [Firebase Test Lab Documentation](https://firebase.google.com/docs/test-lab)
- [Robo Scripts Guide](https://firebase.google.com/docs/test-lab/android/run-robo-scripts)
- [Recording Robo Scripts](https://firebase.google.com/docs/test-lab/android/record-robo-script)
- [gcloud CLI Reference](https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run)

## Contributing

When adding new features to the app, please update the Robo script to include test coverage for:

- New screens and their navigation paths
- Critical user interactions
- Happy path flows
- Edge cases (if applicable)

Follow the existing script structure and use descriptive `notes` for each step.
