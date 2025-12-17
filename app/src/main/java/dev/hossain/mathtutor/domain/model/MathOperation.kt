package dev.hossain.mathtutor.domain.model

/**
 * Represents different mathematical operations supported by the app.
 *
 * @property symbol The symbolic representation of the operation
 * @property displayName The human-readable name of the operation
 */
enum class MathOperation(
    val symbol: String,
    val displayName: String,
) {
    ADDITION("+", "Addition"),
    SUBTRACTION("-", "Subtraction"),
    MULTIPLICATION("×", "Multiplication"),
    DIVISION("÷", "Division"),
    MIXED("?", "Mix It Up"),
    ;

    /**
     * Performs the mathematical operation on two numbers.
     *
     * @param num1 The first operand
     * @param num2 The second operand
     * @return The result of the operation
     * @throws ArithmeticException if attempting to divide by zero
     * @throws IllegalStateException if attempting to calculate MIXED operation directly
     */
    fun calculate(
        num1: Int,
        num2: Int,
    ): Int =
        when (this) {
            ADDITION -> {
                num1 + num2
            }

            SUBTRACTION -> {
                num1 - num2
            }

            MULTIPLICATION -> {
                num1 * num2
            }

            DIVISION -> {
                require(num2 != 0) { "Cannot divide by zero" }
                num1 / num2
            }

            MIXED -> {
                throw IllegalStateException("Cannot calculate MIXED operation directly")
            }
        }
}
