# Phase 4: Grade Levels & Difficulty Progression

**Duration**: 2 weeks  
**Goal**: Personalized difficulty based on grade level with adaptive learning  
**Status**: ✅ Complete (Completed December 18, 2025)

---

## Overview

This phase tailors the app to each child's grade level and implements adaptive difficulty. By the end:
1. Children select their grade during onboarding (K, 1, or 2)
2. Problems automatically adjust to grade-appropriate difficulty
3. App tracks performance and adapts difficulty dynamically
4. Optional name personalization for motivation

**Key Principle**: Meet children where they are. Too easy is boring, too hard is frustrating. Find the sweet spot.

---

## Features Breakdown

### 1. Enhanced Onboarding with Profile Setup

#### Updated Onboarding Flow

```
Screen 1: Welcome
      ↓
Screen 2: Grade Selection (NEW)
      ↓
Screen 3: Name Entry (NEW - Optional)
      ↓
Screen 4: Ready to Start
      ↓
Home Screen
```

#### Grade Selection Screen

```
┌─────────────────────────────────────┐
│                                     │
│   🐶 Which grade are you in?        │
│                                     │
│                                     │
│   ┌───────────────────────┐         │
│   │                       │         │
│   │    Kindergarten       │         │
│   │         K             │         │
│   │                       │         │
│   │   Numbers 1-10        │         │
│   │   Simple addition     │         │
│   │                       │         │
│   └───────────────────────┘         │
│                                     │
│   ┌───────────────────────┐         │
│   │                       │         │
│   │     Grade 1           │         │
│   │         1             │         │
│   │                       │         │
│   │   Numbers 1-20        │         │
│   │   Add, subtract       │         │
│   │                       │         │
│   └───────────────────────┘         │
│                                     │
│   ┌───────────────────────┐         │
│   │                       │         │
│   │     Grade 2           │         │
│   │         2             │         │
│   │                       │         │
│   │   Numbers 1-100       │         │
│   │   All operations      │         │
│   │                       │         │
│   └───────────────────────┘         │
│                                     │
└─────────────────────────────────────┘
```

**UI Specifications**
- Title: "Which grade are you in?"
- Friendly puppy emoji 🐶
- Three large grade cards
  - Card size: Full width, height ~150dp
  - Large grade indicator (displayMedium)
  - Description of difficulty level
  - Examples of problem types
- Material 3 ElevatedCard
- Selected state highlighted with primary color border

#### Name Entry Screen (Optional)

```
┌─────────────────────────────────────┐
│                                     │
│   🐶 What's your name?              │
│                                     │
│   (Optional - we'll cheer for you!) │
│                                     │
│                                     │
│   ┌─────────────────────────┐       │
│   │                         │       │
│   │  [Enter your name]      │       │
│   │                         │       │
│   └─────────────────────────┘       │
│                                     │
│                                     │
│   [Skip]          [Continue]        │
│                                     │
└─────────────────────────────────────┘
```

---

### 2. Grade-Appropriate Problem Generation

#### Problem Difficulty Specifications

**Kindergarten (Grade K)**
- **Addition**: 
  - Numbers: 1-10
  - Result: 2-18 (never exceeds 20)
  - Examples: 3+5, 7+2, 4+6
- **Subtraction**: 
  - Numbers: 1-10
  - Minuend always ≥ subtrahend
  - Result: 0-9
  - Examples: 8-3, 10-5, 7-4

**Grade 1**
- **Addition**: 
  - Numbers: 1-20
  - Result: 2-40
  - Include teen numbers (11-19)
  - Examples: 15+8, 12+7, 9+14
- **Subtraction**: 
  - Numbers: 1-20
  - Result: 0-19
  - Examples: 18-9, 15-7, 20-11
- **Multiplication** (Introductory):
  - Only ×2, ×5, ×10 tables
  - First operand: 1-10
  - Examples: 5×2, 3×5, 4×10

**Grade 2**
- **Addition**: 
  - Numbers: 1-100
  - Two-digit addition
  - Examples: 45+32, 67+28, 53+19
- **Subtraction**: 
  - Numbers: 1-100
  - Two-digit subtraction
  - Examples: 75-32, 88-49, 60-25
- **Multiplication**: 
  - Times tables 2-10
  - First operand: 1-12
  - Examples: 7×8, 9×6, 12×4
- **Division** (Simple):
  - Use multiplication facts in reverse
  - Always divides evenly
  - Examples: 20÷5, 18÷3, 24÷6

#### Updated ProblemGenerator

```kotlin
interface ProblemGenerator {
    fun generateProblems(
        count: Int,
        operation: MathOperation,
        gradeLevel: GradeLevel
    ): List<MathProblem>
}

enum class GradeLevel(val displayName: String) {
    KINDERGARTEN("Kindergarten"),
    GRADE_1("Grade 1"),
    GRADE_2("Grade 2");
    
    fun getNumberRange(operation: MathOperation): IntRange {
        return when (this) {
            KINDERGARTEN -> when (operation) {
                MathOperation.ADDITION, MathOperation.SUBTRACTION -> 1..10
                else -> 1..10
            }
            GRADE_1 -> when (operation) {
                MathOperation.ADDITION, MathOperation.SUBTRACTION -> 1..20
                MathOperation.MULTIPLICATION -> 1..10 // Limited tables
                else -> 1..20
            }
            GRADE_2 -> when (operation) {
                MathOperation.ADDITION, MathOperation.SUBTRACTION -> 1..100
                MathOperation.MULTIPLICATION -> 1..12
                MathOperation.DIVISION -> 1..12
                else -> 1..100
            }
        }
    }
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class GradeAwareProblemGenerator constructor(
    private val random: Random = Random.Default
) : ProblemGenerator {
    
    override fun generateProblems(
        count: Int,
        operation: MathOperation,
        gradeLevel: GradeLevel
    ): List<MathProblem> {
        return (1..count).map {
            when (operation) {
                MathOperation.ADDITION -> generateAddition(gradeLevel)
                MathOperation.SUBTRACTION -> generateSubtraction(gradeLevel)
                MathOperation.MULTIPLICATION -> generateMultiplication(gradeLevel)
                MathOperation.DIVISION -> generateDivision(gradeLevel)
                MathOperation.MIXED -> generateMixed(gradeLevel)
            }
        }
    }
    
    private fun generateAddition(gradeLevel: GradeLevel): MathProblem {
        val range = gradeLevel.getNumberRange(MathOperation.ADDITION)
        val num1 = random.nextInt(range.first, range.last + 1)
        val num2 = random.nextInt(range.first, range.last + 1)
        
        return MathProblem(
            num1 = num1,
            num2 = num2,
            operation = MathOperation.ADDITION,
            correctAnswer = num1 + num2
        )
    }
    
    private fun generateMultiplication(gradeLevel: GradeLevel): MathProblem {
        val range = gradeLevel.getNumberRange(MathOperation.MULTIPLICATION)
        
        val num1 = when (gradeLevel) {
            GradeLevel.KINDERGARTEN -> random.nextInt(1, 6) // Not used, but safe
            GradeLevel.GRADE_1 -> {
                // Only 2, 5, 10 tables
                listOf(2, 5, 10).random(random)
            }
            GradeLevel.GRADE_2 -> random.nextInt(2, 11) // 2-10 tables
        }
        
        val num2 = random.nextInt(1, range.last + 1)
        
        return MathProblem(
            num1 = num1,
            num2 = num2,
            operation = MathOperation.MULTIPLICATION,
            correctAnswer = num1 * num2
        )
    }
    
    private fun generateDivision(gradeLevel: GradeLevel): MathProblem {
        // Only for Grade 2
        if (gradeLevel != GradeLevel.GRADE_2) {
            return generateAddition(gradeLevel) // Fallback
        }
        
        // Generate from multiplication facts
        val divisor = random.nextInt(2, 11)
        val quotient = random.nextInt(1, 13)
        val dividend = divisor * quotient
        
        return MathProblem(
            num1 = dividend,
            num2 = divisor,
            operation = MathOperation.DIVISION,
            correctAnswer = quotient
        )
    }
}
```

---

### 3. Adaptive Difficulty System

#### Performance Tracking

```kotlin
data class OperationPerformance(
    val operation: MathOperation,
    val gradeLevel: GradeLevel,
    val totalAttempts: Int,
    val correctAnswers: Int,
    val averageTimeSeconds: Float,
    val recentAccuracy: Float, // Last 10 problems
    val recommendedDifficulty: DifficultyAdjustment
) {
    val overallAccuracy: Float
        get() = if (totalAttempts > 0) {
            (correctAnswers.toFloat() / totalAttempts) * 100
        } else 0f
    
    fun shouldIncreseDifficulty(): Boolean {
        return recentAccuracy >= 85f && totalAttempts >= 20
    }
    
    fun shouldDecreaseDifficulty(): Boolean {
        return recentAccuracy < 50f && totalAttempts >= 10
    }
}

enum class DifficultyAdjustment {
    EASIER,      // Suggest lower grade level
    CURRENT,     // Stay at current level
    HARDER       // Suggest higher grade level or advanced problems
}
```

#### Adaptive Problem Generator

```kotlin
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AdaptiveProblemGenerator constructor(
    private val baseProblemGenerator: GradeAwareProblemGenerator,
    private val performanceRepository: PerformanceRepository
) : ProblemGenerator {
    
    suspend fun generateAdaptiveProblems(
        count: Int,
        operation: MathOperation,
        userGradeLevel: GradeLevel
    ): List<MathProblem> {
        val performance = performanceRepository.getPerformance(operation).first()
        
        val adjustedLevel = when {
            performance.shouldIncreseDifficulty() -> {
                getNextGradeLevel(userGradeLevel)
            }
            performance.shouldDecreaseDifficulty() -> {
                getPreviousGradeLevel(userGradeLevel)
            }
            else -> userGradeLevel
        }
        
        return baseProblemGenerator.generateProblems(count, operation, adjustedLevel)
    }
    
    private fun getNextGradeLevel(current: GradeLevel): GradeLevel {
        return when (current) {
            GradeLevel.KINDERGARTEN -> GradeLevel.GRADE_1
            GradeLevel.GRADE_1 -> GradeLevel.GRADE_2
            GradeLevel.GRADE_2 -> GradeLevel.GRADE_2 // Max level
        }
    }
    
    private fun getPreviousGradeLevel(current: GradeLevel): GradeLevel {
        return when (current) {
            GradeLevel.KINDERGARTEN -> GradeLevel.KINDERGARTEN // Min level
            GradeLevel.GRADE_1 -> GradeLevel.KINDERGARTEN
            GradeLevel.GRADE_2 -> GradeLevel.GRADE_1
        }
    }
}
```

---

### 4. User Profile Data Model

#### UserProfile.kt

```kotlin
data class UserProfile(
    val name: String? = null,
    val gradeLevel: GradeLevel,
    val createdAt: Instant,
    val adaptiveDifficultyEnabled: Boolean = true
)
```

#### UserProfileRepository

```kotlin
interface UserProfileRepository {
    fun getProfile(): Flow<UserProfile?>
    suspend fun saveProfile(profile: UserProfile)
    suspend fun updateGradeLevel(gradeLevel: GradeLevel)
    suspend fun updateName(name: String?)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class UserProfileRepositoryImpl constructor(
    private val dataStore: DataStore<Preferences>
) : UserProfileRepository {
    
    companion object {
        private val NAME_KEY = stringPreferencesKey("user_name")
        private val GRADE_KEY = stringPreferencesKey("grade_level")
        private val CREATED_AT_KEY = longPreferencesKey("created_at")
        private val ADAPTIVE_KEY = booleanPreferencesKey("adaptive_enabled")
    }
    
    override fun getProfile(): Flow<UserProfile?> {
        return dataStore.data.map { prefs ->
            val gradeName = prefs[GRADE_KEY] ?: return@map null
            
            UserProfile(
                name = prefs[NAME_KEY],
                gradeLevel = GradeLevel.valueOf(gradeName),
                createdAt = Instant.ofEpochMilli(prefs[CREATED_AT_KEY] ?: 0),
                adaptiveDifficultyEnabled = prefs[ADAPTIVE_KEY] ?: true
            )
        }
    }
    
    override suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit { prefs ->
            profile.name?.let { prefs[NAME_KEY] = it }
            prefs[GRADE_KEY] = profile.gradeLevel.name
            prefs[CREATED_AT_KEY] = profile.createdAt.toEpochMilli()
            prefs[ADAPTIVE_KEY] = profile.adaptiveDifficultyEnabled
        }
    }
    
    override suspend fun updateGradeLevel(gradeLevel: GradeLevel) {
        dataStore.edit { prefs ->
            prefs[GRADE_KEY] = gradeLevel.name
        }
    }
    
    override suspend fun updateName(name: String?) {
        dataStore.edit { prefs ->
            if (name != null) {
                prefs[NAME_KEY] = name
            } else {
                prefs.remove(NAME_KEY)
            }
        }
    }
}
```

---

### 5. Personalized Home Screen

#### Updated Home Screen with Name

```
┌─────────────────────────────────────┐
│          Math Time 🐶              │
│                                     │
├─────────────────────────────────────┤
│                                     │
│   👋 Hi Sarah!                      │
│   Grade 1 • 78% accuracy            │
│                                     │
│   ┌───────────────────────────┐    │
│   │   🔥 Daily Streak         │    │
│   │        7 Days!            │    │
│   └───────────────────────────┘    │
│                                     │
│   ... rest of dashboard ...        │
│                                     │
└─────────────────────────────────────┘
```

**Personalization**
- If name provided: "Hi [Name]!"
- If no name: "Welcome back!"
- Show grade level and overall accuracy
- Use name in encouragement messages: "Great job, [Name]!"

---

### 6. Settings Screen Addition

#### Profile Settings

```
┌─────────────────────────────────────┐
│  [←]       Settings          [?]    │
├─────────────────────────────────────┤
│                                     │
│   Profile                           │
│                                     │
│   Name: Sarah                       │
│   [Edit]                            │
│                                     │
│   Grade Level: Grade 1              │
│   [Change]                          │
│                                     │
│   ────────────────────────────      │
│                                     │
│   Adaptive Difficulty               │
│                                     │
│   [✓] Adjust difficulty based       │
│       on performance                │
│                                     │
│   ────────────────────────────      │
│                                     │
│   About                             │
│   Privacy                           │
│   Help                              │
│                                     │
└─────────────────────────────────────┘
```

---

## Technical Implementation

### Database Schema Updates (Room v3)

#### PerformanceEntity.kt

```kotlin
@Entity(tableName = "performance")
data class PerformanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val operation: MathOperation,
    
    val gradeLevel: GradeLevel,
    
    val problemId: String,
    
    val isCorrect: Boolean,
    
    val attemptCount: Int,
    
    val timeSpentSeconds: Long,
    
    val timestamp: Instant
)
```

---

## Testing Strategy

### Unit Tests

```kotlin
@Test
fun `kindergarten addition uses numbers 1-10`() {
    val generator = GradeAwareProblemGenerator()
    val problems = generator.generateProblems(100, MathOperation.ADDITION, GradeLevel.KINDERGARTEN)
    
    problems.forEach { problem ->
        assertTrue(problem.num1 in 1..10)
        assertTrue(problem.num2 in 1..10)
        assertTrue(problem.correctAnswer in 2..20)
    }
}

@Test
fun `grade 2 multiplication uses tables 2-10`() {
    val generator = GradeAwareProblemGenerator()
    val problems = generator.generateProblems(100, MathOperation.MULTIPLICATION, GradeLevel.GRADE_2)
    
    problems.forEach { problem ->
        assertTrue(problem.num1 in 2..10)
        assertTrue(problem.num2 in 1..12)
    }
}

@Test
fun `adaptive difficulty increases after high performance`() {
    val performance = OperationPerformance(
        operation = MathOperation.ADDITION,
        gradeLevel = GradeLevel.KINDERGARTEN,
        totalAttempts = 30,
        correctAnswers = 27,
        averageTimeSeconds = 5f,
        recentAccuracy = 90f,
        recommendedDifficulty = DifficultyAdjustment.HARDER
    )
    
    assertTrue(performance.shouldIncreseDifficulty())
}
```

### Manual Testing Checklist

**Grade Selection**
- [ ] All three grade cards display correctly
- [ ] Selecting grade navigates to next screen
- [ ] Grade selection persists after app restart

**Problem Generation**
- [ ] K problems use numbers 1-10
- [ ] Grade 1 problems use numbers 1-20
- [ ] Grade 2 problems use numbers 1-100
- [ ] Multiplication limited correctly per grade
- [ ] Division only appears for Grade 2

**Adaptive Difficulty**
- [ ] High accuracy increases difficulty
- [ ] Low accuracy decreases difficulty
- [ ] User notification when difficulty changes
- [ ] Can be disabled in settings

**Personalization**
- [ ] Name appears on home screen
- [ ] Name used in encouragement messages
- [ ] Name can be edited in settings
- [ ] Grade level can be changed in settings

---

## Migration Plan from Phase 3

### Step 1: User Profile System (Days 1-2)
1. Create UserProfile data model
2. Implement UserProfileRepository with DataStore
3. Write unit tests
4. Test persistence

### Step 2: Update Onboarding (Days 3-4)
1. Add grade selection screen
2. Add name entry screen
3. Update navigation flow
4. Save profile on completion

### Step 3: Grade-Aware Problem Generation (Days 5-7)
1. Create GradeLevel enum
2. Update ProblemGenerator interface
3. Implement GradeAwareProblemGenerator
4. Test all grade levels thoroughly
5. Add multiplication and division

### Step 4: Adaptive Difficulty (Days 8-10)
1. Create PerformanceEntity and DAO
2. Implement performance tracking
3. Implement AdaptiveProblemGenerator
4. Add difficulty adjustment logic
5. Test adaptation scenarios

### Step 5: Settings Screen (Days 11-12)
1. Create SettingsScreen Circuit
2. Add profile editing
3. Add grade level changing
4. Add adaptive toggle
5. Wire up navigation

### Step 6: Personalization Integration (Days 13-14)
1. Update home screen with name
2. Add personalized messages
3. Testing and polish
4. Documentation updates

---

## Success Metrics

- ✅ Problems appropriate for all three grades
- ✅ Adaptive difficulty working correctly
- ✅ Profile setup completion rate >90%
- ✅ User understands grade selection
- ✅ Performance tracking accurate
- ✅ No crashes with new features

---

## Definition of Done

- ✅ Three grade levels implemented (K, 1, 2)
- ✅ Grade selection in onboarding
- ✅ Problems adjust to grade level
- ✅ Adaptive difficulty working
- ✅ Settings screen for profile management
- ✅ Personalized home screen
- ✅ All tests passing
- ✅ CHANGELOG.md updated
- ✅ Real child testing per grade level

---

*Document created: December 16, 2025*  
*Phase status: 🔴 Not Started*  
*Target completion: Week 9 (after Phase 3)*
