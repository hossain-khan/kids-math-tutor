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
            _Division =
                ImageVector
                    .Builder(
                        name = "Division",
                        defaultWidth = 24.dp,
                        defaultHeight = 24.dp,
                        viewportWidth = 24f,
                        viewportHeight = 24f,
                    ).apply {
                        path(fill = SolidColor(Color(0xFF999999))) {
                            moveTo(5f, 11f)
                            lineToRelative(14f, 0f)
                            lineToRelative(0f, 2f)
                            lineToRelative(-14f, 0f)
                            close()
                        }
                        path(fill = SolidColor(Color(0xFF999999))) {
                            moveTo(9.838f, 8.512f)
                            curveTo(9.226f, 7.9f, 8.92f, 7.159f, 8.923f, 6.291f)
                            curveTo(8.926f, 5.422f, 9.235f, 4.679f, 9.852f, 4.062f)
                            curveTo(10.468f, 3.445f, 11.211f, 3.134f, 12.08f, 3.131f)
                            curveTo(12.949f, 3.127f, 13.689f, 3.432f, 14.302f, 4.044f)
                            curveTo(14.915f, 4.656f, 15.22f, 5.396f, 15.217f, 6.265f)
                            curveTo(15.215f, 7.134f, 14.905f, 7.877f, 14.289f, 8.494f)
                            curveTo(13.672f, 9.111f, 12.93f, 9.422f, 12.061f, 9.425f)
                            curveTo(11.192f, 9.429f, 10.451f, 9.124f, 9.838f, 8.512f)
                            close()
                            moveTo(11.114f, 7.235f)
                            curveTo(11.378f, 7.499f, 11.695f, 7.63f, 12.066f, 7.629f)
                            curveTo(12.437f, 7.628f, 12.755f, 7.494f, 13.021f, 7.228f)
                            curveTo(13.287f, 6.962f, 13.42f, 6.643f, 13.421f, 6.273f)
                            curveTo(13.422f, 5.902f, 13.291f, 5.584f, 13.027f, 5.32f)
                            curveTo(12.763f, 5.057f, 12.445f, 4.925f, 12.074f, 4.927f)
                            curveTo(11.703f, 4.928f, 11.385f, 5.062f, 11.119f, 5.328f)
                            curveTo(10.854f, 5.594f, 10.72f, 5.913f, 10.719f, 6.283f)
                            curveTo(10.718f, 6.654f, 10.85f, 6.972f, 11.114f, 7.235f)
                            close()
                            moveTo(9.804f, 19.956f)
                            curveTo(9.191f, 19.344f, 8.886f, 18.603f, 8.889f, 17.735f)
                            curveTo(8.891f, 16.866f, 9.201f, 16.123f, 9.817f, 15.506f)
                            curveTo(10.434f, 14.889f, 11.176f, 14.578f, 12.045f, 14.575f)
                            curveTo(12.914f, 14.571f, 13.655f, 14.876f, 14.268f, 15.488f)
                            curveTo(14.88f, 16.1f, 15.185f, 16.84f, 15.183f, 17.709f)
                            curveTo(15.18f, 18.578f, 14.871f, 19.321f, 14.254f, 19.938f)
                            curveTo(13.638f, 20.555f, 12.895f, 20.866f, 12.026f, 20.869f)
                            curveTo(11.157f, 20.873f, 10.417f, 20.568f, 9.804f, 19.956f)
                            close()
                            moveTo(11.079f, 18.679f)
                            curveTo(11.343f, 18.943f, 11.661f, 19.074f, 12.032f, 19.073f)
                            curveTo(12.403f, 19.071f, 12.721f, 18.938f, 12.987f, 18.672f)
                            curveTo(13.252f, 18.406f, 13.386f, 18.087f, 13.387f, 17.717f)
                            curveTo(13.388f, 17.346f, 13.256f, 17.028f, 12.992f, 16.764f)
                            curveTo(12.728f, 16.501f, 12.411f, 16.369f, 12.04f, 16.371f)
                            curveTo(11.669f, 16.372f, 11.351f, 16.506f, 11.085f, 16.772f)
                            curveTo(10.819f, 17.038f, 10.686f, 17.356f, 10.685f, 17.727f)
                            curveTo(10.684f, 18.098f, 10.815f, 18.416f, 11.079f, 18.679f)
                            close()
                        }
                    }.build()

            return _Division!!
        }

    @Suppress("ObjectPropertyName")
    private var _Division: ImageVector? = null
}
