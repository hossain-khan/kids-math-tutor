package dev.hossain.mathtutor.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Specification for a single math problem in an explicit challenge.
 *
 * @property operand1 The first operand
 * @property operand2 The second operand
 * @property operation The mathematical operation to perform
 */
@Serializable
@Parcelize
data class ProblemSpec(
    val operand1: Int,
    val operand2: Int,
    val operation: MathOperation,
) : Parcelable
