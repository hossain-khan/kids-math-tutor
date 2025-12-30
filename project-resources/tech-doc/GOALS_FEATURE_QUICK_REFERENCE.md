# Goals Feature - Quick Reference Card

## 📋 One-Pager Summary

**Feature:** Parental goal management system for structured math practice  
**Scope:** Single active goal, reusable catalog, analytics tracking  
**Complexity:** Medium (5 Circuit screens, 4 DB entities, 2-3 repositories)  
**Effort:** 15-20 hours  
**Release:** v1.20.0 (after adaptive layout)

---

## 🎯 Core Concept

```
Parent creates a GOAL with multiple COMPONENTS:
├─ Component 1: "Addition x2" (2 sessions of 10 problems each)
├─ Component 2: "Subtraction x3" (3 sessions of 10 problems each)
└─ Component 3: "Custom Math Set A x1" (1 run-through, however many problems)

Parent assigns GOAL to child → Only that goal is active

Child completes goal one COMPONENT at a time:
1. Choose which component to work on
2. Complete the session
3. Progress shows updated
4. Repeat until all components done

While goal is active:
├─ Games are LOCKED
├─ Core practice (OperationSelector, MathPractice) is OPEN
└─ Home screen shows goal progress

When goal is complete:
├─ Games are UNLOCKED
├─ Analytics available to parent
└─ Goal can be run again or archived
```

---

## 🗂️ Data Models (Quick View)

```kotlin
// CATALOG ENTRY (Reusable)
Goal(
  id: String,
  title: "Math Practice Session",
  components: [
    OperationBased(operation=ADDITION, repetitions=2, order=0),
    CustomChallengeBased(challengeId="abc", repetitions=1, order=1),
  ]
)

// CURRENT STATE (In Progress)
ActiveGoal(
  goal: Goal,
  currentComponentIndex: 0,  // "Addition" is active
  componentProgress: [
    ComponentProgress(completedSessions=1, targetSessions=2),  // Add: 1/2
    ComponentProgress(completedSessions=0, targetSessions=1),  // Custom: 0/1
  ],
  overallProgress: GoalProgress(
    totalSessions=3,
    completedSessions=1,
    percentComplete=0.33
  )
)

// HISTORY (Completed)
GoalHistoryEntry(
  goal: Goal,
  completedAt: Instant,
  totalTimeSeconds: 2700,    // 45 minutes
  overallAccuracy: 0.85,     // 85%
  componentResults: [
    ComponentResult(title="Addition", avgAccuracy=0.90),
    ComponentResult(title="Custom Math Set A", avgAccuracy=0.70),
  ]
)
```

---

## 🎨 UI Screens (5 + Enhancement)

| Screen | User | Purpose | Key Actions |
|--------|------|---------|-------------|
| **GoalCatalogScreen** | Parent | View/manage saved goals | Create, Activate, View History |
| **GoalCreatorScreen** | Parent | Create new goal (wizard) | Add components, select operations/challenges |
| **GoalProgressScreen** | Child | See active goal & work on it | View progress, select component, start session |
| **GoalHistoryScreen** | Parent | View past completions | See analytics, accuracy trends |
| **GoalActiveDialog** | Child | Lock notification | Resume goal or dismiss |
| **HomeScreen** (enhanced) | Child | Goal banner | Quick access to goal progress |

---

## 💾 Database (4 Entities)

```kotlin
// 1. Goal Catalog
GoalEntity {
  id, title, description, components (JSON),
  createdAt, isArchived
}

// 2. Active Goal Progress
ActiveGoalEntity {
  id, goalId, activatedAt, currentComponentIndex,
  componentProgress (JSON)
}

// 3. Goal Completion History
GoalHistoryEntity {
  id, goalId, goalTitle, completedAt,
  totalTimeSeconds, overallAccuracy,
  componentResults (JSON)
}

// 4. Session-to-Goal Link
PracticeSessionToGoalEntity {
  id, sessionId, activeGoalId, componentIndex
}
```

---

## 🔄 Main Flows (5 Critical Paths)

### Path 1: Create Goal (Parent)
```
ParentSettings → Goal Catalog → Create New → 
Wizard (Title → Add Components → Review) → Save
```

### Path 2: Activate Goal (Parent)
```
Goal Catalog → Select Goal → "Assign to Child" → 
Confirm → Goal becomes ACTIVE
```

### Path 3: Complete Session (Child)
```
GoalProgressScreen → Select Component → 
MathPracticeScreen (solve 10) → ResultsScreen → 
Goal Progress Updated
```

### Path 4: Finish Goal (Child)
```
[Last component] → Complete final session → 
GoalCompletionDialog (celebration) → 
Games now UNLOCKED
```

### Path 5: Access Locked Game (Child)
```
Try to tap game → GoalActiveDialog 
("Complete goal first!") → Resume or Dismiss
```

---

## 🤔 Questions Awaiting Your Answers

| Q | Topic | Your Decision |
|---|-------|---------------|
| **1** | Custom challenges: Atomic unit or break down by operation? | ☐ Atomic ☐ Breakdown |
| **2** | Can custom challenges have mixed operations? | ☐ Yes ☐ No |
| **2b** | Progress reporting: Which level of detail? | ☐ Simple ☐ Per-Comp ☐ Per-Session |
| **3** | Difficulty in goals: Fixed per goal or adaptive? | ☐ Fixed ☐ Adaptive |
| **4** | Sessions resumable mid-way? | ☐ Yes ☐ No |
| **5** | Analytics: Which detail level? | ☐ Overall ☐ Per-Comp ☐ Per-Session |

**→ See GOALS_FEATURE_CLARIFICATIONS.md for full context**

---

## 📊 Effort Breakdown

```
Phase 1: Domain + Data           3-4 hours  ████░░░░░░
Phase 2: Repositories + Use Cases 2-3 hours  ██░░░░░░░░
Phase 3: Parent UI               4-5 hours  █████░░░░░
Phase 4: Child UI                3-4 hours  ████░░░░░░
Phase 5: Integration + Polish    2-3 hours  ██░░░░░░░░
                                ─────────
TOTAL                           15-20 hours

Weeks (assuming 5 hrs/week):     3-4 weeks
Weeks (assuming 10 hrs/week):    2 weeks
```

---

## 🧪 Testing Quick Checklist

- [ ] Create goal with mixed components
- [ ] Activate goal and verify only 1 active at a time
- [ ] Complete session and verify progress updates
- [ ] Try to access game while goal active → Dialog shows
- [ ] Resume from lock dialog
- [ ] Complete goal fully → Analytics calculated
- [ ] View goal history
- [ ] Close app mid-goal → Resume dialog shows on reopen
- [ ] Material 3 compliance (colors, spacing, touch targets)
- [ ] Accessibility (TalkBack, high contrast, text sizing)

---

## 🔗 Integration Touchpoints

1. **PracticeSession Completion** → Hook to update goal progress
2. **Game Launch** → Check for active goal, show lock dialog
3. **CustomChallenge Selection** → Use in goal creation wizard
4. **Home Screen** → Add goal progress banner

---

## 📁 Files to Create/Modify

### New Domain Models
```
domain/model/
├─ Goal.kt
├─ GoalComponent.kt
├─ ActiveGoal.kt
└─ GoalHistory.kt
```

### New Data Layer
```
data/local/
├─ entity/
│  ├─ GoalEntity.kt
│  ├─ ActiveGoalEntity.kt
│  ├─ GoalHistoryEntity.kt
│  └─ PracticeSessionToGoalEntity.kt
├─ dao/
│  ├─ GoalDao.kt
│  ├─ ActiveGoalDao.kt
│  ├─ GoalHistoryDao.kt
│  └─ PracticeSessionToGoalDao.kt
└─ Converters.kt (extend existing)

data/repository/
└─ GoalRepositoryImpl.kt
```

### Domain Interfaces
```
domain/repository/
└─ GoalRepository.kt

domain/usecase/
├─ CreateGoalUseCase.kt
├─ ActivateGoalUseCase.kt
├─ CompleteGoalComponentUseCase.kt
└─ GetGoalProgressUseCase.kt
```

### UI Screens (Circuit)
```
ui/goals/
├─ catalog/
│  ├─ GoalCatalogScreen.kt
│  ├─ GoalCatalogPresenter.kt
│  └─ GoalCatalogUi.kt
├─ creator/
│  ├─ GoalCreatorScreen.kt
│  ├─ GoalCreatorPresenter.kt
│  └─ GoalCreatorUi.kt
├─ progress/
│  ├─ GoalProgressScreen.kt
│  ├─ GoalProgressPresenter.kt
│  └─ GoalProgressUi.kt
├─ history/
│  ├─ GoalHistoryScreen.kt
│  ├─ GoalHistoryPresenter.kt
│  └─ GoalHistoryUi.kt
└─ dialog/
   ├─ GoalActiveDialogScreen.kt
   └─ GoalActiveDialogUi.kt

# Also modify
ui/home/HomeScreenPresenter.kt  (add goal banner)
ui/game/GameSelectionPresenter.kt  (add lock check)
```

---

## 🎯 Success Criteria

- ✅ Parent creates goals with mixed component types
- ✅ Parent assigns goal (locks games, shows home banner)
- ✅ Child works on goal one component at a time
- ✅ Child completes goal (games unlock, analytics shown)
- ✅ Lock dialog prevents game access
- ✅ Resume dialog appears if app closes
- ✅ Goal history tracks analytics (accuracy, time)
- ✅ All Material 3 compliant
- ✅ Accessible (TalkBack, high contrast, text sizing)
- ✅ 80%+ unit test coverage

---

## 🚀 Getting Started

**Step 1:** Read these in order (30-45 min total)
1. This file (5 min)
2. GOALS_FEATURE_SUMMARY.md (20 min)
3. GOALS_FEATURE_VISUAL_DIAGRAMS.md (10 min)

**Step 2:** Answer 5 clarification questions (10 min)

**Step 3:** I create GitHub issues with full requirements (15 min)

**Step 4:** Start implementing Phase 1! 🎉

---

## 📞 Questions?

Review docs in this order:
1. Quick question? → This file (quick reference)
2. Understand design? → GOALS_FEATURE_ARCHITECTURE.md
3. Visual reference? → GOALS_FEATURE_VISUAL_DIAGRAMS.md
4. Need clarification? → GOALS_FEATURE_CLARIFICATIONS.md
5. Next steps? → GOALS_FEATURE_SUMMARY.md

**Still stuck?** I can explain any specific aspect in more detail!

---

**Status: ✅ Brainstorm & Design Complete**  
**Next: 🤔 Await your clarification answers**  
**Then: 📝 Create GitHub issues → 💻 Implementation**
