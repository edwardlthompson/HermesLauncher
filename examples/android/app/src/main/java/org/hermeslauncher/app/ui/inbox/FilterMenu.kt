package org.hermeslauncher.app.ui.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.vault.InboxChip
import org.hermeslauncher.app.vault.InboxLayout
import org.hermeslauncher.app.vault.InboxQuery

@Composable
fun FilterMenu(
    expanded: Boolean,
    query: InboxQuery,
    packages: List<String>,
    onDismiss: () -> Unit,
    onQuery: (InboxQuery) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .heightIn(max = 360.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_all)) },
                onClick = { onQuery(query.copy(chip = InboxChip.ALL, packageName = null)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_messages)) },
                onClick = { onQuery(query.copy(chip = InboxChip.MESSAGES, packageName = null)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_unread)) },
                onClick = { onQuery(query.copy(chip = InboxChip.UNREAD, packageName = null)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_pinned)) },
                onClick = { onQuery(query.copy(chip = InboxChip.PINNED, packageName = null)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_layout_app)) },
                onClick = { onQuery(query.copy(layout = InboxLayout.APP)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_layout_category)) },
                onClick = { onQuery(query.copy(layout = InboxLayout.CATEGORY)) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_layout_time)) },
                onClick = { onQuery(query.copy(layout = InboxLayout.TIME)) },
            )
            packages.forEach { pkg ->
                val label = pkg.substringAfterLast('.').ifBlank { pkg.ifBlank { stringResource(R.string.inbox_unknown_app) } }
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = { onQuery(query.copy(packageName = pkg, chip = InboxChip.ALL)) },
                )
            }
        }
    }
}
