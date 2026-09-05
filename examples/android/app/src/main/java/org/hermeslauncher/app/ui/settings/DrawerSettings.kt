package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.AppCatalog
import org.hermeslauncher.app.icons.DrawerPolicy
import org.hermeslauncher.app.icons.DrawerSnapshot
import org.hermeslauncher.app.ui.theme.SpacingMd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrawerSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val snapshot by app.drawerPrefs.snapshot.collectAsStateWithLifecycle(DrawerSnapshot())
    val policies by app.vault.policies.collectAsStateWithLifecycle(emptyList())
    val ignored = policies.filter { !it.storeContent }
    val launchables = AppCatalog.launchables(context.packageManager)
    var hideQuery by remember { mutableStateOf("") }
    var ignoreQuery by remember { mutableStateOf("") }
    val hiddenMatches = remember(hideQuery, launchables, snapshot.hidden) {
        DrawerPolicy.picks(launchables, hideQuery, snapshot.hidden)
    }
    val ignoreMatches = remember(ignoreQuery, launchables, ignored) {
        DrawerPolicy.picks(launchables, ignoreQuery, ignored.map { it.packageName }.toSet())
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.drawer_columns), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            (DrawerPolicy.COLUMNS_MIN..DrawerPolicy.COLUMNS_MAX).forEach { columns ->
                FilterChip(
                    selected = snapshot.columns == columns,
                    onClick = { scope.launch { app.drawerPrefs.setColumns(columns) } },
                    label = { Text(columns.toString()) },
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            FilterChip(
                selected = !snapshot.listMode,
                onClick = { scope.launch { app.drawerPrefs.setListMode(false) } },
                label = { Text(stringResource(R.string.drawer_layout_grid)) },
            )
            FilterChip(
                selected = snapshot.listMode,
                onClick = { scope.launch { app.drawerPrefs.setListMode(true) } },
                label = { Text(stringResource(R.string.drawer_layout_list)) },
            )
        }
        ListItem(
            headlineContent = { Text(stringResource(R.string.drawer_rail)) },
            trailingContent = {
                Switch(
                    checked = snapshot.showRail,
                    onCheckedChange = { value -> scope.launch { app.drawerPrefs.setShowRail(value) } },
                )
            },
        )
        Text(text = stringResource(R.string.drawer_hidden_title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.drawer_hidden_body), style = MaterialTheme.typography.bodySmall)
        InstalledAppPicker(
            query = hideQuery,
            onQueryChange = { hideQuery = it },
            matches = hiddenMatches,
            onPick = { picked ->
                scope.launch { app.drawerPrefs.hide(picked.packageName) }
                hideQuery = ""
            },
            label = stringResource(R.string.drawer_hidden_add),
        )
        if (snapshot.hidden.isEmpty()) {
            Text(text = stringResource(R.string.drawer_hidden_empty), style = MaterialTheme.typography.bodySmall)
        }
        snapshot.hidden.sorted().forEach { pkg ->
            val stop = stringResource(R.string.drawer_hidden_stop, pkg)
            ListItem(
                headlineContent = { Text(pkg) },
                trailingContent = {
                    IconButton(onClick = { scope.launch { app.drawerPrefs.show(pkg) } }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = stop)
                    }
                },
            )
        }
        Text(text = stringResource(R.string.blacklist_title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.blacklist_body), style = MaterialTheme.typography.bodySmall)
        InstalledAppPicker(
            query = ignoreQuery,
            onQueryChange = { ignoreQuery = it },
            matches = ignoreMatches,
            onPick = { picked ->
                scope.launch { app.vault.blacklist(picked.packageName) }
                ignoreQuery = ""
            },
            label = stringResource(R.string.blacklist_add),
        )
        if (ignored.isEmpty()) {
            Text(text = stringResource(R.string.blacklist_empty), style = MaterialTheme.typography.bodySmall)
        }
        ignored.forEach { policy ->
            val stop = stringResource(R.string.blacklist_stop, policy.packageName)
            ListItem(
                headlineContent = { Text(policy.packageName) },
                trailingContent = {
                    IconButton(onClick = { scope.launch { app.vault.unblacklist(policy.packageName) } }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = stop)
                    }
                },
            )
        }
    }
}
