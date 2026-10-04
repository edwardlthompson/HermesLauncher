package org.hermeslauncher.app.feeds

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

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
        FeedRefreshGate.runOrSkip {
            busy.value = true
            try {
                withContext(Dispatchers.IO) {
                    FeedRefresh.run(
                        context,
                        store,
                        articles,
                        publishItems = { items.value = it },
                        publishFailed = { failed.value = it },
                    )
                }
            } finally {
                busy.value = false
            }
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

    suspend fun unsubscribe(url: String) {
        if (url.isBlank()) {
            return
        }
        store.remove(url)
        val before = articles.snapshot()
        val next = FeedApply.dropSource(before, url)
        FeedFull.deleteIds(context.filesDir, FeedFilter.droppedIds(before, next))
        articles.replaceAll(next)
        items.value = next.map { it.item }
    }
}
