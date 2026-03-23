package theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class DynamicTypography(
    val displayLarge: TextUnit = 32.sp,
    val displayMedium: TextUnit = 28.sp,
    val displaySmall: TextUnit = 24.sp,
    val titleLarge: TextUnit = 22.sp,
    val titleMedium: TextUnit = 18.sp,
    val titleSmall: TextUnit = 14.sp,
    val bodyLarge: TextUnit = 16.sp,
    val bodyMedium: TextUnit = 14.sp,
    val bodySmall: TextUnit = 12.sp,
    val labelLarge: TextUnit = 12.sp,
    val labelMedium: TextUnit = 10.sp,
    val labelSmall: TextUnit = 8.sp
)

fun calculateTypography(screenWidth: Int): DynamicTypography {
    val base = screenWidth.toFloat()
    return DynamicTypography(
        displayLarge = (base * 0.09f).sp,
        displayMedium = (base * 0.08f).sp,
        displaySmall = (base * 0.07f).sp,
        titleLarge = (base * 0.06f).sp,
        titleMedium = (base * 0.05f).sp,
        titleSmall = (base * 0.04f).sp,
        bodyLarge = (base * 0.045f).coerceAtLeast(14f).sp,
        bodyMedium = (base * 0.04f).coerceAtLeast(12f).sp,
        bodySmall = (base * 0.035f).coerceAtLeast(10f).sp,
        labelLarge = (base * 0.035f).coerceAtLeast(10f).sp,
        labelMedium = (base * 0.03f).coerceAtLeast(9f).sp,
        labelSmall = (base * 0.025f).coerceAtLeast(8f).sp
    )
}

val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

internal val LocalDynamicTypography = staticCompositionLocalOf { DynamicTypography() }
