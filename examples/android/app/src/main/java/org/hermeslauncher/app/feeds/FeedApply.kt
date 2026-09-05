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
}
