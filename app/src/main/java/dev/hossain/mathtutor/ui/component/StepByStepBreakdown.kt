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
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.MathProblem

/**
 * Displays a step-by-step breakdown of how to solve a math problem.
 *
 * Shows the problem, each step with animated reveal, and the answer.
 *
 * @param problem The math problem being solved
 * @param steps List of solution steps
 * @param answer The final answer
 * @param modifier Optional modifier
 */
@Composable
fun StepByStepBreakdown(
    problem: MathProblem,
    steps: List<WorkBreakdownStep>,
    answer: Int,
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
            text = "✨ Answer: $answer",
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
data class WorkBreakdownStep(
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
