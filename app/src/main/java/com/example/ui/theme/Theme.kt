package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SubTrackPrimaryDark,
    onPrimary = SubTrackOnPrimaryDark,
    primaryContainer = SubTrackPrimaryContainerDark,
    onPrimaryContainer = SubTrackOnPrimaryContainerDark,
    secondary = SubTrackSecondaryDark,
    onSecondary = SubTrackOnSecondaryDark,
    secondaryContainer = SubTrackSecondaryContainerDark,
    onSecondaryContainer = SubTrackOnSecondaryContainerDark,
    tertiary = SubTrackTertiaryDark,
    onTertiary = SubTrackOnTertiaryDark,
    tertiaryContainer = SubTrackTertiaryContainerDark,
    onTertiaryContainer = SubTrackOnTertiaryContainerDark,
    background = SubTrackBackgroundDark,
    onBackground = SubTrackOnBackgroundDark,
    surface = SubTrackSurfaceDark,
    onSurface = SubTrackOnSurfaceDark,
    surfaceVariant = SubTrackSurfaceVariantDark,
    onSurfaceVariant = SubTrackOnSurfaceVariantDark,
    outline = SubTrackOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = SubTrackPrimaryLight,
    onPrimary = SubTrackOnPrimaryLight,
    primaryContainer = SubTrackPrimaryContainerLight,
    onPrimaryContainer = SubTrackOnPrimaryContainerLight,
    secondary = SubTrackSecondaryLight,
    onSecondary = SubTrackOnSecondaryLight,
    secondaryContainer = SubTrackSecondaryContainerLight,
    onSecondaryContainer = SubTrackOnSecondaryContainerLight,
    tertiary = SubTrackTertiaryLight,
    onTertiary = SubTrackOnTertiaryLight,
    tertiaryContainer = SubTrackTertiaryContainerLight,
    onTertiaryContainer = SubTrackOnTertiaryContainerLight,
    background = SubTrackBackgroundLight,
    onBackground = SubTrackOnBackgroundLight,
    surface = SubTrackSurfaceLight,
    onSurface = SubTrackOnSurfaceLight,
    surfaceVariant = SubTrackSurfaceVariantLight,
    onSurfaceVariant = SubTrackOnSurfaceVariantLight,
    outline = SubTrackOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false so custom fintech palette shines through
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
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
