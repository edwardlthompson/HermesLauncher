package org.hermeslauncher.app.feeds

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.hermeslauncher.app.HermesApplication

class FeedRepository(
    private val context: Context,
    private val store: FeedStore,
    private val articles: ArticleStore,
) {
    private val items = MutableStateFlow<List<FeedItem>>(emptyList())
    private val failed = MutableStateFlow(false)
    private val busy = MutableStateFlow(false)
    val feedItems: StateFlow<List<FeedItem>> = items
    val articleRows: Flow<List<ArticleRecord>> = articles.records
    val refreshFailed: StateFlow<Boolean> = failed
    val refreshing: StateFlow<Boolean> = busy

    suspend fun importOpml(uri: Uri, kind: SubKind = SubKind.NEWS) {
        val outlines = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { OpmlImporter.read(it) }.orEmpty()
        }
        val next = FeedOpml.imported(store.snapshot(), outlines, kind)
        next.forEach { store.upsert(it) }
        refresh()
    }

    suspend fun exportOpml(uri: Uri, kind: SubKind = SubKind.NEWS) {
        val outlines = FeedOpml.outlines(store.snapshot(), kind)
        val body = OpmlExporter.write(outlines, FeedOpml.titleFor(kind))
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(body.toByteArray()) }
        }
    }

    suspend fun refresh() {
        busy.value = true
        try {
            ArticleThumb.purgeLegacyThumbs(context.filesDir)
            store.seedIfNeeded()
            val subs = store.snapshot().sortedBy { sub -> if (sub.kind == SubKind.PODCAST) 0 else 1 }
            val urls = subs.map { FeedDiscover.canonicalize(it.url) }.distinct()
            if (urls.toSet() != store.snapshot().map { it.url }.toSet()) {
                store.replaceAll(urls)
            }
            val now = System.currentTimeMillis()
            val before = articles.snapshot()
            val errors = mutableMapOf<String, String?>()
            val xmls = mutableMapOf<String, String?>()
            val fetched = mutableListOf<FeedItem>()
            var shown = before
            withContext(Dispatchers.IO) {
                for (url in urls) {
                    val outcome = FeedFetch.items(url)
                    errors[url] = outcome.error
                    xmls[url] = outcome.xml
                    fetched += outcome.items
                    shown = FeedFilter.merge(before, MixPolicy.withinWindow(fetched, now), now)
                    articles.replaceAll(shown)
                    items.value = shown.map { it.item }
                }
            }
            for (sub in store.snapshot()) {
                var next = sub
                if (sub.url in errors) {
                    next = next.copy(lastError = errors[sub.url])
                }
                next = FeedKindSync.afterFetch(next, xmls[sub.url])
                if (next != sub) {
                    store.upsert(next)
                }
            }
            val filled = withContext(Dispatchers.IO) { ArticleEnrich.fillRecords(shown) }
            FeedFull.deleteIds(context.filesDir, FeedFilter.droppedIds(before, filled))
            articles.replaceAll(filled)
            items.value = filled.map { it.item }
            failed.value = urls.isNotEmpty() && fetched.isEmpty()
            val prefs = (context.applicationContext as? HermesApplication)?.readerPrefs?.settingsFirst() ?: ReaderSettings()
            val live = store.snapshot()
            if (FeedSync.allowDownload(context, prefs)) {
                withContext(Dispatchers.IO) {
                    FeedFull.prefetch(context.filesDir, filled, true, live.filter { it.prefetch }.map { it.url }.toSet())
                    PodcastAudio.prefetch(context.filesDir, filled, live, FeedSync.allowImages(context, prefs))
                }
            }
            FeedNotify.post(context, FeedNotify.newUnread(before, filled, live.filter { it.notify }.map { it.url }.toSet()))
            Log.i(TAG, "refresh urls=${urls.size} items=${items.value.size}")
        } finally {
            busy.value = false
        }
    }

    suspend fun expire() {
        val before = articles.snapshot()
        val next = FeedFilter.purge(before, System.currentTimeMillis())
        FeedFull.deleteIds(context.filesDir, FeedFilter.droppedIds(before, next))
        articles.replaceAll(next)
        items.value = next.map { it.item }
    }

    suspend fun markRead(id: String) = articles.markRead(id)
    suspend fun markUnread(id: String) = articles.markUnread(id)
    suspend fun toggleStar(id: String) = articles.toggleStar(id)
    suspend fun markAllRead(ids: Set<String>? = null) = articles.markAllRead(ids)

    suspend fun itemById(id: String): FeedItem? =
        articles.snapshot().firstOrNull { it.item.id == id }?.item

    suspend fun addFromLink(raw: String, kind: SubKind = SubKind.NEWS): Boolean {
        val url = withContext(Dispatchers.IO) { runCatching { FeedFetcher.resolve(raw) }.getOrNull() } ?: return false
        store.upsert(FeedSub(url = url, kind = kind, prefetch = kind == SubKind.NEWS))
        refresh()
        return true
    }

    companion object {
        private const val TAG = "HermesFeeds"
    }
}
