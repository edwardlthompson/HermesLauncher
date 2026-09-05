package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Column
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

@Composable
fun SearchSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val appRowCap by app.searchPrefs.appRowCap.collectAsStateWithLifecycle(true)
    Column(modifier = modifier) {
        Text(text = stringResource(R.string.search_local_only), style = MaterialTheme.typography.bodySmall)
        ListItem(
            headlineContent = { Text(stringResource(R.string.search_app_row)) },
            supportingContent = { Text(stringResource(R.string.search_app_row_body)) },
            trailingContent = {
                Switch(
                    checked = appRowCap,
                    onCheckedChange = { value -> scope.launch { app.searchPrefs.setAppRowCap(value) } },
                )
            },
        )
    }
}
