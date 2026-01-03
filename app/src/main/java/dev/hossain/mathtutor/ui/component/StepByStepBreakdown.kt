package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.MathOperation
import dev.hossain.mathtutor.domain.model.MathProblem
import dev.hossain.mathtutor.ui.theme.KidsMathTutorAppTheme

/**
 * Displays a step-by-step breakdown of how to solve a math problem.
 *
 * This component is shown in the following scenarios:
 * - When a child clicks the "📚 How to solve" button after requesting hints
 * - Displayed in an AlertDialog as Tier 3 of the progressive hint system
 * - Available after 2 wrong attempts on a problem (if hint system is enabled)
 * - Can be accessed from:
 *   - Portrait mode: Via "💡 Need help?" → "Show Visually" flow
 *   - Landscape mode (tablet/expanded): Dedicated "📚 How to solve" button
 *
 * Shows the problem, each step with animated reveal (200ms stagger), and the final answer.
 *
 * @param problem The math problem being solved
 * @param steps List of solution steps with emoji and description
 * @param modifier Optional modifier
 */
@Composable
fun StepByStepBreakdown(
    problem: MathProblem,
    steps: List<WorkBreakdownStep>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Title
        Text(
            text = "📚 How to Solve",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Problem statement
        Text(
            text = "${problem.num1} ${getProblemSymbol(problem.operation)} ${problem.num2} = ?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        // Steps with staggered animation
        steps.forEachIndexed { index, step ->
            WorkStep(
                stepNumber = index + 1,
                emoji = step.emoji,
                description = step.description,
                delayMillis = index * 200, // Stagger each step by 200ms
            )
        }

        // Final answer
        Text(
            text = "✨ Answer: ${problem.correctAnswer}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Represents a single step in a work breakdown.
 */
public data class WorkBreakdownStep(
    val emoji: String,
    val description: String,
)

private fun getProblemSymbol(operation: dev.hossain.mathtutor.domain.model.MathOperation): String =
    when (operation) {
        dev.hossain.mathtutor.domain.model.MathOperation.ADDITION -> "+"
        dev.hossain.mathtutor.domain.model.MathOperation.SUBTRACTION -> "-"
        dev.hossain.mathtutor.domain.model.MathOperation.MULTIPLICATION -> "×"
        dev.hossain.mathtutor.domain.model.MathOperation.DIVISION -> "÷"
        dev.hossain.mathtutor.domain.model.MathOperation.MIXED -> "?"
    }

// ============================================
// Preview Functions
// ============================================

@Preview(showBackground = true, name = "Addition Breakdown")
@Composable
private fun StepByStepBreakdownAdditionPreview() {
    KidsMathTutorAppTheme {
        StepByStepBreakdown(
            problem =
                MathProblem(
                    id = "preview-1",
                    num1 = 8,
                    num2 = 5,
                    operation = MathOperation.ADDITION,
                    correctAnswer = 13,
                ),
            steps =
                listOf(
                    WorkBreakdownStep(
                        emoji = "🔢",
                        description = "Start with the first number: 8",
                    ),
                    WorkBreakdownStep(
                        emoji = "➕",
                        description = "Add the second number: 5",
                    ),
                    WorkBreakdownStep(
                        emoji = "🧮",
                        description = "Count up from 8: 9, 10, 11, 12, 13",
                    ),
                ),
        )
    }
}

@Preview(showBackground = true, name = "Subtraction Breakdown")
@Composable
private fun StepByStepBreakdownSubtractionPreview() {
    KidsMathTutorAppTheme {
        StepByStepBreakdown(
            problem =
                MathProblem(
                    id = "preview-2",
                    num1 = 13,
                    num2 = 3,
                    operation = MathOperation.SUBTRACTION,
                    correctAnswer = 10,
                ),
            steps =
                listOf(
                    WorkBreakdownStep(
                        emoji = "🔢",
                        description = "Start with: 13",
                    ),
                    WorkBreakdownStep(
                        emoji = "➖",
                        description = "Take away: 3",
                    ),
                    WorkBreakdownStep(
                        emoji = "🧮",
                        description = "Count backwards: 12, 11, 10",
                    ),
                ),
        )
    }
}

@Preview(showBackground = true, name = "Multiplication Breakdown")
@Composable
private fun StepByStepBreakdownMultiplicationPreview() {
    KidsMathTutorAppTheme {
        StepByStepBreakdown(
            problem =
                MathProblem(
                    id = "preview-3",
                    num1 = 4,
                    num2 = 3,
                    operation = MathOperation.MULTIPLICATION,
                    correctAnswer = 12,
                ),
            steps =
                listOf(
                    WorkBreakdownStep(
                        emoji = "👥",
                        description = "Make 4 groups",
                    ),
                    WorkBreakdownStep(
                        emoji = "🔢",
                        description = "Put 3 in each group",
                    ),
                    WorkBreakdownStep(
                        emoji = "🧮",
                        description = "Count all: 3 + 3 + 3 + 3 = 12",
                    ),
                ),
        )
    }
}

@Preview(showBackground = true, name = "Division Breakdown")
@Composable
private fun StepByStepBreakdownDivisionPreview() {
    KidsMathTutorAppTheme {
        StepByStepBreakdown(
            problem =
                MathProblem(
                    id = "preview-4",
                    num1 = 12,
                    num2 = 3,
                    operation = MathOperation.DIVISION,
                    correctAnswer = 4,
                ),
            steps =
                listOf(
                    WorkBreakdownStep(
                        emoji = "🔢",
                        description = "Start with 12 items",
                    ),
                    WorkBreakdownStep(
                        emoji = "👥",
                        description = "Split into groups of 3",
                    ),
                    WorkBreakdownStep(
                        emoji = "🧮",
                        description = "Count the groups: 4 groups",
                    ),
                ),
        )
    }
}

@Preview(showBackground = true, name = "Simple 2-Step Breakdown", widthDp = 400)
@Composable
private fun StepByStepBreakdownSimplePreview() {
    KidsMathTutorAppTheme {
        StepByStepBreakdown(
            problem =
                MathProblem(
                    id = "preview-5",
                    num1 = 5,
                    num2 = 2,
                    operation = MathOperation.ADDITION,
                    correctAnswer = 7,
                ),
            steps =
                listOf(
                    WorkBreakdownStep(
                        emoji = "🔢",
                        description = "Start with 5",
                    ),
                    WorkBreakdownStep(
                        emoji = "➕",
                        description = "Add 2 more to get 7",
                    ),
                ),
        )
    }
}
