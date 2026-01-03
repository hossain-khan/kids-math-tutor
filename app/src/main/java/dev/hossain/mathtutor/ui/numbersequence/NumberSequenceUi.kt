package dev.hossain.mathtutor.ui.numbersequence

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.generator.SequenceQuestion
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope

/**
 * Main UI for [NumberSequenceScreen].
 *
 * Routes to the appropriate screen based on the current game state:
 * - NotStarted → Start screen with game description and start button
 * - Countdown → Animated 3-2-1-GO countdown
 * - Playing → Game screen with timer, sequence, and number pad
 * - Finished → Results screen with score and stats
 */
@CircuitInject(NumberSequenceScreen::class, AppScope::class)
@Composable
fun NumberSequenceUi(
    state: NumberSequenceScreen.State,
    modifier: Modifier = Modifier,
) {
    when (val gameState = state.gameState) {
        is NumberSequenceScreen.GameState.NotStarted -> {
            NumberSequenceStartScreen(
                personalBest = state.personalBest,
                userName = state.userName,
                onStartGame = { state.eventSink(NumberSequenceScreen.Event.StartGame) },
                onNavigateHome = { state.eventSink(NumberSequenceScreen.Event.NavigateHome) },
                modifier = modifier,
            )
        }

        is NumberSequenceScreen.GameState.Countdown -> {
            CountdownScreen(
                countdownValue = gameState.countdownValue,
                modifier = modifier,
            )
        }

        is NumberSequenceScreen.GameState.Playing -> {
            NumberSequenceGameScreen(
                currentSequence = state.currentSequence,
                currentAnswer = state.currentAnswer,
                score = state.score,
                timeRemaining = state.timeRemaining,
                personalBest = state.personalBest,
                lastAnswerCorrect = state.lastAnswerCorrect,
                onNumberEntered = { digit -> state.eventSink(NumberSequenceScreen.Event.NumberEntered(digit)) },
                onBackspace = { state.eventSink(NumberSequenceScreen.Event.Backspace) },
                onCheckAnswer = { state.eventSink(NumberSequenceScreen.Event.CheckAnswer) },
                modifier = modifier,
            )
        }

        is NumberSequenceScreen.GameState.Finished -> {
            NumberSequenceResultsScreen(
                finalScore = gameState.finalScore,
                totalAttempts = gameState.totalAttempts,
                isNewRecord = gameState.isNewRecord,
                accuracy = gameState.accuracy,
                averageTimePerSequence = gameState.averageTimePerSequence,
                personalBest = state.personalBest,
                userName = state.userName,
                unlockedBadges = gameState.unlockedBadges,
                onPlayAgain = { state.eventSink(NumberSequenceScreen.Event.PlayAgain) },
                onNavigateHome = { state.eventSink(NumberSequenceScreen.Event.NavigateHome) },
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
private fun NumberSequenceUiPhoneLandscapeStartPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState = NumberSequenceScreen.GameState.NotStarted,
                    currentSequence = null,
                    currentAnswer = "",
                    score = 0,
                    timeRemaining = 90,
                    personalBest = 12,
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
private fun NumberSequenceUiTabletPortraitStartPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState = NumberSequenceScreen.GameState.NotStarted,
                    currentSequence = null,
                    currentAnswer = "",
                    score = 0,
                    timeRemaining = 90,
                    personalBest = 18,
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
private fun NumberSequenceUiTabletLandscapeStartPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState = NumberSequenceScreen.GameState.NotStarted,
                    currentSequence = null,
                    currentAnswer = "",
                    score = 0,
                    timeRemaining = 90,
                    personalBest = 22,
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
private fun NumberSequenceUiFoldablePortraitStartPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState = NumberSequenceScreen.GameState.NotStarted,
                    currentSequence = null,
                    currentAnswer = "",
                    score = 0,
                    timeRemaining = 90,
                    personalBest = 15,
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
private fun NumberSequenceUiFoldableLandscapeStartPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState = NumberSequenceScreen.GameState.NotStarted,
                    currentSequence = null,
                    currentAnswer = "",
                    score = 0,
                    timeRemaining = 90,
                    personalBest = 20,
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
private fun NumberSequenceUiPhoneLandscapePlayingPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState = NumberSequenceScreen.GameState.Playing,
                    currentSequence =
                        SequenceQuestion(
                            numbers = listOf(2, 4, 6, null, 10),
                            missingIndex = 3,
                            correctAnswer = 8,
                            sequenceType = "+2",
                        ),
                    currentAnswer = "8",
                    score = 6,
                    timeRemaining = 55,
                    personalBest = 12,
                    totalAttempts = 7,
                    correctAnswers = 6,
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
private fun NumberSequenceUiTabletPortraitPlayingPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState = NumberSequenceScreen.GameState.Playing,
                    currentSequence =
                        SequenceQuestion(
                            numbers = listOf(5, 10, null, 20, 25),
                            missingIndex = 2,
                            correctAnswer = 15,
                            sequenceType = "+5",
                        ),
                    currentAnswer = "",
                    score = 10,
                    timeRemaining = 60,
                    personalBest = 18,
                    totalAttempts = 11,
                    correctAnswers = 10,
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
private fun NumberSequenceUiTabletLandscapePlayingPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState = NumberSequenceScreen.GameState.Playing,
                    currentSequence =
                        SequenceQuestion(
                            numbers = listOf(1, 2, 4, null, 16),
                            missingIndex = 3,
                            correctAnswer = 8,
                            sequenceType = "×2",
                        ),
                    currentAnswer = "8",
                    score = 15,
                    timeRemaining = 30,
                    personalBest = 22,
                    totalAttempts = 17,
                    correctAnswers = 15,
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
private fun NumberSequenceUiFoldablePortraitPlayingPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState = NumberSequenceScreen.GameState.Playing,
                    currentSequence =
                        SequenceQuestion(
                            numbers = listOf(3, 6, 9, null, 15),
                            missingIndex = 3,
                            correctAnswer = 12,
                            sequenceType = "+3",
                        ),
                    currentAnswer = "12",
                    score = 8,
                    timeRemaining = 45,
                    personalBest = 15,
                    totalAttempts = 9,
                    correctAnswers = 8,
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
private fun NumberSequenceUiFoldableLandscapePlayingPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState = NumberSequenceScreen.GameState.Playing,
                    currentSequence =
                        SequenceQuestion(
                            numbers = listOf(10, 8, null, 4, 2),
                            missingIndex = 2,
                            correctAnswer = 6,
                            sequenceType = "-2",
                        ),
                    currentAnswer = "",
                    score = 12,
                    timeRemaining = 15,
                    personalBest = 20,
                    totalAttempts = 14,
                    correctAnswers = 12,
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
private fun NumberSequenceUiPhoneLandscapeResultsPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState =
                        NumberSequenceScreen.GameState.Finished(
                            finalScore = 14,
                            totalAttempts = 16,
                            isNewRecord = true,
                            accuracy = 87.5f,
                            averageTimePerSequence = 5.6f,
                        ),
                    currentSequence = null,
                    currentAnswer = "",
                    score = 14,
                    timeRemaining = 0,
                    personalBest = 14,
                    totalAttempts = 16,
                    correctAnswers = 14,
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
private fun NumberSequenceUiTabletPortraitResultsPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState =
                        NumberSequenceScreen.GameState.Finished(
                            finalScore = 20,
                            totalAttempts = 22,
                            isNewRecord = true,
                            accuracy = 90.9f,
                            averageTimePerSequence = 4.1f,
                        ),
                    currentSequence = null,
                    currentAnswer = "",
                    score = 20,
                    timeRemaining = 0,
                    personalBest = 20,
                    totalAttempts = 22,
                    correctAnswers = 20,
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
private fun NumberSequenceUiTabletLandscapeResultsPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState =
                        NumberSequenceScreen.GameState.Finished(
                            finalScore = 12,
                            totalAttempts = 15,
                            isNewRecord = false,
                            accuracy = 80f,
                            averageTimePerSequence = 6f,
                        ),
                    currentSequence = null,
                    currentAnswer = "",
                    score = 12,
                    timeRemaining = 0,
                    personalBest = 22,
                    totalAttempts = 15,
                    correctAnswers = 12,
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
private fun NumberSequenceUiFoldablePortraitResultsPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState =
                        NumberSequenceScreen.GameState.Finished(
                            finalScore = 17,
                            totalAttempts = 19,
                            isNewRecord = true,
                            accuracy = 89.5f,
                            averageTimePerSequence = 4.7f,
                        ),
                    currentSequence = null,
                    currentAnswer = "",
                    score = 17,
                    timeRemaining = 0,
                    personalBest = 17,
                    totalAttempts = 19,
                    correctAnswers = 17,
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
private fun NumberSequenceUiFoldableLandscapeResultsPreview() {
    KidsMathTutorAppTheme {
        NumberSequenceUi(
            state =
                NumberSequenceScreen.State(
                    gameState =
                        NumberSequenceScreen.GameState.Finished(
                            finalScore = 22,
                            totalAttempts = 24,
                            isNewRecord = true,
                            accuracy = 91.7f,
                            averageTimePerSequence = 3.8f,
                        ),
                    currentSequence = null,
                    currentAnswer = "",
                    score = 22,
                    timeRemaining = 0,
                    personalBest = 22,
                    totalAttempts = 24,
                    correctAnswers = 22,
                    lastAnswerCorrect = null,
                    userName = "Taylor",
                    eventSink = {},
                ),
        )
    }
}
