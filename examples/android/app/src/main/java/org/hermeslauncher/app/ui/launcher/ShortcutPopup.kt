package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Popup
import org.hermeslauncher.app.icons.AppShortcuts
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.ui.theme.RadiusMd
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ShortcutPopup(
    app: LaunchableApp?,
    onDismiss: () -> Unit,
) {
    if (app == null) {
        return
    }
    val context = LocalContext.current
    val shortcuts = remember(app.packageName) { AppShortcuts.list(context, app.packageName) }
    if (shortcuts.isEmpty()) {
        LaunchedEffect(app.packageName) { onDismiss() }
        return
    }
    Popup(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(RadiusMd),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = SpacingSm,
            shadowElevation = SpacingSm,
        ) {
            Column(modifier = Modifier.padding(SpacingSm)) {
                shortcuts.forEach { shortcut ->
                    Text(
                        text = shortcut.shortLabel?.toString() ?: shortcut.id,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                AppShortcuts.start(context, shortcut)
                                onDismiss()
                            }
                            .padding(SpacingMd),
                    )
                }
            }
        }
    }
}
