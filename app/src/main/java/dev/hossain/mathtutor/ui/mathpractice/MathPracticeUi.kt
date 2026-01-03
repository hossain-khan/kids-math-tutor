package dev.hossain.mathtutor.ui.mathpractice

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
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
import dev.hossain.mathtutor.R
import dev.hossain.mathtutor.domain.model.DifficultyAdjustment
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.haptic.HapticService
import dev.hossain.mathtutor.ui.animation.SuccessAnimation
import dev.hossain.mathtutor.ui.animation.shake
import dev.hossain.mathtutor.ui.component.AnswerField
import dev.hossain.mathtutor.ui.component.BadgeDetailDialog
import dev.hossain.mathtutor.ui.component.DotVisualizer
import dev.hossain.mathtutor.ui.component.NumberPad
import dev.hossain.mathtutor.ui.component.StepByStepBreakdown
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.EXPANDED_WIDTH_BREAKPOINT
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MAX_CONTENT_WIDTH_NARROW
import dev.hossain.mathtutor.ui.utils.AdaptiveLayoutConstants.MEDIUM_WIDTH_BREAKPOINT
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import timber.log.Timber

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

    // Text hint dialog - shown as overlay to prevent layout push
    if (state.currentHintText != null && state.currentProblem != null) {
        AlertDialog(
            onDismissRequest = { state.eventSink(MathPracticeScreen.Event.DismissHint) },
            title = {
                Text(
                    text = "💡 Hint",
                    modifier =
                        Modifier.semantics {
                            heading()
                        },
                )
            },
            text = {
                BoxWithConstraints {
                    val stickerSize = if (maxWidth >= MEDIUM_WIDTH_BREAKPOINT) 180.dp else 120.dp
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Math Pup teaching sticker
                        Image(
                            painter = painterResource(R.drawable.pup_tutor_sticker_need_help_teaching),
                            contentDescription = "Math Pup offering help",
                            modifier = Modifier.size(stickerSize),
                        )
                        Text(
                            text = state.currentHintText,
                            modifier =
                                Modifier.semantics {
                                    contentDescription = "Hint to help solve the problem"
                                },
                        )
                    }
                }
            },
            confirmButton = {
                // Only show "Show Visually" button if visual hint is feasible for this problem
                if (state.isVisualHintFeasible) {
                    TextButton(
                        onClick = {
                            // Ask if they want visual hint
                            state.eventSink(MathPracticeScreen.Event.ShowVisualHint)
                        },
                        modifier =
                            Modifier.semantics {
                                contentDescription = "Show the hint visually with dots and pictures"
                            },
                    ) {
                        Text("Show Visually")
                    }
                } else {
                    TextButton(
                        onClick = { state.eventSink(MathPracticeScreen.Event.DismissHint) },
                        modifier =
                            Modifier.semantics {
                                contentDescription = "Close this hint"
                            },
                    ) {
                        Text("Got it")
                    }
                }
            },
            dismissButton = {
                if (state.isVisualHintFeasible) {
                    TextButton(
                        onClick = { state.eventSink(MathPracticeScreen.Event.DismissHint) },
                        modifier =
                            Modifier.semantics {
                                contentDescription = "Close this hint"
                            },
                    ) {
                        Text("Got it")
                    }
                } else {
                    null
                }
            },
        )
    }

    // Visual hint dialog - shown as overlay
    if (state.showVisualHint && state.currentProblem != null) {
        AlertDialog(
            onDismissRequest = { state.eventSink(MathPracticeScreen.Event.DismissVisualHint) },
            title = {
                Text(
                    text = "🎨 Visual Hint",
                    modifier =
                        Modifier.semantics {
                            heading()
                        },
                )
            },
            text = {
                BoxWithConstraints {
                    val stickerSize = if (maxWidth >= MEDIUM_WIDTH_BREAKPOINT) 150.dp else 100.dp
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Math Pup juggling balls sticker
                        Image(
                            painter = painterResource(R.drawable.pup_tutor_sticker_juggling_balls),
                            contentDescription = "Math Pup with visual dots",
                            modifier = Modifier.size(stickerSize),
                        )
                        DotVisualizer(
                            operation = state.currentProblem.operation,
                            firstNumber = state.currentProblem.num1,
                            secondNumber = state.currentProblem.num2,
                            modifier =
                                Modifier.semantics {
                                    contentDescription =
                                        "Visual representation showing " +
                                        "${state.currentProblem.num1} and ${state.currentProblem.num2} for ${state.currentProblem.operation.name.lowercase()}"
                                },
                        )
                        Text(
                            text = state.currentHintText ?: "See how the problem works!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier =
                                Modifier.semantics {
                                    contentDescription = "Explanation of the visual hint"
                                },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { state.eventSink(MathPracticeScreen.Event.DismissVisualHint) },
                    modifier =
                        Modifier.semantics {
                            contentDescription = "Close this visual hint"
                        },
                ) {
                    Text("Got it")
                }
            },
        )
    }

    // Work breakdown dialog - shown as overlay
    if (state.showWorkBreakdown && state.currentProblem != null) {
        AlertDialog(
            onDismissRequest = { state.eventSink(MathPracticeScreen.Event.DismissWork) },
            title = {
                Text(
                    text = "📚 How to Solve",
                    modifier =
                        Modifier.semantics {
                            heading()
                        },
                )
            },
            text = {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription =
                                    "Step-by-step breakdown showing how to solve " +
                                    "${state.currentProblem.num1} ${state.currentProblem.operation.name.lowercase()} ${state.currentProblem.num2}"
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    StepByStepBreakdown(
                        problem = state.currentProblem,
                        steps = state.workBreakdownSteps,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { state.eventSink(MathPracticeScreen.Event.DismissWork) },
                    modifier =
                        Modifier.semantics {
                            contentDescription = "Close this step-by-step breakdown"
                        },
                ) {
                    Text("I understand now!")
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
                modifier = Modifier.shadow(elevation = 4.dp),
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
                val isExpandedScreen = maxWidth >= EXPANDED_WIDTH_BREAKPOINT
                val isLandscape = maxWidth > maxHeight

                // Center content on wide screens
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    if (isWideScreen && isLandscape) {
                        // Landscape tablet: side-by-side layout with better proportions
                        val horizontalSpacing = if (isExpandedScreen) 48.dp else 32.dp
                        val verticalPadding = if (isExpandedScreen) 24.dp else 16.dp

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = verticalPadding, vertical = verticalPadding),
                            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Left side: Problem and feedback (larger proportion on expanded)
                            Column(
                                modifier =
                                    Modifier
                                        .weight(if (isExpandedScreen) 1.2f else 1f)
                                        .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                // Progress indicator
                                ProgressSection(
                                    currentIndex = state.currentProblemIndex,
                                    totalProblems = state.totalProblems,
                                    customChallengeTitle = state.customChallengeTitle,
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
                                    currentProblem = state.currentProblem,
                                    userAnswer = state.currentAnswer.toIntOrNull(),
                                )
                            }

                            // Right side: Answer input and number pad
                            Column(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .widthIn(max = if (isExpandedScreen) 600.dp else MAX_CONTENT_WIDTH_NARROW)
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

                                // Work breakdown button (landscape only)
                                if (state.showHintButton && !state.hintButtonClicked) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { state.eventSink(MathPracticeScreen.Event.ShowWork) },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            ),
                                    ) {
                                        Text(
                                            "📚 How to solve",
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Portrait or compact: stacked layout (centered on wide screens)
                        Column(
                            modifier =
                                Modifier
                                    .widthIn(max = MAX_CONTENT_WIDTH_NARROW)
                                    .fillMaxSize()
                                    .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            // Progress indicator
                            ProgressSection(
                                currentIndex = state.currentProblemIndex,
                                totalProblems = state.totalProblems,
                                customChallengeTitle = state.customChallengeTitle,
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
                                currentProblem = state.currentProblem,
                                userAnswer = state.currentAnswer.toIntOrNull(),
                            )

                            // Hint button (appears after feedback when wrong)
                            if (state.showHintButton && !state.hintButtonClicked) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { state.eventSink(MathPracticeScreen.Event.RequestHint) },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        ),
                                ) {
                                    Text(
                                        "💡 Need help?",
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                }
                            }

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
    customChallengeTitle: String? = null,
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
        // Show custom challenge title if present
        if (customChallengeTitle != null) {
            Text(
                text = customChallengeTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
            )
            Text(
                text = "Parent Challenge",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
            )
        }

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
    currentProblem: MathProblem? = null,
    userAnswer: Int? = null,
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
                    if (!userName.isNullOrBlank()) {
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
                // Check if answer is close (off by 1)
                val isClose =
                    if (currentProblem != null && userAnswer != null) {
                        val correctAnswer = currentProblem.correctAnswer
                        kotlin.math.abs(correctAnswer - userAnswer) == 1
                    } else {
                        false
                    }

                // Varied encouragement messages based on closeness
                val feedbackMessage =
                    if (isClose) {
                        // Messages for close answers - more encouraging
                        listOf(
                            "✗ Almost! Try again",
                            "✗ Very close! Try again",
                            "✗ You're getting there!",
                            "✗ So close! Give it another try",
                        ).random()
                    } else {
                        // General messages for incorrect answers
                        listOf(
                            "✗ Try again",
                            "✗ Not quite. Try again",
                            "✗ Keep going!",
                            "✗ Give it another shot",
                        ).random()
                    }

                Text(
                    text = feedbackMessage,
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

@Preview(
    showBackground = true,
    widthDp = 600,
    heightDp = 400,
    name = "MathPractice - Compact Landscape",
)
@Composable
private fun MathPracticeUiCompactLandscapePreview() {
    KidsMathTutorAppTheme {
        MathPracticeUi(
            state =
                MathPracticeScreen.State(
                    currentProblem = MathProblem(num1 = 8, num2 = 6, operation = MathOperation.ADDITION, correctAnswer = 14),
                    currentAnswer = "",
                    currentProblemIndex = 3,
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

@Preview(
    showBackground = true,
    widthDp = 800,
    heightDp = 600,
    name = "MathPractice - Medium Tablet",
)
@Composable
private fun MathPracticeUiMediumTabletPreview() {
    KidsMathTutorAppTheme {
        MathPracticeUi(
            state =
                MathPracticeScreen.State(
                    currentProblem = MathProblem(num1 = 9, num2 = 7, operation = MathOperation.SUBTRACTION, correctAnswer = 2),
                    currentAnswer = "2",
                    currentProblemIndex = 5,
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

@Preview(
    showBackground = true,
    widthDp = 1100,
    heightDp = 600,
    name = "MathPractice - Expanded Tablet Landscape",
)
@Composable
private fun MathPracticeUiExpandedTabletPreview() {
    KidsMathTutorAppTheme {
        MathPracticeUi(
            state =
                MathPracticeScreen.State(
                    currentProblem = MathProblem(num1 = 12, num2 = 8, operation = MathOperation.ADDITION, correctAnswer = 20),
                    currentAnswer = "15",
                    currentProblemIndex = 7,
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

/**
 * Displays a hint card with helpful guidance text.
 * Kids can dismiss the hint to try solving on their own.
 */
@Composable
private fun HintCard(
    hintText: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Math Pup teaching sticker
            Image(
                painter = painterResource(R.drawable.pup_tutor_sticker_need_help_teaching),
                contentDescription = "Math Pup offering help",
                modifier =
                    Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "💡",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Hint:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Text(
                text = hintText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Got it!")
            }
        }
    }
}
