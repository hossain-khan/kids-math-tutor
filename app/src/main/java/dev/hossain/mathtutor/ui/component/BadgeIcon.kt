package dev.hossain.mathtutor.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.hossain.mathtutor.domain.model.BadgeIcon
import dev.hossain.mathtutor.util.BadgeIconMapper

/**
 * Composable for displaying badge icons from BadgeIcon enum.
 *
 * Displays badge images with optional color filter for dimming locked badges.
 * Supports various sizes for different use cases (small, medium, large).
 * Maps BadgeIcon enum to drawable resource ID at runtime for build stability.
 *
 * @param badgeIcon BadgeIcon enum value
 * @param contentDescription Description of the badge for accessibility
 * @param modifier Optional modifier for the Image composable
 * @param size Size of the badge icon in dp (default 48.dp)
 * @param colorFilter Optional color filter for applying effects like dimming
 */
@Composable
fun BadgeIcon(
    badgeIcon: BadgeIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    colorFilter: ColorFilter? = null,
) {
    Image(
        painter = painterResource(id = BadgeIconMapper.toDrawableRes(badgeIcon)),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit,
        colorFilter = colorFilter,
    )
}
