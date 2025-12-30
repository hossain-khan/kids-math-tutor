# Quick Reference: Firebase Robo Scripts

## Files Overview

| File | Test Steps | Purpose | Use Case |
|------|-----------|---------|----------|
| `robo_script.json` | 50 | First Launch | New user onboarding & initial exploration |
| `robo_script_advanced.json` | 45 | Returning User | Deep feature testing with existing data |

## Quick Start Commands

### Firebase Console
1. Build: `./gradlew assembleDebug`
2. Go to: [Firebase Console](https://console.firebase.google.com/) → Test Lab
3. Upload: `app/build/outputs/apk/debug/app-debug.apk`
4. Add script: Select a `.json` file from this directory
5. Run on recommended devices (see README.md)

### gcloud CLI
```bash
# First Launch Test
gcloud firebase test android run \
  --type robo \
  --app app/build/outputs/apk/debug/app-debug.apk \
  --robo-script project-resources/robo-test/robo_script.json \
  --device model=Pixel2,version=28 \
  --timeout 10m

# Returning User Test
gcloud firebase test android run \
  --type robo \
  --app app/build/outputs/apk/debug/app-debug.apk \
  --robo-script project-resources/robo-test/robo_script_advanced.json \
  --device model=Pixel4,version=30 \
  --timeout 10m
```

## Test Coverage Comparison

### Primary Script (robo_script.json)
✅ Complete onboarding (4 pages)
✅ Grade selection (K-2)
✅ Name entry
✅ Home dashboard
✅ Operation selection
✅ Basic math practice
✅ Games discovery
✅ Stats viewing
✅ Badges viewing
✅ Settings access

### Advanced Script (robo_script_advanced.json)
✅ Skip onboarding (returning user)
✅ Mixed operations
✅ Multiple problems in sequence
✅ Answer correction (Clear button)
✅ Early exit from practice
✅ Math Race game play
✅ Music toggle
✅ Audio & Haptics settings
✅ Accessibility settings
✅ Multi-digit answers

## When to Use Each Script

| Scenario | Script to Use |
|----------|---------------|
| Testing fresh install | `robo_script.json` |
| Testing app updates | `robo_script_advanced.json` |
| Verifying onboarding | `robo_script.json` |
| Testing game mechanics | `robo_script_advanced.json` |
| CI/CD pipeline | Both (separate test runs) |
| Pre-release validation | Both (comprehensive coverage) |

## Recommended Test Matrix

```bash
# Full coverage test suite
for SCRIPT in robo_script.json robo_script_advanced.json; do
  for DEVICE in "Pixel2,28" "Pixel4,30" "Pixel6,33"; do
    IFS=',' read MODEL VERSION <<< "$DEVICE"
    gcloud firebase test android run \
      --type robo \
      --app app/build/outputs/apk/debug/app-debug.apk \
      --robo-script "project-resources/robo-test/$SCRIPT" \
      --device "model=$MODEL,version=$VERSION" \
      --timeout 10m
  done
done
```

## Common Event Types

| Event Type | Description | Example |
|------------|-------------|---------|
| `LAUNCH_ACTIVITY` | Start the app | Launch MainActivity |
| `VIEW_TEXT` | Verify text exists | Check "Welcome" appears |
| `VIEW_TEXT_THEN_CLICK` | Find and tap text | Tap "Next" button |
| `VIEW_CONTENT_DESCRIPTION_THEN_CLICK` | Tap by accessibility label | Tap "Settings" icon |
| `PRESS_BACK` | Navigate back | Return to previous screen |
| `DELAYED_MESSAGE_CLICK` | Wait | Pause for animation (ms) |

## Tips

- **Delays**: Adjust `delayTime` based on device performance
- **Optional Steps**: Use `"optional": true` for conditional UI elements
- **Device Selection**: Test on min SDK (28) and target SDK (35)
- **Timeout**: Allow 10-15 minutes for full script execution
- **Logs**: Check Firebase Console for detailed step-by-step results

## Support

For detailed information, see [README.md](README.md) in this directory.
For Firebase Test Lab docs: https://firebase.google.com/docs/test-lab
