package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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

    outline = AppColors.DrawerLine,
    outlineVariant = AppColors.LightGray,

    error = Color(0xFFBA1A1A),
    onError = AppColors.WhiteLetter,
)

@Composable
fun TransposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
