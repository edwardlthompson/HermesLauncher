package org.hermeslauncher.app.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.feeds.FeedKindResolver
import org.hermeslauncher.app.feeds.MixedEntry
import org.hermeslauncher.app.feeds.MixPolicy
import org.hermeslauncher.app.ui.player.FeedCard
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.vault.InboxAppGroup
import org.hermeslauncher.app.vault.InboxChip
import org.hermeslauncher.app.vault.InboxFilter
import org.hermeslauncher.app.vault.InboxLayout
import org.hermeslauncher.app.vault.InboxQuery
import org.hermeslauncher.app.vault.VaultItem
import java.io.File

@Composable
fun InboxFeed(
    query: InboxQuery,
    live: List<VaultItem>,
    history: List<VaultItem>,
    feeds: List<FeedItem>,
    kindOf: (String) -> String,
    onDismiss: (String) -> Unit,
    onDismissGroup: (List<String>) -> Unit,
    onOpen: (String) -> Unit,
    onAction: (String, Int) -> Unit,
    onPin: (String) -> Unit,
    onPlay: (FeedItem) -> Unit,
    imageDir: File,
    itemsEmpty: Boolean,
    modifier: Modifier = Modifier,
) {
    val showFeeds = query.chip == InboxChip.ALL && query.packageName == null
    val feedHits = matchingFeeds(feeds, query.text)
    val searching = query.text.isNotBlank()
    val listState = rememberLazyListState()
    LaunchedEffect(query.layout, query.newestFirst, query.chip, query.packageName) {
        listState.scrollToItem(0)
    }
    when {
        itemsEmpty && history.isEmpty() && (!showFeeds || feedHits.isEmpty()) -> inboxHint()
        live.isEmpty() && history.isEmpty() && !(showFeeds && feedHits.isNotEmpty()) -> filterEmpty()
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            inboxSection(
                query = query,
                items = live,
                feeds = if (showFeeds) feedHits else emptyList(),
                kindOf = kindOf,
                onDismiss = onDismiss,
                onDismissGroup = onDismissGroup,
                onOpen = onOpen,
                onAction = onAction,
                onPin = onPin,
                onPlay = onPlay,
                imageDir = imageDir,
                showDismiss = true,
                keyPrefix = "live",
            )
            if (searching && history.isNotEmpty()) {
                item(key = "history-header") {
                    Text(
                        text = stringResource(R.string.inbox_history_header),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = SpacingMd),
                    )
                }
                inboxSection(
                    query = query,
                    items = history,
                    feeds = emptyList(),
                    kindOf = kindOf,
                    onDismiss = onDismiss,
                    onDismissGroup = onDismissGroup,
                    onOpen = onOpen,
                    onAction = onAction,
                    onPin = onPin,
                    onPlay = onPlay,
                    imageDir = imageDir,
                    showDismiss = false,
                    keyPrefix = "hist",
                )
            }
        }
    }
}

private fun LazyListScope.inboxSection(
    query: InboxQuery,
    items: List<VaultItem>,
    feeds: List<FeedItem>,
    kindOf: (String) -> String,
    onDismiss: (String) -> Unit,
    onDismissGroup: (List<String>) -> Unit,
    onOpen: (String) -> Unit,
    onAction: (String, Int) -> Unit,
    onPin: (String) -> Unit,
    onPlay: (FeedItem) -> Unit,
    imageDir: File,
    showDismiss: Boolean,
    keyPrefix: String,
) {
    when (query.layout) {
        InboxLayout.TIME -> {
            val mixed = MixPolicy.merge(items, feeds, query.newestFirst)
            items(mixed, key = { "$keyPrefix:${mixKey(it)}" }) { entry ->
                mixedRow(entry, imageDir, onDismiss, onOpen, onAction, onPin, onPlay, showDismiss)
            }
        }
        InboxLayout.CATEGORY, InboxLayout.APP -> {
            val groups = if (query.layout == InboxLayout.CATEGORY) {
                InboxFilter.categoryGroups(items, query.newestFirst, kindOf)
            } else {
                InboxFilter.groups(items, query.newestFirst)
            }
            items(groups, key = { "$keyPrefix:${it.displayLabel ?: it.packageName}" }) { group ->
                GroupBlock(
                    group = group,
                    onDismissGroup = { onDismissGroup(group.items.map { it.id }) },
                    onDismissItem = onDismiss,
                    onOpenItem = onOpen,
                    onAction = onAction,
                    onPin = onPin,
                    imageDir = imageDir,
                    showDismiss = showDismiss,
                )
            }
            if (query.layout == InboxLayout.APP && feeds.isNotEmpty()) {
                items(feeds, key = { "$keyPrefix:f:${it.id}" }) { item ->
                    FeedCard(
                        item = item,
                        kind = FeedKindResolver.kindOf(item),
                        onPlay = { onPlay(item) },
                        thumbDir = imageDir,
                        modifier = Modifier.padding(horizontal = SpacingMd),
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupBlock(
    group: InboxAppGroup,
    onDismissGroup: () -> Unit,
    onDismissItem: (String) -> Unit,
    onOpenItem: (String) -> Unit,
    onAction: (String, Int) -> Unit,
    onPin: (String) -> Unit,
    imageDir: File,
    showDismiss: Boolean,
) {
    var expanded by rememberSaveable(group.packageName, group.displayLabel) { mutableStateOf(false) }
    InboxGroup(
        group = group,
        expanded = expanded,
        onToggle = { expanded = !expanded },
        onDismissGroup = onDismissGroup,
        onDismissItem = onDismissItem,
        onOpenItem = onOpenItem,
        onAction = onAction,
        onPin = onPin,
        imageDir = imageDir,
        showDismiss = showDismiss,
        modifier = Modifier.padding(horizontal = SpacingMd),
    )
}

@Composable
private fun mixedRow(
    entry: MixedEntry,
    imageDir: File,
    onDismiss: (String) -> Unit,
    onOpen: (String) -> Unit,
    onAction: (String, Int) -> Unit,
    onPin: (String) -> Unit,
    onPlay: (FeedItem) -> Unit,
    showDismiss: Boolean,
) {
    when (entry) {
        is MixedEntry.Vault -> VaultItemCard(
            item = entry.item,
            imageDir = imageDir,
            showDismiss = showDismiss,
            showSource = true,
            onDismiss = { onDismiss(entry.item.id) },
            onPin = { onPin(entry.item.id) },
            onOpen = { onOpen(entry.item.id) },
            onAction = { onAction(entry.item.id, it) },
            modifier = Modifier.padding(horizontal = SpacingMd),
        )
        is MixedEntry.Feed -> FeedCard(
            item = entry.item,
            kind = entry.kind,
            onPlay = { onPlay(entry.item) },
            thumbDir = imageDir,
            modifier = Modifier.padding(horizontal = SpacingMd),
        )
    }
}

private fun matchingFeeds(feeds: List<FeedItem>, text: String): List<FeedItem> {
    val needle = text.trim()
    if (needle.isEmpty()) {
        return feeds
    }
    val lower = needle.lowercase()
    return feeds.filter { item ->
        item.title.lowercase().contains(lower) || item.feedTitle.lowercase().contains(lower)
    }
}

private fun mixKey(entry: MixedEntry): String {
    return when (entry) {
        is MixedEntry.Vault -> "v:${entry.item.id}"
        is MixedEntry.Feed -> "f:${entry.item.id}"
    }
}

@Composable
private fun inboxHint() {
    val day = rememberSaveable { java.time.LocalDate.now().toEpochDay() }
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
        modifier = Modifier.padding(SpacingMd),
    ) {
        Text(
            text = stringResource(ZeroCopy.pick(ZeroKind.INBOX, day)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(SpacingMd),
        )
    }
}

@Composable
private fun filterEmpty() {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
        modifier = Modifier.padding(SpacingMd),
    ) {
        Text(
            text = stringResource(R.string.filter_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(SpacingMd),
        )
    }
}
