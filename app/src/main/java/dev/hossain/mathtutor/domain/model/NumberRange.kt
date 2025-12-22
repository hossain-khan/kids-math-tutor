package dev.hossain.mathtutor.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Represents a range of numbers for generating math problems.
 *
 * @property min The minimum number in the range (inclusive)
 * @property max The maximum number in the range (inclusive)
 */
@Serializable
@Parcelize
data class NumberRange(
    val min: Int,
    val max: Int,
) : Parcelable {
    init {
        require(min <= max) { "Minimum value ($min) must be less than or equal to maximum value ($max)" }
    }
}
