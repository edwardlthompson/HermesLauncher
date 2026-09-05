package org.hermeslauncher.app.ui.launcher

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.HermesSettingsActivity
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.ArticleRecord
import org.hermeslauncher.app.feeds.DrawerKind
import org.hermeslauncher.app.feeds.DrawerRow
import org.hermeslauncher.app.feeds.FeedFilter
import org.hermeslauncher.app.feeds.FeedQuery
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
    var search by remember { mutableStateOf("") }
    var openTags by remember { mutableStateOf(setOf<String>()) }
    val rows = remember(records, query, search, tags) {
        FeedFilter.drawerRows(records, query, search, tags)
    }
    val visible = rows.filter { row ->
        if (row.kind != DrawerKind.FEED || row.tag.isBlank()) {
            true
        } else {
            row.tag in openTags
        }
    }
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
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text(stringResource(R.string.feed_drawer_search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Search feeds list" },
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(visible, key = { "${it.kind}-${it.sourceUrl}-${it.tag}-${it.title}" }) { row ->
                        DrawerLine(
                            row = row,
                            query = query,
                            expanded = row.tag in openTags,
                            onQuery = { onQuery(it); onDismiss() },
                            onToggleTag = { tag ->
                                openTags = if (tag in openTags) openTags - tag else openTags + tag
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerLine(
    row: DrawerRow,
    query: FeedQuery,
    expanded: Boolean,
    onQuery: (FeedQuery) -> Unit,
    onToggleTag: (String) -> Unit,
) {
    val context = LocalContext.current
    val selected = when (row.kind) {
        DrawerKind.ALL -> query.sourceUrl == null && !query.savedOnly
        DrawerKind.SAVED -> query.savedOnly
        DrawerKind.FEED -> query.sourceUrl == row.sourceUrl && !query.savedOnly
        DrawerKind.TAG -> expanded
    }
    val cd = when (row.kind) {
        DrawerKind.ALL -> "All feeds"
        DrawerKind.SAVED -> "Saved"
        DrawerKind.TAG -> row.tag
        else -> row.title
    }
    Text(
        text = "${row.title} (${row.unread})",
        style = if (selected) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SpacingSm)
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
                    if (row.kind == DrawerKind.FEED) {
                        context.startActivity(
                            Intent(context, HermesSettingsActivity::class.java)
                                .putExtra(HermesSettingsActivity.EXTRA_SECTION, SettingsSection.FEEDS.name),
                        )
                    }
                },
            ),
    )
}
