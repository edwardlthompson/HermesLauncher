package org.hermeslauncher.app.feeds

import java.util.concurrent.Semaphore

/** Fill missing article image URLs from RSS HTML or the article page. */
object ArticleEnrich {
    private val gate = Semaphore(3)

    fun fillRecords(records: List<ArticleRecord>): List<ArticleRecord> {
        return records.map { rec ->
            if (!rec.item.imageUrl.isNullOrBlank()) {
                rec
            } else {
                val url = fromItem(rec.item)
                if (url == null) rec else rec.copy(item = rec.item.copy(imageUrl = url))
            }
        }
    }

    fun fromItem(item: FeedItem): String? {
        ArticleImages.canonicalHero(item.imageUrl, item.enclosureUrl, item.enclosureMime, item.html)?.let { return it }
        return pageHero(item)
    }

    private fun pageHero(item: FeedItem): String? {
        val page = item.articleUrl() ?: return null
        gate.acquire()
        val html = try {
            runCatching { FeedFetcher.fetchXml(page) }.getOrNull()
        } finally {
            gate.release()
        } ?: return null
        return ArticleImages.canonicalHero(null, null, null, html)
    }
}
