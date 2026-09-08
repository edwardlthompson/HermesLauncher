package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.AppCatalog
import org.hermeslauncher.app.icons.DockLayout
import org.hermeslauncher.app.icons.DockMode
import org.hermeslauncher.app.icons.HotseatPolicy
import org.hermeslauncher.app.oem.LivePermissions
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun DockSettings() {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val dock by app.dockStore.layout.collectAsStateWithLifecycle(DockLayout())
    val usageOk = LivePermissions.usageGranted(context)
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        SettingsSwitchRow(
            title = R.string.settings_dock_most_used,
            body = R.string.settings_dock_most_used_body,
            checked = dock.mode == DockMode.USAGE,
            onCheckedChange = { on ->
                scope.launch {
                    val mode = if (on) DockMode.USAGE else DockMode.CUSTOM
                    val next = if (mode == DockMode.CUSTOM && dock.assigned.isEmpty()) {
                        AppCatalog.seeded(context.packageManager).copy(mode = DockMode.CUSTOM)
                    } else {
                        dock.copy(mode = mode)
                    }
                    app.dockStore.save(next)
                }
            },
        )
        SettingsDropdown(
            title = stringResource(R.string.dock_pages),
            options = (HotseatPolicy.MIN_PAGES..HotseatPolicy.MAX_PAGES).toList(),
            selected = dock.pageCount,
            labelOf = { pages -> pages.toString() },
            onSelect = { pages -> scope.launch { app.dockStore.save(dock.copy(pageCount = pages)) } },
        )
        if (!usageOk) {
            Text(
                text = stringResource(R.string.settings_usage_body),
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = {
                LivePermissions.startSafe(context, LivePermissions.usageSettings())
            }) {
                Text(stringResource(R.string.settings_usage_open))
            }
        }
    }
}

@Composable
fun InboxRetentionSettings(onHistory: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val autoDelete by app.inboxPrefs.autoDelete.collectAsStateWithLifecycle(true)
    ListItem(
        headlineContent = { Text(stringResource(R.string.inbox_history)) },
        supportingContent = { Text(stringResource(R.string.inbox_history_body)) },
        modifier = Modifier.clickable(onClick = onHistory),
    )
    SettingsSwitchRow(
        title = R.string.settings_auto_delete,
        body = R.string.settings_auto_delete_body,
        checked = autoDelete,
        onCheckedChange = { on ->
            scope.launch {
                app.inboxPrefs.setAutoDelete(on)
                app.vault.prune(force = true)
            }
        },
    )
}
