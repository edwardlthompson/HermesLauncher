package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import org.hermeslauncher.app.icons.LaunchRecency
import org.hermeslauncher.app.vault.InboxFilter

@Composable
fun BlacklistSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val policies by app.vault.policies.collectAsStateWithLifecycle(emptyList())
    val ignored = InboxFilter.ignoredPackages(policies)
    val launchables = AppCatalog.launchables(context.packageManager)
    val recency = LaunchRecency.snapshot()
    var ignoreQuery by remember { mutableStateOf("") }
    val ignoreMatches = remember(ignoreQuery, launchables, ignored, recency) {
        DrawerPolicy.picks(launchables, ignoreQuery, ignored, lastUsed = recency)
    }
    val labels = remember(launchables) { launchables.associate { it.packageName to it.label } }
    Column(modifier = modifier) {
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
        ignored.sorted().forEach { pkg ->
            val stop = stringResource(R.string.blacklist_stop, labels[pkg] ?: pkg)
            ListItem(
                headlineContent = { Text(labels[pkg] ?: pkg) },
                supportingContent = { Text(pkg) },
                trailingContent = {
                    IconButton(onClick = { scope.launch { app.vault.unblacklist(pkg) } }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = stop)
                    }
                },
            )
        }
    }
}
