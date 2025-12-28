# Grade-Level to Operation Mapping Architecture

## Overview

The app implements a **grade-aware system** where each grade level (Kindergarten, Grade 1, Grade 2) supports different mathematical operations. However, there is currently a **disconnect** between the grade system and the operation selector UI - the operation selector does not dynamically display operations based on the user's grade level.

## Current Architecture

### 1. Grade Levels and Operation Support

The `GradeLevel` enum in [domain/model/GradeLevel.kt](../../app/src/main/java/dev/hossain/mathtutor/domain/model/GradeLevel.kt) defines the number ranges for each operation by grade:

| Grade | Addition | Subtraction | Multiplication | Division | Mixed |
|-------|----------|-------------|-----------------|----------|-------|
| **Kindergarten** | 1-5 | 1-5 | 1-2 (limited) | 1-2 (N/A) | 1-5 |
| **Grade 1** | 1-10 | 1-10 | 1-5 (×2, ×5, ×10) | 1-5 (falls back to subtraction) | 1-10 |
| **Grade 2** | 1-20 | 1-20 | 1-10 (×2-10 tables) | 1-10 (multiplication-derived) | 1-20 |

### 2. Problem Generation by Grade

The `GradeAwareProblemGenerator` in [domain/generator/GradeAwareProblemGenerator.kt](../../app/src/main/java/dev/hossain/mathtutor/domain/generator/GradeAwareProblemGenerator.kt) respects these constraints:

- **Kindergarten**: Addition and Subtraction only (no multiplication or division)
- **Grade 1**: Addition, Subtraction, and limited Multiplication (×2, ×5, ×10 only)
  - Division falls back to Subtraction
- **Grade 2**: All operations including full Multiplication tables (×2-10) and Division

### 3. Operation Selector Screen - Current Implementation

The `OperationSelectorScreen` in [ui/operationselector/](../../app/src/main/java/dev/hossain/mathtutor/ui/operationselector/) is **grade-agnostic**:

**OperationSelectorScreen.kt**:
- State only tracks `hasSessionHistory` (for stats button)
- No grade level information

**OperationSelectorPresenter.kt**:
- Queries `SessionRepository` for session history
- **Missing**: Does NOT fetch or track user's current grade level

**OperationSelectorUi.kt**:
- **Hardcoded to display**:
  1. Addition
  2. Subtraction
  3. Mix It Up (currently sends ADDITION, not true mixed mode)
  
- **Missing**: Grade-level conditional logic to show/hide Multiplication and Division

### 4. User Profile Flow

The grade level is stored in `UserProfileRepository`:
- Set during onboarding (Profile Setup screen)
- Stored in DataStore
- Used by `MathPracticePresenter` to fetch grade-aware problems

**Problem**: The grade is fetched in `MathPracticePresenter` but NOT in `OperationSelectorPresenter`.

## The Issue: Why Grade 2 Doesn't Show Multiplication and Division

### Root Cause

1. **OperationSelectorPresenter** does not fetch the user's grade level from `UserProfileRepository`
2. **OperationSelectorScreen.State** has no field to track `gradeLevel`
3. **OperationSelectorUi** has no conditional logic to show/hide operations based on grade

### Data Flow Gap

```
User Profile (DataStore)
    ↓
MathPracticePresenter → Fetches grade ✓ → Uses for problem generation
    ↓
OperationSelectorPresenter → Fetches session history ✓ → BUT NO GRADE ✗
    ↓
OperationSelectorUi → Hardcoded operations → NOT GRADE-AWARE ✗
```

## Solution Architecture

### What Needs to Change

1. **OperationSelectorPresenter**: Fetch user's grade level from `UserProfileRepository`
2. **OperationSelectorScreen.State**: Add `gradeLevel: GradeLevel` field
3. **OperationSelectorUi**: Conditionally display operations based on grade
4. **GradeLevel** (existing): Add utility function to get available operations

### Implementation Steps

#### Step 1: Add Utility to GradeLevel

Add a function to determine which operations are available for each grade:

```kotlin
// In GradeLevel.kt
fun getAvailableOperations(): List<MathOperation> =
    when (this) {
        KINDERGARTEN -> listOf(
            MathOperation.ADDITION,
            MathOperation.SUBTRACTION,
            MathOperation.MIXED // K mixed = Add/Subtract
        )
        GRADE_1 -> listOf(
            MathOperation.ADDITION,
            MathOperation.SUBTRACTION,
            MathOperation.MULTIPLICATION, // Limited: ×2, ×5, ×10
            MathOperation.MIXED // G1 mixed = Add/Subtract/Multiply
        )
        GRADE_2 -> listOf(
            MathOperation.ADDITION,
            MathOperation.SUBTRACTION,
            MathOperation.MULTIPLICATION, // Full tables: ×2-10
            MathOperation.DIVISION, // New for Grade 2
            MathOperation.MIXED // G2 mixed = all four ops
        )
    }
```

#### Step 2: Update OperationSelectorScreen.State

```kotlin
data class State(
    val gradeLevel: GradeLevel,
    val hasSessionHistory: Boolean,
    val eventSink: (Event) -> Unit,
) : CircuitUiState
```

#### Step 3: Update OperationSelectorPresenter

```kotlin
class OperationSelectorPresenter constructor(
    @Assisted private val navigator: Navigator,
    private val sessionRepository: SessionRepository,
    private val userProfileRepository: UserProfileRepository, // ADD
    private val analyticsService: AnalyticsService,
) : Presenter<OperationSelectorScreen.State> {
    
    @Composable
    override fun present(): OperationSelectorScreen.State {
        // Fetch user profile (including grade)
        val userProfile by userProfileRepository.getUserProfile()
            .collectAsState(initial = null)
        
        val gradeLevel = userProfile?.gradeLevel ?: GradeLevel.GRADE_1 // Default
        
        // ... rest of existing logic for session history
        
        return OperationSelectorScreen.State(
            gradeLevel = gradeLevel,
            hasSessionHistory = hasSessionHistory,
        ) { event -> /* ... */ }
    }
}
```

#### Step 4: Update OperationSelectorUi

```kotlin
fun OperationSelectorUi(
    state: OperationSelectorScreen.State,
    modifier: Modifier = Modifier,
) {
    // ... existing scaffold and header ...
    
    val availableOperations = state.gradeLevel.getAvailableOperations()
    
    // Dynamically render operation cards
    availableOperations.forEach { operation ->
        when (operation) {
            MathOperation.ADDITION -> {
                OperationCard(
                    title = "Addition",
                    icon = Icons.Default.Add,
                    examples = listOf("1 + 1 = ?", "5 + 3 = ?"),
                    operation = MathOperation.ADDITION,
                    onClick = { /* ... */ },
                )
            }
            MathOperation.SUBTRACTION -> { /* ... */ }
            MathOperation.MULTIPLICATION -> {
                OperationCard(
                    title = "Multiplication",
                    icon = Icons.Default.Close, // or custom icon
                    examples = when(state.gradeLevel) {
                        GradeLevel.GRADE_1 -> listOf("2 × 5 = ?", "5 × 10 = ?")
                        GradeLevel.GRADE_2 -> listOf("3 × 7 = ?", "8 × 6 = ?")
                        else -> listOf()
                    },
                    operation = MathOperation.MULTIPLICATION,
                    onClick = { /* ... */ },
                )
            }
            MathOperation.DIVISION -> {
                OperationCard(
                    title = "Division",
                    icon = Icons.Default.Percent, // or custom icon
                    examples = listOf("20 ÷ 5 = ?", "15 ÷ 3 = ?"),
                    operation = MathOperation.DIVISION,
                    onClick = { /* ... */ },
                )
            }
            MathOperation.MIXED -> { /* ... */ }
        }
    }
}
```

## Testing Strategy

### Unit Tests
- `GradeLevel.getAvailableOperations()` returns correct operations per grade
- `OperationSelectorPresenter.present()` fetches grade from profile

### Integration Tests
- **Kindergarten user**: Shows only Addition, Subtraction, Mixed
- **Grade 1 user**: Shows Addition, Subtraction, Multiplication (limited), Mixed
- **Grade 2 user**: Shows Addition, Subtraction, Multiplication (full), Division, Mixed

### Manual Testing Checklist
- [ ] Switch grade in settings, verify operation selector updates
- [ ] Grade 2 user can select Multiplication with ×2-10 examples
- [ ] Grade 2 user can select Division with ÷ examples
- [ ] Grade 1 user CANNOT see Division option
- [ ] Kindergarten user CANNOT see Multiplication or Division options

## Impact Analysis

### Files to Modify
1. [domain/model/GradeLevel.kt](../../app/src/main/java/dev/hossain/mathtutor/domain/model/GradeLevel.kt) - Add utility function
2. [ui/operationselector/OperationSelectorScreen.kt](../../app/src/main/java/dev/hossain/mathtutor/ui/operationselector/OperationSelectorScreen.kt) - Add gradeLevel to State
3. [ui/operationselector/OperationSelectorPresenter.kt](../../app/src/main/java/dev/hossain/mathtutor/ui/operationselector/OperationSelectorPresenter.kt) - Fetch grade level
4. [ui/operationselector/OperationSelectorUi.kt](../../app/src/main/java/dev/hossain/mathtutor/ui/operationselector/OperationSelectorUi.kt) - Conditional rendering

### Backward Compatibility
- **No breaking changes** - grade level has been persisted since Phase 4
- **Default behavior** - if no profile exists, defaults to Grade 1 (existing behavior)
- **Graceful degradation** - all grades still support Addition and Subtraction

## Architecture Principles Preserved

✓ **Circuit UDF** - State flows down (gradeLevel), Events flow up (OperationSelected)  
✓ **Metro DI** - UserProfileRepository injected into Presenter  
✓ **Grade-Aware Design** - Problem generation already respects grade; UI now follows  
✓ **Material 3** - Existing component hierarchy unchanged  
✓ **Accessibility** - Operation cards already support semantics  

## References

- [GradeAwareProblemGenerator.kt](../../app/src/main/java/dev/hossain/mathtutor/domain/generator/GradeAwareProblemGenerator.kt) - Problem generation by grade
- [MathPracticePresenter.kt](../../app/src/main/java/dev/hossain/mathtutor/ui/mathpractice/MathPracticePresenter.kt) - How it fetches grade level
- [UserProfileRepository.kt](../../app/src/main/java/dev/hossain/mathtutor/domain/repository/UserProfileRepository.kt) - Grade storage
- [CHANGELOG.md](../../CHANGELOG.md#130---2025-12-18) - Phase 4 grade implementation details
