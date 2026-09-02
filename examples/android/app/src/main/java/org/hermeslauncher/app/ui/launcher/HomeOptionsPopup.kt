package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun HomeOptionsPopup(
    visible: Boolean,
    onWidgets: () -> Unit,
    onSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingMd),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OptionAction(
                    icon = Icons.Filled.Wallpaper,
                    label = stringResource(R.string.home_option_wallpaper),
                    onClick = {
                        WallpaperIntents.startOrToast(context)
                        onDismiss()
                    },
                )
                OptionAction(
                    icon = Icons.Filled.Animation,
                    label = stringResource(R.string.home_option_live_wallpaper),
                    onClick = {
                        WallpaperIntents.startLiveOrToast(context)
                        onDismiss()
                    },
                )
                OptionAction(
                    icon = Icons.Filled.Widgets,
                    label = stringResource(R.string.home_option_widgets),
                    onClick = onWidgets,
                )
                OptionAction(
                    icon = Icons.Filled.Settings,
                    label = stringResource(R.string.home_option_settings),
                    onClick = onSettings,
                )
            }
        }
    }
}

@Composable
private fun OptionAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .widthIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(SpacingSm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = icon, contentDescription = label)
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}
