package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TvColorScheme = darkColorScheme(
    primary = GoogleBlueLight,
    onPrimary = TvBackground,
    primaryContainer = GoogleBlue,
    onPrimaryContainer = TvTextPrimary,
    secondary = GoogleGreen,
    onSecondary = TvBackground,
    tertiary = GoogleYellow,
    onTertiary = TvBackground,
    background = TvBackground,
    onBackground = TvTextPrimary,
    surface = TvSurface,
    onSurface = TvTextPrimary,
    surfaceVariant = TvSurfaceVariant,
    onSurfaceVariant = TvTextSecondary,
    outline = TvSurfaceHighlight,
    error = GoogleRed
)

@Composable
fun TvPhotoPickerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TvColorScheme,
        typography = Typography,
        content = content
    )
}
