package dev.hossain.mathtutor.ui.utils

import android.graphics.Rect
import androidx.window.layout.FoldingFeature
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Information about the posture of the device, especially for foldable devices.
 *
 * Based on the Reply sample from Google's compose-samples.
 * See: https://github.com/android/compose-samples/blob/main/Reply/app/src/main/java/com/example/reply/ui/utils/WindowStateUtils.kt
 */
sealed interface DevicePosture {
    /**
     * Normal posture - device is not folded or in any special state.
     */
    data object NormalPosture : DevicePosture

    /**
     * Book posture - device is half-opened like a book (vertical fold).
     * @param hingePosition The position of the hinge/fold.
     */
    data class BookPosture(
        val hingePosition: Rect,
    ) : DevicePosture

    /**
     * Separating posture - the fold is separating the screen into two distinct areas.
     * @param hingePosition The position of the hinge/fold.
     * @param orientation The orientation of the fold.
     */
    data class Separating(
        val hingePosition: Rect,
        val orientation: FoldingFeature.Orientation,
    ) : DevicePosture
}

/**
 * Checks if the device is in book posture (half-opened with a vertical fold).
 *
 * @param foldFeature The folding feature to check.
 * @return True if the device is in book posture.
 */
@OptIn(ExperimentalContracts::class)
fun isBookPosture(foldFeature: FoldingFeature?): Boolean {
    contract { returns(true) implies (foldFeature != null) }
    return foldFeature?.state == FoldingFeature.State.HALF_OPENED &&
        foldFeature.orientation == FoldingFeature.Orientation.VERTICAL
}

/**
 * Checks if the fold is separating the screen into distinct areas.
 *
 * @param foldFeature The folding feature to check.
 * @return True if the fold is separating.
 */
@OptIn(ExperimentalContracts::class)
fun isSeparating(foldFeature: FoldingFeature?): Boolean {
    contract { returns(true) implies (foldFeature != null) }
    return foldFeature?.state == FoldingFeature.State.FLAT && foldFeature.isSeparating
}

/**
 * Determines the [DevicePosture] from a [FoldingFeature].
 *
 * @param foldingFeature The folding feature from WindowLayoutInfo.
 * @return The device posture.
 */
fun getDevicePosture(foldingFeature: FoldingFeature?): DevicePosture =
    when {
        isBookPosture(foldingFeature) -> {
            DevicePosture.BookPosture(foldingFeature.bounds)
        }

        isSeparating(foldingFeature) -> {
            DevicePosture.Separating(foldingFeature.bounds, foldingFeature.orientation)
        }

        else -> {
            DevicePosture.NormalPosture
        }
    }
