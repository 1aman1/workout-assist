package com.example.workoutassist.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MintPrimary,
    onPrimary = DarkPage,
    primaryContainer = DarkSurfaceMuted,
    onPrimaryContainer = DarkTextStrong,
    secondary = GoldAccent,
    onSecondary = DarkPage,
    secondaryContainer = CoralAccent.copy(alpha = 0.32f),
    onSecondaryContainer = DarkTextStrong,
    tertiary = CoralAccent,
    onTertiary = DarkPage,
    tertiaryContainer = DarkSurfaceMuted,
    onTertiaryContainer = DarkTextStrong,
    background = DarkPage,
    onBackground = DarkTextStrong,
    surface = DarkSurface,
    onSurface = DarkTextStrong,
    surfaceVariant = DarkSurfaceMuted,
    onSurfaceVariant = DarkTextStrong.copy(alpha = 0.82f),
    outline = OutlineSoft.copy(alpha = 0.58f)
)

private val LightColorScheme = lightColorScheme(
    primary = MintPrimary,
    onPrimary = SurfaceStrong,
    primaryContainer = MintPrimaryContainer,
    onPrimaryContainer = TextStrong,
    secondary = GoldAccent,
    onSecondary = TextStrong,
    secondaryContainer = SurfaceMuted,
    onSecondaryContainer = TextStrong,
    tertiary = CoralAccent,
    onTertiary = SurfaceStrong,
    tertiaryContainer = CoralAccent.copy(alpha = 0.16f),
    onTertiaryContainer = TextStrong,
    background = PageBackground,
    onBackground = TextStrong,
    surface = SurfaceStrong,
    onSurface = TextStrong,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextMuted,
    outline = OutlineSoft
)

@Composable
fun WorkoutAssistTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}