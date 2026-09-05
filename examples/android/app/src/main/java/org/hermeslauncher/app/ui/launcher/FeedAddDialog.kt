package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun FeedAddDialog(
    onDismiss: () -> Unit,
    onAdd: suspend (String) -> Boolean,
) {
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(R.string.feeds_add_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        failed = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                    singleLine = true,
                    label = { Text(stringResource(R.string.feeds_add_url)) },
                    isError = failed,
                )
                if (failed) {
                    Text(
                        text = stringResource(R.string.feeds_add_error),
                        modifier = Modifier.padding(top = SpacingSm),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy && url.isNotBlank(),
                onClick = {
                    scope.launch {
                        busy = true
                        val ok = onAdd(url)
                        busy = false
                        if (ok) {
                            onDismiss()
                        } else {
                            failed = true
                        }
                    }
                },
            ) {
                Text(stringResource(R.string.feeds_add_confirm))
            }
        },
        dismissButton = {
            TextButton(enabled = !busy, onClick = onDismiss) {
                Text(stringResource(R.string.feeds_add_cancel))
            }
        },
    )
}
