package dev.hossain.mathtutor.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

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
     * Generated from SVG for proper rendering.
     */
    val Division: ImageVector
        get() {
            if (_Division != null) {
                return _Division!!
            }
            _Division = ImageVector.Builder(
                name = "Division",
                defaultWidth = 800.dp,
                defaultHeight = 800.dp,
                viewportWidth = 19f,
                viewportHeight = 19f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(15.711f, 9.182f)
                    arcToRelative(1.03f, 1.03f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.03f, 1.03f)
                    lineTo(4.319f, 10.212f)
                    arcToRelative(1.03f, 1.03f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -2.059f)
                    horizontalLineToRelative(10.364f)
                    arcToRelative(1.03f, 1.03f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.029f, 1.03f)
                    close()
                    moveTo(8.03f, 4.586f)
                    arcToRelative(1.47f, 1.47f, 0f, isMoreThanHalf = true, isPositiveArc = true, 1.47f, 1.47f)
                    arcToRelative(1.47f, 1.47f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.47f, -1.47f)
                    close()
                    moveTo(10.97f, 13.779f)
                    arcToRelative(1.47f, 1.47f, 0f, isMoreThanHalf = true, isPositiveArc = true, -1.47f, -1.47f)
                    arcToRelative(1.47f, 1.47f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.47f, 1.47f)
                    close()
                }
            }.build()

            return _Division!!
        }

    @Suppress("ObjectPropertyName")
    private var _Division: ImageVector? = null
}



