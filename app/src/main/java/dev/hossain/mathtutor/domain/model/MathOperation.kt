package dev.hossain.mathtutor.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Represents different mathematical operations supported by the app.
 *
 * @property symbol The symbolic representation of the operation
 * @property displayName The human-readable name of the operation
 * @property spokenName The spoken name for screen readers (e.g., "plus", "minus")
 */
@Serializable(with = MathOperationSerializer::class)
enum class MathOperation(
    val symbol: String,
    val displayName: String,
    val spokenName: String,
) {
    ADDITION("+", "Addition", "plus"),
    SUBTRACTION("-", "Subtraction", "minus"),
    MULTIPLICATION("×", "Multiplication", "times"),
    DIVISION("÷", "Division", "divided by"),
    MIXED("?", "Mix It Up", ""),
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

/**
 * Custom serializer for [MathOperation] that maps enum values to lowercase string representations
 * as expected by the JSON schema (e.g., "addition", "subtraction", "multiplication", "division").
 */
object MathOperationSerializer : KSerializer<MathOperation> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("MathOperation", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: MathOperation,
    ) {
        val stringValue =
            when (value) {
                MathOperation.ADDITION -> "addition"
                MathOperation.SUBTRACTION -> "subtraction"
                MathOperation.MULTIPLICATION -> "multiplication"
                MathOperation.DIVISION -> "division"
                MathOperation.MIXED -> "mixed"
            }
        encoder.encodeString(stringValue)
    }

    override fun deserialize(decoder: Decoder): MathOperation {
        val value = decoder.decodeString().lowercase()
        return when (value) {
            "addition" -> MathOperation.ADDITION
            "subtraction" -> MathOperation.SUBTRACTION
            "multiplication" -> MathOperation.MULTIPLICATION
            "division" -> MathOperation.DIVISION
            "mixed" -> MathOperation.MIXED
            else -> throw IllegalArgumentException("Unknown operation: $value")
        }
    }
}
