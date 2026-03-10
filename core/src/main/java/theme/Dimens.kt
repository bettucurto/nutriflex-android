package theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Dimens(
    val extraSmallPadding: Dp = 4.dp,
    val smallPadding: Dp = 8.dp,
    val mediumPadding: Dp = 16.dp,
    val largePadding: Dp = 20.dp,
    val extraLargePadding: Dp = 24.dp,
    val iconSmall: Dp = 20.dp,
    val iconMedium: Dp = 28.dp,
    val iconLarge: Dp = 40.dp,
    val buttonHeight: Dp = 48.dp,
    val bigTitleSize: TextUnit = 90.sp
)

val SmallDimens = Dimens(
    extraSmallPadding = 2.dp,
    smallPadding = 4.dp,
    mediumPadding = 10.dp,
    largePadding = 14.dp,
    extraLargePadding = 18.dp,
    iconSmall = 16.dp,
    iconMedium = 22.dp,
    iconLarge = 32.dp,
    buttonHeight = 40.dp,
    bigTitleSize = 85.sp
)

val DefaultDimens = Dimens()

val TabletDimens = Dimens(
    extraSmallPadding = 8.dp,
    smallPadding = 12.dp,
    mediumPadding = 24.dp,
    largePadding = 32.dp,
    extraLargePadding = 40.dp,
    iconSmall = 28.dp,
    iconMedium = 40.dp,
    iconLarge = 56.dp,
    buttonHeight = 56.dp,
    bigTitleSize = 140.sp
)

internal val LocalDimens = staticCompositionLocalOf { DefaultDimens }

object AppTheme {
    val dimens: Dimens
        @Composable
        @ReadOnlyComposable
        get() = LocalDimens.current
}
