package org.hermeslauncher.app.ui.inbox

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.vault.InboxLayout
import org.hermeslauncher.app.vault.InboxQuery

@Composable
fun FilterMenu(
    expanded: Boolean,
    query: InboxQuery,
    onDismiss: () -> Unit,
    onQuery: (InboxQuery) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        SortItem(
            label = stringResource(R.string.filter_order_ascending),
            selected = !query.newestFirst,
            onClick = { onQuery(query.copy(newestFirst = false)) },
        )
        SortItem(
            label = stringResource(R.string.filter_order_descending),
            selected = query.newestFirst,
            onClick = { onQuery(query.copy(newestFirst = true)) },
        )
        SortItem(
            label = stringResource(R.string.filter_chip_app),
            selected = query.layout == InboxLayout.APP,
            onClick = { onQuery(query.copy(layout = InboxLayout.APP)) },
        )
        SortItem(
            label = stringResource(R.string.filter_chip_category),
            selected = query.layout == InboxLayout.CATEGORY,
            onClick = { onQuery(query.copy(layout = InboxLayout.CATEGORY)) },
        )
        SortItem(
            label = stringResource(R.string.filter_chip_time),
            selected = query.layout == InboxLayout.TIME,
            onClick = { onQuery(query.copy(layout = InboxLayout.TIME)) },
        )
    }
}

@Composable
private fun SortItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = onClick,
        trailingIcon = if (selected) {
            {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null)
            }
        } else {
            null
        },
    )
}
