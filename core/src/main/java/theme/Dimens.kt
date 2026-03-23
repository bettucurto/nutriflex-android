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
    val bigTitleSize: TextUnit = 90.sp,
    
    // Relative Dimensions (Percentages)
    val screenWidth: Dp = 0.dp,
    val screenHeight: Dp = 0.dp,
    val grid_1: Dp = 0.dp,
    val grid_2: Dp = 0.dp,
    val grid_3: Dp = 0.dp,
    val grid_4: Dp = 0.dp,
    val grid_5: Dp = 0.dp,
    val grid_6: Dp = 0.dp,
    val grid_8: Dp = 0.dp,
    val grid_10: Dp = 0.dp
)

fun calculateDimens(screenWidth: Int, screenHeight: Int): Dimens {
    val sw = screenWidth.dp
    val sh = screenHeight.dp
    
    return when {
        screenWidth < 360 -> Dimens(
            extraSmallPadding = 2.dp,
            smallPadding = 4.dp,
            mediumPadding = 10.dp,
            largePadding = 14.dp,
            extraLargePadding = 18.dp,
            iconSmall = 16.dp,
            iconMedium = 22.dp,
            iconLarge = 32.dp,
            buttonHeight = 40.dp,
            bigTitleSize = (screenWidth * 0.22f).sp,
            screenWidth = sw,
            screenHeight = sh,
            grid_1 = sw * 0.08f,
            grid_2 = sw * 0.16f,
            grid_3 = sw * 0.24f,
            grid_4 = sw * 0.32f,
            grid_5 = sw * 0.40f,
            grid_6 = sw * 0.48f,
            grid_8 = sw * 0.64f,
            grid_10 = sw * 0.80f
        )
        screenWidth > 600 -> Dimens(
            extraSmallPadding = 8.dp,
            smallPadding = 12.dp,
            mediumPadding = 24.dp,
            largePadding = 32.dp,
            extraLargePadding = 40.dp,
            iconSmall = 28.dp,
            iconMedium = 40.dp,
            iconLarge = 56.dp,
            buttonHeight = 56.dp,
            bigTitleSize = (screenWidth * 0.15f).sp,
            screenWidth = sw,
            screenHeight = sh,
            grid_1 = sw * 0.08f,
            grid_2 = sw * 0.16f,
            grid_3 = sw * 0.24f,
            grid_4 = sw * 0.32f,
            grid_5 = sw * 0.40f,
            grid_6 = sw * 0.48f,
            grid_8 = sw * 0.64f,
            grid_10 = sw * 0.80f
        )
        else -> Dimens(
            bigTitleSize = (screenWidth * 0.25f).sp,
            screenWidth = sw,
            screenHeight = sh,
            grid_1 = sw * 0.08f,
            grid_2 = sw * 0.16f,
            grid_3 = sw * 0.24f,
            grid_4 = sw * 0.32f,
            grid_5 = sw * 0.40f,
            grid_6 = sw * 0.48f,
            grid_8 = sw * 0.64f,
            grid_10 = sw * 0.80f
        )
    }
}

val DefaultDimens = Dimens()

internal val LocalDimens = staticCompositionLocalOf { DefaultDimens }
