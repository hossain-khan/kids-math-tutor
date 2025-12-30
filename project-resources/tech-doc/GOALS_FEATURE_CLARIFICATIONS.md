# Goals Feature - Clarification Summary & Remaining Questions

## ✅ Confirmed Understanding

Based on your detailed responses and my code analysis, here's what I've documented in GOALS_FEATURE_ARCHITECTURE.md:

### App Architecture Integration
- **PracticeSession Model:** Existing completion mechanism (10 problems per session)
- **CustomChallenge Model:** Parent-created challenges with 1-50 problems total
- **MathOperation Model:** Addition, Subtraction, Multiplication, Division with difficulty levels
- **GradeLevel:** K, Grade 1, Grade 2 (used to control operation difficulty)

### Goal System Design
- **Goal Catalog:** Reusable saved goals with titles
- **GoalComponent:** Either OperationBased (e.g., "Addition x2") or CustomChallengeBased (e.g., "Math Set A x1")
- **ActiveGoal:** Current goal in progress with per-component tracking
- **GoalHistory:** Completed goals with analytics
- **One Active Goal:** Parent manages which goal is active

### Requirements Captured
- ✅ Kids choose which component to work on (not sequential)
- ✅ 10 problems per operation-based session
- ✅ Custom challenges: variable (1-50 problems, counted as 1 session = 1 completion unit)
- ✅ Progress: (2/2 Additions, 1/3 Subtractions, etc.)
- ✅ Games locked (MathRace, NumberSequence, MemoryMatch)
- ✅ Resume dialog + home screen integration
- ✅ Analytics: time + accuracy per goal

---

## ❓ Critical Questions to Clarify

Before I write implementation code, I need to understand your custom challenge structure better:

### Q1: Custom Challenge Operations Composition

**Scenario:** Parent creates a "Mixed Math Set A" custom challenge with:
- 5 addition problems
- 3 subtraction problems  
- 2 division problems
- (Total: 10 problems)

**Questions:**
1. Is this entire set ONE "worksheet" (counts as 1 completion)?
2. Or is it shown as breaking down into individual operations in the goal?
   - E.g., "Custom Set A → 5 Additions, 3 Subtractions, 2 Division" as separate units?

**Example Goal Using Above:**
Option A (Single Unit):
```
Goal: "Practice Session"
├─ Operation-based: Addition x2
├─ Operation-based: Subtraction x1
└─ Custom Challenge: "Mixed Math Set A" x1  ← Treated as 1 single completion
```

Option B (Breakdown):
```
Goal: "Practice Session"
├─ Operation-based: Addition x2
├─ Operation-based: Subtraction x1
└─ Custom Challenge: "Mixed Math Set A" x1
    → Internally: 5 Additions + 3 Subtractions + 2 Division (shown as breakdown?)
```

**What I assume:** Option A (simpler) - parent's custom challenge is an atomic unit. 
**Confirm or correct?**

### Q2: Custom Challenge Problem Specification

Looking at `CustomChallenge` model in your codebase:
```kotlin
data class CustomChallenge(
    val id: String,
    val title: String,
    val problems: List<MathProblem>,  // Problems already have operation info
    ...
)
```

Each `MathProblem` has:
```kotlin
data class MathProblem(
    val num1: Int,
    val num2: Int,
    val operation: MathOperation,  // Addition, Subtraction, etc.
    val correctAnswer: Int,
)
```

**Questions:**
1. Can a custom challenge contain mixed operations?
   - Yes (e.g., 5 add + 3 subtract)?
   - Or only single operation per challenge?

2. For goal progress reporting, should we:
   - Option A: Show "Custom Challenge 'Math Set A': 1/2 completions" (simple)
   - Option B: Break it down by operation inside the challenge (complex)

**What I assume:** Option A (simpler, matches requirements)
**Confirm or correct?**

### Q3: Problem Difficulty & Randomization

For operation-based goals:

**Current App Behavior:**
- `SimpleProblemGenerator` generates random problems per operation
- `GradeAwareProblemGenerator` adjusts difficulty based on grade level

**Questions:**
1. When parent says "Addition x2" in a goal:
   - Should both sessions (10 problems each) be the same difficulty?
   - Or can difficulty increase based on performance (adaptive)?

2. For custom challenges:
   - Are the problems fixed by parent (no randomization)?
   - Same questions every time?

**What I assume:**
- Operation-based: Fixed difficulty per goal, problems randomized within that difficulty
- Custom: Fixed problems, no randomization
- No adaptive difficulty within a goal (keep it simple)
**Confirm or correct?**

### Q4: Session Completion & "Finishing" Semantics

**In your system:**

When kid does "Addition x2":
- Session 1: Completes 10 addition problems
- Session 2: Completes 10 different addition problems

**Questions:**
1. Does each session need to be completed in one sitting? Or can kid:
   - Start Session 1 (do 5 problems)
   - Close app
   - Reopen and resume from problem 6?

2. For custom challenges with 20 problems:
   - Same (resume-able)?
   - Or must be completed in one sitting?

**What I assume:**
- Sessions must be completed in one sitting (simpler implementation)
- If app closes, resume dialog shows: "You were on [Component]. Resume?"
**Confirm or correct?**

### Q5: Analytics & Accuracy Calculation

**Scenario:** Parent assigns goal with:
- Addition x2
- Custom Math Set (10 problems)

Kid completes:
- Addition Session 1: 9/10 correct (90%)
- Addition Session 2: 8/10 correct (80%)
- Custom Set: 7/10 correct (70%)

**Goal Completion Report Shows:**

Option A (Overall):
```
Goal Completed!
Overall Accuracy: 80% (24/30 problems)
Total Time: 12 minutes
```

Option B (Per-Component):
```
Goal Completed!
- Addition (x2): 85% (17/20 problems), 4 minutes
- Custom Math Set: 70% (7/10 problems), 8 minutes
Overall: 80% (24/30), 12 minutes
```

Option C (Per-Session):
```
Goal Completed!
- Addition Session 1: 90%, 2 min
- Addition Session 2: 80%, 2 min
- Custom Set: 70%, 8 min
Overall: 80%, 12 minutes
```

**What I assume:** Option B (per-component) is most useful for parents
**Confirm or correct?**

---

## 📝 Next Steps (After Clarification)

Once you clarify these questions, I'll:

1. ✅ Finalize domain models (currently drafted)
2. ✅ Create Room database entities & DAOs
3. ✅ Implement repositories & use cases
4. ✅ Create Circuit screens (Presenter + UI)
5. ✅ Add integration hooks (PracticeSession completion, game locks)
6. ✅ Create GitHub issues (6-8 issues for structured implementation)
7. ✅ Create unit tests
8. ✅ Update CHANGELOG.md

**Estimated Effort:** 15-20 hours for complete implementation

---

## 🎯 Decision Summary Template

Here's a template for your answers:

```
Q1 (Custom Challenge Units):     [Option A / Option B]
Q2 (Mixed Operations):             [Yes, mixed / No, single operation only]
Q2b (Progress Reporting):          [Option A / Option B]
Q3 (Difficulty):                   [Fixed / Adaptive]
Q3b (Custom Problems):             [Fixed / Randomized]
Q4 (Session Resumable):            [Yes, in-session resume / No, complete in one go]
Q5 (Analytics Detail Level):       [Option A / Option B / Option C]

Additional Notes:
[Any other context or preferences]
```

---

## 🔄 Architecture Flexibility

The core architecture in `GOALS_FEATURE_ARCHITECTURE.md` is flexible and can accommodate various answers:

- **Domain Models:** Can be extended to track per-operation details in custom challenges
- **Analytics:** Can store data at any granularity (per-goal, per-component, per-session)
- **Session Handling:** Can add SessionState enum (IN_PROGRESS, PAUSED, COMPLETED)
- **Difficulty Tracking:** Can add DifficultyLevel to OperationBased component

So don't worry about "changing everything" - most design decisions are additive or can be toggled with configs.

---

Ready when you are! Let me know your answers to the 5 questions above, and I'll proceed with:
1. Finalized models
2. Implementation GitHub issues
3. Code scaffolding

🚀
