package dev.hossain.mathtutor.ui.numbersequence

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.ui.games.GameBlockerDialog
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
    // If there's an active goal, show the blocker dialog
    if (state.activeGoal != null) {
        GameBlockerDialog(
            activeGoal = state.activeGoal,
            onViewGoalProgressClicked = {
                state.eventSink(NumberSequenceScreen.Event.ViewGoalProgressClicked)
            },
            onBackToHomeClicked = {
                state.eventSink(NumberSequenceScreen.Event.NavigateHome)
            },
        )
        return
    }

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
