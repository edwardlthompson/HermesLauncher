package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.DrawerKind
import org.hermeslauncher.app.feeds.DrawerRow
import org.hermeslauncher.app.ui.scroll.OverflowScrubBar
import org.hermeslauncher.app.ui.scroll.scrubGutter
import org.hermeslauncher.app.ui.theme.SpacingSm

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
            confirmLabel = if (prompt.all) R.string.feed_unsubscribe_all else R.string.feed_unsubscribe,
            destructive = true,
            onDismiss = onDismiss,
            onConfirm = {
                onUnsubscribe(prompt.row, prompt.all)
                onDismiss()
            },
        )
        is DrawerPrompt.DeleteFolder -> ConfirmDialog(
            title = stringResource(R.string.feed_delete_folder),
            confirmLabel = R.string.feed_delete_folder,
            destructive = true,
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
    val folderScroll = rememberScrollState()
    val choices = remember(folders, row.tag) {
        (listOf("") + folders.filter { it != row.tag }).distinct()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feed_move_folder)) },
        text = {
            Box(modifier = Modifier.heightIn(max = 320.dp)) {
                Column(modifier = Modifier.verticalScroll(folderScroll).scrubGutter()) {
                    choices.forEach { tag ->
                        val label = tag.ifBlank { stringResource(R.string.feed_move_none) }
                        ListItem(
                            headlineContent = { Text(label) },
                            modifier = Modifier.clickable { onPick(tag) },
                        )
                    }
                    if (row.kind == DrawerKind.FEED) {
                        OutlinedTextField(
                            value = created,
                            onValueChange = { created = it },
                            label = { Text(stringResource(R.string.feed_new_folder)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = SpacingSm),
                        )
                    }
                }
                OverflowScrubBar(
                    scroll = folderScroll,
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                )
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
                Text(stringResource(R.string.dialog_ok))
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
    confirmLabel: Int,
    destructive: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val confirmColors = if (destructive) {
        ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
    } else {
        ButtonDefaults.textButtonColors()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = confirmColors) {
                Text(stringResource(confirmLabel))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.feeds_add_cancel)) }
        },
    )
}
