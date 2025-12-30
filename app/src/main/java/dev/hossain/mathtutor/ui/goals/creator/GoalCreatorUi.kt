package dev.hossain.mathtutor.ui.goals.creator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.goals.GoalComponent
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen.Event
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen.State
import dev.hossain.mathtutor.ui.goals.creator.GoalCreatorScreen.Step
import dev.zacsweers.metro.AppScope

@CircuitInject(GoalCreatorScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCreatorUi(
    state: State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create New Goal") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(Event.Cancel) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Step indicator
                StepIndicator(
                    currentStep = state.currentStep,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Step content
                when (state.currentStep) {
                    Step.Title -> {
                        TitleStepContent(
                            title = state.goalTitle,
                            description = state.goalDescription,
                            onTitleChange = { state.eventSink(Event.SetTitle(it)) },
                            onDescriptionChange = { state.eventSink(Event.SetDescription(it)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Step.SelectComponents -> {
                        SelectComponentsContent(
                            components = state.components,
                            onAddComponent = { state.eventSink(Event.AddComponent(it)) },
                            onRemoveComponent = { state.eventSink(Event.RemoveComponent(it)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Step.Review -> {
                        ReviewStepContent(
                            goalTitle = state.goalTitle,
                            goalDescription = state.goalDescription,
                            components = state.components,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Navigation buttons
            NavigationButtons(
                currentStep = state.currentStep,
                canAdvance = state.canAdvance,
                isLoading = state.isLoading,
                onPrevious = { state.eventSink(Event.PreviousStep) },
                onNext = { state.eventSink(Event.NextStep) },
                onSave = { state.eventSink(Event.SaveGoal) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Step,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                val step = Step.values()[index]
                val isActive = currentStep == step
                val isCompleted = currentStep.ordinal > step.ordinal

                Card(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(48.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                when {
                                    isActive -> MaterialTheme.colorScheme.primaryContainer
                                    isCompleted -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                },
                        ),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text =
                                when (step) {
                                    Step.Title -> "Title"
                                    Step.SelectComponents -> "Components"
                                    Step.Review -> "Review"
                                },
                            style = MaterialTheme.typography.labelSmall,
                            color =
                                when {
                                    isActive -> MaterialTheme.colorScheme.onPrimaryContainer
                                    isCompleted -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (currentStep.ordinal + 1) / 3f },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun TitleStepContent(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Give your goal a title",
            style = MaterialTheme.typography.headlineSmall,
        )

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Goal Title") },
            placeholder = { Text("e.g., Master Addition") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                ),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (Optional)") },
            placeholder = { Text("What would you like to achieve?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
        )
    }
}

@Composable
private fun SelectComponentsContent(
    components: List<GoalComponent>,
    onAddComponent: (GoalComponent) -> Unit,
    onRemoveComponent: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Select math operations",
            style = MaterialTheme.typography.headlineSmall,
        )

        Text(
            text = "Choose which math operations your child should practice:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Available operations
        val operations =
            listOf(
                MathOperation.Addition,
                MathOperation.Subtraction,
                MathOperation.Multiplication,
                MathOperation.Division,
            )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            operations.forEach { operation ->
                val isSelected = components.any { 
                    it is GoalComponent.OperationBased && it.operation == operation 
                }
                OperationButton(
                    operation = operation,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelected) {
                            onRemoveComponent(
                                components.indexOfFirst { 
                                    it is GoalComponent.OperationBased && it.operation == operation 
                                }
                            )
                        } else {
                            val component = GoalComponent.OperationBased(
                                operation = operation,
                                sessionCount = 1,
                            )
                            onAddComponent(component)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (components.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selected: ${components.size} operation(s)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun OperationButton(
    operation: MathOperation,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors =
            androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                contentColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            ),
    ) {
        Text(
            text = "${operation.symbol} ${operation.displayName}",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun ReviewStepContent(
    goalTitle: String,
    goalDescription: String,
    components: List<GoalComponent>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Review Your Goal",
            style = MaterialTheme.typography.headlineSmall,
        )

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Title",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = goalTitle,
                    style = MaterialTheme.typography.headlineSmall,
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (goalDescription.isNotBlank()) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = goalDescription,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = "Math Operations",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = components.joinToString(", ") { it.getDescription() },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Text(
            text = "Everything looks good? Tap 'Create Goal' to save this goal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NavigationButtons(
    currentStep: Step,
    canAdvance: Boolean,
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (currentStep != Step.Title) {
            OutlinedButton(
                onClick = onPrevious,
                modifier =
                    Modifier
                        .weight(1f)
                        .height(48.dp),
                enabled = !isLoading,
            ) {
                Text("Back")
            }
        }

        when (currentStep) {
            Step.Title, Step.SelectComponents -> {
                Button(
                    onClick = onNext,
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(48.dp),
                    enabled = canAdvance && !isLoading,
                ) {
                    Text(if (currentStep == Step.Title) "Next" else "Review")
                }
            }

            Step.Review -> {
                Button(
                    onClick = onSave,
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(48.dp),
                    enabled = !isLoading,
                ) {
                    Text("Create Goal")
                }
            }
        }
    }
}

// Box composable for step indicator (needed for step indicator card)
@Composable
private fun Box(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier,
        contentAlignment = contentAlignment,
    ) {
        content()
    }
}
