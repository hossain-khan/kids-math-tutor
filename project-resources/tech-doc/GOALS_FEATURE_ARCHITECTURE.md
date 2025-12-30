# Goals Feature Architecture & System Design

## 📋 Feature Overview

**Name:** Parental Goal Management System

**Purpose:** Enable parents to create structured practice goals for their children combining operation-based math practice (Addition, Subtraction, etc.) and custom imported problem sets. Kids complete the assigned goal one component at a time while other app features remain locked until completion.

**User Stories:**
- As a parent, I want to create a goal with specific problem components (e.g., "Addition 2 times, Custom Problem Set A 1 time")
- As a parent, I want to save goals for reuse and assign them to my child
- As a parent, I want to see completion progress and analytics (accuracy, time taken)
- As a child, I want to see my assigned goal and work on it one component at a time
- As a child, I want access to all app features once I complete my goal

---

## 🎯 Core Requirements (As Specified)

### Goal Management
✅ **One Active Goal:** Only one goal active at a time (parent cancels to start another)  
✅ **Goal Titles:** Save goals with titles to a goal catalog  
✅ **No Editing:** Delete and recreate only, no in-place edits  
✅ **No Time Limits:** Progress-based only, not deadline-based  
✅ **History:** Keep history of completed goals with analytics  
✅ **Reusable:** Same goal can be assigned multiple times

### Problem Components
✅ **Child's Choice:** Kids select which component to work on next from overview  
✅ **Fixed Difficulty:** Operation-based problems use fixed difficulty (randomized within level)  
✅ **Custom Fixed:** Parent's custom challenges are fixed as-is  
✅ **Session Size:** 10 problems per operation-based session; custom = parent's set size (1-50)  
✅ **Completion:** Each worksheet/session = one completion unit

### Progress Tracking
✅ **Component Breakdown:** Show each component separately (e.g., "2/2 Additions, 1/3 Subtractions")  
✅ **Completion-Based:** Not accuracy-based for counting progress  
✅ **Home Screen Display:** Show active goal prominently on home page  
✅ **Analytics:** Track completion time, accuracy, history per goal

### UI/UX
✅ **Goal Creation:** Parent Settings > Goal Management  
✅ **Locked Features:** Game modes only (MathRace, NumberSequence, MemoryMatch)  
✅ **Lock Dialog:** "Complete your assigned goal first!" when accessing locked features  
✅ **Resume Dialog:** Show if app closed mid-goal  
✅ **No Account Switching:** Single kid, no user selection

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      CIRCUIT UDF SCREENS                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Goal Creation Wizard:         Goal Management UI:           │
│  ├─ GoalCreatorScreen         ├─ GoalCatalogScreen          │
│  └─ GoalComponentSelectorScreen├─ GoalProgressScreen        │
│                                ├─ GoalHistoryScreen         │
│                                └─ GoalAnalyticsScreen       │
│                                                               │
│  Home Screen Enhancement:      Lock/Resume Dialog:           │
│  └─ HomeScreen (show goal)    └─ GoalActiveDialog           │
│                                                               │
└─────────────────────────────────────────────────────────────┘
                                    ↑
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ↓               ↓               ↓
        ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
        │   DOMAIN LAYER   │ │  REPOSITORY      │ │   USE CASES      │
        ├──────────────────┤ ├──────────────────┤ ├──────────────────┤
        │ • Goal           │ │ • GoalRepository │ │ • CreateGoal     │
        │ • GoalComponent  │ │ • GoalComponent  │ │ • ActivateGoal   │
        │ • GoalProgress   │ │   Repository     │ │ • CompleteGoal   │
        │ • GoalHistory    │ │ • GoalHistory    │ │ • GetGoalStatus  │
        │                  │ │   Repository     │ │                  │
        └──────────────────┘ └──────────────────┘ └──────────────────┘
                    ↑               ↑
                    └───────────────┼───────────────┘
                                    ↓
        ┌─────────────────────────────────────────────┐
        │  DATA LAYER (Room Database)                 │
        ├─────────────────────────────────────────────┤
        │                                              │
        │  • GoalEntity (goals catalog)               │
        │  • GoalComponentEntity                      │
        │  • GoalProgressEntity (active goal state)   │
        │  • GoalHistoryEntity (completed goals)      │
        │  • GoalAnalyticsEntity (accuracy, time)     │
        │                                              │
        └─────────────────────────────────────────────┘
```

---

## 📊 Domain Models

### 1. **Goal** (Catalog Entry - Reusable)

```kotlin
@Parcelize
data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,                          // e.g., "Math Practice Session 1"
    val description: String? = null,            // Optional notes
    val components: List<GoalComponent>,        // What to practice
    val createdAt: Instant = Instant.now(),
    val isArchived: Boolean = false,           // For soft deletes
) : Parcelable {
    fun getTotalComponents(): Int = components.size
    fun getTotalSessions(): Int = components.sumOf { it.repetitions }
}
```

### 2. **GoalComponent** (Individual Practice Unit)

```kotlin
@Parcelize
sealed interface GoalComponent : Parcelable {
    val id: String
    val title: String
    val repetitions: Int  // How many times to do this
    val order: Int       // Order in the goal
    
    /**
     * Operation-based: Addition, Subtraction, etc.
     * Each repetition = 1 session of 10 problems
     */
    @Parcelize
    data class OperationBased(
        override val id: String = UUID.randomUUID().toString(),
        val operation: MathOperation,
        val gradeLevel: GradeLevel,
        override val repetitions: Int,  // Number of 10-problem sessions
        override val order: Int,
        override val title: String = operation.displayName,
    ) : GoalComponent

    /**
     * Custom challenge: A parent-imported problem set
     * Each repetition = 1 complete run through the challenge
     */
    @Parcelize
    data class CustomChallengeBased(
        override val id: String = UUID.randomUUID().toString(),
        val challengeId: String,  // References CustomChallenge.id
        val challengeTitle: String,
        override val repetitions: Int,  // Number of times to run the challenge
        override val order: Int,
        override val title: String = challengeTitle,
    ) : GoalComponent
}
```

### 3. **ActiveGoal** (Current Goal + Progress)

```kotlin
@Entity(tableName = "active_goals")
data class ActiveGoalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val goalId: String,              // Reference to Goal catalog
    val activatedAt: Instant,
    val currentComponentIndex: Int = 0,  // Which component kid is on (0-based)
    val componentProgress: List<ComponentProgress>,  // Progress per component
)

// Domain model (not an entity)
data class ActiveGoal(
    val id: String,
    val goal: Goal,
    val activatedAt: Instant,
    val currentComponent: GoalComponent,  // Currently active component
    val componentProgress: List<ComponentProgress>,  // One per component
    val overallProgress: GoalProgress,  // Calculated from components
)

data class ComponentProgress(
    val componentId: String,
    val completedSessions: Int = 0,      // How many times done
    val targetSessions: Int,              // How many times needed
    val isComplete: Boolean = false,      // completedSessions >= targetSessions
    val sessionsMetadata: List<SessionMetadata> = emptyList(),  // Per-session data
)

data class SessionMetadata(
    val sessionId: String,
    val completedAt: Instant,
    val accuracy: Float,                  // 0f-1f (0%-100%)
    val durationSeconds: Long,
)

data class GoalProgress(
    val totalComponents: Int,
    val completedComponents: Int,
    val totalSessions: Int,
    val completedSessions: Int,
    val percentComplete: Float,           // 0f-1f
    val estimatedTimeRemaining: Long?,    // seconds, calculated from past sessions
)
```

### 4. **GoalHistory** (Completed Goals)

```kotlin
@Entity(tableName = "goal_history")
data class GoalHistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val goalId: String,                   // Reference to original goal
    val goalTitle: String,
    val completedAt: Instant,
    val totalTimeSeconds: Long,
    val overallAccuracy: Float,           // 0f-1f
    val componentResults: List<ComponentResult>,  // Per-component data
)

data class ComponentResult(
    val componentId: String,
    val componentTitle: String,
    val completedSessions: Int,
    val targetSessions: Int,
    val averageAccuracy: Float,
    val totalTimeSeconds: Long,
)

// Domain model
data class GoalHistoryEntry(
    val id: String,
    val goal: Goal,
    val completedAt: Instant,
    val totalTimeSeconds: Long,
    val overallAccuracy: Float,
    val componentResults: List<ComponentResult>,
)
```

---

## 💾 Data Persistence

### Database Entities (Room)

```kotlin
// 1. Goal Catalog
@Entity(tableName = "goals_catalog")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val components: String,  // JSON serialized List<GoalComponent>
    val createdAt: Long,     // Instant.toEpochMilli()
    val isArchived: Boolean = false,
)

// 2. Active Goal Progress
@Entity(tableName = "active_goals")
data class ActiveGoalEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val activatedAt: Long,
    val currentComponentIndex: Int = 0,
    val componentProgress: String,  // JSON serialized List<ComponentProgress>
)

// 3. Goal Completion History
@Entity(tableName = "goal_history")
data class GoalHistoryEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val goalTitle: String,
    val completedAt: Long,
    val totalTimeSeconds: Long,
    val overallAccuracy: Float,
    val componentResults: String,  // JSON serialized
)

// 4. Link between PracticeSession and Active Goal
// (to associate session completions with goal progress)
@Entity(
    tableName = "practice_session_to_goal",
    foreignKeys = [
        ForeignKey(entity = PracticeSessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"]),
        ForeignKey(entity = ActiveGoalEntity::class, parentColumns = ["id"], childColumns = ["activeGoalId"]),
    ]
)
data class PracticeSessionToGoalEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val activeGoalId: String,
    val componentIndex: Int,  // Which component this session fulfills
)
```

### Converters

```kotlin
@ProvidedTypeConverter
class GoalsConverter {
    @TypeConverter
    fun fromComponentList(components: List<GoalComponent>): String {
        return Json.encodeToString(components)
    }

    @TypeConverter
    fun toComponentList(json: String): List<GoalComponent> {
        return Json.decodeFromString(json)
    }

    // Similar converters for ComponentProgress, ComponentResult, etc.
}
```

---

## 🔄 State Flow & Use Cases

### Use Case 1: Create Goal (Parent Flow)

```
[ParentSettings] 
  ↓ "Manage Goals"
[GoalCatalogScreen]
  ↓ "Create New Goal"
[GoalCreatorScreen]
  ├─ Enter goal title
  ├─ Add components one by one
  │  ├─ Operation-based: Select operation + times (e.g., "Addition 2x")
  │  └─ Custom: Select custom challenge + times
  └─ "Create Goal" button
    ↓
[GoalRepository.createGoal(goal)] 
  → Saves to Room DB
  ↓
[GoalCatalogScreen] (returns)
  → Goal added to list
```

### Use Case 2: Activate/Assign Goal (Parent Flow)

```
[GoalCatalogScreen]
  → List of saved goals
  ↓ "Assign to Child"
[GoalRepository.activateGoal(goalId)]
  ├─ Creates ActiveGoalEntity
  ├─ Initializes ComponentProgress for each component
  └─ Sets currentComponentIndex = 0
    ↓
[HomeScreen]
  → Shows active goal banner with progress
  → "Your goal: [Goal Title]"
  → "3/10 Sessions Complete"
```

### Use Case 3: Kid Completes Component Session

```
[GoalProgressScreen]
  → Lists components with progress
  → "Addition (Session 1/2) - TAP TO START"
  ↓ [TAP Component]
[MathPracticeScreen]
  → Kid solves 10 problems
  ↓ [Complete Session]
[PracticeSession completed]
  ↓
[SessionRepository.savePracticeSession(session)]
[GoalRepository.updateGoalProgress(
    activeGoalId,
    componentIndex,
    accuracy,
    duration
)]
  ├─ Creates SessionMetadata
  ├─ Increments ComponentProgress.completedSessions
  ├─ Checks if component complete
  └─ If all sessions done → moves to next component
    ↓
[GoalProgressScreen]
  → Progress updated: "2/2 Additions Complete ✓"
  → Next component auto-highlighted
```

### Use Case 4: Complete Entire Goal

```
[GoalProgressScreen]
  → Last component, last session in progress
  ↓ [Complete Final Session]
[GoalRepository.completeActiveGoal(activeGoalId)]
  ├─ Creates GoalHistoryEntry
  ├─ Clears ActiveGoalEntity
  ├─ Calculates analytics (overall accuracy, time)
  └─ Deletes PracticeSessionToGoal links
    ↓
[GoalCompletionDialog]
  → Shows celebration + summary
  → "Great job! You completed [Goal]!"
  → Stats: Time: 45 min, Accuracy: 92%
    ↓
[HomeScreen]
  → Goal banner gone
  → Games unlocked again
```

### Use Case 5: Access Locked Feature

```
[GameSelectionScreen]
  ↓ [Tap MathRace]
[GoalRepository.getActiveGoal()]
  → Returns ActiveGoal (not null)
    ↓
[GoalActiveDialog]
  → Shows: "Complete your assigned goal first!"
  → "Progress: 3/10 sessions"
  → "Tap here to resume goal"
  ↓ [Dismiss or Resume]
```

---

## 🎨 UI Layer - Circuit Screens

### 1. **GoalCatalogScreen** (Parent Settings)

**State:**
```kotlin
data class GoalCatalogScreen.State(
    val goals: List<Goal> = emptyList(),
    val loading: Boolean = false,
    val eventSink: (Event) -> Unit,
) : CircuitUiState

sealed interface GoalCatalogScreen.Event : CircuitUiEvent {
    object CreateNewGoal : Event
    data class DeleteGoal(val goalId: String) : Event
    data class ActivateGoal(val goalId: String) : Event
    data class ViewHistory(val goalId: String) : Event
    data class ViewAnalytics(val goalId: String) : Event
}
```

**Presenter:**
- Load all goals from GoalRepository
- Handle delete, activate, view history/analytics
- Show dialog on activate: "Assign to child? They can work on this now."

### 2. **GoalCreatorScreen** (Wizard)

**State:**
```kotlin
data class GoalCreatorScreen.State(
    val stepIndex: Int = 0,  // 0=Title, 1=SelectComponents, 2=Review
    val goalTitle: String = "",
    val goalDescription: String? = null,
    val components: List<GoalComponent> = emptyList(),
    val selectedOperation: MathOperation? = null,
    val selectedCustomChallenge: CustomChallenge? = null,
    val eventSink: (Event) -> Unit,
) : CircuitUiState

sealed interface GoalCreatorScreen.Event : CircuitUiEvent {
    data class SetTitle(val title: String) : Event
    data class AddComponent(val component: GoalComponent) : Event
    data class RemoveComponent(val index: Int) : Event
    object NextStep : Event
    object PreviousStep : Event
    object SaveGoal : Event
}
```

**Presenter & UI:**
- Step 1: Text field for goal title
- Step 2: List to add components
  - "Operation Based" button → Shows operation selector + repetitions
  - "Custom Challenge" button → Shows custom challenges list + repetitions
- Step 3: Review components list + Save button

### 3. **GoalProgressScreen** (Kid View - Active Goal)

**State:**
```kotlin
data class GoalProgressScreen.State(
    val activeGoal: ActiveGoal,
    val currentComponent: GoalComponent,
    val componentProgress: ComponentProgress,
    val overallProgress: GoalProgress,
    val eventSink: (Event) -> Unit,
) : CircuitUiState

sealed interface GoalProgressScreen.Event : CircuitUiEvent {
    data class StartComponent(val componentIndex: Int) : Event
    object ResumeCurrentComponent : Event
}
```

**Presenter & UI:**
- Shows goal title + overall progress bar
- Lists all components with individual progress
- Highlights current component
- "Start Next Session" button for current component

### 4. **GoalHistoryScreen** (Parent View)

**State:**
```kotlin
data class GoalHistoryScreen.State(
    val histories: List<GoalHistoryEntry> = emptyList(),
    val selectedHistory: GoalHistoryEntry? = null,
    val eventSink: (Event) -> Unit,
) : CircuitUiState

sealed interface GoalHistoryScreen.Event : CircuitUiEvent {
    data class SelectHistory(val historyId: String) : Event
    object Dismiss : Event
}
```

**UI:**
- Timeline/list of completed goals
- Each entry shows: date completed, time taken, accuracy, components
- Tap for detailed breakdown per component

### 5. **HomeScreen Enhancement**

**Add to HomeScreen:**
```kotlin
if (activeGoal != null) {
    GoalProgressBanner(
        goal = activeGoal.goal,
        progress = activeGoal.overallProgress,
        onTap = { navigator.goTo(GoalProgressScreen()) }
    )
}
```

**Banner shows:**
- Goal title
- Progress bar (X/Y sessions complete)
- Current component

### 6. **GoalActiveDialog** (Lock Behavior)

**Shows when kid accesses locked features:**
```kotlin
AlertDialog(
    title = "Goal in Progress!",
    text = "Complete your assigned goal first!\n\n" +
           "Progress: ${progress.completedSessions}/${progress.totalSessions} sessions",
    buttons = {
        "Resume Goal" → navigator.goTo(GoalProgressScreen())
        "Dismiss"
    }
)
```

---

## 📱 Integration Points

### 1. **PracticeSession Completion Hook**

When a practice session completes (MathPracticeScreen):

```kotlin
// In SessionRepository or usecase
suspend fun completePracticeSession(session: PracticeSession) {
    sessionRepo.save(session)
    
    // Link to goal if one is active
    val activeGoal = goalRepo.getActiveGoal()
    if (activeGoal != null) {
        // Create PracticeSessionToGoalEntity link
        goalRepo.linkSessionToGoal(session.id, activeGoal.id, componentIndex)
        
        // Update goal progress
        goalRepo.updateGoalProgress(
            activeGoalId = activeGoal.id,
            componentIndex = componentIndex,
            accuracy = session.accuracy,
            durationSeconds = session.durationSeconds
        )
    }
}
```

### 2. **Game Access Guard**

In game selection screens (MathRaceScreen, MemoryMatchScreen, NumberSequenceScreen):

```kotlin
@CircuitInject(GameSelectionScreen::class, AppScope::class)
@Composable
fun GameSelectionPresenter(
    @Assisted screen: GameSelectionScreen,
    @Assisted navigator: Navigator,
    goalRepository: GoalRepository,  // Injected
): GameSelectionScreen.State {
    val activeGoal by goalRepository.observeActiveGoal().collectAsState(null)
    
    return GameSelectionScreen.State(
        games = games,
        hasActiveGoal = activeGoal != null,
        eventSink = { event ->
            when (event) {
                is GameSelectionScreen.Event.SelectGame -> {
                    if (activeGoal != null) {
                        // Show dialog instead of navigating
                        showGoalActiveDialog(activeGoal, navigator)
                    } else {
                        navigator.goTo(GameScreen(event.game))
                    }
                }
            }
        }
    )
}
```

### 3. **Custom Challenge Selection**

When parent selects custom challenge for goal component:

```kotlin
// In GoalCreatorPresenter
val customChallenges by customChallengeRepository
    .observeAll()
    .collectAsState(emptyList())

// Show challenges in component selector
// When selected: GoalComponent.CustomChallengeBased(
//     challengeId = challenge.id,
//     challengeTitle = challenge.title,
//     repetitions = userSelectedReps,
//     ...
// )
```

---

## 🔐 Validation & Error Handling

### Goal Validation

```kotlin
data class GoalValidationResult(
    val isValid: Boolean,
    val errors: List<GoalError>,
)

sealed interface GoalError {
    object TitleEmpty : GoalError
    object NoComponents : GoalError
    data class InvalidRepetitions(val componentTitle: String) : GoalError
    data class InvalidCustomChallenge(val challengeId: String) : GoalError
}

fun Goal.validate(): GoalValidationResult {
    val errors = mutableListOf<GoalError>()
    
    if (title.isBlank()) errors.add(GoalError.TitleEmpty)
    if (components.isEmpty()) errors.add(GoalError.NoComponents)
    
    components.forEach { component ->
        when {
            component.repetitions < 1 -> 
                errors.add(GoalError.InvalidRepetitions(component.title))
            component is GoalComponent.CustomChallengeBased && 
                !challengeExists(component.challengeId) ->
                errors.add(GoalError.InvalidCustomChallenge(component.challengeId))
        }
    }
    
    return GoalValidationResult(errors.isEmpty(), errors)
}
```

---

## 📈 Analytics & Reporting

### Data Tracked Per Goal

- **Overall:** Completion time, accuracy, date
- **Per Component:** Time per component, accuracy per component
- **Per Session:** Duration, accuracy, timestamp

### Sample Queries

```kotlin
// Get average completion time for a goal (across all history)
suspend fun getAverageCompletionTime(goalId: String): Long?

// Get accuracy trend for a goal (across multiple completions)
suspend fun getAccuracyTrend(goalId: String): List<Float>

// Get most common component (by completion frequency)
suspend fun getMostPracticedComponent(goalId: String): String?
```

---

## 🧪 Testing Strategy

### Unit Tests
- Goal validation
- Progress calculation
- Component ordering
- History tracking

### Integration Tests
- Creating and saving goals
- Activating goals
- Linking sessions to goals
- Completing goals

### E2E Tests
- Full parent flow: create → activate → kid completes
- Analytics generation
- Resume behavior after app close

---

## 📋 Migration & Versioning

### Initial Version (v1.0)
- Single goal active at once
- No editing goals
- Basic progress tracking
- Simple analytics

### Future Enhancements (v2.0+)
- Multiple active goals (per difficulty or subject)
- Goal templates library
- Advanced analytics (learning curves, weak areas)
- Goal recommendations based on performance
- Weekly/monthly goals with scheduling

---

## 🚀 Implementation Roadmap

### Phase 1: Domain & Data Layer (3-4 hours)
1. Create domain models (Goal, GoalComponent, ActiveGoal, etc.)
2. Create Room entities & DAOs
3. Implement GoalRepository

### Phase 2: Use Cases & Business Logic (2-3 hours)
1. Create use cases (CreateGoal, ActivateGoal, CompleteGoal, etc.)
2. Implement progress calculation logic
3. Add validation

### Phase 3: UI Layer - Parent (4-5 hours)
1. GoalCatalogScreen
2. GoalCreatorScreen (wizard)
3. GoalHistoryScreen
4. Integrate with ParentSettings

### Phase 4: UI Layer - Kid (3-4 hours)
1. GoalProgressScreen
2. GoalActiveDialog
3. HomeScreen enhancement
4. Game screen guards

### Phase 5: Integration & Polish (2-3 hours)
1. Hook PracticeSession completion
2. Analytics calculation
3. Testing & QA
4. Documentation

**Total Estimated: 15-20 hours**

---

## 🔗 Dependencies & Integration

### On Existing Systems
- **CustomChallengeRepository:** For selecting custom challenges
- **PracticeSessionRepository:** For linking completions to goals
- **UserProfileRepository:** For kid profile (grade level)
- **GameRepository:** For checking game state

### Metro DI Registration

```kotlin
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class GoalRepositoryImpl constructor(
    private val goalsDao: GoalsDao,
    private val activeGoalDao: ActiveGoalDao,
    private val goalHistoryDao: GoalHistoryDao,
) : GoalRepository {
    // Implementation
}

// Converters
@ContributesTo(AppScope::class)
object GoalsConvertersBinding {
    @Provides
    fun providesGoalsConverter(): GoalsConverter = GoalsConverter()
}
```

---

## ✅ Success Criteria

- ✅ Parent can create goals with mixed component types
- ✅ Parent can assign goals and view progress
- ✅ Kids see goal progress on home screen
- ✅ Kids complete goals one component at a time
- ✅ Games are locked until goal completes
- ✅ Analytics tracked (accuracy, time, history)
- ✅ Resume works if app closes mid-goal
- ✅ All Material 3 design system compliant
- ✅ Unit tests > 80% coverage
- ✅ Adaptive layout ready for all screen sizes
