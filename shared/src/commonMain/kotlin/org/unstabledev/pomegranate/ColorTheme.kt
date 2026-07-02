package org.unstabledev.pomegranate

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class ColorTheme {
    companion object {
        val AccentDark = Color(0xFFF35331)
        val AccentLight = Color(0xFFF34723)
        val BackgroundLight = Color(0xFFF7FAFD)
        val BackgroundLightSurface = Color(0xFFBDBFC4)
        val BackgroundLightOnSurface = Color(0xFF313234)
        val BackgroundDark = Color(0xFF181A1C)
        val BackgroundDarkSurface = Color(0xFF2A2E32)
        val BackgroundDarkOnSurface = Color(0xFF646A6C)
        val Warning = Color(0xFFFF2929)
        val TextDark = Color(0xFFF9FBFF)
        val TextLight = Color(0xFF151617)
    }

    private val DarkColorScheme = darkColorScheme(
        primary = AccentDark,
        onPrimary = TextDark,
        background = BackgroundDark,
        onBackground = TextDark,
        surface = BackgroundDarkSurface,
        onSurface = BackgroundDarkOnSurface,
        error = Warning
    )

    private val LightColorScheme = lightColorScheme(
        primary = AccentLight,
        onPrimary = TextLight,
        background = BackgroundLight,
        onBackground = TextLight,
        surface = BackgroundLightSurface,
        onSurface = BackgroundLightOnSurface,
        error = Warning
    )

    @Composable
    fun AppTheme(
        colorScheme: ColorScheme = MaterialTheme.colorScheme,
        shapes: Shapes = MaterialTheme.shapes,
        typography: Typography = MaterialTheme.typography,
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = false,
        content: @Composable () -> Unit
    ) {
        val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}