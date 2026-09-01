package org.hermeslauncher.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.ThemeMode

@Composable
fun ThemeToggle(
    themeMode: ThemeMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = when (themeMode) {
        ThemeMode.System -> Icons.Outlined.BrightnessAuto
        ThemeMode.Light -> Icons.Outlined.Brightness6
        ThemeMode.Dark -> Icons.Outlined.Brightness4
    }
    val description = when (themeMode) {
        ThemeMode.System -> stringResource(R.string.theme_mode_system)
        ThemeMode.Light -> stringResource(R.string.theme_mode_light)
        ThemeMode.Dark -> stringResource(R.string.theme_mode_dark)
    }

    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = description,
        )
    }
}
