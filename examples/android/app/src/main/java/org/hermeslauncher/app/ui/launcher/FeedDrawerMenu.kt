package org.hermeslauncher.app.ui.launcher

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
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
        ActionItem(
            label = stringResource(R.string.feed_mark_all_read),
            icon = Icons.Filled.DoneAll,
            onClick = { onPrompt(DrawerPrompt.MarkRead(row)) },
        )
        if (row.kind == DrawerKind.FEED) {
            ActionItem(
                label = stringResource(R.string.feed_move_folder),
                icon = Icons.AutoMirrored.Filled.DriveFileMove,
                onClick = { onPrompt(DrawerPrompt.Move(row)) },
            )
            ActionItem(
                label = stringResource(R.string.feed_drawer_feed_settings),
                icon = Icons.Filled.Settings,
                onClick = { onPrompt(DrawerPrompt.Settings(row)) },
            )
            HorizontalDivider()
            ActionItem(
                label = stringResource(R.string.feed_unsubscribe),
                icon = Icons.Filled.LinkOff,
                onClick = { onPrompt(DrawerPrompt.Unsubscribe(row, all = false)) },
                destructive = true,
            )
        } else {
            ActionItem(
                label = stringResource(R.string.feed_rename_folder),
                icon = Icons.Filled.Edit,
                onClick = { onPrompt(DrawerPrompt.Rename(row)) },
            )
            ActionItem(
                label = stringResource(R.string.feed_move_folder),
                icon = Icons.AutoMirrored.Filled.DriveFileMove,
                onClick = { onPrompt(DrawerPrompt.Move(row)) },
            )
            ActionItem(
                label = stringResource(R.string.feed_drawer_feed_settings),
                icon = Icons.Filled.Settings,
                onClick = { onPrompt(DrawerPrompt.Settings(row)) },
            )
            HorizontalDivider()
            ActionItem(
                label = stringResource(R.string.feed_unsubscribe_all),
                icon = Icons.Filled.LinkOff,
                onClick = { onPrompt(DrawerPrompt.Unsubscribe(row, all = true)) },
                destructive = true,
            )
            ActionItem(
                label = stringResource(R.string.feed_delete_folder),
                icon = Icons.Filled.Delete,
                onClick = { onPrompt(DrawerPrompt.DeleteFolder(row)) },
                destructive = true,
            )
        }
    }
}

@Composable
private fun ActionItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    val tint = if (destructive) MaterialTheme.colorScheme.error else LocalContentColor.current
    DropdownMenuItem(
        text = { Text(text = label, color = tint) },
        onClick = onClick,
        leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = tint) },
    )
}
