package dev.hossain.mathtutor.domain.model

import kotlin.time.Duration

/**
 * Preview data for a custom challenge before it's created.
 *
 * This provides a summary of what the challenge will contain, including
 * sample problems, operations breakdown, and estimated completion time.
 *
 * @property title The title of the challenge
 * @property subtitle Optional subtitle or description
 * @property problemCount Total number of problems in the challenge
 * @property operationsSummary Breakdown of operations (e.g., "8 addition, 2 multiplication")
 * @property sampleProblems First 3-5 problems as examples
 * @property estimatedDuration Estimated time to complete based on problem count and complexity
 */
data class PreviewData(
    val title: String,
    val subtitle: String?,
    val problemCount: Int,
    val operationsSummary: Map<MathOperation, Int>,
    val sampleProblems: List<MathProblem>,
    val estimatedDuration: Duration,
)
