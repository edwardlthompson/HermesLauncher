package org.hermeslauncher.app.ui.inbox

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
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
            icon = Icons.Filled.ArrowUpward,
            selected = !query.newestFirst,
            onClick = { onQuery(query.copy(newestFirst = false)) },
        )
        SortItem(
            label = stringResource(R.string.filter_order_descending),
            icon = Icons.Filled.ArrowDownward,
            selected = query.newestFirst,
            onClick = { onQuery(query.copy(newestFirst = true)) },
        )
        HorizontalDivider()
        SortItem(
            label = stringResource(R.string.filter_chip_app),
            icon = Icons.Filled.Apps,
            selected = query.layout == InboxLayout.APP,
            onClick = { onQuery(query.copy(layout = InboxLayout.APP)) },
        )
        SortItem(
            label = stringResource(R.string.filter_chip_category),
            icon = Icons.Filled.Category,
            selected = query.layout == InboxLayout.CATEGORY,
            onClick = { onQuery(query.copy(layout = InboxLayout.CATEGORY)) },
        )
        SortItem(
            label = stringResource(R.string.filter_chip_time),
            icon = Icons.Filled.Schedule,
            selected = query.layout == InboxLayout.TIME,
            onClick = { onQuery(query.copy(layout = InboxLayout.TIME)) },
        )
    }
}

@Composable
private fun SortItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = onClick,
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        trailingIcon = if (selected) {
            { Icon(imageVector = Icons.Filled.Check, contentDescription = null) }
        } else {
            null
        },
    )
}
