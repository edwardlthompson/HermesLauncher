package org.hermeslauncher.app.feeds

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class FeedRepository(
    private val context: Context,
    private val store: FeedStore,
) {
    private val items = MutableStateFlow<List<FeedItem>>(emptyList())
    val feedItems: StateFlow<List<FeedItem>> = items

    suspend fun importOpml(uri: Uri) {
        val outlines = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { OpmlImporter.read(it) }.orEmpty()
        }
        store.addAll(outlines.map { it.xmlUrl })
        refresh()
    }

    suspend fun refresh() {
        val urls = store.urls.first()
        val fetched = withContext(Dispatchers.IO) {
            urls.flatMap { url ->
                runCatching { FeedFetcher.itemsFromXml(FeedFetcher.fetchXml(url)) }
                    .getOrDefault(emptyList())
            }
        }
        items.value = MixPolicy.withinWindow(fetched, System.currentTimeMillis())
    }

    suspend fun addFromLink(raw: String): Boolean {
        val url = withContext(Dispatchers.IO) {
            runCatching { FeedFetcher.resolve(raw) }.getOrNull()
        } ?: return false
        store.addAll(listOf(url))
        refresh()
        return true
    }
}
