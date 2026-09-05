package org.hermeslauncher.app.ui.inbox

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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

@Composable
fun FilterBar(
    unread: Int,
    searchText: String,
    onSearchText: (String) -> Unit,
    modifier: Modifier = Modifier,
    searchLabel: String = stringResource(R.string.filter_search),
    filterLabel: String = stringResource(R.string.filter_open),
    filterMenu: @Composable (expanded: Boolean, onDismiss: () -> Unit) -> Unit,
    onRefresh: (() -> Unit)? = null,
    refreshLabel: String = "",
    refreshing: Boolean = false,
    onSettings: (() -> Unit)? = null,
    settingsLabel: String = "",
    onOpenFeeds: (() -> Unit)? = null,
    feedsUnread: Int = 0,
    feedsLabel: String = "",
) {
    var searchOpen by remember { mutableStateOf(false) }
    var filterOpen by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val searchFocus = remember { FocusRequester() }
    fun closeSearch() {
        searchOpen = false
        focus.clearFocus()
        onSearchText("")
    }
    BackHandler(enabled = searchOpen) { closeSearch() }
    LaunchedEffect(searchOpen) {
        if (searchOpen) {
            searchFocus.requestFocus()
        }
    }
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
                    contentDescription = searchLabel,
                    onClick = { if (searchOpen) closeSearch() else searchOpen = true },
                )
                if (searchOpen) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = onSearchText,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = SpacingSm)
                            .focusRequester(searchFocus),
                        label = { Text(searchLabel) },
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
                        contentDescription = filterLabel,
                        onClick = { filterOpen = true },
                    )
                    filterMenu(filterOpen) { filterOpen = false }
                }
                if (onSettings != null && !searchOpen) {
                    ContrastIcon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = settingsLabel,
                        onClick = onSettings,
                    )
                }
                if (onRefresh != null && !searchOpen) {
                    if (refreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(start = SpacingSm)
                                .size(24.dp)
                                .semantics { contentDescription = refreshLabel },
                        )
                    } else {
                        ContrastIcon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = refreshLabel,
                            onClick = onRefresh,
                        )
                    }
                }
            }
            if (!searchOpen) {
                if (onOpenFeeds != null) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FeedsBubble(unread = feedsUnread, label = feedsLabel, onClick = onOpenFeeds)
                        UnreadBubble(unread = unread)
                    }
                } else {
                    UnreadBubble(
                        unread = unread,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedsBubble(unread: Int, label: String, onClick: () -> Unit) {
    val active = unread > 0
    Surface(
        modifier = Modifier
            .size(40.dp)
            .semantics { contentDescription = label },
        shape = CircleShape,
        color = if (active) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.inverseSurface
        },
        contentColor = if (active) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.inverseOnSurface
        },
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (unread > 0) {
                Text(text = InboxFilter.unreadLabel(unread), style = MaterialTheme.typography.labelMedium)
            } else {
                Icon(imageVector = Icons.Filled.RssFeed, contentDescription = label)
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
