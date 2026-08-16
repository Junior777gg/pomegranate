package org.unstabledev.pomegranate.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.unstabledev.pomegranate.ThemeMode

class ColorTheme {
    companion object {
        val AccentDark = Color(0xFFF35331)
        val AccentLight = Color(0xFFF34723)
        val BackgroundLight = Color(0xFFF7FAFD)
        val SurfaceLight = Color(0xFFE7EBEF)
        val OnSurfaceLight = Color(0xFFA3A6A8)
        val BackgroundDark = Color(0xFF181A1C)
        val SurfaceDark = Color(0xFF292B2D)
        val OnSurfaceDark = Color(0xFF717376)
        val BackgroundAmoled = Color(0xFF000000)
        val SurfaceAmoled = Color(0xFF101011)
        val OnSurfaceAmoled = Color(0xFF1C1D1F)
        val Warning = Color(0xFFFF2929)
        val TextDark = Color(0xFFF9FBFF)
        val TextLight = Color(0xFF151617)

        val MyMessageBubble = Color(0xFF8BFF1A)
        val MessageAccent = Color(0xFF3390EC)
    }

    private val DarkColorScheme = darkColorScheme(
        primary = AccentDark,
        onPrimary = TextDark,
        background = BackgroundDark,
        onBackground = TextDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        error = Warning
    )

    private val AmoledColorScheme = darkColorScheme(
        primary = AccentLight,
        onPrimary = TextDark,
        background = BackgroundAmoled,
        onBackground = TextDark,
        surface = SurfaceAmoled,
        onSurface = OnSurfaceAmoled,
        error = Warning
    )

    private val LightColorScheme = lightColorScheme(
        primary = AccentLight,
        onPrimary = TextLight,
        background = BackgroundLight,
        onBackground = TextLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        error = Warning
    )

    @Composable
    fun AppTheme(
        theme: ThemeMode = ThemeMode.SYSTEM,
        shapes: Shapes = MaterialTheme.shapes,
        typography: Typography = MaterialTheme.typography,
        dynamicColor: Boolean = false,
        content: @Composable () -> Unit
    ) {
        val darkTheme = when(theme) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.AMOLED -> true
        }
        val colorScheme = if (theme==ThemeMode.AMOLED) AmoledColorScheme else if (darkTheme) DarkColorScheme else LightColorScheme

        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}