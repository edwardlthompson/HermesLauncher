package org.hermeslauncher.app.ui.inbox

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm
import org.hermeslauncher.app.vault.InboxFilter
import org.hermeslauncher.app.vault.InboxQuery

@Composable
fun FilterBar(
    query: InboxQuery,
    unread: Int,
    onQuery: (InboxQuery) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchOpen by remember { mutableStateOf(false) }
    var filterOpen by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    fun closeSearch() {
        searchOpen = false
        focus.clearFocus()
        onQuery(query.copy(text = ""))
    }
    BackHandler(enabled = searchOpen) { closeSearch() }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMd, vertical = SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ContrastIcon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.filter_search),
                    onClick = { if (searchOpen) closeSearch() else searchOpen = true },
                )
                if (searchOpen) {
                    OutlinedTextField(
                        value = query.text,
                        onValueChange = { onQuery(query.copy(text = it)) },
                        modifier = Modifier.weight(1f).padding(horizontal = SpacingSm),
                        label = { Text(stringResource(R.string.filter_search)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { closeSearch() }),
                        trailingIcon = {
                            IconButton(onClick = { closeSearch() }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.filter_close),
                                )
                            }
                        },
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Box {
                    ContrastIcon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = stringResource(R.string.filter_open),
                        onClick = { filterOpen = true },
                    )
                    FilterMenu(
                        expanded = filterOpen,
                        query = query,
                        onDismiss = { filterOpen = false },
                        onQuery = {
                            onQuery(it)
                            filterOpen = false
                        },
                    )
                }
            }
            if (!searchOpen) {
                UnreadBubble(
                    unread = unread,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun UnreadBubble(unread: Int, modifier: Modifier = Modifier) {
    val label = InboxFilter.unreadLabel(unread)
    val cd = stringResource(R.string.inbox_unread_count, unread)
    val active = unread > 0
    Surface(
        modifier = modifier
            .size(40.dp)
            .semantics { contentDescription = cd },
        shape = CircleShape,
        color = if (active) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.inverseSurface
        },
        contentColor = if (active) {
            MaterialTheme.colorScheme.onError
        } else {
            MaterialTheme.colorScheme.inverseOnSurface
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ContrastIcon(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.inverseSurface,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                )
            }
        }
    }
}
