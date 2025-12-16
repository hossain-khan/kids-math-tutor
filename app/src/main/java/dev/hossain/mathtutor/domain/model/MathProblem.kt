package dev.hossain.mathtutor.domain.model

import java.util.UUID

/**
 * Represents a single math problem with its operands, operation, and correct answer.
 *
 * @property id Unique identifier for the problem
 * @property num1 First operand
 * @property num2 Second operand
 * @property operation The mathematical operation to perform
 * @property correctAnswer The correct answer to the problem
 */
data class MathProblem(
    val id: String = UUID.randomUUID().toString(),
    val num1: Int,
    val num2: Int,
    val operation: MathOperation,
    val correctAnswer: Int,
) {
    /**
     * Returns a human-readable string representation of the problem.
     * Example: "3 + 5 = ?"
     *
     * @return Formatted problem string
     */
    fun getDisplayString(): String = "$num1 ${operation.symbol} $num2 = ?"

    /**
     * Checks if the user's answer is correct.
     *
     * @param userAnswer The answer provided by the user
     * @return true if the answer is correct, false otherwise
     */
    fun checkAnswer(userAnswer: Int): Boolean = userAnswer == correctAnswer
}
