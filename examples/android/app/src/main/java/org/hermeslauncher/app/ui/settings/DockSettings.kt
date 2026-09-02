package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import org.hermeslauncher.app.oem.LivePermissions
import org.hermeslauncher.app.ui.theme.SpacingMd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DockSettings() {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val dock by app.dockStore.layout.collectAsStateWithLifecycle(DockLayout())
    val usageOk = LivePermissions.usageGranted(context)
    Text(text = stringResource(R.string.settings_dock_mode), style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
        FilterChip(
            selected = dock.mode == DockMode.USAGE,
            onClick = { scope.launch { app.dockStore.save(dock.copy(mode = DockMode.USAGE)) } },
            label = { Text(stringResource(R.string.settings_dock_usage)) },
        )
        FilterChip(
            selected = dock.mode == DockMode.CUSTOM,
            onClick = {
                scope.launch {
                    val pm = context.packageManager
                    val next = if (dock.assigned.isEmpty()) {
                        AppCatalog.seeded(pm).copy(mode = DockMode.CUSTOM)
                    } else {
                        dock.copy(mode = DockMode.CUSTOM)
                    }
                    app.dockStore.save(next)
                }
            },
            label = { Text(stringResource(R.string.settings_dock_custom)) },
        )
    }
    if (!usageOk) {
        Text(
            text = stringResource(R.string.settings_usage_body),
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = {
            val intent = LivePermissions.usageSettings()
            LivePermissions.startSafe(context, intent)
        }) {
            Text(stringResource(R.string.settings_usage_open))
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
    Text(text = stringResource(R.string.settings_auto_delete))
    Text(text = stringResource(R.string.settings_auto_delete_body), style = MaterialTheme.typography.bodySmall)
    Switch(
        checked = autoDelete,
        onCheckedChange = { on ->
            scope.launch {
                app.inboxPrefs.setAutoDelete(on)
                app.vault.prune(force = true)
            }
        },
    )
}
