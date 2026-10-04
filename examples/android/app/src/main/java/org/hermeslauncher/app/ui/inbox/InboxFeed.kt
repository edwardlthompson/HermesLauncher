package org.hermeslauncher.app.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.feeds.FeedKindResolver
import org.hermeslauncher.app.feeds.MixedEntry
import org.hermeslauncher.app.feeds.MixPolicy
import org.hermeslauncher.app.ui.player.FeedCard
import org.hermeslauncher.app.ui.scroll.LazyScrubBar
import org.hermeslauncher.app.ui.scroll.scrubGutter
import org.hermeslauncher.app.ui.theme.MotionPrefs
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.vault.InboxChip
import org.hermeslauncher.app.vault.InboxEmpty
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
    listenerOn: Boolean,
    modifier: Modifier = Modifier,
) {
    val reduced = MotionPrefs.reduced(LocalContext.current)
    val showFeeds = query.chip == InboxChip.ALL && query.packageName == null
    val feedHits = matchingFeeds(feeds, query.text)
    val searching = query.text.isNotBlank()
    val listState = rememberLazyListState()
    val emptyKind = InboxEmpty.kind(
        listenerOn = listenerOn,
        itemsEmpty = itemsEmpty,
        liveEmpty = live.isEmpty(),
        historyEmpty = history.isEmpty(),
        hasVisibleFeeds = showFeeds && feedHits.isNotEmpty(),
    )
    var expandedKeys by rememberSaveable { mutableStateOf(setOf<String>()) }
    LaunchedEffect(query.layout, query.newestFirst, query.chip, query.packageName) {
        listState.scrollToItem(0)
    }
    if (emptyKind != InboxEmpty.Kind.CONTENT) {
        InboxEmptyPane(kind = emptyKind)
        return
    }
    Box(modifier = modifier.fillMaxSize()) {
            InboxStickToTop(
                listState = listState,
                newestFirst = query.newestFirst,
                revision = live.size to (live.maxOfOrNull { it.postedAt } ?: 0L),
            )
            LazyColumn(
            modifier = Modifier.fillMaxSize().scrubGutter(),
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
                reduced = reduced,
                expandedKeys = expandedKeys,
                onToggleGroup = { key ->
                    expandedKeys = if (key in expandedKeys) expandedKeys - key else expandedKeys + key
                },
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
                    reduced = reduced,
                    expandedKeys = expandedKeys,
                    onToggleGroup = { key ->
                        expandedKeys = if (key in expandedKeys) expandedKeys - key else expandedKeys + key
                    },
                )
            }
            }
            LazyScrubBar(state = listState, modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight())
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
    reduced: Boolean,
    expandedKeys: Set<String>,
    onToggleGroup: (String) -> Unit,
) {
    when (query.layout) {
        InboxLayout.TIME -> {
            val mixed = MixPolicy.merge(items, feeds, query.newestFirst)
            items(mixed, key = { "$keyPrefix:${mixKey(it)}" }) { entry ->
                mixedRow(
                    entry,
                    imageDir,
                    onDismiss,
                    onOpen,
                    onAction,
                    onPin,
                    onPlay,
                    showDismiss,
                    rowModifier(reduced),
                )
            }
        }
        InboxLayout.CATEGORY, InboxLayout.APP -> {
            val groups = if (query.layout == InboxLayout.CATEGORY) {
                InboxFilter.categoryGroups(items, query.newestFirst, kindOf)
            } else {
                InboxFilter.groups(items, query.newestFirst)
            }
            val rows = InboxGroupRows.flatten(groups, expandedKeys)
            items(rows, key = { row ->
                when (row) {
                    is InboxGroupRow.Header -> "$keyPrefix:h:${InboxGroupRows.groupKey(row.group)}"
                    is InboxGroupRow.Child -> "$keyPrefix:c:${row.item.id}"
                }
            }) { row ->
                when (row) {
                    is InboxGroupRow.Header -> {
                        val key = InboxGroupRows.groupKey(row.group)
                        InboxGroup(
                            group = row.group,
                            expanded = row.expanded,
                            onToggle = { onToggleGroup(key) },
                            onDismissGroup = { onDismissGroup(row.group.items.map { it.id }) },
                            onDismissItem = onDismiss,
                            onOpenItem = onOpen,
                            onAction = onAction,
                            onPin = onPin,
                            imageDir = imageDir,
                            showDismiss = showDismiss,
                            showChildren = false,
                            modifier = Modifier.padding(horizontal = SpacingMd),
                        )
                    }
                    is InboxGroupRow.Child -> VaultItemCard(
                        item = row.item,
                        imageDir = imageDir,
                        showDismiss = showDismiss,
                        onDismiss = { onDismiss(row.item.id) },
                        onPin = { onPin(row.item.id) },
                        onOpen = { onOpen(row.item.id) },
                        onAction = { index -> onAction(row.item.id, index) },
                        modifier = Modifier.padding(horizontal = SpacingMd),
                    )
                }
            }
            if (query.layout == InboxLayout.APP && feeds.isNotEmpty()) {
                items(feeds, key = { "$keyPrefix:f:${it.id}" }) { item ->
                    FeedCard(
                        item = item,
                        kind = FeedKindResolver.kindOf(item),
                        onPlay = { onPlay(item) },
                        thumbDir = imageDir,
                        modifier = rowModifier(reduced),
                    )
                }
            }
        }
    }
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
    modifier: Modifier,
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
            modifier = modifier,
        )
        is MixedEntry.Feed -> FeedCard(
            item = entry.item,
            kind = entry.kind,
            onPlay = { onPlay(entry.item) },
            thumbDir = imageDir,
            modifier = modifier,
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

private fun LazyItemScope.rowModifier(reduced: Boolean): Modifier {
    val pad = Modifier.padding(horizontal = SpacingMd)
    return if (MotionPrefs.animateDismiss(reduced)) {
        Modifier.animateItem().then(pad)
    } else {
        pad
    }
}
