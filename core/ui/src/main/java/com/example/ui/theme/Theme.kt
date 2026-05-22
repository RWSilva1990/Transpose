package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.util.constants.AppColors

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.BlueBackground,
    onPrimary = AppColors.WhiteLetter,
    primaryContainer = AppColors.StatusBarBackground,
    onPrimaryContainer = AppColors.WhiteLetter,

    secondary = AppColors.StatusBarBackground,
    onSecondary = AppColors.WhiteLetter,
    secondaryContainer = AppColors.CharcoalGray,
    onSecondaryContainer = AppColors.WhiteLetter,

    tertiary = AppColors.BlueBackground,
    onTertiary = AppColors.WhiteLetter,

    background = AppColors.BlackBackground,
    onBackground = AppColors.WhiteLetter,

    surface = Color(0xFF1E1E1E),
    onSurface = AppColors.WhiteLetter,

    surfaceVariant = AppColors.CharcoalGray,
    onSurfaceVariant = AppColors.DrawerLine,

    surfaceContainerLowest = Color(0xFF0F1729),
    surfaceContainerLow = Color(0xFF141B30),
    surfaceContainer = Color(0xFF182039),
    surfaceContainerHigh = Color(0xFF1A2238),
    surfaceContainerHighest = Color(0xFF222B45),

    outline = AppColors.DrawerLine,
    outlineVariant = AppColors.CharcoalGray,

    error = Color(0xFFFF6B6B),
    onError = AppColors.WhiteLetter,
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.BlueBackground,
    onPrimary = AppColors.WhiteLetter,
    primaryContainer = AppColors.BlueBackgroundAlpha30,
    onPrimaryContainer = AppColors.StatusBarBackground,

    secondary = AppColors.StatusBarBackground,
    onSecondary = AppColors.WhiteLetter,
    secondaryContainer = AppColors.LightGray,
    onSecondaryContainer = AppColors.BlackBackground,

    tertiary = AppColors.DarkBlue,
    onTertiary = AppColors.WhiteLetter,

    background = AppColors.WhiteLetter,
    onBackground = AppColors.BlackBackground,

    surface = AppColors.WhiteLetter,
    onSurface = AppColors.BlackBackground,

    surfaceVariant = AppColors.OutsideBackground,
    onSurfaceVariant = AppColors.DescriptionColor,

    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F8FC),
    surfaceContainer = Color(0xFFEFF3F9),
    surfaceContainerHigh = Color(0xFFE8EEF7),
    surfaceContainerHighest = Color(0xFFE0E8F4),

    outline = AppColors.DrawerLine,
    outlineVariant = AppColors.LightGray,

    error = Color(0xFFBA1A1A),
    onError = AppColors.WhiteLetter,
)

@Composable
fun TransposeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
