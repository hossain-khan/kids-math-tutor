# Phase 1: Core Math Experience - MVP

**Duration**: 3 weeks  
**Goal**: Kids can practice basic math problems and see immediate results  
**Status**: 🔴 Not Started

---

## Overview

This phase establishes the foundational math practice experience. By the end, children should be able to:
1. Open the app and start practicing math immediately
2. Solve simple addition problems (numbers 1-10)
3. Get instant feedback on their answers
4. See their session results

**Key Principle**: Focus on the core loop - problem → answer → feedback → next problem. Everything else can wait.

---

## Features Breakdown

### 1. Math Practice Screen

#### Screen Components

```
┌─────────────────────────────────────┐
│  [Back]         Math Time      [?]  │ ← Top Bar
├─────────────────────────────────────┤
│                                     │
│         Problem 3 of 10             │ ← Progress
│                                     │
│                                     │
│            3 + 5 = ?                │ ← Problem Display
│                                     │
│         ┌─────────────┐             │
│         │      8      │             │ ← Answer Input
│         └─────────────┘             │
│                                     │
│    [1] [2] [3] [4] [5]              │
│    [6] [7] [8] [9] [0]              │ ← Number Pad
│                                     │
│    [Clear]      [Check Answer]      │ ← Action Buttons
│                                     │
└─────────────────────────────────────┘
```

#### UI Specifications

**Top Bar**
- Back button (returns to onboarding for now)
- Title: "Math Time" (dynamic: could be "Addition Practice")
- Help icon (shows simple instructions overlay)

**Progress Indicator**
- Text: "Problem X of 10"
- Material 3 LinearProgressIndicator below text
- Color: `MaterialTheme.colorScheme.primary`

**Problem Display**
- Typography: `MaterialTheme.typography.displayLarge`
- Font size: 48sp minimum (large for readability)
- Color: `MaterialTheme.colorScheme.onSurface`
- Center aligned
- Format: "{num1} {operator} {num2} = ?"

**Answer Input Field**
- Material 3 `OutlinedTextField`
- Read-only (input via number pad only)
- Center aligned text
- Large font: `titleLarge` (32sp)
- Placeholder: "?"
- Max width: 200dp

**Number Pad**
- 2 rows of 5 buttons each
- Button size: 64dp × 64dp (child-friendly touch target)
- `FilledTonalButton` style
- Typography: `titleLarge`
- Spacing: 8dp between buttons
- Grid layout (LazyVerticalGrid)

**Action Buttons**
- Clear button (left): `OutlinedButton`, width 120dp
- Check Answer button (right): `FilledButton`, width 180dp
- Position: Bottom of screen with 16dp padding
- Enable/disable based on input state

#### States & Behavior

**Initial State**
- Answer field is empty
- Check Answer button is disabled
- Clear button is disabled

**Input State**
- User taps number buttons → append to answer field
- Max input length: 2 digits (since answers are 2-18)
- Check Answer button enables when answer field has ≥1 digit
- Clear button enables when answer field has ≥1 digit

**Check Answer Action**
```kotlin
when (checkAnswer()) {
    AnswerResult.CORRECT -> {
        // Show success animation
        // Play success sound (future)
        // Show "Correct! 🎉" message
        // Auto-advance to next problem after 1.5s
    }
    AnswerResult.INCORRECT -> {
        // Shake animation on answer field
        // Show "Try again!" message
        // Clear answer field
        // Keep same problem
        // Track attempt count (for later analytics)
    }
}
```

**Next Problem**
- After correct answer, wait 1.5 seconds
- Fade out current problem
- Fade in next problem
- Reset answer field
- Increment progress counter

**End of Session**
- After 10 problems, navigate to Results Screen

---

### 2. Results Screen

#### Screen Layout

```
┌─────────────────────────────────────┐
│              Great Job!             │
│                                     │
│              🎉 🐶 🎉              │
│                                     │
│         You got 8 out of 10         │
│              correct!               │
│                                     │
│           ⭐⭐⭐⭐⭐               │
│           80% Accuracy              │
│                                     │
│      ┌───────────────────┐          │
│      │  Practice Again   │          │
│      └───────────────────┘          │
│                                     │
│      ┌───────────────────┐          │
│      │   Back to Home    │          │
│      └───────────────────┘          │
│                                     │
└─────────────────────────────────────┘
```

#### UI Specifications

**Title**
- Typography: `displayMedium`
- Text: Dynamic based on performance
  - 90-100%: "Excellent Work!"
  - 70-89%: "Great Job!"
  - 50-69%: "Good Try!"
  - <50%: "Keep Practicing!"

**Emoji/Icon**
- Large celebratory icons
- Size: 64dp
- Center aligned
- Use 🎉 and 🐶 (puppy mascot placeholder)

**Score Display**
- Typography: `headlineLarge`
- Text: "You got {correct} out of {total} correct!"
- Color: `primary` for correct number
- Center aligned

**Star Rating**
- Visual representation of performance
- 5 stars total, filled based on percentage:
  - 90-100%: 5 stars
  - 70-89%: 4 stars
  - 50-69%: 3 stars
  - 30-49%: 2 stars
  - <30%: 1 star
- Size: 32dp per star
- Color: Gold/Yellow from theme

**Accuracy Percentage**
- Typography: `titleLarge`
- Text: "{percentage}% Accuracy"
- Color: `onSurface`

**Action Buttons**
- Practice Again: `FilledButton`, navigate back to Math Practice Screen
- Back to Home: `OutlinedButton`, navigate to home (onboarding for now)
- Width: 250dp
- Height: 56dp
- Spacing: 16dp between buttons

---

### 3. Basic Navigation Flow

```
Onboarding Screen
      ↓
[Start Practicing] button
      ↓
Math Practice Screen (10 problems)
      ↓
Results Screen
      ↓
[Practice Again] → Math Practice Screen
[Back to Home] → Onboarding Screen
```

---

## Technical Implementation

### Architecture

```
app/src/main/java/dev/hossain/mathtutor/
├── circuit/
│   ├── practice/
│   │   ├── MathPracticeScreen.kt       (Screen definition)
│   │   ├── MathPracticePresenter.kt    (Business logic)
│   │   └── MathPracticeUi.kt           (UI composition)
│   └── results/
│       ├── ResultsScreen.kt
│       ├── ResultsPresenter.kt
│       └── ResultsUi.kt
├── domain/
│   ├── model/
│   │   ├── MathProblem.kt
│   │   ├── MathOperation.kt
│   │   └── PracticeSession.kt
│   └── generator/
│       └── ProblemGenerator.kt
└── ui/
    └── components/
        ├── NumberPad.kt
        └── AnswerField.kt
```

### Data Models

#### MathProblem.kt
```kotlin
data class MathProblem(
    val id: String = UUID.randomUUID().toString(),
    val num1: Int,
    val num2: Int,
    val operation: MathOperation,
    val correctAnswer: Int
) {
    fun getDisplayString(): String {
        return "$num1 ${operation.symbol} $num2 = ?"
    }
    
    fun checkAnswer(userAnswer: Int): Boolean {
        return userAnswer == correctAnswer
    }
}
```

#### MathOperation.kt
```kotlin
enum class MathOperation(val symbol: String) {
    ADDITION("+"),
    SUBTRACTION("-"),
    MULTIPLICATION("×"),
    DIVISION("÷");
    
    fun calculate(num1: Int, num2: Int): Int {
        return when (this) {
            ADDITION -> num1 + num2
            SUBTRACTION -> num1 - num2
            MULTIPLICATION -> num1 * num2
            DIVISION -> num1 / num2
        }
    }
}
```

#### PracticeSession.kt
```kotlin
data class PracticeSession(
    val totalProblems: Int = 10,
    val problems: List<MathProblem>,
    val answers: MutableMap<String, SessionAnswer> = mutableMapOf()
) {
    fun getCorrectCount(): Int {
        return answers.values.count { it.isCorrect }
    }
    
    fun getAccuracy(): Float {
        if (answers.isEmpty()) return 0f
        return (getCorrectCount().toFloat() / answers.size) * 100
    }
}

data class SessionAnswer(
    val problemId: String,
    val userAnswer: Int,
    val isCorrect: Boolean,
    val attemptCount: Int = 1,
    val timeSpentSeconds: Long = 0
)
```

### Problem Generator

#### ProblemGenerator.kt
```kotlin
interface ProblemGenerator {
    fun generateProblems(count: Int, operation: MathOperation): List<MathProblem>
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SimpleProblemGenerator constructor() : ProblemGenerator {
    
    override fun generateProblems(
        count: Int,
        operation: MathOperation
    ): List<MathProblem> {
        return (1..count).map {
            generateSingleProblem(operation)
        }
    }
    
    private fun generateSingleProblem(operation: MathOperation): MathProblem {
        return when (operation) {
            MathOperation.ADDITION -> generateAddition()
            else -> throw IllegalArgumentException("Only ADDITION supported in Phase 1")
        }
    }
    
    private fun generateAddition(): MathProblem {
        // Phase 1: Numbers 1-10
        val num1 = Random.nextInt(1, 11)
        val num2 = Random.nextInt(1, 11)
        val answer = num1 + num2
        
        return MathProblem(
            num1 = num1,
            num2 = num2,
            operation = MathOperation.ADDITION,
            correctAnswer = answer
        )
    }
}
```

### Circuit Implementation

#### MathPracticeScreen.kt
```kotlin
@Parcelize
data class MathPracticeScreen(
    val operation: MathOperation = MathOperation.ADDITION,
    val problemCount: Int = 10
) : Screen {
    
    data class State(
        val currentProblemIndex: Int,
        val currentProblem: MathProblem,
        val userAnswer: String,
        val showFeedback: Boolean,
        val feedbackMessage: String,
        val isCorrect: Boolean,
        val session: PracticeSession,
        val eventSink: (Event) -> Unit
    ) : CircuitUiState
    
    sealed interface Event : CircuitUiEvent {
        data class NumberPressed(val number: Int) : Event
        data object ClearPressed : Event
        data object CheckAnswerPressed : Event
        data object NextProblemPressed : Event
    }
}
```

#### MathPracticePresenter.kt
```kotlin
@CircuitInject(MathPracticeScreen::class, AppScope::class)
@Composable
fun mathPracticePresenter(
    screen: MathPracticeScreen,
    navigator: Navigator,
    problemGenerator: ProblemGenerator
): MathPracticeScreen.State {
    
    // Initialize session
    val session = remember {
        val problems = problemGenerator.generateProblems(
            count = screen.problemCount,
            operation = screen.operation
        )
        PracticeSession(
            totalProblems = screen.problemCount,
            problems = problems
        )
    }
    
    var currentIndex by remember { mutableIntStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var showFeedback by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }
    
    val currentProblem = session.problems[currentIndex]
    
    return MathPracticeScreen.State(
        currentProblemIndex = currentIndex,
        currentProblem = currentProblem,
        userAnswer = userAnswer,
        showFeedback = showFeedback,
        feedbackMessage = feedbackMessage,
        isCorrect = isCorrect,
        session = session
    ) { event ->
        when (event) {
            is MathPracticeScreen.Event.NumberPressed -> {
                if (userAnswer.length < 2) {
                    userAnswer += event.number.toString()
                }
            }
            
            is MathPracticeScreen.Event.ClearPressed -> {
                userAnswer = ""
                showFeedback = false
            }
            
            is MathPracticeScreen.Event.CheckAnswerPressed -> {
                val answer = userAnswer.toIntOrNull() ?: 0
                isCorrect = currentProblem.checkAnswer(answer)
                
                session.answers[currentProblem.id] = SessionAnswer(
                    problemId = currentProblem.id,
                    userAnswer = answer,
                    isCorrect = isCorrect
                )
                
                feedbackMessage = if (isCorrect) "Correct! 🎉" else "Try again!"
                showFeedback = true
                
                if (isCorrect) {
                    // Auto-advance after delay
                    LaunchedEffect(Unit) {
                        delay(1500)
                        if (currentIndex < session.totalProblems - 1) {
                            currentIndex++
                            userAnswer = ""
                            showFeedback = false
                        } else {
                            // Navigate to results
                            navigator.goTo(ResultsScreen(session))
                        }
                    }
                } else {
                    userAnswer = ""
                }
            }
            
            is MathPracticeScreen.Event.NextProblemPressed -> {
                // Manual next (not used in Phase 1, auto-advance)
                if (currentIndex < session.totalProblems - 1) {
                    currentIndex++
                    userAnswer = ""
                    showFeedback = false
                }
            }
        }
    }
}
```

#### MathPracticeUi.kt
```kotlin
@CircuitInject(MathPracticeScreen::class, AppScope::class)
@Composable
fun MathPracticeUi(
    state: MathPracticeScreen.State,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Math Time") },
                navigationIcon = {
                    IconButton(onClick = { /* Navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Show help */ }) {
                        Icon(Icons.Default.Info, contentDescription = "Help")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Progress indicator
            ProgressSection(
                current = state.currentProblemIndex + 1,
                total = state.session.totalProblems
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Problem display
            ProblemDisplay(
                problem = state.currentProblem,
                modifier = Modifier.padding(vertical = 32.dp)
            )
            
            // Feedback message
            AnimatedVisibility(visible = state.showFeedback) {
                FeedbackMessage(
                    message = state.feedbackMessage,
                    isCorrect = state.isCorrect
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Answer field
            AnswerField(
                answer = state.userAnswer,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Number pad
            NumberPad(
                onNumberClick = { state.eventSink(MathPracticeScreen.Event.NumberPressed(it)) },
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Action buttons
            ActionButtons(
                canClear = state.userAnswer.isNotEmpty(),
                canCheck = state.userAnswer.isNotEmpty() && !state.showFeedback,
                onClear = { state.eventSink(MathPracticeScreen.Event.ClearPressed) },
                onCheck = { state.eventSink(MathPracticeScreen.Event.CheckAnswerPressed) }
            )
        }
    }
}

@Composable
private fun ProgressSection(current: Int, total: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Problem $current of $total",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { current.toFloat() / total },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )
    }
}

@Composable
private fun ProblemDisplay(problem: MathProblem, modifier: Modifier = Modifier) {
    Text(
        text = problem.getDisplayString(),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
private fun FeedbackMessage(message: String, isCorrect: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            color = if (isCorrect) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}
```

---

## UI Components (Reusable)

### NumberPad.kt
```kotlin
@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: 1-5
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..5).forEach { number ->
                NumberButton(
                    number = number,
                    onClick = { onNumberClick(number) }
                )
            }
        }
        
        // Row 2: 6-0
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (6..9).forEach { number ->
                NumberButton(
                    number = number,
                    onClick = { onNumberClick(number) }
                )
            }
            NumberButton(
                number = 0,
                onClick = { onNumberClick(0) }
            )
        }
    }
}

@Composable
private fun NumberButton(
    number: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.size(64.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleLarge
        )
    }
}
```

### AnswerField.kt
```kotlin
@Composable
fun AnswerField(
    answer: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = answer,
        onValueChange = { }, // Read-only
        readOnly = true,
        textStyle = MaterialTheme.typography.displaySmall.copy(
            textAlign = TextAlign.Center
        ),
        placeholder = {
            Text(
                text = "?",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = modifier
            .width(200.dp)
            .height(80.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}
```

---

## Testing Criteria

### Unit Tests

**ProblemGenerator Tests**
```kotlin
@Test
fun `generateAddition returns correct problem count`() {
    val generator = SimpleProblemGenerator()
    val problems = generator.generateProblems(10, MathOperation.ADDITION)
    assertEquals(10, problems.size)
}

@Test
fun `generateAddition creates problems with numbers 1-10`() {
    val generator = SimpleProblemGenerator()
    val problems = generator.generateProblems(100, MathOperation.ADDITION)
    
    problems.forEach { problem ->
        assertTrue(problem.num1 in 1..10)
        assertTrue(problem.num2 in 1..10)
    }
}

@Test
fun `MathProblem checkAnswer returns true for correct answer`() {
    val problem = MathProblem(
        num1 = 3,
        num2 = 5,
        operation = MathOperation.ADDITION,
        correctAnswer = 8
    )
    
    assertTrue(problem.checkAnswer(8))
    assertFalse(problem.checkAnswer(7))
}
```

**PracticeSession Tests**
```kotlin
@Test
fun `session calculates correct accuracy`() {
    val session = PracticeSession(
        totalProblems = 10,
        problems = emptyList()
    )
    
    session.answers["1"] = SessionAnswer("1", 5, isCorrect = true)
    session.answers["2"] = SessionAnswer("2", 3, isCorrect = false)
    session.answers["3"] = SessionAnswer("3", 7, isCorrect = true)
    
    assertEquals(2, session.getCorrectCount())
    assertEquals(66.67f, session.getAccuracy(), 0.01f)
}
```

### UI Tests (Compose)

```kotlin
@Test
fun `number pad displays all digits`() {
    composeTestRule.setContent {
        NumberPad(onNumberClick = {})
    }
    
    (0..9).forEach { number ->
        composeTestRule.onNodeWithText(number.toString()).assertExists()
    }
}

@Test
fun `clicking number button triggers callback`() {
    var clickedNumber = -1
    
    composeTestRule.setContent {
        NumberPad(onNumberClick = { clickedNumber = it })
    }
    
    composeTestRule.onNodeWithText("5").performClick()
    assertEquals(5, clickedNumber)
}
```

### Manual Testing Checklist

- [ ] App launches without crashes
- [ ] Math practice screen displays correctly
- [ ] Can tap number buttons to input answer
- [ ] Answer field updates when numbers tapped
- [ ] Clear button clears answer field
- [ ] Check button disabled when no answer
- [ ] Correct answer shows success message
- [ ] Incorrect answer shows try again message
- [ ] Progress indicator updates correctly
- [ ] After 10 problems, navigates to results screen
- [ ] Results screen shows correct score
- [ ] Practice Again button starts new session
- [ ] All Material 3 colors used (no hardcoded colors)
- [ ] Works in light and dark mode
- [ ] Touch targets are at least 48dp
- [ ] Text is readable (proper contrast)

---

## Success Metrics

### Technical Metrics
- ✅ App launches in <2 seconds
- ✅ No crashes during 10-problem session
- ✅ 60 FPS during animations
- ✅ All UI tests pass
- ✅ All unit tests pass (>80% coverage)

### User Experience Metrics
- ✅ Child can complete 10 problems without confusion
- ✅ Feedback is immediate (<100ms after button press)
- ✅ Results screen accurately reflects performance
- ✅ App feels responsive and smooth

### Code Quality Metrics
- ✅ Follows Material 3 guidelines
- ✅ No hardcoded colors
- ✅ Proper Circuit architecture
- ✅ Metro DI used correctly
- ✅ Code formatted with ktlint

---

## Migration Plan from Current State

### Step 1: Create Domain Models (Day 1)
1. Create `domain/model/` package
2. Implement `MathProblem.kt`
3. Implement `MathOperation.kt`
4. Implement `PracticeSession.kt`
5. Write unit tests for models

### Step 2: Create Problem Generator (Day 2)
1. Create `domain/generator/` package
2. Implement `ProblemGenerator` interface
3. Implement `SimpleProblemGenerator`
4. Write unit tests
5. Register with Metro DI

### Step 3: Create UI Components (Day 3)
1. Create `ui/components/` package
2. Implement `NumberPad.kt`
3. Implement `AnswerField.kt`
4. Create preview functions
5. Test in isolation

### Step 4: Implement Math Practice Circuit (Days 4-5)
1. Create `circuit/practice/` package
2. Implement `MathPracticeScreen.kt` (data class)
3. Implement `MathPracticePresenter.kt`
4. Implement `MathPracticeUi.kt`
5. Wire up with Circuit
6. Test navigation flow

### Step 5: Implement Results Circuit (Day 6)
1. Create `circuit/results/` package
2. Implement `ResultsScreen.kt`
3. Implement `ResultsPresenter.kt`
4. Implement `ResultsUi.kt`
5. Wire up navigation from practice screen

### Step 6: Update Onboarding Screen (Day 7)
1. Modify `OnboardingScreen.kt`
2. Change "Get Started" to "Start Practicing"
3. Navigate to `MathPracticeScreen` instead of example screens
4. Test complete flow

### Step 7: Testing & Polish (Days 8-10)
1. Run all unit tests
2. Write and run UI tests
3. Manual testing with real device
4. Fix bugs and edge cases
5. Polish animations and transitions
6. Dark mode testing
7. Accessibility testing (TalkBack)

### Step 8: Code Review & Documentation (Days 11-12)
1. Run `./gradlew formatKotlin`
2. Run `./gradlew lintKotlin`
3. Update `CHANGELOG.md`
4. Internal code review
5. Update this document with learnings

### Step 9: User Testing (Days 13-15)
1. Install on test device
2. Have a child (K-2) try the app
3. Observe their experience
4. Note confusion points
5. Iterate based on feedback
6. Final polish

---

## Known Limitations (To Address in Phase 2+)

- ❌ No data persistence (progress lost on app close)
- ❌ Only addition supported (no subtraction, multiplication, division)
- ❌ Fixed difficulty (always 1-10 range)
- ❌ No grade level selection
- ❌ No sound effects
- ❌ No haptic feedback
- ❌ No badges or achievements
- ❌ No streaks or motivation system
- ❌ Limited to 10 problems per session
- ❌ No ability to review missed problems

---

## Dependencies Needed

Check `gradle/libs.versions.toml` for existing versions:

```toml
[libraries]
# Already included (verify):
androidx-compose-material3
androidx-compose-ui
circuit-foundation
circuit-overlay
kotlinx-coroutines-core

# May need to add:
# (None for Phase 1 - all dependencies exist)
```

---

## Definition of Done

Phase 1 is complete when:

- ✅ All code committed and pushed
- ✅ All tests passing (unit + UI)
- ✅ Code formatted with ktlint
- ✅ No lint warnings
- ✅ CHANGELOG.md updated
- ✅ Manual testing completed
- ✅ Real child testing completed (at least 1 session)
- ✅ All success metrics met
- ✅ Build successfully on CI/CD
- ✅ APK installed and tested on physical device
- ✅ Dark mode verified
- ✅ TalkBack accessibility verified

---

## Next Steps (Preview of Phase 2)

After Phase 1 is complete and tested:

1. **Set up Room database** for persistence
2. **Add subtraction** operation
3. **Create operation selector** screen
4. **Implement stats screen** with historical data
5. **Add animations** for correct/incorrect feedback

---

*Document created: December 15, 2025*  
*Phase status: 🔴 Not Started*  
*Target completion: Week 3*
