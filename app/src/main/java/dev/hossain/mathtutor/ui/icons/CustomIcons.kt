package dev.hossain.mathtutor.ui.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Custom icon set for math operations and other custom needs.
 *
 * These icons supplement Material Design icons when specialized symbols are needed.
 */
object CustomIcons {
    /**
     * Division operator icon (÷)
     *
     * Represents mathematical division operation with two dots and a horizontal line.
     * Follows Material Design icon conventions (24x24 grid).
     */
    val Division: ImageVector
        get() =
            materialIcon(name = "Custom.Division") {
                materialPath {
                    // Top dot (circle at ~8, 6)
                    moveTo(8f, 5f)
                    curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
                    curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
                    curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
                    curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
                    close()

                    // Horizontal line
                    moveTo(2f, 12f)
                    lineTo(22f, 12f)
                    lineTo(22f, 14f)
                    lineTo(2f, 14f)
                    close()

                    // Bottom dot (circle at ~8, 18)
                    moveTo(8f, 17f)
                    curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
                    curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
                    curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
                    curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
                    close()
                }
            }
}


