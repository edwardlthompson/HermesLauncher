package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.ArticleRecord
import org.hermeslauncher.app.feeds.FeedChip
import org.hermeslauncher.app.feeds.FeedFilter
import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.feeds.FeedKindResolver
import org.hermeslauncher.app.feeds.FeedQuery
import org.hermeslauncher.app.ui.inbox.FilterBar
import org.hermeslauncher.app.ui.player.FeedCard
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun FeedsPage(
    records: List<ArticleRecord>,
    onPlay: (FeedItem) -> Unit,
    onLongPressHome: () -> Unit,
    onDoubleTapHome: () -> Unit = {},
    onEmptySwipe: (org.hermeslauncher.app.launcher.GestureSlot) -> Unit = {},
    onAddFeed: suspend (String) -> Boolean = { false },
    onOpen: (ArticleRecord) -> Unit = {},
    query: FeedQuery,
    onQuery: (FeedQuery) -> Unit,
    refreshFailed: Boolean = false,
    onRetry: () -> Unit = {},
    onRefresh: () -> Unit = {},
    refreshing: Boolean = false,
    showThumbs: Boolean = true,
    downloadThumbs: Boolean = true,
    onMarkAllRead: () -> Unit = {},
    lastError: String? = null,
    tags: Map<String, String> = emptyMap(),
    onStar: (String) -> Unit = {},
    onToggleRead: (ArticleRecord) -> Unit = {},
    onShare: (ArticleRecord) -> Unit = {},
    onPlayNext: ((FeedItem) -> Unit)? = null,
    emptyKind: org.hermeslauncher.app.ui.inbox.ZeroKind = org.hermeslauncher.app.ui.inbox.ZeroKind.NEWS,
    modifier: Modifier = Modifier,
) {
    var adding by remember { mutableStateOf(false) }
    var feedsOpen by remember { mutableStateOf(false) }
    val visible = remember(records, query) { FeedFilter.apply(records, query) }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val emptyDay = remember { java.time.LocalDate.now().toEpochDay() }
    val feedUnread = remember(records) {
        records.groupBy { it.item.sourceUrl ?: it.item.feedTitle }.count { (_, rows) -> rows.any { !it.read } }
    }
    LaunchedEffect(query.newestFirst, query.sourceUrl, query.savedOnly) {
        listState.scrollToItem(0)
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPressHome() },
                    onDoubleTap = { onDoubleTapHome() },
                )
            },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilterBar(
                unread = FeedFilter.unreadCount(records),
                searchText = query.text,
                onSearchText = { onQuery(query.copy(text = it)) },
                searchLabel = stringResource(R.string.feed_search),
                filterLabel = buildString {
                    append(stringResource(R.string.feed_filter_open))
                    if (query.chip == FeedChip.UNREAD) {
                        append(", ")
                        append(stringResource(R.string.filter_unread))
                    }
                },
                filterMenu = { expanded, onDismiss ->
                    FeedFilterMenu(
                        expanded = expanded,
                        query = query,
                        onDismiss = onDismiss,
                        onQuery = {
                            onQuery(it)
                            onDismiss()
                        },
                        onMarkAllRead = onMarkAllRead,
                    )
                },
                onRefresh = onRefresh,
                refreshLabel = stringResource(R.string.feed_refresh),
                refreshing = refreshing,
                onOpenFeeds = { feedsOpen = true },
                feedsUnread = feedUnread,
                feedsLabel = stringResource(R.string.feed_open_feeds),
            )
            if (!lastError.isNullOrBlank()) {
                Text(
                    text = lastError,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = SpacingMd).clickable(onClick = onRetry),
                )
            }
            if (visible.isEmpty()) {
                Text(
                    text = stringResource(
                        when {
                            records.isEmpty() && refreshFailed -> R.string.workspace_feeds_fetch_failed
                            records.isEmpty() -> org.hermeslauncher.app.ui.inbox.ZeroCopy.pick(emptyKind, emptyDay)
                            else -> R.string.feed_empty_filter
                        },
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(SpacingMd)
                        .clickable(onClick = onRetry),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(bottom = SpacingMd * 5),
                    verticalArrangement = Arrangement.spacedBy(SpacingMd),
                ) {
                    items(visible, key = { it.item.id }) { rec ->
                        FeedCard(
                            item = rec.item,
                            kind = FeedKindResolver.kindOf(rec.item),
                            read = rec.read,
                            starred = rec.starred,
                            thumbDir = context.filesDir,
                            showThumb = showThumbs,
                            downloadThumb = downloadThumbs,
                            onPlay = { onPlay(rec.item) },
                            onOpen = { onOpen(rec) },
                            onStar = { onStar(rec.item.id) },
                            onToggleRead = { onToggleRead(rec) },
                            onShare = { onShare(rec) },
                            onPlayNext = onPlayNext?.let { next -> { next(rec.item) } },
                            modifier = Modifier.padding(horizontal = SpacingMd),
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { adding = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(SpacingMd),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.feeds_add_title),
            )
        }
        if (adding) {
            FeedAddDialog(
                onDismiss = { adding = false },
                onAdd = onAddFeed,
            )
        }
        if (feedsOpen) {
            FeedsDrawer(
                records = records,
                query = query,
                tags = tags,
                onQuery = onQuery,
                onDismiss = { feedsOpen = false },
            )
        }
    }
}
