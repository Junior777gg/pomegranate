package org.unstabledev.pomegranate.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.unstabledev.pomegranate.common.ThemeMode
import org.unstabledev.pomegranate.common.Util.Companion.mix
import org.unstabledev.pomegranate.platform.getSystemMaterialColor

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
        val SurfaceAmoled = Color(0xFF18181A)
        val OnSurfaceAmoled = Color(0xFF515158)
        val Warning = Color(0xFFFF2929)
        val TextDark = Color(0xFFF9FBFF)
        val TextLight = Color(0xFF151617)
        val MessageAccent = Color(0xFF3390EC)

        val DarkColorScheme = darkColorScheme(
            primary = AccentDark,
            onPrimary = TextDark,
            background = BackgroundDark,
            onBackground = TextDark,
            surface = SurfaceDark,
            onSurface = OnSurfaceDark,
            error = Warning
        )

        val AmoledColorScheme = darkColorScheme(
            primary = AccentLight,
            onPrimary = TextDark,
            background = BackgroundAmoled,
            onBackground = TextDark,
            surface = SurfaceAmoled,
            onSurface = OnSurfaceAmoled,
            error = Warning
        )

        val LightColorScheme = lightColorScheme(
            primary = AccentLight,
            onPrimary = TextLight,
            background = BackgroundLight,
            onBackground = TextLight,
            surface = SurfaceLight,
            onSurface = OnSurfaceLight,
            error = Warning
        )
    }

    @Composable
    fun AppTheme(
        theme: ThemeMode = ThemeMode.SYSTEM,
        useAmoledOnDarkSystem: Boolean = false,
        useMDColorPrinciple: Boolean = false,
        useMDSystemColor: Boolean = false,
        primaryColor: Color? = null,
        content: @Composable () -> Unit
    ) {
        val darkTheme = when(theme) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.AMOLED -> true
        }
        var colorScheme =
            if (theme==ThemeMode.AMOLED || (useAmoledOnDarkSystem && theme==ThemeMode.SYSTEM && darkTheme)) AmoledColorScheme
            else if (darkTheme) DarkColorScheme
            else LightColorScheme

        val mdCol=getSystemMaterialColor(darkTheme)
        if (useMDSystemColor && mdCol!=null) colorScheme=colorScheme.copy(primary = mdCol)
        if (primaryColor!=null) colorScheme=colorScheme.copy(primary = primaryColor)
        if (useMDColorPrinciple)
            colorScheme=colorScheme.copy(
                onPrimary = colorScheme.onPrimary.mix(colorScheme.primary, 0.15f),
                background = colorScheme.background.mix(colorScheme.primary, 0.15f),
                surface = colorScheme.surface.mix(colorScheme.primary, 0.3f),
                onSurface = colorScheme.onSurface.mix(colorScheme.primary, 0.45f),
            )

        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}