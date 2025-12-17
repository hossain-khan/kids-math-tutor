package dev.hossain.mathtutor.ui.mathpractice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.hossain.mathtutor.domain.generator.ProblemGenerator
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.domain.model.PracticeSession
import dev.hossain.mathtutor.domain.model.SessionAnswer
import dev.hossain.mathtutor.domain.repository.SessionRepository
import dev.hossain.mathtutor.ui.practiceresults.ResultsScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant

/**
 * Presenter for [MathPracticeScreen].
 *
 * Manages the state and business logic for the math practice session.
 */
class MathPracticePresenter
    @AssistedInject
    constructor(
        @Assisted private val screen: MathPracticeScreen,
        @Assisted private val navigator: Navigator,
        private val problemGenerator: ProblemGenerator,
        private val sessionRepository: SessionRepository,
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

            var problems by remember {
                mutableStateOf(
                    problemGenerator.generateProblems(
                        count = screen.problemCount,
                        operation = screen.operation,
                    ),
                )
            }
            var currentProblemIndex by remember { mutableStateOf(0) }
            var currentAnswer by remember { mutableStateOf("") }
            var isCorrect by remember { mutableStateOf<Boolean?>(null) }
            var userAnswers by remember { mutableStateOf<List<Int?>>(emptyList()) }

            val currentProblem = problems.getOrNull(currentProblemIndex)

            return MathPracticeScreen.State(
                currentProblem = currentProblem,
                currentAnswer = currentAnswer,
                currentProblemIndex = currentProblemIndex,
                totalProblems = problems.size,
                isCorrect = isCorrect,
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
                            isCorrect = userAnswer?.let { currentProblem.checkAnswer(it) }

                            // Store the user's answer
                            val updatedAnswers = userAnswers.toMutableList()
                            while (updatedAnswers.size <= currentProblemIndex) {
                                updatedAnswers.add(null)
                            }
                            updatedAnswers[currentProblemIndex] = userAnswer
                            userAnswers = updatedAnswers
                        }
                    }

                    is MathPracticeScreen.Event.NextProblem -> {
                        if (currentProblemIndex < problems.size - 1) {
                            currentProblemIndex++
                            currentAnswer = ""
                            isCorrect = null
                        } else {
                            // All problems completed, save session and navigate to results
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

                            // Save session to database asynchronously
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    Timber.d("Saving session to database...")
                                    sessionRepository.saveSession(
                                        session = practiceSession,
                                        operation = practiceSession.operation!!,
                                        durationSeconds = practiceSession.durationSeconds!!,
                                    )
                                    Timber.d("Session saved successfully")
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to save session")
                                }
                            }

                            // Navigate to results
                            navigator.goTo(
                                ResultsScreen(
                                    problems = problems,
                                    userAnswers = userAnswers,
                                ),
                            )
                        }
                    }

                    is MathPracticeScreen.Event.NavigateBack -> {
                        navigator.pop()
                    }
                }
            }
        }
    }
