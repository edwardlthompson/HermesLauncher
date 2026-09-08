package org.hermeslauncher.app.ui.launcher

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.FeedChip
import org.hermeslauncher.app.feeds.FeedQuery

@Composable
fun FeedFilterMenu(
    expanded: Boolean,
    query: FeedQuery,
    onDismiss: () -> Unit,
    onQuery: (FeedQuery) -> Unit,
    onMarkAllRead: () -> Unit = {},
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        RowItem(
            label = stringResource(R.string.filter_unread),
            icon = Icons.Filled.MarkEmailUnread,
            isSelected = query.chip == FeedChip.UNREAD,
            onClick = { onQuery(query.copy(chip = FeedChip.UNREAD)) },
        )
        RowItem(
            label = stringResource(R.string.feed_filter_read),
            icon = Icons.Filled.MarkEmailRead,
            isSelected = query.chip == FeedChip.READ,
            onClick = { onQuery(query.copy(chip = FeedChip.READ)) },
        )
        RowItem(
            label = stringResource(R.string.feed_filter_starred),
            icon = Icons.Filled.Star,
            isSelected = query.chip == FeedChip.STARRED,
            onClick = { onQuery(query.copy(chip = FeedChip.STARRED)) },
        )
        RowItem(
            label = stringResource(R.string.filter_all),
            icon = Icons.Filled.Inbox,
            isSelected = query.chip == FeedChip.ALL,
            onClick = { onQuery(query.copy(chip = FeedChip.ALL)) },
        )
        HorizontalDivider()
        RowItem(
            label = stringResource(R.string.filter_order_descending),
            icon = Icons.Filled.ArrowDownward,
            isSelected = query.newestFirst,
            onClick = { onQuery(query.copy(newestFirst = true)) },
        )
        RowItem(
            label = stringResource(R.string.filter_order_ascending),
            icon = Icons.Filled.ArrowUpward,
            isSelected = !query.newestFirst,
            onClick = { onQuery(query.copy(newestFirst = false)) },
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.feed_mark_all_read)) },
            onClick = {
                onMarkAllRead()
                onDismiss()
            },
            leadingIcon = { Icon(imageVector = Icons.Filled.DoneAll, contentDescription = null) },
        )
    }
}

@Composable
private fun RowItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = onClick,
        modifier = Modifier.semantics {
            selected = isSelected
            contentDescription = if (isSelected) "$label selected" else label
        },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        trailingIcon = if (isSelected) {
            { Icon(imageVector = Icons.Filled.Check, contentDescription = null) }
        } else {
            null
        },
    )
}
