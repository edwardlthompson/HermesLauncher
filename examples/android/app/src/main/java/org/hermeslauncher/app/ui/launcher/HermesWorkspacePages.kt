package org.hermeslauncher.app.ui.launcher

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.ArticleOpen
import org.hermeslauncher.app.feeds.ArticleRecord
import org.hermeslauncher.app.feeds.ArticleTarget
import org.hermeslauncher.app.feeds.FeedFilter
import org.hermeslauncher.app.feeds.FeedQuery
import org.hermeslauncher.app.feeds.FeedSync
import org.hermeslauncher.app.feeds.ReaderSettings
import org.hermeslauncher.app.feeds.SubKind
import org.hermeslauncher.app.feeds.SubKindFilter
import org.hermeslauncher.app.l3.L3GestureHost
import org.hermeslauncher.app.ui.onboarding.InboxGrantHost
import org.hermeslauncher.app.ui.onboarding.NovaSetupCard
import org.hermeslauncher.app.ui.player.FeedReader
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.workspace.NovaImportApply

@Composable
fun HermesNewsPage(
    onLongPressHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val records by app.feeds.articleRows.collectAsStateWithLifecycle(emptyList())
    val refreshFailed by app.feeds.refreshFailed.collectAsStateWithLifecycle(false)
    val refreshing by app.feeds.refreshing.collectAsStateWithLifecycle(false)
    val prefs by app.readerPrefs.settings.collectAsStateWithLifecycle(ReaderSettings())
    val subs by app.feedStore.subs.collectAsStateWithLifecycle(emptyList())
    val pending by app.pendingArticleId.collectAsStateWithLifecycle(null)
    LaunchedEffect(app, prefs.refreshOnOpen) {
        if (prefs.refreshOnOpen && FeedSync.allowAuto(context, prefs)) {
            app.feeds.refresh()
        }
    }
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf(FeedQuery()) }
    LaunchedEffect(prefs.chip, prefs.newestFirst, prefs.sourceUrl, prefs.savedOnly, prefs.blocked) {
        query = query.copy(
            chip = prefs.chip,
            newestFirst = prefs.newestFirst,
            sourceUrl = prefs.sourceUrl,
            savedOnly = prefs.savedOnly,
            blocked = prefs.blocked,
        )
    }
    var readingId by remember { mutableStateOf<String?>(null) }
    var trail by remember { mutableStateOf<List<String>>(emptyList()) }
    val newsRecords = remember(records, subs) { SubKindFilter.records(records, subs, SubKind.NEWS) }
    LaunchedEffect(pending, newsRecords) {
        val id = pending ?: return@LaunchedEffect
        if (newsRecords.any { it.item.id == id }) {
            readingId = id
        }
        app.pendingArticleId.value = null
    }
    val reading = newsRecords.firstOrNull { it.item.id == readingId }
    val (prevId, nextId) = remember(trail, readingId) {
        FeedFilter.adjacent(trail, readingId.orEmpty())
    }
    Surface(modifier = modifier.fillMaxSize()) {
        val article = reading
        if (article != null) {
            FeedReader(
                item = article.item,
                starred = article.starred,
                onClose = { readingId = null },
                onStar = { scope.launch { app.feeds.toggleStar(article.item.id) } },
                onUnread = { scope.launch { app.feeds.markUnread(article.item.id) } },
                onViewed = { scope.launch { app.feeds.markRead(article.item.id) } },
                onPrev = prevId?.let { id -> { readingId = id } },
                onNext = nextId?.let { id -> { readingId = id } },
                downloadImages = FeedSync.allowImages(context, prefs),
                bodyScale = prefs.bodyScale,
                onBodyScale = { scale -> scope.launch { app.readerPrefs.setBodyScale(scale) } },
            )
        } else {
            FeedsPage(
                records = newsRecords,
                onPlay = { item ->
                    item.enclosureUrl?.takeIf { it.isNotBlank() }?.let { app.player.play(it) }
                },
                onLongPressHome = onLongPressHome,
                onDoubleTapHome = { L3GestureHost.onDoubleTap(com.android.launcher3.Launcher.getLauncher(context)) },
                onAddFeed = { url -> app.feeds.addFromLink(url) },
                onOpen = { rec ->
                    trail = FeedFilter.apply(newsRecords, query).map { it.item.id }
                    openArticle(app, prefs.target, rec) { readingId = rec.item.id }
                },
                query = query,
                onQuery = {
                    query = it
                    scope.launch { app.readerPrefs.setListQuery(it.chip, it.newestFirst, it.sourceUrl, it.savedOnly) }
                },
                refreshFailed = refreshFailed,
                onRetry = { scope.launch { app.feeds.refresh() } },
                onRefresh = { scope.launch { app.feeds.refresh() } },
                refreshing = refreshing,
                showThumbs = prefs.showThumbs,
                downloadThumbs = FeedSync.allowImages(context, prefs),
                onMarkAllRead = { scope.launch { app.feeds.markAllRead(newsRecords.map { it.item.id }.toSet()) } },
                lastError = subs.filter { it.kind == SubKind.NEWS }.firstOrNull { !it.lastError.isNullOrBlank() }?.lastError,
                tags = SubKindFilter.tags(subs, SubKind.NEWS),
                onStar = { id -> scope.launch { app.feeds.toggleStar(id) } },
                onToggleRead = { rec ->
                    scope.launch {
                        if (rec.read) app.feeds.markUnread(rec.item.id) else app.feeds.markRead(rec.item.id)
                    }
                },
                onShare = { rec ->
                    rec.item.articleUrl()?.let { url ->
                        org.hermeslauncher.app.feeds.FeedShare.intent(rec.item.title, url)?.let { send ->
                            context.startActivity(Intent.createChooser(send, context.getString(R.string.feed_reader_share)))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = context.getString(R.string.launcher_page_news)
                    },
            )
        }
    }
}

private fun openArticle(
    app: HermesApplication,
    opener: ArticleTarget,
    rec: ArticleRecord,
    showReader: () -> Unit,
) {
    val url = rec.item.articleUrl()
    if (opener != ArticleTarget.LAUNCHER && url != null) {
        kotlinx.coroutines.CoroutineScope(Dispatchers.Main.immediate).launch {
            app.feeds.markRead(rec.item.id)
        }
        if (!ArticleOpen.openBrowser(app, url)) {
            showReader()
        }
    } else {
        showReader()
    }
}

@Composable
fun HermesInboxPage(
    onLongPressHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val items by app.vault.visibleItems.collectAsStateWithLifecycle(emptyList())
    val novaDone by app.homePrefs.novaImportDone.collectAsStateWithLifecycle(true)
    val scope = rememberCoroutineScope()
    LaunchedEffect(app) {
        launch(Dispatchers.IO) { NovaImportApply.auto(app) }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = context.getString(R.string.launcher_page_feed)
            },
    ) {
        FeedPage(
            items = items,
            feeds = emptyList(),
            onDismiss = { id -> scope.launch { app.vault.archive(id) } },
            onDismissGroup = { ids ->
                scope.launch(Dispatchers.IO) { ids.forEach { app.vault.archive(it) } }
            },
            onPin = { id -> scope.launch { app.vault.togglePin(id) } },
            onPlay = { item ->
                item.enclosureUrl?.takeIf { it.isNotBlank() }?.let { app.player.play(it) }
            },
            onLongPressHome = onLongPressHome,
            onDoubleTapHome = { L3GestureHost.onDoubleTap(com.android.launcher3.Launcher.getLauncher(context)) },
            onSettings = {
                context.startActivity(Intent(context, org.hermeslauncher.app.HermesSettingsActivity::class.java))
            },
            modifier = Modifier.fillMaxSize(),
        )
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            InboxGrantHost()
            if (!novaDone) {
                NovaSetupCard(
                    onLater = { scope.launch { app.homePrefs.setNovaImportDone(true) } },
                    modifier = Modifier.padding(SpacingMd),
                )
            }
        }
    }
}
