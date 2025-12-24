package dev.hossain.mathtutor.ui.numbersequence

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
import dev.hossain.mathtutor.domain.generator.SequenceGenerator
import dev.hossain.mathtutor.domain.generator.SequenceQuestion
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.Game
import dev.hossain.mathtutor.domain.model.GameSession
import dev.hossain.mathtutor.domain.model.GradeLevel
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
 * Presenter for [NumberSequenceScreen].
 *
 * Manages the state and game logic for the Number Sequence mini-game.
 * This includes:
 * - 3-2-1 countdown before game starts
 * - 90-second timer with 10-second warning
 * - Sequence generation appropriate for grade level
 * - Answer checking and score tracking
 * - Personal best detection
 * - Saving game session to database
 */
@AssistedInject
class NumberSequencePresenter
    constructor(
        @Assisted private val screen: NumberSequenceScreen,
        @Assisted private val navigator: Navigator,
        private val sequenceGenerator: SequenceGenerator,
        private val gameRepository: GameRepository,
        private val userProfileRepository: UserProfileRepository,
        private val checkBadgeUnlocksUseCase: CheckBadgeUnlocksUseCase,
        private val audioService: AudioService,
        private val hapticService: HapticService,
        private val analyticsService: AnalyticsService,
    ) : Presenter<NumberSequenceScreen.State> {
        @CircuitInject(NumberSequenceScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(
                screen: NumberSequenceScreen,
                navigator: Navigator,
            ): NumberSequencePresenter
        }

        companion object {
            /** Duration of the game in seconds */
            private const val GAME_DURATION_SECONDS = 90

            /** Seconds remaining when warning sound plays */
            private const val WARNING_THRESHOLD_SECONDS = 7

            /** Maximum digits allowed in answer */
            private const val MAX_ANSWER_DIGITS = 4

            /** Countdown start value (3-2-1-GO) */
            private const val COUNTDOWN_START = 3
        }

        @Composable
        override fun present(): NumberSequenceScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Number Sequence",
                    screenClass = NumberSequenceScreen::class.java.name,
                )
            }

            val coroutineScope = rememberCoroutineScope()

            // Game state
            var gameState by remember {
                mutableStateOf<NumberSequenceScreen.GameState>(NumberSequenceScreen.GameState.NotStarted)
            }
            var currentSequence by remember { mutableStateOf<SequenceQuestion?>(null) }
            var currentAnswer by remember { mutableStateOf("") }
            var score by remember { mutableIntStateOf(0) }
            var timeRemaining by remember { mutableIntStateOf(GAME_DURATION_SECONDS) }
            var personalBest by remember { mutableIntStateOf(0) }
            var totalAttempts by remember { mutableIntStateOf(0) }
            var correctAnswers by remember { mutableIntStateOf(0) }
            var lastAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
            var userName by remember { mutableStateOf<String?>(null) }
            var unlockedBadges by remember { mutableStateOf<List<Badge>>(emptyList()) }

            // Internal state for game logic
            var gradeLevel by remember { mutableStateOf(GradeLevel.GRADE_1) }
            var gameStartTime by remember { mutableStateOf<Instant?>(null) }
            var warningPlayed by remember { mutableStateOf(false) }

            // Load user profile and personal best
            LaunchedEffect(Unit) {
                Timber.d("[NumberSequence] Loading user profile and personal best...")
                val profile = userProfileRepository.getProfile().firstOrNull()
                gradeLevel = profile?.gradeLevel ?: GradeLevel.GRADE_1
                userName = profile?.name
                Timber.d("[NumberSequence] User profile loaded: name=$userName, gradeLevel=$gradeLevel")

                // Load personal best for Number Sequence
                gameRepository.getPersonalBest(Game.NUMBER_SEQUENCE).collect { best ->
                    personalBest = best
                    Timber.d("[NumberSequence] Personal best loaded: $best")
                }
            }

            // Timer countdown effect
            LaunchedEffect(gameState) {
                if (gameState == NumberSequenceScreen.GameState.Playing) {
                    Timber.d("[NumberSequence] Timer started: $timeRemaining seconds")
                    while (timeRemaining > 0) {
                        delay(1000L)
                        timeRemaining--

                        // Play countdown tick at 10 seconds
                        if (timeRemaining == WARNING_THRESHOLD_SECONDS && !warningPlayed) {
                            audioService.playCountdown()
                            hapticService.triggerLongPress()
                            warningPlayed = true
                            Timber.d("[NumberSequence] Warning sound played at $timeRemaining seconds")
                        }
                    }

                    // Game ended - calculate and save stats
                    val gameEndTime = Instant.now()
                    val actualDuration =
                        gameStartTime?.let {
                            java.time.Duration
                                .between(it, gameEndTime)
                                .seconds
                                .toInt()
                        } ?: GAME_DURATION_SECONDS

                    val accuracy =
                        if (totalAttempts > 0) {
                            (correctAnswers.toFloat() / totalAttempts) * 100f
                        } else {
                            0f
                        }
                    val avgTime =
                        if (totalAttempts > 0) {
                            actualDuration.toFloat() / totalAttempts
                        } else {
                            0f
                        }
                    val isNewRecord = score > personalBest

                    Timber.d(
                        "[NumberSequence] Game ended - Score: $score, Attempts: $totalAttempts, " +
                            "Accuracy: $accuracy%, New record: $isNewRecord, Trial mode: ${screen.isTrialMode}",
                    )

                    // Save game session and check for badge unlocks
                    // Skip badge checking if in trial mode (locked game)
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val session =
                                GameSession(
                                    game = Game.NUMBER_SEQUENCE,
                                    startTime = gameStartTime ?: Instant.now(),
                                    endTime = gameEndTime,
                                    score = score,
                                    correctAnswers = correctAnswers,
                                    totalAttempts = totalAttempts,
                                    durationSeconds = actualDuration,
                                    gradeLevel = gradeLevel,
                                    isNewRecord = isNewRecord,
                                )
                            gameRepository.saveGameSession(session)
                            Timber.d("[NumberSequence] Game session saved successfully")

                            // Check for badge unlocks only if NOT in trial mode
                            val newBadges =
                                if (screen.isTrialMode) {
                                    Timber.d("[NumberSequence] Skipping badge check - trial mode active")
                                    emptyList()
                                } else {
                                    checkBadgeUnlocksUseCase.checkAndUnlockBadges().also {
                                        Timber.d("[NumberSequence] Badge check complete. Unlocked: ${it.size} badges")
                                    }
                                }

                            withContext(Dispatchers.Main) {
                                unlockedBadges = newBadges

                                // Play audio based on result
                                if (newBadges.isNotEmpty()) {
                                    audioService.playBadgeUnlock()
                                    hapticService.triggerBadgeUnlock()
                                    Timber.d("[NumberSequence] Badge(s) unlocked! Playing badge unlock audio")
                                } else if (isNewRecord && score > 0) {
                                    audioService.playPerfectScore()
                                    hapticService.triggerBadgeUnlock()
                                    Timber.d("[NumberSequence] New record achieved! Playing celebration audio")
                                }

                                // Update game state to Finished with unlocked badges
                                gameState =
                                    NumberSequenceScreen.GameState.Finished(
                                        finalScore = score,
                                        totalAttempts = totalAttempts,
                                        isNewRecord = isNewRecord,
                                        accuracy = accuracy,
                                        averageTimePerSequence = avgTime,
                                        unlockedBadges = newBadges,
                                    )
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "[NumberSequence] Failed to save game session")
                            // Still update game state even if save failed
                            withContext(Dispatchers.Main) {
                                gameState =
                                    NumberSequenceScreen.GameState.Finished(
                                        finalScore = score,
                                        totalAttempts = totalAttempts,
                                        isNewRecord = isNewRecord,
                                        accuracy = accuracy,
                                        averageTimePerSequence = avgTime,
                                    )
                            }
                        }
                    }
                }
            }

            /**
             * Generates a new sequence puzzle appropriate for the grade level.
             */
            fun generateNewSequence(): SequenceQuestion {
                val sequence = sequenceGenerator.generateSequence(gradeLevel)
                Timber.d("[NumberSequence] Generated new sequence: ${sequence.numbers}, answer=${sequence.correctAnswer}")
                return sequence
            }

            /**
             * Starts the countdown sequence (3-2-1-GO).
             */
            suspend fun startCountdown() {
                Timber.d("[NumberSequence] Starting countdown...")

                // Reset game state
                score = 0
                timeRemaining = GAME_DURATION_SECONDS
                totalAttempts = 0
                correctAnswers = 0
                currentAnswer = ""
                lastAnswerCorrect = null
                warningPlayed = false

                // Play countdown audio at the start
                audioService.playGo()
                hapticService.triggerSuccess()
                Timber.d("[NumberSequence] Countdown started")

                // Countdown 3-2-1 (visual only, no sound per count)
                for (count in COUNTDOWN_START downTo 1) {
                    gameState = NumberSequenceScreen.GameState.Countdown(count)
                    hapticService.triggerButtonClick()
                    Timber.d("[NumberSequence] Countdown: $count")
                    delay(1000L)
                }

                // Transition to playing state
                gameState = NumberSequenceScreen.GameState.Countdown(0) // 0 represents "GO"
                Timber.d("[NumberSequence] Starting game...")
                delay(500L) // Brief pause before game starts

                // Start game
                gameStartTime = Instant.now()
                currentSequence = generateNewSequence()
                gameState = NumberSequenceScreen.GameState.Playing
                Timber.d("[NumberSequence] Game started! First sequence: ${currentSequence?.numbers}")
            }

            return NumberSequenceScreen.State(
                gameState = gameState,
                currentSequence = currentSequence,
                currentAnswer = currentAnswer,
                score = score,
                timeRemaining = timeRemaining,
                personalBest = personalBest,
                totalAttempts = totalAttempts,
                correctAnswers = correctAnswers,
                lastAnswerCorrect = lastAnswerCorrect,
                userName = userName,
            ) { event ->
                when (event) {
                    NumberSequenceScreen.Event.StartGame -> {
                        if (gameState == NumberSequenceScreen.GameState.NotStarted ||
                            gameState is NumberSequenceScreen.GameState.Finished
                        ) {
                            coroutineScope.launch {
                                startCountdown()
                            }
                        }
                    }

                    is NumberSequenceScreen.Event.NumberEntered -> {
                        if (gameState == NumberSequenceScreen.GameState.Playing) {
                            // Limit answer to MAX_ANSWER_DIGITS digits
                            if (currentAnswer.length < MAX_ANSWER_DIGITS) {
                                currentAnswer += event.digit.toString()
                                hapticService.triggerButtonClick()
                            }
                        }
                    }

                    NumberSequenceScreen.Event.Backspace -> {
                        if (gameState == NumberSequenceScreen.GameState.Playing && currentAnswer.isNotEmpty()) {
                            currentAnswer = currentAnswer.dropLast(1)
                            hapticService.triggerButtonClick()
                        }
                    }

                    NumberSequenceScreen.Event.CheckAnswer -> {
                        if (gameState == NumberSequenceScreen.GameState.Playing && currentSequence != null) {
                            val userAnswer = currentAnswer.toIntOrNull()
                            if (userAnswer != null) {
                                totalAttempts++
                                val isCorrect = userAnswer == currentSequence!!.correctAnswer
                                lastAnswerCorrect = isCorrect

                                if (isCorrect) {
                                    score++
                                    correctAnswers++
                                    audioService.playSuccess()
                                    hapticService.triggerSuccess()
                                    Timber.d("[NumberSequence] Correct! Score: $score")
                                } else {
                                    audioService.playError()
                                    hapticService.triggerError()
                                    Timber.d(
                                        "[NumberSequence] Incorrect! Answer: $userAnswer, " +
                                            "Expected: ${currentSequence!!.correctAnswer}",
                                    )
                                }

                                // Generate next sequence immediately
                                currentSequence = generateNewSequence()
                                currentAnswer = ""

                                // Clear feedback after a short delay
                                coroutineScope.launch {
                                    delay(200L)
                                    lastAnswerCorrect = null
                                }
                            }
                        }
                    }

                    NumberSequenceScreen.Event.PlayAgain -> {
                        if (gameState is NumberSequenceScreen.GameState.Finished) {
                            // Reset to NotStarted so player can start again
                            gameState = NumberSequenceScreen.GameState.NotStarted
                            currentSequence = null
                            currentAnswer = ""
                            score = 0
                            timeRemaining = GAME_DURATION_SECONDS
                            totalAttempts = 0
                            correctAnswers = 0
                            lastAnswerCorrect = null
                            warningPlayed = false
                            Timber.d("[NumberSequence] Reset for new game")
                        }
                    }

                    NumberSequenceScreen.Event.NavigateHome -> {
                        navigator.pop()
                    }
                }
            }
        }
    }
