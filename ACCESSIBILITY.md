# Accessibility Guide

Kids Math Pup Tutor is designed to be accessible to all K-2 children, regardless of their abilities. This guide documents the accessibility features implemented in the app.

## Overview

The app follows **WCAG 2.1 Level AA** guidelines and includes comprehensive support for:
- TalkBack screen reader
- High contrast mode
- Dynamic text sizing
- Touch target compliance
- Semantic navigation

## TalkBack Support

### Screen Reader Announcements

All interactive elements have descriptive content descriptions that TalkBack reads aloud:

#### Math Problems
- **Visual**: `3 + 5 = ?`
- **TalkBack**: "3 plus 5 equals"

The app converts mathematical symbols to spoken words:
- `+` → "plus"
- `-` → "minus"
- `×` → "times"
- `÷` → "divided by"

#### Number Pad Buttons
- Each button announces: "Number 1, button", "Number 2, button", etc.
- Buttons have `Role.Button` semantic property for proper screen reader behavior

#### Answer Field
- Empty state: "Your Answer, empty"
- With input: "Your Answer, 1 2" (digits announced separately for clarity)

Note: The field's label "Your Answer" is announced automatically by TalkBack, followed by the content description which provides just the state ("empty") or the digit sequence to avoid redundancy.

#### Action Buttons
- **Check button**: 
  - Enabled: "Check your answer, button"
  - Disabled: "Enter an answer first, button, disabled"
- **Clear button**: 
  - Enabled: "Clear answer, button"
  - Disabled: "Clear, disabled, button"
- **Next button**: "Next problem, button"

#### Navigation
- Back button: "Go back, button"
- Settings icon: "Settings, button"

#### Progress Indicator
- Text: "Problem 3 of 10"
- Progress bar: "Progress: 3 out of 10 problems completed"

### Implementation Details

All content descriptions are implemented using:
```kotlin
Modifier.semantics {
    contentDescription = "Description for screen reader"
    role = Role.Button // For buttons
    heading() // For section titles
}
```

Problem cards use merged descendants to ensure TalkBack reads the entire problem as one announcement:
```kotlin
Modifier.semantics(mergeDescendants = true) {
    contentDescription = problem.getSpokenString()
}
```

## High Contrast Mode

### Color Scheme

The app includes a high contrast theme (`HighContrastTheme.kt`) with WCAG 2.1 AA compliant colors:

```kotlin
High Contrast Colors:
- Primary: White (#FFFFFF)
- On Primary: Black (#000000)
- Surface: Black (#000000)
- On Surface: White (#FFFFFF)
- Error: Pure Red (#FF0000)
- Secondary Container: Dark Gray (#333333)
```

All color pairs maintain a **contrast ratio ≥ 4.5:1** for normal text and ≥ 3:1 for large text.

### Activation

High contrast mode can be enabled through:
1. System accessibility settings (Android's high contrast preference)
2. App settings (future implementation)

## Dynamic Text Sizing

### AccessibleText Component

The `AccessibleText` composable (`ui/accessibility/AccessibleText.kt`) provides semantic support for accessibility while leveraging Compose's built-in font scaling:

```kotlin
@Composable
fun AccessibleText(
    text: String,
    style: TextStyle = LocalTextStyle.current,
    contentDescription: String? = null,
    // ... other parameters
)
```

Features:
- **Automatic font scaling**: Compose Text automatically respects system font scale settings via `LocalConfiguration`, so no manual scaling is needed
- Adds semantic properties for better screen reader support
- Supports custom content descriptions for screen readers
- Works with all Material 3 text styles
- Includes Timber logging for debugging accessibility announcements

### Usage Example

```kotlin
AccessibleText(
    text = "Problem 1 of 10",
    style = MaterialTheme.typography.titleMedium,
    contentDescription = "Progress: Problem 1 of 10"
)
```

## Touch Target Compliance

All interactive elements meet the **minimum 48dp × 48dp** touch target size requirement:

- **Number pad buttons**: 64dp × 64dp (exceeds minimum)
- **Action buttons**: Full width with adequate height
- **Icon buttons**: Standard Material 3 size (48dp minimum)

This ensures children can easily tap buttons, even with motor skill challenges.

## Semantic Properties

### Button Roles
All buttons explicitly declare their role:
```kotlin
Modifier.semantics {
    role = Role.Button
}
```

### Heading Semantics
Section titles use heading semantics for better navigation:
```kotlin
Modifier.semantics {
    heading()
}
```

### State Descriptions
Buttons provide state information:
```kotlin
Modifier.semantics {
    contentDescription = if (enabled) {
        "Check your answer"
    } else {
        "Enter an answer first, disabled"
    }
}
```

## Testing with TalkBack

### Enabling TalkBack
1. Open Android Settings
2. Go to Accessibility → TalkBack
3. Turn on TalkBack
4. Use volume keys to control navigation

### Testing Checklist
- [ ] Math problems announced correctly with spoken operations
- [ ] All buttons have clear, descriptive labels
- [ ] Answer field announces current state and value
- [ ] Progress updates are announced
- [ ] Navigation flow is logical and predictable
- [ ] State changes (enabled/disabled) are announced

### Expected Behavior
1. **Problem Screen Load**: TalkBack announces "Problem 1 of 10, heading" → "3 plus 5 equals"
2. **Number Button Tap**: "Number 7, button" with haptic feedback
3. **Answer Updated**: "Your answer: 7"
4. **Check Button**: "Check your answer, button"
5. **Correct Answer**: Success sound + haptic + "Great job!" announcement

## WCAG 2.1 Level AA Compliance

### Implemented Guidelines

✅ **1.1 Text Alternatives** - All non-text content has text alternatives
- Math problems have spoken equivalents
- Icons have content descriptions
- Images have alt text

✅ **1.3 Adaptable** - Content structure is preserved
- Semantic headings for sections
- Logical reading order
- Programmatically determinable relationships

✅ **1.4 Distinguishable** - Content is distinguishable
- High contrast mode: ≥4.5:1 contrast ratios
- Text resizing support via AccessibleText
- Information not conveyed by color alone (haptics + audio)

✅ **2.1 Keyboard Accessible** - All functionality keyboard accessible
- All interactive elements have semantic roles
- Focus can reach all controls
- No keyboard traps

✅ **2.4 Navigable** - Users can navigate content
- Logical navigation order
- Descriptive button labels
- Heading hierarchy for sections

✅ **3.1 Readable** - Text is readable and understandable
- Clear, simple language for K-2 audience
- Spoken format for math operations

✅ **3.2 Predictable** - Functionality is predictable
- Consistent navigation patterns
- Buttons behave as expected
- State changes announced clearly

✅ **3.3 Input Assistance** - Help users avoid errors
- Clear error messages ("Try again")
- Disabled states prevent invalid actions
- Encouraging feedback

✅ **4.1 Compatible** - Compatible with assistive technologies
- Semantic HTML equivalents in Compose
- Proper ARIA role equivalents (Role.Button)
- Screen reader announcements

## Accessibility Settings Model

The app includes an `AccessibilitySettings` data class for tracking accessibility preferences:

```kotlin
data class AccessibilitySettings(
    val isHighContrastEnabled: Boolean = false,
    val isLargeTextEnabled: Boolean = false,
    val isTalkBackEnabled: Boolean = false,
)
```

Future enhancements will allow users to persist these settings via DataStore.

## Best Practices Followed

1. **Content Descriptions**: All interactive elements have meaningful descriptions
2. **Semantic Roles**: Buttons, headings, and other elements use proper roles
3. **Merged Descendants**: Complex components merge into single announcements
4. **State Awareness**: Disabled buttons announce their unavailability
5. **Contrast Ratios**: All text meets WCAG AA standards (≥4.5:1)
6. **Touch Targets**: All buttons exceed 48dp minimum size
7. **No Color-Only Information**: Color + haptics + audio for feedback
8. **Consistent Navigation**: Predictable patterns across screens

## Future Enhancements

- [ ] Traversal index for custom reading order
- [ ] Focus management with FocusRequester
- [ ] Keyboard navigation improvements
- [ ] Android Accessibility Scanner validation
- [ ] User preference persistence for accessibility settings
- [ ] Voice input for answers
- [ ] Audio reading of problems and instructions

## Resources

- [Android Accessibility Guide](https://developer.android.com/guide/topics/ui/accessibility)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Material 3 Accessibility](https://m3.material.io/foundations/accessible-design/overview)
- [TalkBack Documentation](https://support.google.com/accessibility/android/answer/6283677)

## Support

If you encounter any accessibility issues, please report them on our GitHub repository with:
- Device and Android version
- Accessibility feature being used (TalkBack, high contrast, etc.)
- Description of the issue
- Steps to reproduce
