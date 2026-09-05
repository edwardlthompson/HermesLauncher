package org.hermeslauncher.app.feeds

import java.io.File

object FeedFull {
    fun file(dir: File, id: String): File {
        return File(File(dir, "feed-full"), "${id.hashCode() and 0x7fffffff}.html")
    }

    fun save(dir: File, id: String, html: String) {
        if (html.length < 40) {
            return
        }
        val dest = file(dir, id)
        runCatching {
            dest.parentFile?.mkdirs()
            dest.writeText(html.take(500_000))
        }
    }

    fun load(dir: File, id: String): String? {
        val dest = file(dir, id)
        if (!dest.isFile) {
            return null
        }
        return runCatching { dest.readText() }.getOrNull()?.takeIf { it.length >= 40 }
    }

    fun deleteIds(dir: File, ids: Set<String>) {
        for (id in ids) {
            runCatching { file(dir, id).delete() }
            runCatching { ArticleThumb.originalFile(dir, id).delete() }
        }
    }

    fun prefetch(dir: File, records: List<ArticleRecord>, allow: Boolean, prefetchUrls: Set<String>) {
        if (!allow) {
            return
        }
        for (rec in records) {
            val url = rec.item.articleUrl() ?: continue
            val source = rec.item.sourceUrl
            if (source != null && source !in prefetchUrls && prefetchUrls.isNotEmpty()) {
                continue
            }
            if (load(dir, rec.item.id) != null) {
                continue
            }
            val html = runCatching { FeedFetcher.fetchXml(url) }.getOrNull() ?: continue
            save(dir, rec.item.id, html)
        }
    }
}
