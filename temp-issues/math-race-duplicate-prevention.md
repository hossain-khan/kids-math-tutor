# Prevent duplicate answers in Math Race game

## Problem Statement

The Math Race game currently lacks duplicate answer prevention. During a 60-second timed session with 15-20 problems, users may encounter the same correct answer multiple times, which:
- Reduces learning effectiveness (children don't learn new problems)
- Creates a confusing experience (appears like a bug or bad randomization)
- Increases chance of identical problems appearing (e.g., "2+3=5" appears twice)

### Example Scenario
**Grade 1 range (1-20):**
- Only ~30 possible answers
- With 15-20 problems, **40-50% probability of duplicates** in a session
- Very noticeable to users

**Grade 2 range (1-100):**
- ~150+ possible answers
- Still significant risk of duplicates in longer sessions

## Root Cause

[MathRacePresenter.kt](app/src/main/java/dev/hossain/mathtutor/ui/mathrace/MathRacePresenter.kt#L253-L264):
```kotlin
fun generateNewProblem() {
    // Lines 253-264
    val randomIndex = (0 until problems.size).random()
    val problem = problems[randomIndex]
    
    // No duplicate checking - just selects from pre-generated list
    // with no tracking of already-shown answers
}
```

The presenter generates a list of problems upfront but never tracks which problems have been shown during the session.

## Solution: Session-Level Duplicate Prevention

Implement two-layer duplicate prevention (same pattern as PR #301 Memory Match):

### Layer 1: Optimistic Generation (Retry)
- Track shown answers during the session
- When generating new problem, ensure its answer hasn't been shown before
- Retry generation (up to reasonable limit) if duplicate found

### Layer 2: Fallback Deduplication
- If retry limit hit, manually filter to find unique answer
- Last-resort mechanism for grade ranges with limited answers

## Implementation Details

### 1. Update MathRaceScreen.State
Add session tracking:
```kotlin
data class State(...) {
    val shownAnswers: Set<Int> = emptySet()  // Track shown correct answers
    val shownProblems: Set<String> = emptySet()  // Track shown problem strings
    // ...existing fields...
}
```

### 2. Update MathRacePresenter
```kotlin
class MathRacePresenter constructor(
    @Assisted private val screen: MathRaceScreen,
    @Assisted private val navigator: Navigator,
    private val problemGenerator: ProblemGenerator
) : Presenter<MathRaceScreen.State> {
    
    fun generateNewProblem(): MathProblem {
        return generateProblemWithUniqueAnswer(
            shownAnswers = currentState.shownAnswers,
            shownProblems = currentState.shownProblems
        )
    }
    
    private fun generateProblemWithUniqueAnswer(
        shownAnswers: Set<Int>,
        shownProblems: Set<String>
    ): MathProblem {
        val maxAttempts = 100
        var attempts = 0
        
        do {
            val problem = problemGenerator.generateProblems(1, operation)[0]
            
            // Check if this problem's answer is unique
            if (problem.correctAnswer !in shownAnswers &&
                problem.getDisplayString() !in shownProblems) {
                return problem
            }
            attempts++
        } while (attempts < maxAttempts)
        
        // Fallback: Force find unique from generated problems
        return forceFindUniqueAnswer(shownAnswers, shownProblems)
    }
    
    private fun forceFindUniqueAnswer(
        shownAnswers: Set<Int>,
        shownProblems: Set<String>
    ): MathProblem {
        // Generate batch and manually filter
        val problems = problemGenerator.generateProblems(50, operation)
        return problems.firstOrNull { problem ->
            problem.correctAnswer !in shownAnswers &&
            problem.getDisplayString() !in shownProblems
        } ?: problemGenerator.generateProblems(1, operation)[0]
    }
    
    fun onProblemAnswered(event: ProblemAnswered) {
        // Update shown answers
        val newAnswers = state.shownAnswers + event.problem.correctAnswer
        val newProblems = state.shownProblems + event.problem.getDisplayString()
        
        // Update state
        state = state.copy(
            shownAnswers = newAnswers,
            shownProblems = newProblems,
            // ...other updates...
        )
        
        // Generate next problem
        if (shouldContinueSession()) {
            val nextProblem = generateNewProblem()
            // ...update UI...
        }
    }
}
```

### 3. Create Unit Tests
Tests should verify:
- ✅ Duplicate answers are prevented (same answer doesn't appear twice)
- ✅ Different answer values allow repetition (different problems with different answers)
- ✅ Problem string uniqueness checked (not just answer values)
- ✅ Fallback mechanism works when normal retry fails
- ✅ Session tracking maintains state across multiple problems

Create `MathRacePresenterDuplicateTest.kt`:
```kotlin
class MathRacePresenterDuplicateTest {
    // Test that same answer doesn't appear twice
    // Test that different problems can appear (no false deduplication)
    // Test edge case: all available answers exhausted
    // Test state tracking through session
}
```

## Reference Implementation

See [PR #301](https://github.com/hossain-khan/kids-math-tutor/pull/301) for complete duplicate prevention pattern in Memory Match game:
- File: `MemoryMatchPresenter.kt` (Lines 145-233)
- Tests: `MemoryMatchPresenterTest.kt` (10 comprehensive tests)
- This PR#301 fix shows both layers working together

## Acceptance Criteria

- ✅ No duplicate correct answers in a single Math Race session
- ✅ Different problems can appear with different answers
- ✅ Session tracking works across all 15-20 problems
- ✅ Fallback deduplication works when needed
- ✅ Unit tests cover all scenarios (3-4 tests minimum)
- ✅ Build passes with `./gradlew build`
- ✅ Code formatted with `./gradlew formatKotlin`
- ✅ All 296 existing tests still pass

## Estimated Effort
- Implementation: ~90 minutes
- Testing: ~30 minutes
- Total: ~2 hours

## Priority
🔴 Medium (impacts learning experience for Grade 1 users where duplicates very likely)

## Related

- [PR #301](https://github.com/hossain-khan/kids-math-tutor/pull/301) - Memory Match duplicate prevention (reference implementation)
- [MathRacePresenter.kt](app/src/main/java/dev/hossain/mathtutor/ui/mathrace/MathRacePresenter.kt) - Current implementation (Lines 253-264)
- [MathRacePresenterTest.kt](app/src/test/java/dev/hossain/mathtutor/ui/mathrace/MathRacePresenterTest.kt) - Existing test file

## Next Steps for AI Agent

1. **Review PR #301** to understand pattern
2. **Modify MathRaceScreen.State** to add tracking fields
3. **Update MathRacePresenter** with duplicate prevention logic
4. **Create unit tests** following PR #301 test patterns
5. **Run tests** with `./gradlew test`
6. **Format code** with `./gradlew formatKotlin`
7. **Create PR** linking to this issue
