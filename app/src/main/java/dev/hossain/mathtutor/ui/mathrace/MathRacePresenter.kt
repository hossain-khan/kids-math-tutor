package dev.hossain.mathtutor.ui.mathrace

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
import dev.hossain.mathtutor.analytics.AnalyticsEvent
import dev.hossain.mathtutor.analytics.AnalyticsParam
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
 * Presenter for [MathRaceScreen].
 *
 * Manages the state and game logic for the Math Race mini-game.
 * This includes:
 * - 3-2-1 countdown before game starts
 * - 60-second timer with 10-second warning
 * - Problem generation and answer checking
 * - Score tracking and personal best detection
 * - Saving game session to database
 */
@AssistedInject
class MathRacePresenter
    constructor(
        @Assisted private val screen: MathRaceScreen,
        @Assisted private val navigator: Navigator,
        private val problemGenerator: ProblemGenerator,
        private val gameRepository: GameRepository,
        private val userProfileRepository: UserProfileRepository,
        private val checkBadgeUnlocksUseCase: CheckBadgeUnlocksUseCase,
        private val audioService: AudioService,
        private val hapticService: HapticService,
        private val analyticsService: AnalyticsService,
    ) : Presenter<MathRaceScreen.State> {
        @CircuitInject(MathRaceScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(
                screen: MathRaceScreen,
                navigator: Navigator,
            ): MathRacePresenter
        }

        companion object {
            /** Duration of the game in seconds */
            private const val GAME_DURATION_SECONDS = 60

            /** Seconds remaining when warning sound plays */
            private const val WARNING_THRESHOLD_SECONDS = 10

            /** Maximum digits allowed in answer */
            private const val MAX_ANSWER_DIGITS = 4

            /** Countdown start value (3-2-1-GO) */
            private const val COUNTDOWN_START = 3
        }

        @Composable
        override fun present(): MathRaceScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Math Race",
                    screenClass = MathRaceScreen::class.java.name,
                )
            }

            val coroutineScope = rememberCoroutineScope()

            // Game state
            var gameState by remember {
                mutableStateOf<MathRaceScreen.GameState>(MathRaceScreen.GameState.NotStarted)
            }
            var currentProblem by remember { mutableStateOf<MathProblem?>(null) }
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
                Timber.d("[MathRace] Loading user profile and personal best...")
                val profile = userProfileRepository.getProfile().firstOrNull()
                gradeLevel = profile?.gradeLevel ?: GradeLevel.GRADE_1
                userName = profile?.name
                Timber.d("[MathRace] User profile loaded: name=$userName, gradeLevel=$gradeLevel")

                // Load personal best for Math Race
                gameRepository.getPersonalBest(Game.MATH_RACE).collect { best ->
                    personalBest = best
                    Timber.d("[MathRace] Personal best loaded: $best")
                }
            }

            // Timer countdown effect
            LaunchedEffect(gameState) {
                if (gameState == MathRaceScreen.GameState.Playing) {
                    Timber.d("[MathRace] Timer started: $timeRemaining seconds")
                    while (timeRemaining > 0) {
                        delay(1000L)
                        timeRemaining--

                        // Play warning at 10 seconds
                        if (timeRemaining == WARNING_THRESHOLD_SECONDS && !warningPlayed) {
                            audioService.playWarning()
                            hapticService.triggerLongPress()
                            warningPlayed = true
                            Timber.d("[MathRace] Warning sound played at $timeRemaining seconds")
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
                        "[MathRace] Game ended - Score: $score, Attempts: $totalAttempts, " +
                            "Accuracy: $accuracy%, New record: $isNewRecord",
                    )

                    // Save game session and check for badge unlocks
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val session =
                                GameSession(
                                    game = Game.MATH_RACE,
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
                            Timber.d("[MathRace] Game session saved successfully")

                            // Check for badge unlocks after saving the session
                            val newBadges = checkBadgeUnlocksUseCase.checkAndUnlockBadges()
                            Timber.d("[MathRace] Badge check complete. Unlocked: ${newBadges.size} badges")

                            withContext(Dispatchers.Main) {
                                unlockedBadges = newBadges

                                // Play audio based on result
                                if (newBadges.isNotEmpty()) {
                                    audioService.playBadgeUnlock()
                                    hapticService.triggerBadgeUnlock()
                                    Timber.d("[MathRace] Badge(s) unlocked! Playing badge unlock audio")
                                } else if (isNewRecord && score > 0) {
                                    audioService.playPerfectScore()
                                    hapticService.triggerBadgeUnlock()
                                    Timber.d("[MathRace] New record achieved! Playing celebration audio")
                                }

                                // Update game state to Finished with unlocked badges
                                gameState =
                                    MathRaceScreen.GameState.Finished(
                                        finalScore = score,
                                        totalAttempts = totalAttempts,
                                        isNewRecord = isNewRecord,
                                        accuracy = accuracy,
                                        averageTimePerProblem = avgTime,
                                        unlockedBadges = newBadges,
                                    )
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "[MathRace] Failed to save game session")
                            // Still update game state even if save failed
                            withContext(Dispatchers.Main) {
                                gameState =
                                    MathRaceScreen.GameState.Finished(
                                        finalScore = score,
                                        totalAttempts = totalAttempts,
                                        isNewRecord = isNewRecord,
                                        accuracy = accuracy,
                                        averageTimePerProblem = avgTime,
                                    )
                            }
                        }
                    }
                }
            }

            /**
             * Generates a new math problem appropriate for the grade level.
             */
            fun generateNewProblem(): MathProblem {
                val problems =
                    problemGenerator.generateProblems(
                        count = 1,
                        operation = MathOperation.MIXED,
                        gradeLevel = gradeLevel,
                    )
                return problems.first()
            }

            /**
             * Starts the countdown sequence (3-2-1-GO).
             */
            suspend fun startCountdown() {
                Timber.d("[MathRace] Starting countdown...")

                // Reset game state
                score = 0
                timeRemaining = GAME_DURATION_SECONDS
                totalAttempts = 0
                correctAnswers = 0
                currentAnswer = ""
                lastAnswerCorrect = null
                warningPlayed = false

                // Countdown 3-2-1
                for (count in COUNTDOWN_START downTo 1) {
                    gameState = MathRaceScreen.GameState.Countdown(count)
                    audioService.playCountdown()
                    hapticService.triggerButtonClick()
                    Timber.d("[MathRace] Countdown: $count")
                    delay(1000L)
                }

                // GO!
                gameState = MathRaceScreen.GameState.Countdown(0) // 0 represents "GO"
                audioService.playGo()
                hapticService.triggerSuccess()
                Timber.d("[MathRace] GO!")
                delay(500L) // Brief pause to see "GO!"

                // Start game
                gameStartTime = Instant.now()
                currentProblem = generateNewProblem()
                gameState = MathRaceScreen.GameState.Playing
                Timber.d("[MathRace] Game started! First problem: ${currentProblem?.getDisplayString()}")
            }

            return MathRaceScreen.State(
                gameState = gameState,
                currentProblem = currentProblem,
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
                    MathRaceScreen.Event.StartGame -> {
                        if (gameState == MathRaceScreen.GameState.NotStarted ||
                            gameState is MathRaceScreen.GameState.Finished
                        ) {
                            coroutineScope.launch {
                                startCountdown()
                            }
                        }
                    }

                    is MathRaceScreen.Event.NumberEntered -> {
                        if (gameState == MathRaceScreen.GameState.Playing) {
                            // Limit answer to MAX_ANSWER_DIGITS digits
                            if (currentAnswer.length < MAX_ANSWER_DIGITS) {
                                currentAnswer += event.digit.toString()
                                hapticService.triggerButtonClick()
                            }
                        }
                    }

                    MathRaceScreen.Event.Backspace -> {
                        if (gameState == MathRaceScreen.GameState.Playing && currentAnswer.isNotEmpty()) {
                            currentAnswer = currentAnswer.dropLast(1)
                            hapticService.triggerButtonClick()
                        }
                    }

                    MathRaceScreen.Event.CheckAnswer -> {
                        if (gameState == MathRaceScreen.GameState.Playing && currentProblem != null) {
                            val userAnswer = currentAnswer.toIntOrNull()
                            if (userAnswer != null) {
                                totalAttempts++
                                val isCorrect = currentProblem!!.checkAnswer(userAnswer)
                                lastAnswerCorrect = isCorrect

                                if (isCorrect) {
                                    score++
                                    correctAnswers++
                                    audioService.playSuccess()
                                    hapticService.triggerSuccess()
                                    Timber.d("[MathRace] Correct! Score: $score")
                                } else {
                                    audioService.playError()
                                    hapticService.triggerError()
                                    Timber.d(
                                        "[MathRace] Incorrect! Answer: $userAnswer, " +
                                            "Expected: ${currentProblem!!.correctAnswer}",
                                    )
                                }

                                // Generate next problem immediately
                                currentProblem = generateNewProblem()
                                currentAnswer = ""

                                // Clear feedback after a short delay
                                coroutineScope.launch {
                                    delay(200L)
                                    lastAnswerCorrect = null
                                }
                            }
                        }
                    }

                    MathRaceScreen.Event.PlayAgain -> {
                        if (gameState is MathRaceScreen.GameState.Finished) {
                            // Reset to NotStarted so player can start again
                            gameState = MathRaceScreen.GameState.NotStarted
                            currentProblem = null
                            currentAnswer = ""
                            score = 0
                            timeRemaining = GAME_DURATION_SECONDS
                            totalAttempts = 0
                            correctAnswers = 0
                            lastAnswerCorrect = null
                            warningPlayed = false
                            Timber.d("[MathRace] Reset for new game")
                        }
                    }

                    MathRaceScreen.Event.NavigateHome -> {
                        navigator.pop()
                    }
                }
            }
        }
    }
