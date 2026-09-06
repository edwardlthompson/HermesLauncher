package org.hermeslauncher.app.feeds

internal object FeedApply {
    fun sourceMatches(item: FeedItem, sourceUrl: String?): Boolean {
        if (sourceUrl.isNullOrBlank()) {
            return true
        }
        return item.sourceUrl == sourceUrl || (item.sourceUrl.isNullOrBlank() && item.feedTitle == sourceUrl)
    }

    fun savedMatches(rec: ArticleRecord, savedOnly: Boolean): Boolean {
        return !savedOnly || rec.starred
    }

    fun blocked(item: FeedItem, blocked: String): Boolean {
        val needles = blocked.split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        if (needles.isEmpty()) {
            return false
        }
        val hay = listOf(item.title, item.feedTitle, item.link, item.sourceUrl)
            .joinToString(" ") { it.orEmpty() }
            .lowercase()
        return needles.any { it in hay }
    }

    fun dropSource(
        records: List<ArticleRecord>,
        url: String,
        keepStarred: Boolean = true,
    ): List<ArticleRecord> {
        if (url.isBlank()) {
            return records
        }
        val canon = FeedDiscover.canonicalize(url)
        return records.filter { rec ->
            val src = rec.item.sourceUrl.orEmpty()
            val match = src == url || (src.isNotBlank() && FeedDiscover.canonicalize(src) == canon)
            !match || (keepStarred && rec.starred)
        }
    }
}
