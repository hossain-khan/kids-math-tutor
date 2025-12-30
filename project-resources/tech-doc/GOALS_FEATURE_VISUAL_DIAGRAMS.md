# Goals Feature - Visual Architecture & Data Flow

## 🎯 High-Level User Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          PARENT - Goal Creation Flow                             │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                   │
│  ParentSettings                                                                  │
│       ↓                                                                          │
│   "Manage Goals"                                                                │
│       ↓                                                                          │
│  GoalCatalogScreen                                                              │
│  ├─ View saved goals                                                            │
│  └─ "Create New Goal"                                                           │
│       ↓                                                                          │
│  GoalCreatorScreen (Wizard)                                                     │
│  ├─ Step 1: Enter goal title                                                   │
│  ├─ Step 2: Add components                                                      │
│  │   ├─ Option A: Operation-Based (Operation + Repetitions)                    │
│  │   │            (e.g., "Addition 2x" = 2 sessions of 10 problems each)       │
│  │   │                                                                          │
│  │   └─ Option B: Custom Challenge (Select Challenge + Repetitions)            │
│  │                (e.g., "Math Set A 1x" = 1 complete run-through)            │
│  │                                                                              │
│  ├─ Step 3: Review & Save                                                       │
│  └─ Goal saved to catalog                                                       │
│       ↓                                                                          │
│  GoalCatalogScreen (List Updated)                                              │
│  ├─ Can assign goal to child                                                    │
│  ├─ Can view completion history                                                │
│  └─ Can view analytics                                                          │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                         PARENT - Assign Goal Flow                                │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                   │
│  GoalCatalogScreen                                                              │
│  ├─ [Tap goal]                                                                  │
│  └─ "Assign to Child?"                                                          │
│       ↓                                                                          │
│  [Confirm]                                                                      │
│       ↓                                                                          │
│  Goal becomes ACTIVE                                                            │
│  └─ Only 1 active goal at a time                                               │
│       ↓                                                                          │
│  Parent can now:                                                                │
│  ├─ View child's progress on this screen                                        │
│  ├─ View analytics (time, accuracy)                                             │
│  └─ Cancel/Archive the goal                                                     │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           CHILD - Practice Goal Flow                             │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                   │
│  HomeScreen                                                                      │
│  ┌─────────────────────────────────────┐                                        │
│  │  🎯 Goal in Progress!               │                                        │
│  │  "Math Practice Session"             │                                        │
│  │  [=====●●●---] 5/10 Sessions        │                                        │
│  │  [TAP TO SEE PROGRESS]              │                                        │
│  └─────────────────────────────────────┘                                        │
│       ↓                                                                          │
│  [TAP Banner]                                                                   │
│       ↓                                                                          │
│  GoalProgressScreen                                                             │
│  ┌────────────────────────────────────┐                                        │
│  │ Goal: "Math Practice Session"       │                                        │
│  │ [=========●●●------] Overall: 50%  │                                        │
│  │                                    │                                        │
│  │ ✅ Addition (Session 1/2)          │                                        │
│  │    [====●------] 60% Accuracy      │                                        │
│  │                                    │                                        │
│  │ ▶️  Subtraction (Session 0/3)      │                                        │
│  │    [---] Not started               │                                        │
│  │    [START SESSION]                 │                                        │
│  │                                    │                                        │
│  │ ⭕ Custom Math Set A (1/1)         │                                        │
│  │    [---] Not started               │                                        │
│  │                                    │                                        │
│  └────────────────────────────────────┘                                        │
│       ↓ [Child taps "Subtraction Session"]                                      │
│  MathPracticeScreen                                                             │
│  ├─ Solves 10 subtraction problems                                             │
│  ├─ Gets immediate feedback                                                     │
│  └─ Hits "Submit Session"                                                       │
│       ↓                                                                          │
│  ResultsScreen (Shows Session Results)                                          │
│  ├─ Accuracy: 90%                                                               │
│  ├─ Time: 5 minutes                                                             │
│  └─ [Continue Working]                                                          │
│       ↓                                                                          │
│  GoalProgressScreen (UPDATED)                                                   │
│  ├─ Subtraction Session 0/3 → 1/3 ✓                                            │
│  └─ Overall progress updated                                                    │
│       ↓                                                                          │
│  [Child works on next component]                                                │
│  ...                                                                             │
│       ↓                                                                          │
│  [Child completes all components]                                               │
│       ↓                                                                          │
│  GoalCompletionDialog                                                           │
│  ┌───────────────────────────────────┐                                         │
│  │  🎉 Goal Complete!                │                                         │
│  │  "Math Practice Session"           │                                         │
│  │                                   │                                         │
│  │  📊 Summary:                       │                                         │
│  │  • Time: 45 minutes               │                                         │
│  │  • Accuracy: 85%                  │                                         │
│  │  • Sessions: 6/6 ✓                │                                         │
│  │                                   │                                         │
│  │  [Great Job! All games unlocked] │                                         │
│  └───────────────────────────────────┘                                         │
│       ↓                                                                          │
│  HomeScreen (Goal banner GONE)                                                  │
│  └─ Games are now UNLOCKED again                                               │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                      CHILD - Try to Access Locked Feature                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                   │
│  HomeScreen (or OperationSelectorScreen)                                       │
│       ↓                                                                          │
│  [Tap "Math Race" game]                                                         │
│       ↓                                                                          │
│  ⚠️  GoalActiveDialog                                                            │
│  ┌──────────────────────────────────┐                                          │
│  │ ⏳ Goal in Progress!              │                                          │
│  │ Complete your assigned goal first!│                                          │
│  │                                  │                                          │
│  │ Progress: 5/10 sessions          │                                          │
│  │ Current: Subtraction (2/3)       │                                          │
│  │                                  │                                          │
│  │ [Resume Goal] [Dismiss]         │                                          │
│  └──────────────────────────────────┘                                          │
│       ↓                                                                          │
│  [Resume Goal] → GoalProgressScreen                                             │
│  [Dismiss] → Returns to where they were                                         │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Data Model Relationships

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                     GOAL CATALOG (Saved Goals)                                │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                                │
│  Goal (Reusable Template)                                                    │
│  ├─ id: String                                                               │
│  ├─ title: String (e.g., "Math Practice Session")                           │
│  ├─ description: String?                                                     │
│  ├─ components: List<GoalComponent>  ───────┐                               │
│  ├─ createdAt: Instant                      │                               │
│  └─ isArchived: Boolean                     │                               │
│                                             ↓                               │
│                                  GoalComponent (Union Type)                 │
│                                  ├─ OperationBased                          │
│                                  │  ├─ operation: MathOperation             │
│                                  │  ├─ gradeLevel: GradeLevel              │
│                                  │  ├─ repetitions: Int (e.g., 2)          │
│                                  │  ├─ order: Int                          │
│                                  │  └─ title: String                       │
│                                  │                                          │
│                                  └─ CustomChallengeBased                    │
│                                     ├─ challengeId: String ─────┐           │
│                                     ├─ challengeTitle: String   │           │
│                                     ├─ repetitions: Int         │           │
│                                     ├─ order: Int               │           │
│                                     └─ title: String            │           │
│                                                                 ↓           │
│                                                        CustomChallenge      │
│                                                        (Existing)           │
│                                                        ├─ id                │
│                                                        ├─ title            │
│                                                        ├─ problems: List<  │
│                                                        │    MathProblem>   │
│                                                        └─ ...             │
│                                                                            │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│                    ACTIVE GOAL (Current Practice)                             │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                                │
│  ActiveGoal (Currently Assigned to Child)                                    │
│  ├─ id: String                                                               │
│  ├─ goal: Goal  ────────────┐ (Reference to catalog)                        │
│  ├─ activatedAt: Instant    │                                               │
│  ├─ currentComponentIndex: Int (e.g., 1 = "Subtraction")                   │
│  ├─ componentProgress: List<ComponentProgress>  ─┐                          │
│  │  │                                            │                          │
│  │  ├─ [0] ComponentProgress                     │                          │
│  │  │  ├─ componentId: String                    │                          │
│  │  │  ├─ completedSessions: Int (e.g., 1)     │                          │
│  │  │  ├─ targetSessions: Int (e.g., 2)        │                          │
│  │  │  ├─ isComplete: Boolean                   │                          │
│  │  │  └─ sessionsMetadata: List<SessionMeta>  │                          │
│  │  │     ├─ [0] SessionMetadata                │                          │
│  │  │     │  ├─ sessionId: String               │                          │
│  │  │     │  ├─ completedAt: Instant            │                          │
│  │  │     │  ├─ accuracy: Float (0.9)           │                          │
│  │  │     │  └─ durationSeconds: Long           │                          │
│  │  │     └─ ...                                │                          │
│  │  │                                            │                          │
│  │  ├─ [1] ComponentProgress                     │                          │
│  │  │  ├─ componentId: String                    │                          │
│  │  │  ├─ completedSessions: Int (e.g., 0)     │                          │
│  │  │  ├─ targetSessions: Int (e.g., 3)        │                          │
│  │  │  └─ sessionsMetadata: []                  │                          │
│  │  │                                            │                          │
│  │  └─ [2] ComponentProgress                     │                          │
│  │     ├─ componentId: String                    │                          │
│  │     ├─ completedSessions: Int (e.g., 0)     │                          │
│  │     ├─ targetSessions: Int (e.g., 1)        │                          │
│  │     └─ sessionsMetadata: []                  │                          │
│  │                                               │                          │
│  └─ overallProgress: GoalProgress  ◄────────────┘                           │
│     ├─ totalComponents: Int (3)                                             │
│     ├─ completedComponents: Int (1)                                         │
│     ├─ totalSessions: Int (6)                                              │
│     ├─ completedSessions: Int (1)                                          │
│     ├─ percentComplete: Float (0.167 = 16.7%)                              │
│     └─ estimatedTimeRemaining: Long? (minutes)                             │
│                                                                                │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│                  GOAL HISTORY (Completed Goals Archive)                       │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                                │
│  GoalHistoryEntry (One entry per goal completion)                           │
│  ├─ id: String                                                               │
│  ├─ goal: Goal (snapshot of the goal template)                             │
│  ├─ completedAt: Instant                                                     │
│  ├─ totalTimeSeconds: Long (e.g., 2700 = 45 minutes)                       │
│  ├─ overallAccuracy: Float (e.g., 0.85 = 85%)                              │
│  └─ componentResults: List<ComponentResult>                                 │
│     ├─ [0] ComponentResult (Addition)                                       │
│     │  ├─ componentId: String                                              │
│     │  ├─ componentTitle: String ("Addition")                             │
│     │  ├─ completedSessions: Int (2)                                      │
│     │  ├─ targetSessions: Int (2)                                         │
│     │  ├─ averageAccuracy: Float (0.90)                                   │
│     │  └─ totalTimeSeconds: Long (600)                                    │
│     │                                                                      │
│     ├─ [1] ComponentResult (Subtraction)                                   │
│     │  ├─ componentId: String                                              │
│     │  ├─ componentTitle: String ("Subtraction")                          │
│     │  ├─ completedSessions: Int (3)                                      │
│     │  ├─ targetSessions: Int (3)                                         │
│     │  ├─ averageAccuracy: Float (0.80)                                   │
│     │  └─ totalTimeSeconds: Long (900)                                    │
│     │                                                                      │
│     └─ [2] ComponentResult (Custom Math Set)                                │
│        ├─ componentId: String                                              │
│        ├─ componentTitle: String ("Math Set A")                           │
│        ├─ completedSessions: Int (1)                                      │
│        ├─ targetSessions: Int (1)                                         │
│        ├─ averageAccuracy: Float (0.70)                                   │
│        └─ totalTimeSeconds: Long (480)                                    │
│                                                                                │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔗 Integration Points with Existing Systems

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PRACTICE SESSION COMPLETION HOOK                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  MathPracticeScreen (Kid solves 10 problems)                               │
│       ↓                                                                      │
│  ResultsScreen (Shows accuracy, time)                                       │
│       ↓                                                                      │
│  SessionRepository.savePracticeSession(session)                            │
│       ├─ Saves to PracticeSession entity                                   │
│       │                                                                      │
│       └─ Check: Is there an ActiveGoal?                                    │
│          ↓                                                                  │
│          GoalRepository.getActiveGoal()                                    │
│          ├─ Yes → Link session to goal                                     │
│          │  └─ Create PracticeSessionToGoal entity                         │
│          │  └─ Call updateGoalProgress()                                   │
│          │     ├─ Updates ComponentProgress.completedSessions              │
│          │     ├─ Adds SessionMetadata (accuracy, time)                    │
│          │     ├─ Checks if component complete                             │
│          │     └─ If all components done → completeActiveGoal()            │
│          │        └─ Creates GoalHistoryEntry                              │
│          │                                                                  │
│          └─ No → Session saved normally (no goal tracking)                 │
│                                                                               │
│  Result: Child can resume GoalProgressScreen with updated progress         │
│                                                                               │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                       GAME ACCESS GUARD LOGIC                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  GameSelectionScreen (Kid taps game)                                        │
│       ↓                                                                      │
│  In Event Handler:                                                          │
│  ├─ GoalRepository.getActiveGoal()                                         │
│  ├─ If activeGoal != null                                                  │
│  │  └─ Show GoalActiveDialog (instead of launching game)                   │
│  └─ If activeGoal == null                                                  │
│     └─ Proceed to GameScreen normally                                      │
│                                                                               │
│  Guarded Games:                                                             │
│  ├─ MathRaceScreen                                                         │
│  ├─ NumberSequenceScreen                                                   │
│  └─ MemoryMatchScreen                                                      │
│                                                                               │
│  Unguarded (Always Available):                                             │
│  ├─ HomeScreen                                                             │
│  ├─ OperationSelectorScreen                                                │
│  ├─ MathPracticeScreen                                                     │
│  ├─ StatsScreen                                                            │
│  ├─ BadgesScreen                                                           │
│  └─ SettingsScreen                                                         │
│                                                                               │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                    CUSTOM CHALLENGE SELECTION                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│  GoalCreatorScreen                                                          │
│       ↓                                                                      │
│  User taps "Add Custom Challenge Component"                                 │
│       ↓                                                                      │
│  CustomChallengeRepository.observeAll()                                    │
│  ├─ Lists all available custom challenges                                  │
│  │  ├─ Challenge ID                                                        │
│  │  ├─ Challenge Title                                                     │
│  │  ├─ Problem Count                                                       │
│  │  └─ Operations (for info)                                               │
│  │                                                                          │
│  └─ User selects one                                                       │
│     ├─ Input: How many times? (repetitions)                               │
│     └─ Creates: GoalComponent.CustomChallengeBased(                        │
│          challengeId = selected.id,                                        │
│          challengeTitle = selected.title,                                  │
│          repetitions = userInput,                                          │
│          order = nextComponentOrder                                        │
│        )                                                                    │
│                                                                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Screen Hierarchy & Navigation

```
HomeScreen
├─ Active Goal Banner (if goal active)
│  └─ [TAP] → GoalProgressScreen
│
├─ [Parent Settings] → ParentSettingsScreen
│  └─ "Goal Management" → GoalCatalogScreen
│     ├─ List of saved goals
│     ├─ [Tap Goal] → Goal Detail
│     │  ├─ [Assign to Child] → Activate goal
│     │  ├─ [View History] → GoalHistoryScreen
│     │  └─ [View Analytics] → GoalAnalyticsScreen
│     │
│     └─ [Create New Goal] → GoalCreatorScreen (Wizard)
│        ├─ Step 1: Enter Title
│        ├─ Step 2: Add Components
│        │  ├─ Operation-based selector
│        │  └─ Custom challenge selector
│        └─ Step 3: Review & Save
│
├─ OperationSelectorScreen
│  └─ [Choose Operation] → MathPracticeScreen
│
├─ GameSelectionScreen
│  └─ [Tap Game] → Check for active goal
│     ├─ If goal active → GoalActiveDialog
│     └─ If no goal → GameScreen (MathRaceScreen, etc.)
│
└─ [While goal active]
   └─ GoalProgressScreen
      ├─ Shows all components with progress
      ├─ [Tap component] → MathPracticeScreen (or launch custom challenge)
      ├─ [Resume] button for current component
      └─ Shows overall progress
         └─ On completion → GoalCompletionDialog

```

---

## ⏱️ Timeline Estimates (by Phase)

```
Phase 1: Domain & Data (3-4 hours)
├─ Domain models (Goal, GoalComponent, ActiveGoal, etc.)
├─ Room entities & converters
└─ Database DAOs

Phase 2: Repositories & Use Cases (2-3 hours)
├─ GoalRepository
├─ Use cases (CreateGoal, ActivateGoal, CompleteGoal, etc.)
└─ Validation logic

Phase 3: Parent UI (4-5 hours)
├─ GoalCatalogScreen
├─ GoalCreatorScreen (multi-step wizard)
├─ GoalHistoryScreen
└─ Integration with ParentSettings

Phase 4: Child UI (3-4 hours)
├─ GoalProgressScreen
├─ GoalActiveDialog
├─ HomeScreen enhancement (goal banner)
└─ Game access guards

Phase 5: Integration & Polish (2-3 hours)
├─ Hook PracticeSession completion
├─ Analytics calculation
├─ Testing
└─ Documentation

Total: 15-20 hours
```

---

## 🧪 Test Coverage Plan

```
Unit Tests (Domain):
├─ GoalComponent validation
├─ Progress calculation
├─ Analytics aggregation
└─ Serialization/deserialization

Integration Tests (Data):
├─ Goal CRUD operations
├─ ActiveGoal lifecycle
├─ History tracking
└─ Component progress updates

UI Tests (Circuit):
├─ GoalCreatorScreen (all steps)
├─ GoalProgressScreen (different states)
├─ GoalActiveDialog (lock behavior)
└─ HomeScreen (banner display)

E2E Tests:
├─ Full parent: create → activate → view analytics
└─ Full child: complete goal → unlock games
```

---

This architecture is ready to go once you clarify the 5 questions! 🚀
