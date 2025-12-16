package dev.hossain.mathtutor.domain.model

/**
 * Represents different mathematical operations supported by the app.
 *
 * @property symbol The symbolic representation of the operation
 */
enum class MathOperation(
    val symbol: String,
) {
    ADDITION("+"),
    SUBTRACTION("-"),
    MULTIPLICATION("×"),
    DIVISION("÷"),
    ;

    /**
     * Performs the mathematical operation on two numbers.
     *
     * @param num1 The first operand
     * @param num2 The second operand
     * @return The result of the operation
     */
    fun calculate(
        num1: Int,
        num2: Int,
    ): Int =
        when (this) {
            ADDITION -> num1 + num2
            SUBTRACTION -> num1 - num2
            MULTIPLICATION -> num1 * num2
            DIVISION -> num1 / num2
        }
}
