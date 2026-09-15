package org.hermeslauncher.app.ui.inbox

import androidx.compose.animation.animateContentSize
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.launcher.UnreadDot
import org.hermeslauncher.app.ui.theme.ElevationLevel2
import org.hermeslauncher.app.ui.theme.MotionPrefs
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun FilterBar(
    unread: Int,
    searchText: String,
    onSearchText: (String) -> Unit,
    modifier: Modifier = Modifier,
    barTitle: String = stringResource(R.string.launcher_page_feed),
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
    val reduced = MotionPrefs.reduced(LocalContext.current)
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
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = ElevationLevel2,
        shadowElevation = ElevationLevel2,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (MotionPrefs.animateSize(reduced)) Modifier.animateContentSize() else Modifier)
                .padding(horizontal = SpacingSm, vertical = SpacingSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (searchOpen) closeSearch() else searchOpen = true }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = searchLabel)
            }
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
                Text(
                    text = barTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.padding(end = SpacingSm),
                )
                UnreadDot(
                    count = unread,
                    description = stringResource(R.string.inbox_unread_count, unread),
                    modifier = Modifier.padding(end = SpacingSm),
                )
                Box(modifier = Modifier.weight(1f))
            }
            Box {
                IconButton(onClick = { filterOpen = true }) {
                    Icon(imageVector = Icons.Filled.FilterList, contentDescription = filterLabel)
                }
                filterMenu(filterOpen) { filterOpen = false }
            }
            if (onOpenFeeds != null && !searchOpen) {
                Box {
                    IconButton(onClick = onOpenFeeds) {
                        Icon(imageVector = Icons.Filled.RssFeed, contentDescription = feedsLabel)
                    }
                    UnreadDot(
                        count = feedsUnread,
                        description = feedsLabel,
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
            }
            if (onSettings != null && !searchOpen) {
                IconButton(onClick = onSettings) {
                    Icon(imageVector = Icons.Filled.Settings, contentDescription = settingsLabel)
                }
            }
            if (onRefresh != null && !searchOpen) {
                if (refreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = SpacingSm)
                            .size(24.dp)
                            .semantics { contentDescription = refreshLabel },
                    )
                } else {
                    IconButton(onClick = onRefresh) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = refreshLabel)
                    }
                }
            }
        }
    }
}
