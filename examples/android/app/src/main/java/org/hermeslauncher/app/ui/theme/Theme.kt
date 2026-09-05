package org.hermeslauncher.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.hermeslauncher.app.ui.insets.ApplySystemBarStyle
import java.util.Calendar

@Composable
fun HermesTheme(
    themeMode: ThemeMode,
    nightSchedule: NightSchedule = NightSchedule.OFF,
    wallpaperSeed: Color? = null,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val cal = Calendar.getInstance()
    val nowMinute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val darkTheme = ThemeResolve.isDark(themeMode, nightSchedule, systemDark, nowMinute)
    val base = if (darkTheme) DarkHermesColors else LightHermesColors
    val colorScheme = wallpaperSeed?.let { seed ->
        tintScheme(base, seed, darkTheme)
    } ?: base

    ApplySystemBarStyle(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HermesTypography,
        content = content,
    )
}

private fun tintScheme(base: ColorScheme, seed: Color, dark: Boolean): ColorScheme {
    return if (dark) {
        darkColorScheme(
            primary = seed,
            onPrimary = base.onPrimary,
            secondary = base.secondary,
            onSecondary = base.onSecondary,
            tertiary = base.tertiary,
            onTertiary = base.onTertiary,
            background = base.background,
            onBackground = base.onBackground,
            surface = base.surface,
            onSurface = base.onSurface,
            error = base.error,
            onError = base.onError,
        )
    } else {
        lightColorScheme(
            primary = seed,
            onPrimary = base.onPrimary,
            secondary = base.secondary,
            onSecondary = base.onSecondary,
            tertiary = base.tertiary,
            onTertiary = base.onTertiary,
            background = base.background,
            onBackground = base.onBackground,
            surface = base.surface,
            onSurface = base.onSurface,
            error = base.error,
            onError = base.onError,
        )
    }
}
