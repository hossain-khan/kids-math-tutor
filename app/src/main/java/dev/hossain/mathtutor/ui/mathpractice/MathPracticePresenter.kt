package dev.hossain.mathtutor.ui.mathpractice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import dev.hossain.mathtutor.domain.generator.AdaptiveProblemGenerator
import dev.hossain.mathtutor.domain.generator.ProblemGenerator
import dev.hossain.mathtutor.domain.model.Badge
import dev.hossain.mathtutor.domain.model.DifficultyAdjustment
import dev.hossain.mathtutor.domain.model.GradeLevel
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionAnswer
import dev.hossain.mathtutor.domain.repository.PerformanceRepository
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.domain.repository.UserProfileRepository
import dev.hossain.mathtutor.domain.usecase.CheckBadgeUnlocksUseCase
import dev.hossain.mathtutor.domain.usecase.UpdateStreakUseCase
import dev.hossain.mathtutor.haptic.HapticService
import dev.hossain.mathtutor.ui.practiceresults.ResultsScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant

/**
 * Presenter for [MathPracticeScreen].
 *
 * Manages the state and business logic for the math practice session.
 * Supports adaptive difficulty when enabled in user profile, which adjusts
 * problem difficulty based on recent performance.
 */
@AssistedInject
class MathPracticePresenter
    constructor(
        @Assisted private val screen: MathPracticeScreen,
        @Assisted private val navigator: Navigator,
        private val problemGenerator: ProblemGenerator,
        private val adaptiveProblemGenerator: AdaptiveProblemGenerator,
        private val sessionRepository: SessionRepository,
        private val userProfileRepository: UserProfileRepository,
        private val performanceRepository: PerformanceRepository,
        private val checkBadgeUnlocksUseCase: CheckBadgeUnlocksUseCase,
        private val updateStreakUseCase: UpdateStreakUseCase,
        private val audioService: AudioService,
        private val hapticService: HapticService,
        private val analyticsService: AnalyticsService,
        private val customChallengeService: dev.hossain.mathtutor.domain.service.CustomChallengeService,
    ) : Presenter<MathPracticeScreen.State> {
        @CircuitInject(MathPracticeScreen::class, AppScope::class)
        @AssistedFactory
        interface Factory {
            fun create(
                screen: MathPracticeScreen,
                navigator: Navigator,
            ): MathPracticePresenter
        }

        @Composable
        override fun present(): MathPracticeScreen.State {
            // Track screen view
            LaunchedImpressionEffect {
                analyticsService.logScreenView(
                    screenName = "Math Practice",
                    screenClass = MathPracticeScreen::class.java.name,
                    parameters =
                        mapOf(
                            AnalyticsParam.OPERATION_TYPE to screen.operation.name.lowercase(),
                            AnalyticsParam.PROBLEM_COUNT to screen.problemCount,
                        ),
                )
            }

            // Track session start time
            val sessionStartTime = remember { Instant.now() }
            // Use lifecycle-aware coroutine scope
            val coroutineScope = rememberCoroutineScope()

            var problems by remember { mutableStateOf<List<MathProblem>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var currentProblemIndex by remember { mutableStateOf(0) }
            var currentAnswer by remember { mutableStateOf("") }
            var isCorrect by remember { mutableStateOf<Boolean?>(null) }
            var userAnswers by remember { mutableStateOf<List<Int?>>(emptyList()) }
            var unlockedBadges by remember { mutableStateOf<List<Badge>>(emptyList()) }
            var showBadgeUnlock by remember { mutableStateOf(false) }
            var currentBadgeIndex by remember { mutableStateOf(0) }
            var difficultyAdjustment by remember { mutableStateOf<DifficultyAdjustment?>(null) }
            var actualGradeLevel by remember { mutableStateOf<GradeLevel?>(null) }
            var showDifficultyChangeNotice by remember { mutableStateOf(false) }
            var problemStartTime by remember { mutableStateOf(Instant.now()) }
            var currentGradeLevel by remember { mutableStateOf<GradeLevel?>(null) }
            var isAdaptiveEnabled by remember { mutableStateOf(false) }
            var userName by remember { mutableStateOf<String?>(null) }
            var customChallengeTitle by remember { mutableStateOf<String?>(null) }

            // Fetch user profile and generate problems in a single LaunchedEffect
            LaunchedEffect(Unit) {
                Timber.d("Starting problem generation for operation ${screen.operation}")
                val profile = userProfileRepository.getProfile().firstOrNull()
                val grade = profile?.gradeLevel ?: GradeLevel.GRADE_1
                isAdaptiveEnabled = profile?.adaptiveDifficultyEnabled ?: true
                currentGradeLevel = grade
                userName = profile?.name
                Timber.d(
                    "Fetched user grade: $grade (profile exists: ${profile != null}, adaptive: $isAdaptiveEnabled, name: $userName)",
                )

                // Check if this is a custom challenge
                if (screen.customChallengeId != null) {
                    Timber.d("Loading custom challenge: ${screen.customChallengeId}")
                    val challenge = customChallengeService.getChallengeById(screen.customChallengeId)
                    if (challenge != null) {
                        problems = challenge.problems
                        customChallengeTitle = challenge.title
                        actualGradeLevel = grade // Use user's grade for custom challenges
                        Timber.d(
                            "Loaded ${problems.size} problems from custom challenge '${challenge.title}'",
                        )
                    } else {
                        Timber.e("Custom challenge not found: ${screen.customChallengeId}")
                        // Fall back to regular problem generation
                        problems =
                            problemGenerator.generateProblems(
                                count = screen.problemCount,
                                operation = screen.operation,
                                gradeLevel = grade,
                            )
                        actualGradeLevel = grade
                    }
                } else if (isAdaptiveEnabled) {
                    // Use adaptive problem generator
                    val result =
                        adaptiveProblemGenerator.generateAdaptiveProblems(
                            count = screen.problemCount,
                            operation = screen.operation,
                            baseGradeLevel = grade,
                        )
                    problems = result.problems
                    actualGradeLevel = result.actualGradeLevel
                    difficultyAdjustment = result.adjustment
                    if (result.wasAdjusted) {
                        showDifficultyChangeNotice = true
                        // Play level up audio and haptic if difficulty was increased
                        if (result.wasIncreased) {
                            audioService.playLevelUp()
                            hapticService.triggerSuccess()
                            Timber.d(
                                "[MathPractice] Difficulty increased from $grade to ${result.actualGradeLevel} - " +
                                    "played level up audio and haptic feedback",
                            )
                        }
                        Timber.d(
                            "Difficulty adjusted from $grade to ${result.actualGradeLevel} " +
                                "(adjustment: ${result.adjustment})",
                        )
                    }
                } else {
                    // Use standard problem generator
                    problems =
                        problemGenerator.generateProblems(
                            count = screen.problemCount,
                            operation = screen.operation,
                            gradeLevel = grade,
                        )
                    actualGradeLevel = grade
                }
                Timber.d(
                    "Generated ${problems.size} problems for grade $actualGradeLevel " +
                        "and operation ${screen.operation}",
                )
                problemStartTime = Instant.now()
                isLoading = false
            }

            val currentProblem = problems.getOrNull(currentProblemIndex)

            return MathPracticeScreen.State(
                currentProblem = currentProblem,
                currentAnswer = currentAnswer,
                currentProblemIndex = currentProblemIndex,
                totalProblems = problems.size,
                isCorrect = isCorrect,
                isLoading = isLoading,
                userName = userName,
                unlockedBadges = unlockedBadges,
                showBadgeUnlock = showBadgeUnlock,
                currentBadgeIndex = currentBadgeIndex,
                difficultyAdjustment = difficultyAdjustment,
                actualGradeLevel = actualGradeLevel,
                showDifficultyChangeNotice = showDifficultyChangeNotice,
                customChallengeTitle = customChallengeTitle,
            ) { event ->
                when (event) {
                    is MathPracticeScreen.Event.NumberClicked -> {
                        // Append number to current answer
                        currentAnswer += event.number.toString()
                    }

                    is MathPracticeScreen.Event.ClearAnswer -> {
                        currentAnswer = ""
                        isCorrect = null
                    }

                    is MathPracticeScreen.Event.CheckAnswer -> {
                        if (currentProblem != null) {
                            val userAnswer = currentAnswer.toIntOrNull()
                            val correct = userAnswer?.let { currentProblem.checkAnswer(it) } ?: false
                            isCorrect = if (userAnswer != null) correct else null

                            // Play audio and haptic feedback based on correctness
                            if (userAnswer != null) {
                                if (correct) {
                                    audioService.playSuccess()
                                    hapticService.triggerSuccess()
                                    Timber.d(
                                        "[MathPractice] Correct answer for problem ${currentProblem.id} - " +
                                            "played success audio and haptic feedback",
                                    )
                                } else {
                                    audioService.playError()
                                    hapticService.triggerError()
                                    Timber.d(
                                        "[MathPractice] Incorrect answer for problem ${currentProblem.id} - " +
                                            "played error audio and haptic feedback",
                                    )
                                }
                            }

                            // Store the user's answer
                            val updatedAnswers = userAnswers.toMutableList()
                            while (updatedAnswers.size <= currentProblemIndex) {
                                updatedAnswers.add(null)
                            }
                            updatedAnswers[currentProblemIndex] = userAnswer
                            userAnswers = updatedAnswers

                            // Record performance for adaptive difficulty (if enabled)
                            if (isAdaptiveEnabled && currentGradeLevel != null && userAnswer != null) {
                                val timeSpent =
                                    java.time.Duration
                                        .between(problemStartTime, Instant.now())
                                        .seconds
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        performanceRepository.recordPerformance(
                                            operation = screen.operation,
                                            gradeLevel = currentGradeLevel!!,
                                            problemId = currentProblem.id,
                                            isCorrect = correct,
                                            timeSpentSeconds = timeSpent,
                                        )
                                        Timber.d(
                                            "Recorded performance: problem=${currentProblem.id}, " +
                                                "correct=$correct, time=${timeSpent}s",
                                        )
                                    } catch (e: Exception) {
                                        Timber.e(e, "Failed to record performance")
                                    }
                                }
                            }
                        }
                    }

                    is MathPracticeScreen.Event.NextProblem -> {
                        if (currentProblemIndex < problems.size - 1) {
                            currentProblemIndex++
                            currentAnswer = ""
                            isCorrect = null
                            problemStartTime = Instant.now() // Reset timer for next problem
                        } else {
                            // All problems completed, save session and check for badges/streak
                            val sessionEndTime = Instant.now()
                            val durationSeconds =
                                java.time.Duration
                                    .between(sessionStartTime, sessionEndTime)
                                    .seconds

                            Timber.d("Session completed: duration=${durationSeconds}s, operation=${screen.operation}")

                            // Create PracticeSession with answers for ALL problems (including unanswered)
                            val sessionAnswers = mutableMapOf<String, SessionAnswer>()
                            problems.forEachIndexed { index, problem ->
                                val userAnswer = userAnswers.getOrNull(index)
                                // Save all problems, including unanswered ones
                                sessionAnswers[problem.id] =
                                    SessionAnswer(
                                        problemId = problem.id,
                                        userAnswer = userAnswer,
                                        isCorrect =
                                            userAnswer?.let { answer ->
                                                problem.checkAnswer(answer)
                                            } ?: false,
                                    )
                            }

                            val correctCount = sessionAnswers.values.count { it.isCorrect }
                            Timber.d(
                                "[MathPractice] Session stats: answered=${sessionAnswers.count {
                                    it.value.userAnswer != null
                                }}/${problems.size}, " +
                                    "correct=$correctCount",
                            )

                            // Check for perfect score
                            val isPerfectScore = correctCount == problems.size && problems.isNotEmpty()
                            if (isPerfectScore) {
                                audioService.playPerfectScore()
                                Timber.d(
                                    "[MathPractice] Perfect score achieved ($correctCount/${problems.size}) - played perfect score audio",
                                )
                            }

                            val practiceSession =
                                PracticeSession(
                                    totalProblems = problems.size,
                                    problems = problems,
                                    answers = sessionAnswers,
                                    operation = screen.operation,
                                    durationSeconds = durationSeconds,
                                    completedAt = sessionEndTime,
                                )

                            // Save session, update streak, and check badges asynchronously
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    Timber.d("Saving session to database...")
                                    sessionRepository.saveSession(
                                        session = practiceSession,
                                        operation = practiceSession.operation!!,
                                        durationSeconds = practiceSession.durationSeconds!!,
                                    )
                                    Timber.d("Session saved successfully")

                                    // Record custom challenge practice session if this is a custom challenge
                                    if (screen.customChallengeId != null) {
                                        try {
                                            val challengeSession =
                                                dev.hossain.mathtutor.domain.model.ChallengePracticeSession(
                                                    startTime = sessionStartTime,
                                                    endTime = sessionEndTime,
                                                    problemsAttempted = problems.size,
                                                    correctAnswers = correctCount,
                                                    totalTimeMs =
                                                        java.time.Duration
                                                            .between(sessionStartTime, sessionEndTime)
                                                            .toMillis(),
                                                )
                                            customChallengeService.recordPracticeSession(
                                                screen.customChallengeId,
                                                challengeSession,
                                            )
                                            Timber.d(
                                                "[MathPractice] Recorded custom challenge session: challengeId=${screen.customChallengeId}",
                                            )
                                        } catch (e: Exception) {
                                            Timber.e(e, "Failed to record custom challenge session")
                                        }
                                    }

                                    // Update streak
                                    Timber.d("[MathPractice] Updating streak...")
                                    val previousStreak = updateStreakUseCase.getCurrentStreak()
                                    val updatedStreak = updateStreakUseCase.updateStreak()
                                    Timber.d(
                                        "[MathPractice] Streak updated: current=${updatedStreak.currentStreak}, " +
                                            "longest=${updatedStreak.longestStreak}",
                                    )

                                    // Check for badge unlocks
                                    Timber.d("[MathPractice] Checking for badge unlocks...")
                                    val newlyUnlocked = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

                                    // Switch to Main dispatcher for audio/haptic feedback and state updates
                                    withContext(Dispatchers.Main) {
                                        // Play streak continue audio if streak was maintained or increased
                                        if (updatedStreak.currentStreak > 0 &&
                                            updatedStreak.currentStreak >= previousStreak.currentStreak
                                        ) {
                                            audioService.playStreakContinue()
                                            Timber.d(
                                                "[MathPractice] Streak continued (${updatedStreak.currentStreak} days) - " +
                                                    "played streak continue audio",
                                            )
                                        }

                                        // Play badge unlock audio and haptic if badges were unlocked
                                        if (newlyUnlocked.isNotEmpty()) {
                                            audioService.playBadgeUnlock()
                                            hapticService.triggerBadgeUnlock()
                                            Timber.d(
                                                "[MathPractice] ${newlyUnlocked.size} badge(s) unlocked - " +
                                                    "played badge unlock audio and haptic feedback",
                                            )
                                        }

                                        // Update state and navigation
                                        if (newlyUnlocked.isNotEmpty()) {
                                            Timber.d(
                                                "[MathPractice] Showing badge dialog for: ${newlyUnlocked.map { it.name }}",
                                            )
                                            unlockedBadges = newlyUnlocked
                                            showBadgeUnlock = true
                                            currentBadgeIndex = 0
                                        } else {
                                            Timber.d("No new badges unlocked")
                                            // Navigate to results immediately if no badges
                                            navigator.goTo(
                                                ResultsScreen(
                                                    problems = problems,
                                                    userAnswers = userAnswers,
                                                    badgesAlreadyChecked = true,
                                                    customChallengeId = screen.customChallengeId,
                                                    customChallengeTitle = customChallengeTitle,
                                                ),
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to save session or check achievements")
                                    // Navigate to results even on error - use Main dispatcher
                                    withContext(Dispatchers.Main) {
                                        navigator.goTo(
                                            ResultsScreen(
                                                problems = problems,
                                                userAnswers = userAnswers,
                                                badgesAlreadyChecked = true,
                                                customChallengeId = screen.customChallengeId,
                                                customChallengeTitle = customChallengeTitle,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is MathPracticeScreen.Event.NavigateBack -> {
                        navigator.pop()
                    }

                    is MathPracticeScreen.Event.DismissBadgeDialog -> {
                        // Check if there are more badges to show
                        if (currentBadgeIndex < unlockedBadges.size - 1) {
                            currentBadgeIndex++
                        } else {
                            // All badges shown, hide dialog and navigate to results
                            showBadgeUnlock = false
                            navigator.goTo(
                                ResultsScreen(
                                    problems = problems,
                                    userAnswers = userAnswers,
                                    badgesAlreadyChecked = true,
                                    customChallengeId = screen.customChallengeId,
                                    customChallengeTitle = customChallengeTitle,
                                ),
                            )
                        }
                    }

                    is MathPracticeScreen.Event.DismissDifficultyNotice -> {
                        showDifficultyChangeNotice = false
                    }
                }
            }
        }
    }
