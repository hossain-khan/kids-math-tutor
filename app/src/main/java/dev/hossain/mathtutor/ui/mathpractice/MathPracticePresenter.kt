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

            // Fetch user profile and generate problems in a single LaunchedEffect
            LaunchedEffect(Unit) {
                Timber.d("Starting problem generation for operation ${screen.operation}")
                val profile = userProfileRepository.getProfile().firstOrNull()
                val grade = profile?.gradeLevel ?: GradeLevel.GRADE_1
                isAdaptiveEnabled = profile?.adaptiveDifficultyEnabled ?: true
                currentGradeLevel = grade
                Timber.d(
                    "Fetched user grade: $grade (profile exists: ${profile != null}, adaptive: $isAdaptiveEnabled)",
                )

                if (isAdaptiveEnabled) {
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
                unlockedBadges = unlockedBadges,
                showBadgeUnlock = showBadgeUnlock,
                currentBadgeIndex = currentBadgeIndex,
                difficultyAdjustment = difficultyAdjustment,
                actualGradeLevel = actualGradeLevel,
                showDifficultyChangeNotice = showDifficultyChangeNotice,
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
                            isCorrect = userAnswer?.let { correct }

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
                                "Session stats: answered=${sessionAnswers.count { it.value.userAnswer != null }}/${problems.size}, " +
                                    "correct=$correctCount",
                            )

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

                                    // Update streak
                                    Timber.d("Updating streak...")
                                    val updatedStreak = updateStreakUseCase.updateStreak()
                                    Timber.d(
                                        "Streak updated: current=${updatedStreak.currentStreak}, longest=${updatedStreak.longestStreak}",
                                    )

                                    // Check for badge unlocks
                                    Timber.d("Checking for badge unlocks...")
                                    val newlyUnlocked = checkBadgeUnlocksUseCase.checkAndUnlockBadges()

                                    // Switch to Main dispatcher for state updates and navigation
                                    withContext(Dispatchers.Main) {
                                        if (newlyUnlocked.isNotEmpty()) {
                                            Timber.d("Unlocked ${newlyUnlocked.size} badges: ${newlyUnlocked.map { it.name }}")
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
