package org.hermeslauncher.app.ui.launcher

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.DrawerKind
import org.hermeslauncher.app.feeds.DrawerRow

internal sealed class DrawerPrompt {
    data class Move(val row: DrawerRow) : DrawerPrompt()
    data class Rename(val row: DrawerRow) : DrawerPrompt()
    data class Unsubscribe(val row: DrawerRow, val all: Boolean) : DrawerPrompt()
    data class DeleteFolder(val row: DrawerRow) : DrawerPrompt()
    data class MarkRead(val row: DrawerRow) : DrawerPrompt()
    data class Settings(val row: DrawerRow) : DrawerPrompt()
}

@Composable
internal fun FeedDrawerMenu(
    row: DrawerRow,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onPrompt: (DrawerPrompt) -> Unit,
) {
    if (row.kind != DrawerKind.FEED && row.kind != DrawerKind.TAG) {
        return
    }
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.feed_mark_all_read)) },
            onClick = { onPrompt(DrawerPrompt.MarkRead(row)) },
        )
        if (row.kind == DrawerKind.FEED) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feed_move_folder)) },
                onClick = { onPrompt(DrawerPrompt.Move(row)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feed_unsubscribe)) },
                onClick = { onPrompt(DrawerPrompt.Unsubscribe(row, all = false)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feed_drawer_feed_settings)) },
                onClick = { onPrompt(DrawerPrompt.Settings(row)) },
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feed_rename_folder)) },
                onClick = { onPrompt(DrawerPrompt.Rename(row)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feed_move_folder)) },
                onClick = { onPrompt(DrawerPrompt.Move(row)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feed_delete_folder)) },
                onClick = { onPrompt(DrawerPrompt.DeleteFolder(row)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feed_unsubscribe_all)) },
                onClick = { onPrompt(DrawerPrompt.Unsubscribe(row, all = true)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feed_drawer_feed_settings)) },
                onClick = { onPrompt(DrawerPrompt.Settings(row)) },
            )
        }
    }
}
