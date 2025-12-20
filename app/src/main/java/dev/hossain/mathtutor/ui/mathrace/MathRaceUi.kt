package dev.hossain.mathtutor.ui.mathrace

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
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
