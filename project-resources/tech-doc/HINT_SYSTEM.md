# Hint System Architecture

Kids Math Pup Tutor implements a progressive 3-tier hint system designed to guide children toward solving math problems while maintaining pedagogical best practices. This document describes the complete hint system architecture, implementation details, and design decisions.

## Overview

The hint system provides graduated assistance to children struggling with math problems, following educational psychology principles:
1. **Minimal intervention first**: Start with gentle encouragement
2. **Visual learning support**: Provide concrete visual representations
3. **Explicit instruction**: Show step-by-step solutions when needed

The system is **device-agnostic** - the same hint logic works on phones, tablets, and foldable devices, with only UI sizing adapting to screen dimensions.

## Progressive 3-Tier Hint System

### Activation Trigger

Hints become available after **2 consecutive wrong attempts** on a problem:
- First wrong attempt: No hints shown (encourages independent problem-solving)
- Second wrong attempt: "💡 Need help?" button appears
- System prevents hints from appearing too early to promote learning

### Tier 1: Text Hints (HintProvider)

**Button**: "💡 Need help?"

**Purpose**: Provide operation-specific guidance without revealing the answer.

**Implementation**:
- Interface: `HintProvider` ([HintProvider.kt](../../app/src/main/java/dev/hossain/mathtutor/domain/hint/HintProvider.kt))
- Default: `DefaultHintProvider`
- Location: `domain/hint/` package

**Hint Examples**:
```kotlin
// Addition
"Try counting both numbers together"
"Add the first number and the second number"

// Subtraction
"Start with 13, then take away 3. Count what's left!"

// Multiplication
"Make 4 groups of 3 dots each"

// Division
"Split 12 into groups of 3"
```

**Characteristics**:
- Max 60 characters for readability
- Operation-specific language
- Encourages mental strategies
- Never reveals the answer directly

**UI Presentation**:
- Shows in `AlertDialog` with "💡 Hint" title
- Includes Math Pup teaching sticker
- Adaptive sticker sizing:
  - Phone (<600dp): 120dp
  - Tablet (≥600dp): 180dp

### Tier 2: Visual Hints (DotVisualizer)

**Button**: "🎨 Show visually"

**Purpose**: Provide concrete visual representations using animated dots to help children understand the mathematical operation.

**Implementation**:
- Component: `DotVisualizer` ([DotVisualizer.kt](../../app/src/main/java/dev/hossain/mathtutor/ui/component/DotVisualizer.kt))
- Feasibility Check: `VisualHintFeasibilityChecker` ([VisualHintFeasibilityChecker.kt](../../app/src/main/java/dev/hossain/mathtutor/domain/hint/VisualHintFeasibilityChecker.kt))
- Location: `ui/component/` package

**Visual Representations by Operation**:

#### Addition (e.g., 8 + 5)
- Shows first group of dots (8 blue dots)
- Shows plus sign (➕)
- Shows second group of dots (5 purple dots)
- All dots animate in with staggered timing
- Encourages counting all dots together

#### Subtraction (e.g., 13 - 3)
- Shows all starting dots (13 dots)
- After 1 second delay, dims the last N dots (3 dots fade to gray)
- Remaining bright dots show the answer
- Animation helps visualize "taking away"
- Hint text: "Start with X, then take away Y. Count what's left!"

#### Multiplication (e.g., 4 × 3)
- Shows dots arranged in groups
- Each group contains the multiplicand (3 dots per group)
- Shows multiplier number of groups (4 groups)
- Helps visualize repeated addition concept

#### Division (e.g., 12 ÷ 3)
- Shows all dots being distributed into equal groups
- Helps visualize fair sharing concept
- Shows how many groups can be formed

**Animation Details**:
- Staggered dot appearance using `LaunchedEffect` with delays
- `animationDurationMs = 1200` (default)
- `staggerDelayMs = 100` (default)
- `LinearEasing` for consistent timing
- Each dot scales from 0f to 1f for smooth reveal

**UI Presentation**:
- Shows in `AlertDialog` with "🎨 Visual Hint" title
- Includes Math Pup juggling balls sticker
- Adaptive sticker sizing:
  - Phone (<600dp): 100dp
  - Tablet (≥600dp): 150dp
- Displays dot visualization with operation-specific hint text
- Centered layout for optimal viewing

**Accessibility**:
- Content description: "Visual representation showing X and Y for [operation]"
- Explanation text provided alongside visualization
- Works with TalkBack screen reader

### Tier 3: Step-by-Step Work Breakdown (WorkProvider)

**Button**: "📚 How to solve"

**Purpose**: Provide explicit step-by-step solution when visual hints aren't sufficient.

**Implementation**:
- Interface: `WorkProvider` ([WorkProvider.kt](../../app/src/main/java/dev/hossain/mathtutor/domain/work/WorkProvider.kt))
- Default: `DefaultWorkProvider`
- Component: `StepByStepBreakdown` ([StepByStepBreakdown.kt](../../app/src/main/java/dev/hossain/mathtutor/ui/component/StepByStepBreakdown.kt))
- Location: `domain/work/` and `ui/component/` packages

**Work Breakdown Structure**:
```kotlin
data class WorkBreakdownStep(
    val emoji: String,      // Visual indicator
    val description: String // Step explanation
)
```

**Example Breakdown (Addition: 8 + 5)**:
```
Step 1: 🔢 Start with the first number: 8
Step 2: ➕ Add the second number: 5
Step 3: 🧮 Count up from 8: 9, 10, 11, 12, 13
✨ Answer: 13
```

**Example Breakdown (Subtraction: 13 - 3)**:
```
Step 1: 🔢 Start with: 13
Step 2: ➖ Take away: 3
Step 3: 🧮 Count backwards: 12, 11, 10
✨ Answer: 10
```

**Characteristics**:
- Operation-specific steps
- Uses emoji for visual engagement
- Shows thinking process, not just answer
- Staggered animation (200ms delay per step)

**UI Presentation**:
- Shows in `AlertDialog` with "📚 How to Solve" title
- Problem display at top
- Animated step reveal (each step appears 200ms after previous)
- Final answer highlighted at bottom
- Confirm button: "Understand now!"

**Display Logic**:
- **Portrait mode (phone)**: Shows via "💡 Need help?" button flow
- **Landscape mode (tablet/expanded)**: Shows dedicated "📚 How to solve" button

## Visual Hint Feasibility Detection

Not all problems are suitable for visual representation with dots. The system uses **smart feasibility checking** to prevent overwhelming visualizations.

### Feasibility Thresholds

**Addition** (`isAdditionFeasible`):
- ✅ Feasible: Both operands ≤ 20
- ❌ Not feasible: Any operand > 20
- Reason: 54 + 43 = 97 dots would be overwhelming

**Subtraction** (`isSubtractionFeasible`):
- ✅ Feasible: Minuend ≤ 20
- ❌ Not feasible: Minuend > 20
- Reason: Only need to show the minuend as dots

**Multiplication** (`isMultiplicationFeasible`):
- ✅ Feasible: Both operands ≤ 9
- ❌ Not feasible: Any operand > 9
- Reason: 14 × 11 = 154 dots in groups would be too complex

**Division** (`isDivisionFeasible`):
- ✅ Feasible: Dividend ≤ 100 AND divisor ≤ 10
- ❌ Not feasible: Otherwise
- Reason: Large dividends create too many dots to distribute

**Mixed Operations**:
- ❌ Never feasible
- Reason: No clear visual representation strategy

### UI Impact

When visual hints are **not feasible**:
- "🎨 Show visually" button is **hidden** in hint dialog
- Only "Got it" button appears as dismiss action
- Text hints remain available
- Work breakdown remains available

When visual hints **are feasible**:
- Two buttons appear: "🎨 Show visually" and "Got it"
- User can choose to see visual or dismiss

### State Management

```kotlin
// In MathPracticeScreen.State
val isVisualHintFeasible: Boolean = false

// Set by presenter based on current problem
isVisualHintFeasible = visualHintFeasibilityChecker.isFeasible(currentProblem)
```

## Adaptive Layout Considerations

The hint system adapts to different screen sizes while maintaining consistent functionality:

### Screen Width Breakpoints

```kotlin
private val MEDIUM_WIDTH_BREAKPOINT: Dp = 600.dp
private val EXPANDED_WIDTH_BREAKPOINT: Dp = 840.dp
```

### Adaptive Elements

**Sticker Sizes** (Math Pup illustrations):
```kotlin
// Text Hint Dialog
val stickerSize = if (maxWidth >= MEDIUM_WIDTH_BREAKPOINT) 180.dp else 120.dp

// Visual Hint Dialog
val stickerSize = if (maxWidth >= MEDIUM_WIDTH_BREAKPOINT) 150.dp else 100.dp
```

**Button Placement**:
- **Portrait/Compact**: All hint buttons appear after "Check Answer" button
- **Landscape/Expanded**: "📚 How to solve" button may appear in right column

**Dialog Layout**:
- Uses `BoxWithConstraints` to measure available width
- Adjusts spacing and sizing based on measurements
- All dialogs use `AlertDialog` for consistent behavior

### Device Type Consistency

**Important**: The hint system does NOT have different implementations for phones vs tablets:
- Same hint text generation logic
- Same visual hint animations
- Same work breakdown steps
- Only UI sizing adapts to screen dimensions

## Parent Controls

Parents can control hint system availability via **Parent Settings**.

### Hint System Toggle

**Location**: [ParentSettingsUi.kt](../../app/src/main/java/dev/hossain/mathtutor/ui/parentsettings/ParentSettingsUi.kt) (lines 464-560)

**Options**:
- **Enable** (default): All hint tiers available to children
- **Disable**: No hint buttons appear, even after wrong attempts

**Settings Card UI**:
```kotlin
HintSystemCard(
    isHintSystemEnabled = state.isHintSystemEnabled,
    onToggleHintSystem = { enabled ->
        state.eventSink(ParentSettingsScreen.Event.HintSystemToggled(enabled))
    }
)
```

**Storage**:
- Stored in `DataStore` via `UserPreferencesRepository`
- Key: `"hint_system_enabled"`
- Default: `true`

**Presenter Logic**:
```kotlin
// Load hint system preference
LaunchedEffect(Unit) {
    userPreferencesRepository.isHintSystemEnabled.collect { enabled ->
        isHintSystemEnabled = enabled
    }
}

// Only show hint button if system is enabled
showHintButton = wrongAttempts >= 2 && isHintSystemEnabled
```

### Analytics Tracking

**Event**: `PARENT_HINT_SYSTEM_TOGGLED`

**Parameters**:
- `enabled`: Boolean value (true/false)

**Usage**: Helps track how many parents disable hints and its impact on learning outcomes.

## State Management

The hint system uses Circuit's unidirectional data flow pattern.

### State Properties

```kotlin
data class State(
    // ... other properties
    val wrongAttempts: Int = 0,              // Tracks attempts per problem
    val showHintButton: Boolean = false,      // Show "Need help?" button
    val currentHintText: String? = null,      // Current hint text to display
    val hintButtonClicked: Boolean = false,   // Track if hint was requested
    val showVisualHint: Boolean = false,      // Show visual hint dialog
    val isVisualHintFeasible: Boolean = false, // Can show visual for this problem
    val showWorkBreakdown: Boolean = false,   // Show work breakdown dialog
    val workBreakdownSteps: List<WorkBreakdownStep> = emptyList(), // Steps to display
    val eventSink: (Event) -> Unit
) : CircuitUiState
```

### Events

```kotlin
sealed interface Event : CircuitUiEvent {
    data object RequestHint : Event        // User clicked "Need help?"
    data object DismissHint : Event        // User closed hint dialog
    data object ShowVisualHint : Event     // User clicked "Show visually"
    data object DismissVisualHint : Event  // User closed visual hint
    data object ShowWork : Event           // User clicked "How to solve"
    data object DismissWork : Event        // User closed work breakdown
}
```

### State Updates

**Wrong Answer Handling**:
```kotlin
if (!correct) {
    wrongAttempts++
    if (wrongAttempts >= 2 && isHintSystemEnabled) {
        showHintButton = true
    }
    // Trigger shake animation and error haptic
}
```

**Hint Request**:
```kotlin
is MathPracticeScreen.Event.RequestHint -> {
    currentHintText = hintProvider.getFirstHint(currentProblem)
    hintButtonClicked = true
    isVisualHintFeasible = visualHintFeasibilityChecker.isFeasible(currentProblem)
    
    // Analytics tracking
    analyticsService.logEvent(
        eventName = AnalyticsEvent.HINT_REQUESTED,
        parameters = mapOf(
            "hint_level" to "text",
            "operation" to currentProblem.operation.name,
            "problem" to currentProblem.getDisplayString()
        )
    )
}
```

**Visual Hint Request**:
```kotlin
is MathPracticeScreen.Event.ShowVisualHint -> {
    showVisualHint = true
    
    // Analytics tracking
    analyticsService.logEvent(
        eventName = AnalyticsEvent.VISUAL_HINT_SHOWN,
        parameters = mapOf(
            "operation" to currentProblem.operation.name,
            "problem" to currentProblem.getDisplayString()
        )
    )
}
```

**State Reset on New Problem**:
```kotlin
is MathPracticeScreen.Event.NextProblem -> {
    // Reset all hint-related state
    wrongAttempts = 0
    showHintButton = false
    currentHintText = null
    hintButtonClicked = false
    showVisualHint = false
    showWorkBreakdown = false
    workBreakdownSteps = emptyList()
}
```

## Analytics & Metrics

The hint system includes comprehensive analytics tracking.

### Tracked Events

**Event**: `HINT_REQUESTED`
- **When**: User clicks "💡 Need help?" button
- **Parameters**:
  - `hint_level`: "text"
  - `operation`: MathOperation name
  - `problem`: Problem display string (e.g., "8 + 5")
  - `wrong_attempts`: Number of wrong attempts before hint

**Event**: `VISUAL_HINT_SHOWN`
- **When**: User clicks "🎨 Show visually" button
- **Parameters**:
  - `hint_level`: "visual"
  - `operation`: MathOperation name
  - `problem`: Problem display string

**Event**: `PARENT_HINT_SYSTEM_TOGGLED`
- **When**: Parent enables/disables hint system
- **Parameters**:
  - `enabled`: Boolean value

### Analytics Constants

Location: [AnalyticsConstants.kt](../../app/src/main/java/dev/hossain/mathtutor/analytics/AnalyticsConstants.kt)

```kotlin
object AnalyticsEvent {
    const val HINT_REQUESTED = "hint_requested"
    const val VISUAL_HINT_SHOWN = "visual_hint_shown"
    const val PARENT_HINT_SYSTEM_TOGGLED = "parent_hint_system_toggled"
}

object AnalyticsParam {
    const val HINT_LEVEL = "hint_level"
}
```

### Key Metrics to Monitor

1. **Hint Usage Rate**: Percentage of problems where hints are requested
2. **Visual vs Text Hints**: Preference for visual learning aids
3. **Hint Effectiveness**: Success rate after viewing hints
4. **Parent Disable Rate**: How often parents turn off hints
5. **Operation-Specific Usage**: Which operations need most help

## Implementation Files

### Domain Layer

**Interfaces**:
- `domain/hint/HintProvider.kt` - Hint text generation contract
- `domain/work/WorkProvider.kt` - Work breakdown generation contract

**Implementations**:
- `domain/hint/DefaultHintProvider.kt` - Operation-specific hints
- `domain/work/DefaultWorkProvider.kt` - Operation-specific steps
- `domain/hint/VisualHintFeasibilityChecker.kt` - Visual hint validation

**Dependency Injection**:
```kotlin
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultHintProvider : HintProvider { ... }

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultWorkProvider : WorkProvider { ... }
```

### UI Layer

**Screen & State**:
- `ui/mathpractice/MathPracticeScreen.kt` - Screen definition with State and Events
- `ui/mathpractice/MathPracticePresenter.kt` - Business logic and state management
- `ui/mathpractice/MathPracticeUi.kt` - UI composition and hint dialogs

**Components**:
- `ui/component/DotVisualizer.kt` - Animated dot visualizations
- `ui/component/StepByStepBreakdown.kt` - Work breakdown display
- `ui/component/WorkStep.kt` - Individual work step with animation
- `ui/component/VisualHintCard.kt` - (Deprecated, kept for reference)

### Data Layer

**Preferences**:
- `data/UserPreferencesRepository.kt` - Hint system enabled/disabled storage
  - `isHintSystemEnabled: Flow<Boolean>`
  - `setHintSystemEnabled(enabled: Boolean)`

## Accessibility Compliance

The hint system follows WCAG 2.1 Level AA guidelines:

### TalkBack Support

**Hint Dialog**:
```kotlin
// Title marked as heading
modifier = Modifier.semantics { heading() }

// Content description for hint text
modifier = Modifier.semantics {
    contentDescription = "Hint to help solve the problem"
}

// Button descriptions
contentDescription = "Show the hint visually with dots and pictures"
contentDescription = "Close this hint"
```

**Visual Hint Dialog**:
```kotlin
// Visual representation description
contentDescription = "Visual representation showing $num1 and $num2 for $operation"

// Explanation text
contentDescription = "Explanation of the visual hint"

// Dismiss button
contentDescription = "Close this visual hint"
```

**Work Breakdown Dialog**:
```kotlin
// Title marked as heading
modifier = Modifier.semantics { heading() }

// Full breakdown description
contentDescription = "Step-by-step breakdown showing how to solve $problem"

// Dismiss button
contentDescription = "Close this step-by-step breakdown"
```

### Touch Targets

All hint buttons meet minimum 48dp × 48dp touch target requirements:
- "💡 Need help?" button: Full width with adequate padding
- "🎨 Show visually" button: TextButton with default Material 3 sizing
- "📚 How to solve" button: Full width button with padding

### Color Contrast

All hint UI elements use Material 3 theme colors:
- Text hint dialog: `MaterialTheme.colorScheme.surface` background
- Visual hint: `MaterialTheme.colorScheme.tertiaryContainer` for cards
- Work breakdown: `MaterialTheme.colorScheme.surface` background
- All text uses appropriate `onSurface` or `onContainer` colors

### Focus Management

Dialogs properly manage focus:
- Auto-focus on dialog appearance
- Focus returns to trigger button on dismiss
- Keyboard navigation supported for dismiss actions

## Performance Considerations

### Animation Optimization

**Dot Visualizer**:
- Uses `LaunchedEffect` with `remember` to avoid recomposition issues
- Staggered animations run on main thread but are lightweight
- Total animation duration: ~1.2 seconds for smooth appearance

**Work Breakdown**:
- Step-by-step reveal uses 200ms delays
- Each step animates independently
- No heavy computations during animation

### State Management

**Efficient Updates**:
- Hint text only computed on request, not preemptively
- Visual feasibility checked once per problem
- Work breakdown steps generated lazily

**Memory Management**:
- Hint text stored as nullable String (small footprint)
- Work breakdown steps are lightweight data classes
- No bitmap caching required for dots (drawn with Canvas)

## Testing Strategies

### Unit Tests

**HintProvider Tests**:
- Verify correct hints for each operation
- Test hint length constraints (≤60 chars)
- Validate operation-specific language

**WorkProvider Tests**:
- Verify correct number of steps per operation
- Test step descriptions for accuracy
- Validate emoji usage

**VisualHintFeasibilityChecker Tests**:
- Test threshold boundaries for each operation
- Verify mixed operations always return false
- Edge cases (zero, negative numbers)

### Integration Tests

**Presenter Tests** ([MathPracticePresenterTest.kt](../../app/src/test/java/dev/hossain/mathtutor/ui/mathpractice/MathPracticePresenterTest.kt)):
- Verify hint button appears after 2 wrong attempts
- Test hint system enable/disable toggle
- Verify state resets on new problem
- Test analytics event firing

**UI Tests**:
- Verify hint dialogs appear with correct content
- Test visual hint animations
- Verify work breakdown step reveals
- Test dismiss actions

### Manual Testing Checklist

- [ ] Hint button appears after exactly 2 wrong attempts
- [ ] Text hint displays operation-specific guidance
- [ ] Visual hint shows for feasible problems only
- [ ] Visual hint animations are smooth and complete
- [ ] Work breakdown steps appear with staggered timing
- [ ] All dialogs dismiss properly
- [ ] Parent settings toggle works correctly
- [ ] TalkBack announces all hint content
- [ ] Hints work on phone, tablet, and foldable devices
- [ ] Landscape mode shows all hint options

## Future Enhancements

### Potential Improvements

**Adaptive Hint Difficulty**:
- Track hint usage patterns per child
- Adjust hint detail level based on learning progress
- Provide more scaffolding for struggling learners

**Voice Hints**:
- Text-to-speech for hint text
- Audio narration of work breakdown steps
- Helpful for pre-readers or audio learners

**Animated Work Breakdown**:
- Show visual representations alongside work steps
- Integrate dot animations with step descriptions
- More engaging and concrete learning experience

**Hint Personalization**:
- Learn which hint types work best for each child
- Prioritize visual vs text hints based on effectiveness
- Adjust hint timing based on individual needs

**Additional Visual Strategies**:
- Number line visualizations
- Ten-frame representations for addition/subtraction
- Array models for multiplication
- More diverse visual learning tools

**Hint History**:
- Track which problems needed hints
- Show parents where child struggles
- Inform adaptive difficulty adjustments

### Technical Debt

**VisualHintCard Component**:
- Currently unused, replaced by AlertDialog approach
- Consider removing or repurposing for inline hints
- Keep if planning future card-based hint UI

**Hint Caching**:
- Consider caching hint text per problem ID
- Avoid regenerating identical hints
- Balance memory vs computation tradeoff

## Design Decisions & Rationale

### Why 2 Wrong Attempts?

**Research-Based Decision**:
- One attempt: Too early, doesn't promote problem-solving
- Two attempts: Balances frustration prevention with learning
- Three+ attempts: Risk of disengagement and negative experience

**Educational Psychology**:
- Zone of Proximal Development: Provide help when needed but not too soon
- Growth Mindset: Allow struggle, but scaffold before frustration

### Why Progressive Tiers?

**Gradual Complexity**:
1. Text hints require abstract thinking
2. Visual hints provide concrete support
3. Work breakdown gives explicit instruction

**Respects Different Learning Styles**:
- Verbal learners: Text hints sufficient
- Visual learners: Benefit from dot representations
- Need explicit instruction: Full work breakdown available

### Why Not Show All Hints Together?

**Cognitive Load Management**:
- Too much information overwhelms young learners
- Progressive disclosure reduces cognitive burden
- Allows child to choose level of help needed

**Encourages Independence**:
- Text hint often sufficient for many problems
- Visual hint as needed escalation
- Work breakdown as last resort

### Why Feasibility Checking?

**User Experience**:
- 97 dots for 54 + 43 would be overwhelming and unhelpful
- Visual hints work best for "subitizable" quantities (≤20)
- Protects children from confusing visualizations

**Educational Value**:
- Visual representations most effective for small quantities
- Larger numbers benefit more from strategies than counting
- Aligns with research on number sense development

## Conclusion

The hint system is a core pedagogical feature of Kids Math Pup Tutor, designed to:
- **Support diverse learning styles** with text, visual, and explicit instruction
- **Promote independence** through progressive disclosure
- **Prevent frustration** with timely, appropriate assistance
- **Respect development** with age-appropriate visual strategies
- **Empower parents** with control over hint availability

The system balances educational best practices with engaging UX, creating a supportive learning environment for K-2 children developing mathematical fluency.

---

**Last Updated**: January 3, 2026
**Related Documentation**:
- [ACCESSIBILITY.md](ACCESSIBILITY.md) - Accessibility features
- [ANALYTICS.md](ANALYTICS.md) - Analytics tracking
- [ADAPTIVE_LAYOUT.md](ADAPTIVE_LAYOUT.md) - Responsive design
