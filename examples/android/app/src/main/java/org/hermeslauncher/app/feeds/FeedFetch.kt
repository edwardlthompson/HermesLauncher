package org.hermeslauncher.app.feeds

import android.util.Log
import java.net.URI

data class FetchOutcome(
    val items: List<FeedItem>,
    val error: String?,
    val xml: String? = null,
)

object FeedFetch {
    fun items(url: String): FetchOutcome {
        var error: String? = null
        repeat(2) { attempt ->
            val result = runCatching {
                val xml = FeedFetcher.fetchXml(url)
                val rows = FeedFetcher.itemsFromXml(xml).map { item ->
                    item.copy(sourceUrl = url, feedTitle = item.feedTitle.ifBlank { hostOf(url) })
                }
                xml to rows
            }
            error = result.exceptionOrNull()?.message
            result.exceptionOrNull()?.let { err ->
                Log.w("HermesFeeds", "fetch $url ${err.javaClass.simpleName}: ${err.message}")
            }
            val pair = result.getOrNull()
            if (pair != null) {
                return FetchOutcome(items = pair.second, error = null, xml = pair.first)
            }
            if (attempt < 1) {
                Thread.sleep(400L)
            }
        }
        return FetchOutcome(items = emptyList(), error = error)
    }

    private fun hostOf(url: String): String {
        return runCatching { URI(url).host.orEmpty() }.getOrDefault(url)
    }
}
