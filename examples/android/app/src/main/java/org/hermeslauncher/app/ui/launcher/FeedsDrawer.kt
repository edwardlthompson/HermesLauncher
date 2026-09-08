package org.hermeslauncher.app.ui.launcher

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.HermesSettingsActivity
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.ArticleRecord
import org.hermeslauncher.app.feeds.DrawerKind
import org.hermeslauncher.app.feeds.DrawerRow
import org.hermeslauncher.app.feeds.FeedApply
import org.hermeslauncher.app.feeds.FeedFilter
import org.hermeslauncher.app.feeds.FeedQuery
import org.hermeslauncher.app.feeds.FeedSubPolicy
import org.hermeslauncher.app.ui.scroll.LazyScrubBar
import org.hermeslauncher.app.ui.scroll.scrubGutter
import org.hermeslauncher.app.ui.settings.SettingsSection
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun FeedsDrawer(
    records: List<ArticleRecord>,
    query: FeedQuery,
    tags: Map<String, String>,
    onQuery: (FeedQuery) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val subs by app.feedStore.subs.collectAsStateWithLifecycle(emptyList())
    var search by remember { mutableStateOf("") }
    var hideEmpty by rememberSaveable { mutableStateOf(true) }
    var openTags by remember { mutableStateOf(setOf<String>()) }
    var menu by remember { mutableStateOf<DrawerRow?>(null) }
    var prompt by remember { mutableStateOf<DrawerPrompt?>(null) }
    val drawerList = rememberLazyListState()
    val rows = remember(records, query, search, tags, hideEmpty) {
        FeedFilter.drawerRows(records, query, search, tags, hideEmpty)
    }
    val visible = remember(rows, openTags, search) {
        FeedFilter.drawerVisible(rows, openTags, search.isNotBlank())
    }
    val folders = remember(subs) { FeedSubPolicy.folderNames(subs) }
    BackHandler(onBack = onDismiss)
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f),
            onClick = onDismiss,
        ) {}
        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(300.dp)
                .semantics { contentDescription = "Feeds drawer" },
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(SpacingMd)) {
                val hideEmptyLabel = stringResource(R.string.feed_drawer_hide_empty)
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text(stringResource(R.string.feed_drawer_search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Search feeds list" },
                )
                FilterChip(
                    selected = hideEmpty,
                    onClick = { hideEmpty = !hideEmpty },
                    label = { Text(hideEmptyLabel) },
                    modifier = Modifier.semantics { contentDescription = hideEmptyLabel },
                )
                Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(state = drawerList, modifier = Modifier.fillMaxSize().scrubGutter()) {
                    items(visible, key = { "${it.kind}-${it.sourceUrl}-${it.tag}-${it.title}-${it.depth}" }) { row ->
                        DrawerLine(
                            row = row,
                            query = query,
                            expanded = row.tag in openTags,
                            menuOpen = menu == row,
                            onQuery = { onQuery(it); onDismiss() },
                            onToggleTag = { tag ->
                                openTags = if (tag in openTags) openTags - tag else openTags + tag
                            },
                            onMenu = { menu = if (menu == row) null else row },
                            onDismissMenu = { menu = null },
                            onPrompt = { prompt = it; menu = null },
                        )
                    }
                }
                LazyScrubBar(
                    state = drawerList,
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                )
                }
            }
        }
    }
    FeedDrawerPromptHost(
        prompt = prompt,
        folders = folders,
        onDismiss = { prompt = null },
        onMove = { row, tag ->
            scope.launch {
                val next = if (row.kind == DrawerKind.TAG) {
                    FeedSubPolicy.renameTag(subs, row.tag, tag)
                } else {
                    FeedSubPolicy.setTag(subs, row.sourceUrl.orEmpty(), tag)
                }
                app.feedStore.replaceSubs(next)
            }
        },
        onRename = { row, name ->
            scope.launch { app.feedStore.replaceSubs(FeedSubPolicy.renameTag(subs, row.tag, name)) }
        },
        onUnsubscribe = { row, all ->
            scope.launch {
                if (all) {
                    FeedSubPolicy.folderUrls(subs, row.tag).forEach { app.feeds.unsubscribe(it) }
                } else {
                    app.feeds.unsubscribe(row.sourceUrl.orEmpty())
                }
            }
        },
        onDeleteFolder = { row ->
            scope.launch { app.feedStore.replaceSubs(FeedSubPolicy.renameTag(subs, row.tag, "")) }
        },
        onMarkRead = { row ->
            val ids = if (row.kind == DrawerKind.TAG) {
                FeedApply.idsForTag(records, row.tag, tags)
            } else {
                FeedApply.idsForUrls(records, setOfNotNull(row.sourceUrl))
            }
            scope.launch { app.feeds.markAllRead(ids) }
        },
        onSettings = {
            context.startActivity(
                Intent(context, HermesSettingsActivity::class.java)
                    .putExtra(HermesSettingsActivity.EXTRA_SECTION, SettingsSection.FEEDS_SUBS.name),
            )
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerLine(
    row: DrawerRow,
    query: FeedQuery,
    expanded: Boolean,
    menuOpen: Boolean,
    onQuery: (FeedQuery) -> Unit,
    onToggleTag: (String) -> Unit,
    onMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onPrompt: (DrawerPrompt) -> Unit,
) {
    val selected = when (row.kind) {
        DrawerKind.ALL -> query.sourceUrl == null && !query.savedOnly
        DrawerKind.SAVED -> query.savedOnly
        DrawerKind.FEED -> query.sourceUrl == row.sourceUrl && !query.savedOnly
        DrawerKind.TAG -> expanded
    }
    val chevron = when {
        row.kind != DrawerKind.TAG -> ""
        expanded -> "▾ "
        else -> "▸ "
    }
    val cd = when (row.kind) {
        DrawerKind.ALL -> "All feeds"
        DrawerKind.SAVED -> "Saved"
        DrawerKind.TAG -> stringResource(
            if (expanded) R.string.feed_drawer_folder_open else R.string.feed_drawer_folder_closed,
            row.tag,
        )
        else -> row.title
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = SpacingMd * row.depth, top = SpacingSm, bottom = SpacingSm)
                .semantics { contentDescription = cd }
                .combinedClickable(
                    onClick = {
                        when (row.kind) {
                            DrawerKind.ALL -> onQuery(query.copy(sourceUrl = null, savedOnly = false))
                            DrawerKind.SAVED -> onQuery(query.copy(sourceUrl = null, savedOnly = true))
                            DrawerKind.FEED -> onQuery(query.copy(sourceUrl = row.sourceUrl, savedOnly = false))
                            DrawerKind.TAG -> onToggleTag(row.tag)
                        }
                    },
                    onLongClick = {
                        if (row.kind == DrawerKind.FEED || row.kind == DrawerKind.TAG) {
                            onMenu()
                        }
                    },
                ),
        ) {
            Text(
                text = "$chevron${row.title} (${row.unread})",
                style = if (selected) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            )
        }
        FeedDrawerMenu(
            row = row,
            expanded = menuOpen,
            onDismiss = onDismissMenu,
            onPrompt = onPrompt,
        )
    }
}
