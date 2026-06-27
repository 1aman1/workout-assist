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
    primary = DoneGreen,
    onPrimary = DarkPage,
    primaryContainer = DoneGreen.copy(alpha = 0.34f),
    onPrimaryContainer = DarkTextStrong,
    secondary = StatusTurquoise,
    onSecondary = DarkPage,
    secondaryContainer = StatusTurquoise.copy(alpha = 0.3f),
    onSecondaryContainer = DarkTextStrong,
    tertiary = StatusTurquoise,
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
    primary = DoneGreen,
    onPrimary = SurfaceStrong,
    primaryContainer = DoneGreenContainer,
    onPrimaryContainer = TextStrong,
    secondary = StatusTurquoise,
    onSecondary = TextStrong,
    secondaryContainer = StatusTurquoiseContainer,
    onSecondaryContainer = TextStrong,
    tertiary = StatusTurquoise,
    onTertiary = SurfaceStrong,
    tertiaryContainer = StatusTurquoiseContainer.copy(alpha = 0.45f),
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