package dev.hossain.mathtutor.ui.memorymatch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.mathtutor.analytics.AnalyticsService
import dev.hossain.mathtutor.audio.AudioService
import dev.hossain.mathtutor.domain.generator.ProblemGenerator
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GameSession
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.repository.GameRepository
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.domain.usecase.CheckBadgeUnlocksUseCase
import dev.hossain.mathtutor.haptic.HapticService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant

/**
 * Presenter for [MemoryMatchScreen].
 *
 * Manages the state and game logic for the Memory Match mini-game.
 * This includes:
 * - Generating 8 problem-answer pairs (16 cards total)
 * - Card flip logic with two-card matching
 * - Match detection and completion tracking
 * - Timer for total game duration
 * - Saving game session to database
 */
@AssistedInject
class MemoryMatchPresenter
    constructor(
        @Assisted private val screen: MemoryMatchScreen,
        @Assisted private val navigator: Navigator,
        private val problemGenerator: ProblemGenerator,
        private val gameRepository: GameRepository,
        private val userProfileRepository: UserProfileRepository,
        private val checkBadgeUnlocksUseCase: CheckBadgeUnlocksUseCase,
        private val audioService: AudioService,
        private val hapticService: HapticService,
        private val analyticsService: AnalyticsService,
    ) : Presenter<MemoryMatchScreen.State> {
        @CircuitInject(MemoryMatchScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(
                screen: MemoryMatchScreen,
                navigator: Navigator,
            ): MemoryMatchPresenter
        }

        companion object {
            /** Number of pairs in the game */
            private const val TOTAL_PAIRS = 8

            /** Total number of cards (pairs * 2) */
            private const val TOTAL_CARDS = TOTAL_PAIRS * 2

            /** Countdown start value (3-2-1-GO) */
            private const val COUNTDOWN_START = 3

            /** Delay before auto-flipping unmatched cards back */
            private const val FLIP_BACK_DELAY_MS = 1000L
        }

        @Composable
        override fun present(): MemoryMatchScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Memory Match",
                    screenClass = MemoryMatchScreen::class.java.name,
                )
            }

            val coroutineScope = rememberCoroutineScope()

            // Game state
            var gameState by remember {
                mutableStateOf<MemoryMatchScreen.GameState>(MemoryMatchScreen.GameState.NotStarted)
            }
            var cards by remember { mutableStateOf<List<MemoryMatchScreen.Card>>(emptyList()) }
            var moves by remember { mutableIntStateOf(0) }
            var timeElapsed by remember { mutableIntStateOf(0) }
            var matchesFound by remember { mutableIntStateOf(0) }
            var personalBestTime by remember { mutableIntStateOf(0) }
            var firstFlippedCard by remember { mutableStateOf<MemoryMatchScreen.Card?>(null) }
            var secondFlippedCard by remember { mutableStateOf<MemoryMatchScreen.Card?>(null) }
            var userName by remember { mutableStateOf<String?>(null) }
            var unlockedBadges by remember { mutableStateOf<List<Badge>>(emptyList()) }

            // Internal state for game logic
            var gradeLevel by remember { mutableStateOf(GradeLevel.GRADE_1) }
            var gameStartTime by remember { mutableStateOf<Instant?>(null) }
            var isProcessingFlip by remember { mutableStateOf(false) }

            // Load user profile and personal best
            LaunchedEffect(Unit) {
                Timber.d("[MemoryMatch] Loading user profile and personal best...")
                val profile = userProfileRepository.getProfile().firstOrNull()
                gradeLevel = profile?.gradeLevel ?: GradeLevel.GRADE_1
                userName = profile?.name
                Timber.d("[MemoryMatch] User profile loaded: name=$userName, gradeLevel=$gradeLevel")

                // Load personal best for Memory Match (best time in seconds)
                gameRepository.getPersonalBest(Game.MEMORY_MATCH).collect { best ->
                    personalBestTime = best
                    Timber.d("[MemoryMatch] Personal best loaded: $best seconds")
                }
            }

            // Timer effect - counts up during gameplay
            LaunchedEffect(gameState) {
                if (gameState == MemoryMatchScreen.GameState.Playing) {
                    Timber.d("[MemoryMatch] Timer started")
                    while (gameState == MemoryMatchScreen.GameState.Playing) {
                        delay(1000L)
                        timeElapsed++
                    }
                }
            }

            /**
             * Generates cards for the memory match game.
             * Creates 8 problem-answer pairs and shuffles them into a 4×4 grid.
             */
            fun generateCards(): List<MemoryMatchScreen.Card> {
                val problems =
                    problemGenerator.generateProblems(
                        count = TOTAL_PAIRS,
                        operation = MathOperation.MIXED,
                        gradeLevel = gradeLevel,
                    )

                val cardList = mutableListOf<MemoryMatchScreen.Card>()
                problems.forEachIndexed { index, problem ->
                    // Add problem card
                    cardList.add(
                        MemoryMatchScreen.Card(
                            id = index * 2,
                            content = problem.getDisplayString().replace(" = ?", ""),
                            pairId = index,
                        ),
                    )
                    // Add answer card
                    cardList.add(
                        MemoryMatchScreen.Card(
                            id = index * 2 + 1,
                            content = problem.correctAnswer.toString(),
                            pairId = index,
                        ),
                    )
                }

                // Shuffle cards randomly
                return cardList.shuffled()
            }

            /**
             * Starts the countdown sequence (3-2-1-GO).
             */
            suspend fun startCountdown() {
                Timber.d("[MemoryMatch] Starting countdown...")

                // Reset game state
                moves = 0
                timeElapsed = 0
                matchesFound = 0
                firstFlippedCard = null
                secondFlippedCard = null
                isProcessingFlip = false

                // Generate cards
                cards = generateCards()
                Timber.d("[MemoryMatch] Generated ${cards.size} cards")

                // Play countdown audio at the start
                audioService.playGo()
                hapticService.triggerSuccess()
                Timber.d("[MemoryMatch] Countdown started")

                // Countdown 3-2-1 (visual only, no sound per count)
                for (count in COUNTDOWN_START downTo 1) {
                    gameState = MemoryMatchScreen.GameState.Countdown(count)
                    hapticService.triggerButtonClick()
                    Timber.d("[MemoryMatch] Countdown: $count")
                    delay(1000L)
                }

                // Transition to playing state
                gameState = MemoryMatchScreen.GameState.Countdown(0) // 0 represents "GO"
                Timber.d("[MemoryMatch] Starting game...")
                delay(500L) // Brief pause before game starts

                // Start game
                gameStartTime = Instant.now()
                gameState = MemoryMatchScreen.GameState.Playing
                Timber.d("[MemoryMatch] Game started!")
            }

            /**
             * Completes the game, saves session, and checks for badge unlocks.
             */
            fun completeGame() {
                coroutineScope.launch(Dispatchers.IO) {
                    val gameEndTime = Instant.now()
                    val actualDuration =
                        gameStartTime?.let {
                            java.time.Duration
                                .between(it, gameEndTime)
                                .seconds
                                .toInt()
                        } ?: timeElapsed

                    val accuracy =
                        if (moves > 0) {
                            (matchesFound.toFloat() / moves) * 100f
                        } else {
                            0f
                        }

                    val isNewRecord =
                        personalBestTime == 0 || actualDuration < personalBestTime

                    Timber.d(
                        "[MemoryMatch] Game complete - Time: $actualDuration seconds, " +
                            "Moves: $moves, New record: $isNewRecord",
                    )

                    // Save game session and check for badge unlocks
                    try {
                        val session =
                            GameSession(
                                game = Game.MEMORY_MATCH,
                                startTime = gameStartTime ?: Instant.now(),
                                endTime = gameEndTime,
                                score = actualDuration, // For Memory Match, lower time is better
                                correctAnswers = matchesFound,
                                totalAttempts = moves,
                                durationSeconds = actualDuration,
                                gradeLevel = gradeLevel,
                                isNewRecord = isNewRecord,
                            )
                        gameRepository.saveGameSession(session)
                        Timber.d("[MemoryMatch] Game session saved successfully")

                        // Check for badge unlocks after saving the session
                        val newBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()
                        Timber.d(
                            "[MemoryMatch] Badge check complete. Unlocked: ${newBadges.size} badges",
                        )

                        withContext(Dispatchers.Main) {
                            unlockedBadges = newBadges

                            // Play audio based on result
                            if (newBadges.isNotEmpty()) {
                                audioService.playBadgeUnlock()
                                hapticService.triggerBadgeUnlock()
                                Timber.d(
                                    "[MemoryMatch] Badge(s) unlocked! Playing badge unlock audio",
                                )
                            } else if (isNewRecord) {
                                audioService.playPerfectScore()
                                hapticService.triggerBadgeUnlock()
                                Timber.d("[MemoryMatch] New record achieved! Playing celebration audio")
                            }

                            // Update game state to Finished with unlocked badges
                            gameState =
                                MemoryMatchScreen.GameState.Finished(
                                    moves = moves,
                                    timeElapsed = actualDuration,
                                    isNewRecord = isNewRecord,
                                    accuracy = accuracy,
                                    unlockedBadges = newBadges,
                                )
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "[MemoryMatch] Failed to save game session")
                        // Still update game state even if save failed
                        withContext(Dispatchers.Main) {
                            gameState =
                                MemoryMatchScreen.GameState.Finished(
                                    moves = moves,
                                    timeElapsed = actualDuration,
                                    isNewRecord = isNewRecord,
                                    accuracy = accuracy,
                                )
                        }
                    }
                }
            }

            /**
             * Handles card flip logic and match detection.
             */
            suspend fun handleCardFlip(cardId: Int) {
                if (isProcessingFlip) {
                    Timber.d("[MemoryMatch] Flip ignored - already processing")
                    return
                }

                val card = cards.find { it.id == cardId } ?: return

                // Ignore if card is already flipped or matched
                if (card.isFlipped || card.isMatched) {
                    Timber.d("[MemoryMatch] Card $cardId already flipped/matched")
                    return
                }

                // Flip the card
                cards =
                    cards.map { c ->
                        if (c.id == cardId) c.copy(isFlipped = true) else c
                    }
                hapticService.triggerButtonClick()
                Timber.d("[MemoryMatch] Flipped card $cardId: ${card.content}")

                when {
                    firstFlippedCard == null -> {
                        // First card flipped
                        firstFlippedCard = card.copy(isFlipped = true)
                        Timber.d("[MemoryMatch] First card set: ${card.content}")
                    }

                    secondFlippedCard == null -> {
                        // Second card flipped - check for match
                        secondFlippedCard = card.copy(isFlipped = true)
                        moves++
                        isProcessingFlip = true

                        Timber.d(
                            "[MemoryMatch] Second card set: ${card.content}, " +
                                "Checking match (Move #$moves)",
                        )

                        delay(500L) // Brief delay to show both cards

                        // Check if cards match
                        val first = firstFlippedCard!!
                        val second = secondFlippedCard!!

                        if (first.pairId == second.pairId) {
                            // Match found!
                            matchesFound++
                            cards =
                                cards.map { c ->
                                    if (c.pairId == first.pairId) {
                                        c.copy(isMatched = true, isFlipped = true)
                                    } else {
                                        c
                                    }
                                }
                            audioService.playSuccess()
                            hapticService.triggerSuccess()
                            Timber.d(
                                "[MemoryMatch] Match found! Pair #$matchesFound of $TOTAL_PAIRS",
                            )

                            // Check if game is complete
                            if (matchesFound == TOTAL_PAIRS) {
                                Timber.d("[MemoryMatch] All pairs matched! Game complete!")
                                completeGame()
                            }
                        } else {
                            // No match - flip cards back after delay
                            audioService.playError()
                            hapticService.triggerError()
                            Timber.d("[MemoryMatch] No match - flipping back")

                            delay(FLIP_BACK_DELAY_MS)

                            cards =
                                cards.map { c ->
                                    if (c.id == first.id || c.id == second.id) {
                                        c.copy(isFlipped = false)
                                    } else {
                                        c
                                    }
                                }
                        }

                        // Reset flipped cards
                        firstFlippedCard = null
                        secondFlippedCard = null
                        isProcessingFlip = false
                    }
                }
            }

            return MemoryMatchScreen.State(
                gameState = gameState,
                cards = cards,
                moves = moves,
                timeElapsed = timeElapsed,
                matchesFound = matchesFound,
                totalPairs = TOTAL_PAIRS,
                personalBestTime = personalBestTime,
                firstFlippedCard = firstFlippedCard,
                secondFlippedCard = secondFlippedCard,
                userName = userName,
            ) { event ->
                when (event) {
                    is MemoryMatchScreen.Event.StartGame -> {
                        coroutineScope.launch {
                            startCountdown()
                        }
                    }

                    is MemoryMatchScreen.Event.CardFlipped -> {
                        coroutineScope.launch {
                            handleCardFlip(event.cardId)
                        }
                    }

                    is MemoryMatchScreen.Event.PlayAgain -> {
                        gameState = MemoryMatchScreen.GameState.NotStarted
                        unlockedBadges = emptyList()
                    }

                    is MemoryMatchScreen.Event.NavigateHome -> {
                        navigator.pop()
                    }
                }
            }
        }
    }
