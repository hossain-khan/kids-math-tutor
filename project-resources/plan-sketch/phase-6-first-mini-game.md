# Phase 6: First Mini-Game

**Duration**: 2 weeks  
**Goal**: Make practice fun with gamification  
**Status**: 🔴 Not Started

---

## Overview

This phase introduces the first mini-game to make math practice feel like play. By the end:
1. Children can play a 60-second Math Race game
2. Game unlocks after solving 50 total problems
3. Personal best leaderboard tracks high scores
4. Special badges reward game achievements
5. Game selection screen shows available games

**Key Principle**: Learning through play. When math feels like a game, children practice more willingly and develop positive associations with mathematics.

---

## Features Breakdown

### 1. Math Race Game

#### Game Concept
- **Duration**: 60 seconds countdown
- **Objective**: Solve as many problems as possible
- **Difficulty**: Matches user's grade level
- **Scoring**: 1 point per correct answer
- **Feedback**: Instant visual/audio feedback
- **Result**: Personal best tracking

#### Game Flow

```
Game Start
    ↓
Countdown: 3... 2... 1... GO!
    ↓
┌─────────────────────────────┐
│  Timer: 0:45  Score: 12     │
├─────────────────────────────┤
│                             │
│       7 + 5 = ?             │
│                             │
│      [  1  2  ]             │
│                             │
├─────────────────────────────┤
│  [Number Pad]               │
└─────────────────────────────┘
    ↓
Correct Answer → +1 Score → Next Problem
Incorrect Answer → Shake Animation → Next Problem
    ↓
Timer Expires (0:00)
    ↓
┌─────────────────────────────┐
│   Game Over! 🎉             │
│                             │
│   Score: 18 problems        │
│   Personal Best: 15         │
│                             │
│   🌟 New Record!            │
│                             │
│   [Play Again] [Home]       │
└─────────────────────────────┘
```

#### Game Screen UI

```
┌─────────────────────────────────────┐
│  Math Race ⏱️                       │
│                                     │
│  🕐 0:47          ⭐ Score: 15      │
│                                     │
├─────────────────────────────────────┤
│                                     │
│                                     │
│          8 + 4 = ?                  │
│                                     │
│       ┌──────────────┐              │
│       │   [ 1  2 ]   │              │
│       └──────────────┘              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  [1] [2] [3]                │   │
│  │  [4] [5] [6]                │   │
│  │  [7] [8] [9]                │   │
│  │  [⌫] [0] [✓]                │   │
│  └─────────────────────────────┘   │
│                                     │
│  Personal Best: 18                  │
│                                     │
└─────────────────────────────────────┘
```

**UI Specifications**
- Timer: Large, prominent, displayMedium typography
- Score: Real-time update, celebrate milestones (10, 20, 30)
- Problem: displayLarge typography, centered
- Answer field: Large, clear input
- Number pad: Same as practice mode
- Personal best: Always visible at bottom
- Use Material 3 colors exclusively
- Pulsing animation on timer when <10 seconds

---

### 2. Game Selection Screen

#### Game Hub Concept
Central location to discover and launch mini-games.

#### Game Hub UI

```
┌─────────────────────────────────────┐
│  [←]      Games Hub      [?]        │
├─────────────────────────────────────┤
│                                     │
│   🎮 Play fun math games!           │
│                                     │
│   ┌─────────────────────────┐      │
│   │  ⏱️ Math Race            │      │
│   │                         │      │
│   │  Solve as many as you   │      │
│   │  can in 60 seconds!     │      │
│   │                         │      │
│   │  🏆 Best: 18 problems   │      │
│   │                         │      │
│   │  [PLAY]                 │      │
│   └─────────────────────────┘      │
│                                     │
│   ┌─────────────────────────┐      │
│   │  🧩 Memory Match  🔒     │      │
│   │                         │      │
│   │  Match math problems    │      │
│   │  with answers!          │      │
│   │                         │      │
│   │  Unlock: Solve 100      │      │
│   │          problems        │      │
│   │                         │      │
│   │  [LOCKED]               │      │
│   └─────────────────────────┘      │
│                                     │
│   ┌─────────────────────────┐      │
│   │  🎲 Number Sequence 🔒   │      │
│   │                         │      │
│   │  Find the missing       │      │
│   │  number!                │      │
│   │                         │      │
│   │  Unlock: Solve 200      │      │
│   │          problems        │      │
│   │                         │      │
│   │  [LOCKED]               │      │
│   └─────────────────────────┘      │
│                                     │
└─────────────────────────────────────┘
```

**UI Specifications**
- Game cards: Full width, elevated cards
- Unlocked games: Full color, clear "PLAY" button
- Locked games: Semi-transparent, lock icon, unlock requirements
- Personal best: Show prominently for unlocked games
- Navigation: From home screen "Games" button

---

### 3. Game Results Screen

#### Results Display

```
┌─────────────────────────────────────┐
│                                     │
│          Game Over! 🎉              │
│                                     │
├─────────────────────────────────────┤
│                                     │
│      Math Race Results              │
│                                     │
│   ┌─────────────────────────┐      │
│   │                         │      │
│   │    Score: 21            │      │
│   │                         │      │
│   │    🏆 New Record!       │      │
│   │    (Previous: 18)       │      │
│   │                         │      │
│   └─────────────────────────┘      │
│                                     │
│   Stats:                            │
│   • Correct: 21/23 (91%)            │
│   • Average time: 2.6s              │
│   • Fastest solve: 1.2s             │
│                                     │
│   🌟 Badge Unlocked!                │
│   "Speed Demon"                     │
│   Solve 20+ in 60 seconds           │
│                                     │
│   [Play Again]    [Home]            │
│                                     │
└─────────────────────────────────────┘
```

**Results Features**
- Final score with celebration
- New record notification with animation
- Detailed stats (accuracy, average time, fastest)
- Badge unlock notification (if earned)
- Quick replay or return home

---

### 4. Game Unlock System

#### Unlock Requirements

```kotlin
enum class Game(
    val id: String,
    val displayName: String,
    val description: String,
    val unlockRequirement: Int
) {
    MATH_RACE(
        id = "math_race",
        displayName = "Math Race",
        description = "Solve as many as you can in 60 seconds!",
        unlockRequirement = 50 // Total problems solved
    ),
    MEMORY_MATCH(
        id = "memory_match",
        displayName = "Memory Match",
        description = "Match math problems with answers!",
        unlockRequirement = 100
    ),
    NUMBER_SEQUENCE(
        id = "number_sequence",
        displayName = "Number Sequence",
        description = "Find the missing number!",
        unlockRequirement = 200
    );
    
    fun isUnlocked(totalProblemsSolved: Int): Boolean {
        return totalProblemsSolved >= unlockRequirement
    }
}
```

#### Unlock Notification

```
┌─────────────────────────────────────┐
│                                     │
│       🎉 New Game Unlocked! 🎉      │
│                                     │
│   ┌─────────────────────────┐      │
│   │                         │      │
│   │    ⏱️ Math Race          │      │
│   │                         │      │
│   │    Solve as many as     │      │
│   │    you can in 60        │      │
│   │    seconds!             │      │
│   │                         │      │
│   └─────────────────────────┘      │
│                                     │
│   You've solved 50 problems!        │
│   Ready for a challenge?            │
│                                     │
│   [Play Now]    [Later]             │
│                                     │
└─────────────────────────────────────┘
```

---

### 5. Special Game Badges

#### New Badge Category

```kotlin
// Add GAMES to existing BadgeCategory enum in BadgeCategory.kt
enum class BadgeCategory {
    GETTING_STARTED,
    VOLUME,
    OPERATION_MASTERY,
    SPEED_ACCURACY,
    STREAK,
    GAMES, // NEW: Game achievement badges
}
```

#### New Badge Definitions

```kotlin
// Add to BadgeDefinitions.kt getAllBadges() list

// Game badges (GAMES category)
Badge(
    id = "game_master",
    name = "Game Master",
    description = "Play 10 games",
    icon = "🎮",
    category = BadgeCategory.GAMES,
    requirement = BadgeRequirement.GameCount(10),
    isUnlocked = false,
    unlockedAt = null
),
Badge(
    id = "speed_demon",
    name = "Speed Demon",
    description = "Score 20+ in Math Race",
    icon = "⚡",
    category = BadgeCategory.GAMES,
    requirement = BadgeRequirement.MathRaceScore(20),
    isUnlocked = false,
    unlockedAt = null
),
Badge(
    id = "racing_champion",
    name = "Racing Champion",
    description = "Score 30+ in Math Race",
    icon = "🏆",
    category = BadgeCategory.GAMES,
    requirement = BadgeRequirement.MathRaceScore(30),
    isUnlocked = false,
    unlockedAt = null
),
Badge(
    id = "perfect_race",
    name = "Perfect Race",
    description = "100% accuracy in a game",
    icon = "💯",
    category = BadgeCategory.GAMES,
    requirement = BadgeRequirement.PerfectGameAccuracy,
    isUnlocked = false,
    unlockedAt = null
)

// Add new requirement types to BadgeRequirement.kt
sealed class BadgeRequirement {
    // ... existing requirements ...
    
    /** Badge requirement based on total games played. */
    data class GameCount(val count: Int) : BadgeRequirement()
    
    /** Badge requirement based on Math Race score. */
    data class MathRaceScore(val minScore: Int) : BadgeRequirement()
    
    /** Badge requirement for 100% accuracy in any game. */
    data object PerfectGameAccuracy : BadgeRequirement()
}
```

---

## Technical Implementation

### 1. Game Data Models

#### GameSession.kt

```kotlin
@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val gameId: String, // "math_race", etc.
    
    val startTime: Instant,
    
    val endTime: Instant,
    
    val score: Int,
    
    val correctAnswers: Int,
    
    val totalAttempts: Int,
    
    val duration: Long, // Duration in seconds
    
    val gradeLevel: GradeLevel
) {
    val accuracy: Float
        get() = if (totalAttempts > 0) {
            (correctAnswers.toFloat() / totalAttempts) * 100
        } else 0f
}

data class GameSession(
    val id: Long = 0,
    val gameId: String,
    val startTime: Instant,
    val endTime: Instant,
    val score: Int,
    val correctAnswers: Int,
    val totalAttempts: Int,
    val duration: Long,
    val gradeLevel: GradeLevel,
    val isNewRecord: Boolean = false
) {
    val accuracy: Float
        get() = if (totalAttempts > 0) {
            (correctAnswers.toFloat() / totalAttempts) * 100
        } else 0f
}
```

#### GameStats.kt

```kotlin
data class GameStats(
    val gameId: String,
    val personalBest: Int,
    val totalPlays: Int,
    val averageScore: Float,
    val bestAccuracy: Float,
    val lastPlayedAt: Instant?
)
```

---

### 2. Database Schema (Room v5)

#### GameSessionDao.kt

```kotlin
@Dao
interface GameSessionDao {
    @Insert
    suspend fun insertSession(session: GameSessionEntity): Long
    
    @Query("SELECT * FROM game_sessions WHERE gameId = :gameId ORDER BY score DESC LIMIT 1")
    fun getPersonalBest(gameId: String): Flow<GameSessionEntity?>
    
    @Query("SELECT * FROM game_sessions WHERE gameId = :gameId ORDER BY startTime DESC")
    fun getAllSessions(gameId: String): Flow<List<GameSessionEntity>>
    
    @Query("SELECT COUNT(*) FROM game_sessions WHERE gameId = :gameId")
    fun getTotalPlays(gameId: String): Flow<Int>
    
    @Query("""
        SELECT AVG(score) FROM game_sessions 
        WHERE gameId = :gameId
    """)
    suspend fun getAverageScore(gameId: String): Float?
    
    @Query("SELECT * FROM game_sessions ORDER BY startTime DESC LIMIT 10")
    fun getRecentSessions(): Flow<List<GameSessionEntity>>
}
```

#### Database Migration

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS game_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                gameId TEXT NOT NULL,
                startTime INTEGER NOT NULL,
                endTime INTEGER NOT NULL,
                score INTEGER NOT NULL,
                correctAnswers INTEGER NOT NULL,
                totalAttempts INTEGER NOT NULL,
                duration INTEGER NOT NULL,
                gradeLevel TEXT NOT NULL
            )
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_game_sessions_gameId 
            ON game_sessions(gameId)
        """)
    }
}
```

---

### 3. Game Repository

#### GameRepository.kt

```kotlin
interface GameRepository {
    suspend fun saveGameSession(session: GameSession): Long
    fun getPersonalBest(gameId: String): Flow<Int>
    fun getGameStats(gameId: String): Flow<GameStats>
    fun getTotalGamesPlayed(): Flow<Int>
    fun isGameUnlocked(game: Game): Flow<Boolean>
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class GameRepositoryImpl constructor(
    private val gameSessionDao: GameSessionDao,
    private val sessionRepository: SessionRepository
) : GameRepository {
    
    override suspend fun saveGameSession(session: GameSession): Long {
        val entity = GameSessionEntity(
            gameId = session.gameId,
            startTime = session.startTime,
            endTime = session.endTime,
            score = session.score,
            correctAnswers = session.correctAnswers,
            totalAttempts = session.totalAttempts,
            duration = session.duration,
            gradeLevel = session.gradeLevel
        )
        return gameSessionDao.insertSession(entity)
    }
    
    override fun getPersonalBest(gameId: String): Flow<Int> {
        return gameSessionDao.getPersonalBest(gameId).map { it?.score ?: 0 }
    }
    
    override fun getGameStats(gameId: String): Flow<GameStats> {
        return combine(
            gameSessionDao.getPersonalBest(gameId),
            gameSessionDao.getTotalPlays(gameId),
            gameSessionDao.getAllSessions(gameId)
        ) { personalBest, totalPlays, sessions ->
            GameStats(
                gameId = gameId,
                personalBest = personalBest?.score ?: 0,
                totalPlays = totalPlays,
                averageScore = sessions.map { it.score }.average().toFloat(),
                bestAccuracy = sessions.maxOfOrNull { it.accuracy } ?: 0f,
                lastPlayedAt = sessions.firstOrNull()?.endTime
            )
        }
    }
    
    override fun getTotalGamesPlayed(): Flow<Int> {
        return gameSessionDao.getRecentSessions().map { it.size }
    }
    
    override fun isGameUnlocked(game: Game): Flow<Boolean> {
        // Use getOverallStats().totalProblems instead of getTotalProblemsCompleted()
        return sessionRepository.getOverallStats().map { stats ->
            game.isUnlocked(stats.totalProblems)
        }
    }
}
```

---

### 4. Math Race Game Screen (Circuit)

#### MathRaceScreen.kt

```kotlin
@Parcelize
data class MathRaceScreen() : Screen {
    data class State(
        val gameState: GameState,
        val currentProblem: MathProblem?,
        val currentAnswer: String,
        val score: Int,
        val timeRemaining: Int, // Seconds
        val personalBest: Int,
        val totalAttempts: Int,
        val correctAnswers: Int,
        val eventSink: (Event) -> Unit
    ) : CircuitUiState
    
    sealed class GameState {
        object NotStarted : GameState()
        object Countdown : GameState()
        object Playing : GameState()
        data class Finished(
            val finalScore: Int,
            val isNewRecord: Boolean,
            val accuracy: Float,
            val averageTime: Float,
            val fastestTime: Float
        ) : GameState()
    }
    
    sealed interface Event : CircuitUiEvent {
        object StartGame : Event
        data class NumberEntered(val number: Int) : Event
        object Backspace : Event
        object CheckAnswer : Event
        object PlayAgain : Event
        object NavigateHome : Event
    }
}

@CircuitInject(MathRaceScreen::class, AppScope::class)
@Composable
fun MathRacePresenter(
    @Assisted screen: MathRaceScreen,
    @Assisted navigator: Navigator,
    problemGenerator: ProblemGenerator,
    gameRepository: GameRepository,
    audioService: AudioService,
    hapticService: HapticService,
    userProfileRepository: UserProfileRepository
): MathRaceScreen.State {
    
    var gameState by remember { mutableStateOf<MathRaceScreen.GameState>(
        MathRaceScreen.GameState.NotStarted
    ) }
    var currentProblem by remember { mutableStateOf<MathProblem?>(null) }
    var currentAnswer by remember { mutableStateOf("") }
    var score by remember { mutableStateOf(0) }
    var timeRemaining by remember { mutableStateOf(60) }
    var totalAttempts by remember { mutableStateOf(0) }
    var correctAnswers by remember { mutableStateOf(0) }
    
    val personalBest by gameRepository.getPersonalBest(Game.MATH_RACE.id)
        .collectAsState(initial = 0)
    
    val userProfile by userProfileRepository.getProfile()
        .collectAsState(initial = null)
    
    // Timer countdown
    LaunchedEffect(gameState) {
        if (gameState is MathRaceScreen.GameState.Playing) {
            while (timeRemaining > 0) {
                delay(1000)
                timeRemaining--
                
                // Audio cue when time running out
                if (timeRemaining == 10) {
                    audioService.playWarning()
                }
            }
            
            // Game over
            val isNewRecord = score > personalBest
            gameState = MathRaceScreen.GameState.Finished(
                finalScore = score,
                isNewRecord = isNewRecord,
                accuracy = if (totalAttempts > 0) {
                    (correctAnswers.toFloat() / totalAttempts) * 100
                } else 0f,
                averageTime = 60f / totalAttempts,
                fastestTime = 0f // TODO: Track per problem
            )
            
            // Save game session
            val session = GameSession(
                gameId = Game.MATH_RACE.id,
                startTime = Clock.System.now().minus(60.seconds),
                endTime = Clock.System.now(),
                score = score,
                correctAnswers = correctAnswers,
                totalAttempts = totalAttempts,
                duration = 60,
                gradeLevel = userProfile?.gradeLevel ?: GradeLevel.GRADE_1,
                isNewRecord = isNewRecord
            )
            gameRepository.saveGameSession(session)
            
            if (isNewRecord) {
                audioService.playPerfectScore()
                hapticService.triggerBadgeUnlock()
            }
        }
    }
    
    // Generate new problem
    fun generateNewProblem() {
        val gradeLevel = userProfile?.gradeLevel ?: GradeLevel.GRADE_1
        currentProblem = problemGenerator.generateProblems(
            count = 1,
            operation = MathOperation.MIXED,
            gradeLevel = gradeLevel
        ).first()
        currentAnswer = ""
    }
    
    return MathRaceScreen.State(
        gameState = gameState,
        currentProblem = currentProblem,
        currentAnswer = currentAnswer,
        score = score,
        timeRemaining = timeRemaining,
        personalBest = personalBest,
        totalAttempts = totalAttempts,
        correctAnswers = correctAnswers
    ) { event ->
        when (event) {
            is MathRaceScreen.Event.StartGame -> {
                gameState = MathRaceScreen.GameState.Countdown
                score = 0
                timeRemaining = 60
                totalAttempts = 0
                correctAnswers = 0
                
                // Countdown: 3... 2... 1... GO!
                scope.launch {
                    delay(1000)
                    audioService.playCountdown()
                    delay(1000)
                    audioService.playCountdown()
                    delay(1000)
                    audioService.playGo()
                    gameState = MathRaceScreen.GameState.Playing
                    generateNewProblem()
                }
            }
            
            is MathRaceScreen.Event.NumberEntered -> {
                if (currentAnswer.length < 4) {
                    currentAnswer += event.number.toString()
                    hapticService.triggerButtonClick()
                }
            }
            
            is MathRaceScreen.Event.Backspace -> {
                if (currentAnswer.isNotEmpty()) {
                    currentAnswer = currentAnswer.dropLast(1)
                    hapticService.triggerButtonClick()
                }
            }
            
            is MathRaceScreen.Event.CheckAnswer -> {
                val isCorrect = currentAnswer == currentProblem?.correctAnswer?.toString()
                totalAttempts++
                
                if (isCorrect) {
                    correctAnswers++
                    score++
                    audioService.playSuccess()
                    hapticService.triggerSuccess()
                } else {
                    audioService.playError()
                    hapticService.triggerError()
                }
                
                // Next problem immediately
                generateNewProblem()
            }
            
            is MathRaceScreen.Event.PlayAgain -> {
                gameState = MathRaceScreen.GameState.NotStarted
            }
            
            is MathRaceScreen.Event.NavigateHome -> {
                navigator.pop()
            }
        }
    }
}

@CircuitInject(MathRaceScreen::class, AppScope::class)
@Composable
fun MathRaceUi(
    state: MathRaceScreen.State,
    modifier: Modifier = Modifier
) {
    when (val gameState = state.gameState) {
        is MathRaceScreen.GameState.NotStarted -> {
            MathRaceStartScreen(state)
        }
        is MathRaceScreen.GameState.Countdown -> {
            CountdownScreen()
        }
        is MathRaceScreen.GameState.Playing -> {
            MathRaceGameScreen(state)
        }
        is MathRaceScreen.GameState.Finished -> {
            MathRaceResultsScreen(state, gameState)
        }
    }
}

@Composable
private fun MathRaceGameScreen(state: MathRaceScreen.State) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header with timer and score
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Timer with pulsing animation when < 10s
            val timerColor = if (state.timeRemaining <= 10) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = timerColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "0:${state.timeRemaining.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.displaySmall,
                    color = timerColor
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Score: ${state.score}",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        
        // Problem display
        state.currentProblem?.let { problem ->
            MathProblemDisplay(
                problem = problem,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Answer field
        AnswerField(
            answer = state.currentAnswer,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Number pad
        NumberPad(
            onNumberClick = { state.eventSink(MathRaceScreen.Event.NumberEntered(it)) },
            onBackspace = { state.eventSink(MathRaceScreen.Event.Backspace) },
            onCheck = { state.eventSink(MathRaceScreen.Event.CheckAnswer) },
            checkEnabled = state.currentAnswer.isNotEmpty()
        )
        
        // Personal best
        Text(
            text = "Personal Best: ${state.personalBest}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
```

---

### 5. Game Selection Screen (Circuit)

#### GameSelectionScreen.kt

```kotlin
@Parcelize
data class GameSelectionScreen() : Screen {
    data class State(
        val games: List<GameInfo>,
        val eventSink: (Event) -> Unit
    ) : CircuitUiState
    
    data class GameInfo(
        val game: Game,
        val isUnlocked: Boolean,
        val personalBest: Int,
        val totalPlays: Int
    )
    
    sealed interface Event : CircuitUiEvent {
        data class PlayGame(val game: Game) : Event
        object NavigateBack : Event
    }
}

@CircuitInject(GameSelectionScreen::class, AppScope::class)
@Composable
fun GameSelectionPresenter(
    @Assisted screen: GameSelectionScreen,
    @Assisted navigator: Navigator,
    gameRepository: GameRepository,
    sessionRepository: SessionRepository
): GameSelectionScreen.State {
    
    // Use getOverallStats() which exists in current SessionRepository
    val overallStats by sessionRepository.getOverallStats()
        .collectAsState(initial = SessionStats.EMPTY)
    
    val games = Game.values().map { game ->
        val personalBest by gameRepository.getPersonalBest(game.id)
            .collectAsState(initial = 0)
        val stats by gameRepository.getGameStats(game.id)
            .collectAsState(initial = null)
        
        GameSelectionScreen.GameInfo(
            game = game,
            isUnlocked = game.isUnlocked(overallStats.totalProblems),
            personalBest = personalBest,
            totalPlays = stats?.totalPlays ?: 0
        )
    }
    
    return GameSelectionScreen.State(
        games = games
    ) { event ->
        when (event) {
            is GameSelectionScreen.Event.PlayGame -> {
                if (event.game == Game.MATH_RACE) {
                    navigator.goTo(MathRaceScreen())
                }
                // Add other games when implemented
            }
            is GameSelectionScreen.Event.NavigateBack -> {
                navigator.pop()
            }
        }
    }
}

@CircuitInject(GameSelectionScreen::class, AppScope::class)
@Composable
fun GameSelectionUi(
    state: GameSelectionScreen.State,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Games Hub") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(GameSelectionScreen.Event.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "🎮 Play fun math games!",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(state.games) { gameInfo ->
                GameCard(
                    gameInfo = gameInfo,
                    onClick = {
                        if (gameInfo.isUnlocked) {
                            state.eventSink(GameSelectionScreen.Event.PlayGame(gameInfo.game))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GameCard(
    gameInfo: GameSelectionScreen.GameInfo,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = gameInfo.isUnlocked, onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (gameInfo.isUnlocked) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${gameInfo.game.displayName}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (!gameInfo.isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = gameInfo.game.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (gameInfo.isUnlocked) {
                if (gameInfo.personalBest > 0) {
                    Text(
                        text = "🏆 Best: ${gameInfo.personalBest} problems",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                Button(
                    onClick = onClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("PLAY")
                }
            } else {
                Text(
                    text = "Unlock: Solve ${gameInfo.game.unlockRequirement} problems",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("LOCKED")
                }
            }
        }
    }
}
```

---

## Testing Strategy

### Unit Tests

```kotlin
@Test
fun `game unlocks after 50 problems solved`() {
    val game = Game.MATH_RACE
    
    assertFalse(game.isUnlocked(49))
    assertTrue(game.isUnlocked(50))
    assertTrue(game.isUnlocked(100))
}

@Test
fun `game session records score correctly`() {
    val session = GameSession(
        gameId = "math_race",
        startTime = Clock.System.now(),
        endTime = Clock.System.now(),
        score = 15,
        correctAnswers = 15,
        totalAttempts = 18,
        duration = 60,
        gradeLevel = GradeLevel.GRADE_1
    )
    
    assertEquals(15, session.score)
    assertEquals(83.33f, session.accuracy, 0.01f)
}

@Test
fun `personal best updated when score higher`() = runTest {
    val repository = FakeGameRepository()
    
    // First game
    repository.saveGameSession(
        GameSession(
            gameId = "math_race",
            score = 15,
            // ... other fields
        )
    )
    
    assertEquals(15, repository.getPersonalBest("math_race").first())
    
    // Second game with higher score
    repository.saveGameSession(
        GameSession(
            gameId = "math_race",
            score = 20,
            // ... other fields
        )
    )
    
    assertEquals(20, repository.getPersonalBest("math_race").first())
}
```

### UI Tests

```kotlin
@Test
fun `timer counts down correctly`() {
    composeTestRule.setContent {
        MathRaceGameScreen(
            state = MathRaceScreen.State(
                timeRemaining = 60,
                // ... other fields
            )
        )
    }
    
    composeTestRule.onNodeWithText("0:60").assertExists()
    
    // Simulate time passing
    advanceTimeBy(10_000) // 10 seconds
    
    composeTestRule.onNodeWithText("0:50").assertExists()
}

@Test
fun `score increases on correct answer`() {
    // Test score increment
}

@Test
fun `game over shows results when timer expires`() {
    // Test game over flow
}
```

### Manual Testing Checklist

**Math Race Game**
- [ ] 3-2-1 countdown plays correctly
- [ ] Timer starts at 60 seconds
- [ ] Timer counts down accurately
- [ ] Score increments on correct answer
- [ ] No score change on incorrect answer
- [ ] Problems appropriate for grade level
- [ ] Instant feedback (audio + haptic)
- [ ] Game ends at 0:00
- [ ] Results screen shows correct stats
- [ ] New record detected and celebrated
- [ ] Personal best saves correctly
- [ ] Play again restarts game
- [ ] Home button returns to home screen

**Game Selection**
- [ ] Math Race unlocks at 50 problems
- [ ] Locked games show unlock requirements
- [ ] Unlocked games are playable
- [ ] Personal best displays correctly
- [ ] Game cards styled correctly
- [ ] Lock icon appears on locked games
- [ ] Navigation works correctly

**Badges**
- [ ] "Speed Demon" unlocks at 20+ score
- [ ] "Racing Champion" unlocks at 30+ score
- [ ] "Perfect Race" unlocks at 100% accuracy
- [ ] "Game Master" unlocks after 10 games
- [ ] Badge unlock notification appears
- [ ] Badges appear in badges screen

**Performance**
- [ ] No lag during gameplay
- [ ] Timer accurate (not drifting)
- [ ] Fast problem transitions
- [ ] Smooth animations
- [ ] Audio synced with actions

---

## Migration Plan from Phase 5

### Step 1: Game Data Layer (Days 1-2)
1. Create GameSession data model
2. Create GameSessionEntity and Dao
3. Implement database migration (v3 → v4)
4. Create GameRepository interface and implementation
5. Unit tests for repository
6. Test database operations

### Step 2: Math Race Game Logic (Days 3-5)
1. Create MathRaceScreen Circuit
2. Implement game state management
3. Add countdown timer
4. Implement score tracking
5. Add problem generation
6. Test game logic thoroughly

### Step 3: Math Race UI (Days 6-7)
1. Create MathRaceUi composable
2. Design countdown screen
3. Design playing screen
4. Design results screen
5. Add animations (timer pulse, score celebration)
6. Integrate audio and haptics

### Step 4: Game Selection Screen (Days 8-9)
1. Create Game enum with unlock requirements
2. Create GameSelectionScreen Circuit
3. Design game cards (locked/unlocked states)
4. Implement unlock logic
5. Add navigation from home screen
6. Test unlock flow

### Step 5: Game Badges (Days 10-11)
1. Add new badge types for games
2. Update badge checking logic
3. Add badge unlock notification
4. Test badge unlocking
5. Verify badges appear in badge screen

### Step 6: Polish & Testing (Days 12-14)
1. End-to-end game testing
2. Performance optimization
3. Timer accuracy verification
4. Real child testing
5. Bug fixes
6. CHANGELOG.md update
7. Documentation

---

## Success Metrics

- ✅ Game playable and fun
- ✅ Timer accurate to within 1 second
- ✅ Score tracking correct
- ✅ Personal best saves reliably
- ✅ Unlock system works correctly
- ✅ No performance issues during gameplay
- ✅ Children want to replay
- ✅ Average session length >2 games

---

## Definition of Done

- ✅ Math Race game fully implemented
- ✅ 60-second countdown timer working
- ✅ Score tracking and personal best
- ✅ Game results screen with stats
- ✅ Game selection screen with lock/unlock
- ✅ Unlock after 50 problems solved
- ✅ 4 new game badges implemented
- ✅ Audio and haptic feedback integrated
- ✅ Database migration (v3 → v4) successful
- ✅ All tests passing
- ✅ Real child testing completed
- ✅ Children enjoy the game
- ✅ CHANGELOG.md updated
- ✅ Code formatted (`./gradlew formatKotlin`)

---

## Future Game Ideas (Phase 7+)

### Memory Match
- Match math problems with answers
- 4×4 grid of cards
- Flip two cards at a time
- Find all matching pairs

### Number Sequence
- Fill in the missing number
- Pattern recognition (2, 4, 6, ?, 10)
- Skip counting practice
- Grade-appropriate sequences

### Math Bingo
- 5×5 bingo card with answers
- Problems called out randomly
- Mark correct answers
- Get 5 in a row to win

### Target Number
- Reach target using given numbers
- Use +, -, ×, ÷ operations
- Multiple solutions possible
- Puzzle-style gameplay

---

*Document created: December 16, 2025*  
*Last updated: December 19, 2025*  
*Phase status: 🔴 Not Started*  
*Target completion: Week 13 (after Phase 5)*

---

## Notes for Implementation

> **Important**: This document was reviewed on December 19, 2025 after Phase 5 completion.
> Key updates made:
> - Database migration updated to v4 → v5 (was v3 → v4)
> - Badge system aligned with current `BadgeCategory` enum + `BadgeDefinitions` pattern
> - Added GAMES category to BadgeCategory
> - Updated GameRepository to use `getOverallStats().totalProblems`
> - AudioService needs new methods: `playWarning()`, `playCountdown()`, `playGo()` (add in Phase 6)
