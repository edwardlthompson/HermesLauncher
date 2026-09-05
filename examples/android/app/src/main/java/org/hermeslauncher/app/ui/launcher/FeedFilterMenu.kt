package org.hermeslauncher.app.ui.launcher

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            isSelected = query.chip == FeedChip.UNREAD,
            onClick = { onQuery(query.copy(chip = FeedChip.UNREAD)) },
        )
        RowItem(
            label = stringResource(R.string.feed_filter_read),
            isSelected = query.chip == FeedChip.READ,
            onClick = { onQuery(query.copy(chip = FeedChip.READ)) },
        )
        RowItem(
            label = stringResource(R.string.feed_filter_starred),
            isSelected = query.chip == FeedChip.STARRED,
            onClick = { onQuery(query.copy(chip = FeedChip.STARRED)) },
        )
        RowItem(
            label = stringResource(R.string.filter_all),
            isSelected = query.chip == FeedChip.ALL,
            onClick = { onQuery(query.copy(chip = FeedChip.ALL)) },
        )
        RowItem(
            label = stringResource(R.string.filter_order_descending),
            isSelected = query.newestFirst,
            onClick = { onQuery(query.copy(newestFirst = true)) },
        )
        RowItem(
            label = stringResource(R.string.filter_order_ascending),
            isSelected = !query.newestFirst,
            onClick = { onQuery(query.copy(newestFirst = false)) },
        )
        RowItem(
            label = stringResource(R.string.feed_mark_all_read),
            isSelected = false,
            onClick = {
                onMarkAllRead()
                onDismiss()
            },
        )
    }
}

@Composable
private fun RowItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = onClick,
        modifier = Modifier.semantics {
            selected = isSelected
            contentDescription = if (isSelected) "$label selected" else label
        },
        trailingIcon = if (isSelected) {
            { Icon(imageVector = Icons.Filled.Check, contentDescription = null) }
        } else {
            null
        },
    )
}
