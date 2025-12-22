package dev.hossain.mathtutor.ui.mathpractice

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import dev.hossain.mathtutor.domain.model.DifficultyAdjustment
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.haptic.HapticService
import dev.hossain.mathtutor.ui.animation.SuccessAnimation
import dev.hossain.mathtutor.ui.animation.shake
import dev.hossain.mathtutor.ui.component.AnswerField
import dev.hossain.mathtutor.ui.component.BadgeDetailDialog
import dev.hossain.mathtutor.ui.component.NumberPad
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import timber.log.Timber

// Width breakpoints for adaptive layouts
private val MEDIUM_WIDTH_BREAKPOINT: Dp = 600.dp
private val MAX_CONTENT_WIDTH: Dp = 500.dp

/**
 * Custom Ui.Factory for MathPracticeScreen that injects HapticService.
 */
@Inject
@ContributesIntoSet(AppScope::class)
class MathPracticeUiFactory(
    private val hapticService: HapticService,
) : Ui.Factory {
    override fun create(
        screen: Screen,
        context: CircuitContext,
    ): Ui<*>? =
        when (screen) {
            is MathPracticeScreen -> {
                ui<MathPracticeScreen.State> { state, modifier ->
                    MathPracticeUi(state = state, modifier = modifier, hapticService = hapticService)
                }
            }

            else -> {
                null
            }
        }
}

/**
 * UI for [MathPracticeScreen].
 *
 * Displays the math problem, answer input field, number pad, and action buttons.
 *
 * Adaptive Layout:
 * - Compact: Stacked vertical layout
 * - Medium/Expanded: Centered with max width, landscape shows side-by-side problem and input
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MathPracticeUi(
    state: MathPracticeScreen.State,
    modifier: Modifier = Modifier,
    hapticService: HapticService,
) {
    // Track shake animation state and previous isCorrect value
    var shouldShake by remember { mutableStateOf(false) }
    var previousIsCorrect by remember { mutableStateOf<Boolean?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Trigger shake only on transition to incorrect state
    if (state.isCorrect == false && previousIsCorrect != false) {
        shouldShake = true
        previousIsCorrect = false
    } else if (state.isCorrect != false) {
        previousIsCorrect = state.isCorrect
    }

    /*
     * Handle system back button to show exit confirmation.
     * This prevents accidental exits during practice which would lose progress.
     * Different from Settings/Games BackHandler which prevents ANR - this is intentional UX.
     */
    BackHandler {
        showExitDialog = true
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Practice?") },
            text = { Text("Your progress will be lost if you exit now.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        state.eventSink(MathPracticeScreen.Event.NavigateBack)
                    },
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Continue")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Math Practice")
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showExitDialog = true },
                        modifier =
                            Modifier.semantics {
                                contentDescription = "Go back"
                                role = Role.Button
                            },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null, // Described by button semantics
                        )
                    }
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        if (state.isLoading) {
            // Show loading state while fetching profile and generating problems
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Preparing problems...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        } else {
            BoxWithConstraints(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                val isWideScreen = maxWidth >= MEDIUM_WIDTH_BREAKPOINT
                val isLandscape = maxWidth > maxHeight

                // Center content on wide screens
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    if (isWideScreen && isLandscape) {
                        // Landscape tablet: side-by-side layout
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Left side: Problem and feedback
                            Column(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                // Progress indicator
                                ProgressSection(
                                    currentIndex = state.currentProblemIndex,
                                    totalProblems = state.totalProblems,
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Problem display
                                state.currentProblem?.let { problem ->
                                    ProblemCard(problem = problem)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Feedback display with success animation
                                FeedbackSection(
                                    isCorrect = state.isCorrect,
                                    userName = state.userName,
                                )
                            }

                            // Right side: Answer input and number pad
                            Column(
                                modifier =
                                    Modifier
                                        .widthIn(max = MAX_CONTENT_WIDTH)
                                        .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                // Answer field with shake animation on incorrect answer
                                AnswerField(
                                    answer = state.currentAnswer,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .shake(
                                                shouldShake = shouldShake,
                                                onAnimationComplete = {
                                                    shouldShake = false
                                                },
                                            ),
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Number pad
                                NumberPad(
                                    onNumberClick = { number ->
                                        state.eventSink(MathPracticeScreen.Event.NumberClicked(number))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    hapticService = hapticService,
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Action buttons
                                ActionButtons(
                                    hasAnswer = state.currentAnswer.isNotEmpty(),
                                    isCorrect = state.isCorrect,
                                    onClear = { state.eventSink(MathPracticeScreen.Event.ClearAnswer) },
                                    onCheck = { state.eventSink(MathPracticeScreen.Event.CheckAnswer) },
                                    onNext = { state.eventSink(MathPracticeScreen.Event.NextProblem) },
                                    hapticService = hapticService,
                                )
                            }
                        }
                    } else {
                        // Portrait or compact: stacked layout (centered on wide screens)
                        Column(
                            modifier =
                                Modifier
                                    .widthIn(max = MAX_CONTENT_WIDTH)
                                    .fillMaxSize()
                                    .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            // Progress indicator
                            ProgressSection(
                                currentIndex = state.currentProblemIndex,
                                totalProblems = state.totalProblems,
                            )

                            // Problem display
                            state.currentProblem?.let { problem ->
                                ProblemCard(problem = problem)
                            }

                            // Answer field with shake animation on incorrect answer
                            AnswerField(
                                answer = state.currentAnswer,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .shake(
                                            shouldShake = shouldShake,
                                            onAnimationComplete = {
                                                shouldShake = false
                                            },
                                        ),
                            )

                            // Feedback display with success animation
                            FeedbackSection(
                                isCorrect = state.isCorrect,
                                userName = state.userName,
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Number pad
                            NumberPad(
                                onNumberClick = { number ->
                                    state.eventSink(MathPracticeScreen.Event.NumberClicked(number))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                hapticService = hapticService,
                            )

                            // Action buttons
                            ActionButtons(
                                hasAnswer = state.currentAnswer.isNotEmpty(),
                                isCorrect = state.isCorrect,
                                onClear = { state.eventSink(MathPracticeScreen.Event.ClearAnswer) },
                                onCheck = { state.eventSink(MathPracticeScreen.Event.CheckAnswer) },
                                onNext = { state.eventSink(MathPracticeScreen.Event.NextProblem) },
                                hapticService = hapticService,
                            )
                        }
                    }
                }
            }
        }

        // Badge unlock dialog - shown after session completion
        if (state.showBadgeUnlock && state.unlockedBadges.isNotEmpty()) {
            val currentBadge = state.unlockedBadges.getOrNull(state.currentBadgeIndex)
            if (currentBadge != null) {
                BadgeDetailDialog(
                    badge = currentBadge,
                    onDismiss = { state.eventSink(MathPracticeScreen.Event.DismissBadgeDialog) },
                )
            }
        }

        // Difficulty change notification dialog
        if (state.showDifficultyChangeNotice && state.difficultyAdjustment != null) {
            DifficultyChangeDialog(
                adjustment = state.difficultyAdjustment,
                actualGradeLevel = state.actualGradeLevel,
                onDismiss = { state.eventSink(MathPracticeScreen.Event.DismissDifficultyNotice) },
            )
        }
    }
}

@Composable
private fun ProgressSection(
    currentIndex: Int,
    totalProblems: Int,
    modifier: Modifier = Modifier,
) {
    val progressDescription = "Problem ${currentIndex + 1} of $totalProblems"

    // Log only when progress changes (not on every recomposition)
    LaunchedEffect(currentIndex, totalProblems) {
        val progressDescriptionForLog = "Problem ${currentIndex + 1} of $totalProblems"
        Timber.d("[ProgressSection] Rendering progress: $progressDescriptionForLog")
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = progressDescription,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier.semantics {
                    contentDescription = progressDescription
                    heading()
                },
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalProblems },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Progress: ${currentIndex + 1} out of $totalProblems problems completed"
                    },
        )
    }
}

@Composable
private fun ProblemCard(
    problem: MathProblem,
    modifier: Modifier = Modifier,
) {
    val spokenProblem = problem.getSpokenString()

    // Log only when problem changes (not on every recomposition)
    LaunchedEffect(problem) {
        val spokenProblemForLog = problem.getSpokenString()
        Timber.d("[ProblemCard] Rendering problem with TalkBack announcement: '$spokenProblemForLog'")
    }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    // Use spoken format for TalkBack: "3 plus 5 equals"
                    contentDescription = spokenProblem
                },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = problem.getDisplayString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FeedbackSection(
    isCorrect: Boolean?,
    userName: String? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (isCorrect) {
            true -> {
                // Random personalized success messages
                val successMessages =
                    if (userName != null) {
                        listOf(
                            "✓ Great job, $userName!",
                            "✓ Excellent work, $userName!",
                            "✓ You're doing awesome, $userName!",
                            "✓ Perfect, $userName!",
                        )
                    } else {
                        listOf(
                            "✓ Correct!",
                            "✓ Great job!",
                            "✓ Excellent work!",
                            "✓ You're doing awesome!",
                        )
                    }

                // Success animation for correct answer
                SuccessAnimation(
                    isVisible = true,
                    content = {
                        Text(
                            text = successMessages.random(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                )
            }

            false -> {
                Text(
                    text = "✗ Try again",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            null -> {
                // Empty space when no feedback
            }
        }
    }
}

@Composable
private fun ActionButtons(
    hasAnswer: Boolean,
    isCorrect: Boolean?,
    onClear: () -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    hapticService: HapticService? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Clear button
        Button(
            onClick = {
                hapticService?.triggerButtonClick()
                Timber.d("[MathPracticeUi] Clear button clicked - triggered haptic feedback")
                onClear()
            },
            enabled = hasAnswer,
            modifier =
                Modifier
                    .weight(1f)
                    .semantics {
                        role = Role.Button
                        contentDescription =
                            if (hasAnswer) {
                                "Clear answer"
                            } else {
                                "Clear, disabled"
                            }
                    },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = null, // Described by button semantics
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear")
        }

        // Check/Next button
        Button(
            onClick = {
                hapticService?.triggerButtonClick()
                val action = if (isCorrect == true) "Next" else "Check"
                Timber.d("[MathPracticeUi] $action button clicked - triggered haptic feedback")
                if (isCorrect == true) onNext() else onCheck()
            },
            enabled = hasAnswer,
            modifier =
                Modifier
                    .weight(1f)
                    .semantics {
                        role = Role.Button
                        contentDescription =
                            when {
                                isCorrect == true -> "Next problem"
                                hasAnswer -> "Check your answer"
                                else -> "Enter an answer first, disabled"
                            }
                    },
        ) {
            Text(if (isCorrect == true) "Next" else "Check")
        }
    }
}

/**
 * Dialog shown when difficulty has been adjusted based on performance.
 */
@Composable
private fun DifficultyChangeDialog(
    adjustment: DifficultyAdjustment,
    actualGradeLevel: dev.hossain.mathtutor.domain.model.GradeLevel?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (emoji, title, message) =
        when (adjustment) {
            DifficultyAdjustment.HARDER -> {
                Triple(
                    "⬆️",
                    "Level Up!",
                    "Great job! You're doing so well that we're giving you " +
                        "harder problems. Now practicing at ${actualGradeLevel?.displayName ?: "higher level"}!",
                )
            }

            DifficultyAdjustment.EASIER -> {
                Triple(
                    "⬇️",
                    "Let's Practice More",
                    "We've adjusted the difficulty to help you practice. " +
                        "Now practicing at ${actualGradeLevel?.displayName ?: "easier level"}!",
                )
            }

            DifficultyAdjustment.CURRENT -> {
                Triple(
                    "✨",
                    "Keep Going!",
                    "You're on track! Keep practicing at ${actualGradeLevel?.displayName ?: "current level"}.",
                )
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "$emoji $title",
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it!")
            }
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun MathPracticeUiPreview() {
    KidsMathTutorAppTheme {
        MathPracticeUi(
            state =
                MathPracticeScreen.State(
                    currentProblem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
                    currentAnswer = "8",
                    currentProblemIndex = 0,
                    totalProblems = 10,
                    isCorrect = null,
                    eventSink = {},
                ),
            hapticService =
                object : HapticService {
                    override fun triggerSuccess() {}

                    override fun triggerError() {}

                    override fun triggerBadgeUnlock() {}

                    override fun triggerButtonClick() {}

                    override fun triggerLongPress() {}

                    override fun setHapticsEnabled(enabled: Boolean) {}
                },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MathPracticeUiCorrectPreview() {
    KidsMathTutorAppTheme {
        MathPracticeUi(
            state =
                MathPracticeScreen.State(
                    currentProblem = MathProblem(num1 = 5, num2 = 3, operation = MathOperation.ADDITION, correctAnswer = 8),
                    currentAnswer = "8",
                    currentProblemIndex = 2,
                    totalProblems = 10,
                    isCorrect = true,
                    eventSink = {},
                ),
            hapticService =
                object : HapticService {
                    override fun triggerSuccess() {}

                    override fun triggerError() {}

                    override fun triggerBadgeUnlock() {}

                    override fun triggerButtonClick() {}

                    override fun triggerLongPress() {}

                    override fun setHapticsEnabled(enabled: Boolean) {}
                },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MathPracticeUiIncorrectPreview() {
    KidsMathTutorAppTheme(darkTheme = true) {
        MathPracticeUi(
            state =
                MathPracticeScreen.State(
                    currentProblem = MathProblem(num1 = 7, num2 = 4, operation = MathOperation.ADDITION, correctAnswer = 11),
                    currentAnswer = "10",
                    currentProblemIndex = 5,
                    totalProblems = 10,
                    isCorrect = false,
                    eventSink = {},
                ),
            hapticService =
                object : HapticService {
                    override fun triggerSuccess() {}

                    override fun triggerError() {}

                    override fun triggerBadgeUnlock() {}

                    override fun triggerButtonClick() {}

                    override fun triggerLongPress() {}

                    override fun setHapticsEnabled(enabled: Boolean) {}
                },
        )
    }
}
