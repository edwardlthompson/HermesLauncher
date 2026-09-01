package org.hermeslauncher.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.hermeslauncher.app.ui.insets.ApplySystemBarStyle

@Composable
fun HermesTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colorScheme = if (darkTheme) DarkHermesColors else LightHermesColors

    ApplySystemBarStyle(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HermesTypography,
        content = content,
    )
}
