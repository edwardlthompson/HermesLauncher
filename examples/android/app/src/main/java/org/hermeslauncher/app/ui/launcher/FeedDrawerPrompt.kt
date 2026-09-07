package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingSm
import org.hermeslauncher.app.feeds.DrawerKind
import org.hermeslauncher.app.feeds.DrawerRow

@Composable
internal fun FeedDrawerPromptHost(
    prompt: DrawerPrompt?,
    folders: List<String>,
    onDismiss: () -> Unit,
    onMove: (DrawerRow, String) -> Unit,
    onRename: (DrawerRow, String) -> Unit,
    onUnsubscribe: (DrawerRow, Boolean) -> Unit,
    onDeleteFolder: (DrawerRow) -> Unit,
    onMarkRead: (DrawerRow) -> Unit,
    onSettings: (DrawerRow) -> Unit,
) {
    LaunchedEffect(prompt) {
        when (prompt) {
            is DrawerPrompt.MarkRead -> {
                onMarkRead(prompt.row)
                onDismiss()
            }
            is DrawerPrompt.Settings -> {
                onSettings(prompt.row)
                onDismiss()
            }
            else -> Unit
        }
    }
    when (prompt) {
        null, is DrawerPrompt.MarkRead, is DrawerPrompt.Settings -> Unit
        is DrawerPrompt.Move -> MoveFolderDialog(
            row = prompt.row,
            folders = folders,
            onDismiss = onDismiss,
            onPick = { tag ->
                onMove(prompt.row, tag)
                onDismiss()
            },
        )
        is DrawerPrompt.Rename -> NameDialog(
            title = stringResource(R.string.feed_rename_folder),
            initial = prompt.row.tag,
            onDismiss = onDismiss,
            onConfirm = { name ->
                onRename(prompt.row, name)
                onDismiss()
            },
        )
        is DrawerPrompt.Unsubscribe -> ConfirmDialog(
            title = stringResource(
                if (prompt.all) R.string.feed_unsubscribe_all else R.string.feed_unsubscribe,
            ),
            onDismiss = onDismiss,
            onConfirm = {
                onUnsubscribe(prompt.row, prompt.all)
                onDismiss()
            },
        )
        is DrawerPrompt.DeleteFolder -> ConfirmDialog(
            title = stringResource(R.string.feed_delete_folder),
            onDismiss = onDismiss,
            onConfirm = {
                onDeleteFolder(prompt.row)
                onDismiss()
            },
        )
    }
}

@Composable
private fun MoveFolderDialog(
    row: DrawerRow,
    folders: List<String>,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    var created by remember { mutableStateOf("") }
    val choices = remember(folders, row.tag) {
        (listOf("") + folders.filter { it != row.tag }).distinct()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feed_move_folder)) },
        text = {
            Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                choices.forEach { tag ->
                    val label = tag.ifBlank { stringResource(R.string.feed_move_none) }
                    Text(
                        text = label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(tag) }
                            .padding(vertical = SpacingSm),
                    )
                }
                if (row.kind == DrawerKind.FEED) {
                    OutlinedTextField(
                        value = created,
                        onValueChange = { created = it },
                        label = { Text(stringResource(R.string.feed_new_folder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = created.isNotBlank(),
                onClick = { onPick(created.trim()) },
            ) {
                Text(stringResource(R.string.feeds_add_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.feeds_add_cancel)) }
        },
    )
}

@Composable
private fun NameDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                enabled = value.isNotBlank(),
                onClick = { onConfirm(value.trim()) },
            ) {
                Text(stringResource(R.string.feeds_add_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.feeds_add_cancel)) }
        },
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.feeds_add_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.feeds_add_cancel)) }
        },
    )
}
