package org.hermeslauncher.app.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun InboxHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val archived by app.vault.archivedItems.collectAsStateWithLifecycle(emptyList())
    val filesDir = context.applicationContext.filesDir
    val scope = rememberCoroutineScope()
    Column(modifier = modifier.fillMaxSize().padding(SpacingMd)) {
        Text(
            text = stringResource(R.string.inbox_history),
            style = MaterialTheme.typography.headlineSmall,
        )
        if (archived.isEmpty()) {
            Text(
                text = stringResource(R.string.inbox_history_empty),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = SpacingMd),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                items(archived, key = { it.id }) { item ->
                    VaultItemCard(
                        item = item,
                        imageDir = filesDir,
                        showDismiss = false,
                        onDismiss = {},
                        onPin = { scope.launch { app.vault.togglePin(item.id) } },
                        onOpen = { scope.launch { app.vault.open(item.id) } },
                        onAction = { index -> scope.launch { app.vault.runAction(item.id, index) } },
                    )
                }
            }
        }
        Button(onClick = onBack, modifier = Modifier.padding(top = SpacingMd)) {
            Text(stringResource(R.string.settings_close))
        }
    }
}
