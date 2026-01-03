package dev.hossain.mathtutor.ui.mathrace

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope

/**
 * Main UI for [MathRaceScreen].
 *
 * Routes to the appropriate screen based on the current game state:
 * - NotStarted → Start screen with game description and start button
 * - Countdown → Animated 3-2-1-GO countdown
 * - Playing → Game screen with timer, problem, and number pad
 * - Finished → Results screen with score and stats
 */
@CircuitInject(MathRaceScreen::class, AppScope::class)
@Composable
fun MathRaceUi(
    state: MathRaceScreen.State,
    modifier: Modifier = Modifier,
) {
    when (val gameState = state.gameState) {
        is MathRaceScreen.GameState.NotStarted -> {
            MathRaceStartScreen(
                personalBest = state.personalBest,
                userName = state.userName,
                onStartGame = { state.eventSink(MathRaceScreen.Event.StartGame) },
                onNavigateHome = { state.eventSink(MathRaceScreen.Event.NavigateHome) },
                modifier = modifier,
            )
        }

        is MathRaceScreen.GameState.Countdown -> {
            CountdownScreen(
                countdownValue = gameState.countdownValue,
                modifier = modifier,
            )
        }

        is MathRaceScreen.GameState.Playing -> {
            MathRaceGameScreen(
                currentProblem = state.currentProblem,
                currentAnswer = state.currentAnswer,
                score = state.score,
                timeRemaining = state.timeRemaining,
                personalBest = state.personalBest,
                lastAnswerCorrect = state.lastAnswerCorrect,
                onNumberEntered = { digit -> state.eventSink(MathRaceScreen.Event.NumberEntered(digit)) },
                onBackspace = { state.eventSink(MathRaceScreen.Event.Backspace) },
                onCheckAnswer = { state.eventSink(MathRaceScreen.Event.CheckAnswer) },
                modifier = modifier,
            )
        }

        is MathRaceScreen.GameState.Finished -> {
            MathRaceResultsScreen(
                finalScore = gameState.finalScore,
                totalAttempts = gameState.totalAttempts,
                isNewRecord = gameState.isNewRecord,
                accuracy = gameState.accuracy,
                averageTimePerProblem = gameState.averageTimePerProblem,
                personalBest = state.personalBest,
                userName = state.userName,
                unlockedBadges = gameState.unlockedBadges,
                onPlayAgain = { state.eventSink(MathRaceScreen.Event.PlayAgain) },
                onNavigateHome = { state.eventSink(MathRaceScreen.Event.NavigateHome) },
                modifier = modifier,
            )
        }
    }
}

// Preview composables - Start Screen state
@Preview(
    showBackground = true,
    device = "spec:width=891dp,height=411dp,dpi=420,isRound=false,orientation=landscape",
    name = "Phone Landscape - Start",
)
@Composable
private fun MathRaceUiPhoneLandscapeStartPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState = MathRaceScreen.GameState.NotStarted,
                    currentProblem = null,
                    currentAnswer = "",
                    score = 0,
                    timeRemaining = 60,
                    personalBest = 15,
                    totalAttempts = 0,
                    correctAnswers = 0,
                    lastAnswerCorrect = null,
                    userName = "Alex",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240,isRound=false,orientation=portrait",
    name = "Tablet Portrait - Start",
)
@Composable
private fun MathRaceUiTabletPortraitStartPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState = MathRaceScreen.GameState.NotStarted,
                    currentProblem = null,
                    currentAnswer = "",
                    score = 0,
                    timeRemaining = 60,
                    personalBest = 20,
                    totalAttempts = 0,
                    correctAnswers = 0,
                    lastAnswerCorrect = null,
                    userName = "Jordan",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,isRound=false,orientation=landscape",
    name = "Tablet Landscape - Start",
)
@Composable
private fun MathRaceUiTabletLandscapeStartPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState = MathRaceScreen.GameState.NotStarted,
                    currentProblem = null,
                    currentAnswer = "",
                    score = 0,
                    timeRemaining = 60,
                    personalBest = 25,
                    totalAttempts = 0,
                    correctAnswers = 0,
                    lastAnswerCorrect = null,
                    userName = "Sam",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=673dp,height=841dp,dpi=373,isRound=false,orientation=portrait",
    name = "Foldable Portrait (Pixel Fold Unfolded) - Start",
)
@Composable
private fun MathRaceUiFoldablePortraitStartPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState = MathRaceScreen.GameState.NotStarted,
                    currentProblem = null,
                    currentAnswer = "",
                    score = 0,
                    timeRemaining = 60,
                    personalBest = 18,
                    totalAttempts = 0,
                    correctAnswers = 0,
                    lastAnswerCorrect = null,
                    userName = "Chris",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=841dp,height=673dp,dpi=373,isRound=false,orientation=landscape",
    name = "Foldable Landscape (Pixel Fold Unfolded) - Start",
)
@Composable
private fun MathRaceUiFoldableLandscapeStartPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState = MathRaceScreen.GameState.NotStarted,
                    currentProblem = null,
                    currentAnswer = "",
                    score = 0,
                    timeRemaining = 60,
                    personalBest = 22,
                    totalAttempts = 0,
                    correctAnswers = 0,
                    lastAnswerCorrect = null,
                    userName = "Taylor",
                    eventSink = {},
                ),
        )
    }
}

// Preview composables - Playing state
@Preview(
    showBackground = true,
    device = "spec:width=891dp,height=411dp,dpi=420,isRound=false,orientation=landscape",
    name = "Phone Landscape - Playing",
)
@Composable
private fun MathRaceUiPhoneLandscapePlayingPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState = MathRaceScreen.GameState.Playing,
                    currentProblem =
                        MathProblem(
                            num1 = 7,
                            num2 = 5,
                            operation = MathOperation.ADDITION,
                            correctAnswer = 12,
                        ),
                    currentAnswer = "1",
                    score = 8,
                    timeRemaining = 35,
                    personalBest = 15,
                    totalAttempts = 9,
                    correctAnswers = 8,
                    lastAnswerCorrect = true,
                    userName = "Alex",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240,isRound=false,orientation=portrait",
    name = "Tablet Portrait - Playing",
)
@Composable
private fun MathRaceUiTabletPortraitPlayingPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState = MathRaceScreen.GameState.Playing,
                    currentProblem =
                        MathProblem(
                            num1 = 9,
                            num2 = 3,
                            operation = MathOperation.SUBTRACTION,
                            correctAnswer = 6,
                        ),
                    currentAnswer = "",
                    score = 12,
                    timeRemaining = 42,
                    personalBest = 20,
                    totalAttempts = 13,
                    correctAnswers = 12,
                    lastAnswerCorrect = null,
                    userName = "Jordan",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,isRound=false,orientation=landscape",
    name = "Tablet Landscape - Playing",
)
@Composable
private fun MathRaceUiTabletLandscapePlayingPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState = MathRaceScreen.GameState.Playing,
                    currentProblem =
                        MathProblem(
                            num1 = 6,
                            num2 = 4,
                            operation = MathOperation.MULTIPLICATION,
                            correctAnswer = 24,
                        ),
                    currentAnswer = "24",
                    score = 18,
                    timeRemaining = 15,
                    personalBest = 25,
                    totalAttempts = 20,
                    correctAnswers = 18,
                    lastAnswerCorrect = true,
                    userName = "Sam",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=673dp,height=841dp,dpi=373,isRound=false,orientation=portrait",
    name = "Foldable Portrait (Pixel Fold Unfolded) - Playing",
)
@Composable
private fun MathRaceUiFoldablePortraitPlayingPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState = MathRaceScreen.GameState.Playing,
                    currentProblem =
                        MathProblem(
                            num1 = 8,
                            num2 = 6,
                            operation = MathOperation.ADDITION,
                            correctAnswer = 14,
                        ),
                    currentAnswer = "14",
                    score = 10,
                    timeRemaining = 28,
                    personalBest = 18,
                    totalAttempts = 11,
                    correctAnswers = 10,
                    lastAnswerCorrect = true,
                    userName = "Chris",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=841dp,height=673dp,dpi=373,isRound=false,orientation=landscape",
    name = "Foldable Landscape (Pixel Fold Unfolded) - Playing",
)
@Composable
private fun MathRaceUiFoldableLandscapePlayingPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState = MathRaceScreen.GameState.Playing,
                    currentProblem =
                        MathProblem(
                            num1 = 5,
                            num2 = 9,
                            operation = MathOperation.ADDITION,
                            correctAnswer = 14,
                        ),
                    currentAnswer = "",
                    score = 15,
                    timeRemaining = 8,
                    personalBest = 22,
                    totalAttempts = 17,
                    correctAnswers = 15,
                    lastAnswerCorrect = false,
                    userName = "Taylor",
                    eventSink = {},
                ),
        )
    }
}

// Preview composables - Results state
@Preview(
    showBackground = true,
    device = "spec:width=891dp,height=411dp,dpi=420,isRound=false,orientation=landscape",
    name = "Phone Landscape - Results",
)
@Composable
private fun MathRaceUiPhoneLandscapeResultsPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState =
                        MathRaceScreen.GameState.Finished(
                            finalScore = 18,
                            totalAttempts = 20,
                            isNewRecord = true,
                            accuracy = 90f,
                            averageTimePerProblem = 3.0f,
                        ),
                    currentProblem = null,
                    currentAnswer = "",
                    score = 18,
                    timeRemaining = 0,
                    personalBest = 18,
                    totalAttempts = 20,
                    correctAnswers = 18,
                    lastAnswerCorrect = null,
                    userName = "Alex",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240,isRound=false,orientation=portrait",
    name = "Tablet Portrait - Results",
)
@Composable
private fun MathRaceUiTabletPortraitResultsPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState =
                        MathRaceScreen.GameState.Finished(
                            finalScore = 22,
                            totalAttempts = 25,
                            isNewRecord = true,
                            accuracy = 88f,
                            averageTimePerProblem = 2.4f,
                        ),
                    currentProblem = null,
                    currentAnswer = "",
                    score = 22,
                    timeRemaining = 0,
                    personalBest = 22,
                    totalAttempts = 25,
                    correctAnswers = 22,
                    lastAnswerCorrect = null,
                    userName = "Jordan",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,isRound=false,orientation=landscape",
    name = "Tablet Landscape - Results",
)
@Composable
private fun MathRaceUiTabletLandscapeResultsPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState =
                        MathRaceScreen.GameState.Finished(
                            finalScore = 15,
                            totalAttempts = 18,
                            isNewRecord = false,
                            accuracy = 83.3f,
                            averageTimePerProblem = 3.3f,
                        ),
                    currentProblem = null,
                    currentAnswer = "",
                    score = 15,
                    timeRemaining = 0,
                    personalBest = 25,
                    totalAttempts = 18,
                    correctAnswers = 15,
                    lastAnswerCorrect = null,
                    userName = "Sam",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=673dp,height=841dp,dpi=373,isRound=false,orientation=portrait",
    name = "Foldable Portrait (Pixel Fold Unfolded) - Results",
)
@Composable
private fun MathRaceUiFoldablePortraitResultsPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState =
                        MathRaceScreen.GameState.Finished(
                            finalScore = 20,
                            totalAttempts = 22,
                            isNewRecord = true,
                            accuracy = 90.9f,
                            averageTimePerProblem = 2.7f,
                        ),
                    currentProblem = null,
                    currentAnswer = "",
                    score = 20,
                    timeRemaining = 0,
                    personalBest = 20,
                    totalAttempts = 22,
                    correctAnswers = 20,
                    lastAnswerCorrect = null,
                    userName = "Chris",
                    eventSink = {},
                ),
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=841dp,height=673dp,dpi=373,isRound=false,orientation=landscape",
    name = "Foldable Landscape (Pixel Fold Unfolded) - Results",
)
@Composable
private fun MathRaceUiFoldableLandscapeResultsPreview() {
    KidsMathTutorAppTheme {
        MathRaceUi(
            state =
                MathRaceScreen.State(
                    gameState =
                        MathRaceScreen.GameState.Finished(
                            finalScore = 24,
                            totalAttempts = 26,
                            isNewRecord = true,
                            accuracy = 92.3f,
                            averageTimePerProblem = 2.3f,
                        ),
                    currentProblem = null,
                    currentAnswer = "",
                    score = 24,
                    timeRemaining = 0,
                    personalBest = 24,
                    totalAttempts = 26,
                    correctAnswers = 24,
                    lastAnswerCorrect = null,
                    userName = "Taylor",
                    eventSink = {},
                ),
        )
    }
}
