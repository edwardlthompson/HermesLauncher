package org.hermeslauncher.app.feeds

import java.io.File

object PodcastAudio {
    const val DIR: String = "podcast-audio"
    const val DEFAULT_KEEP: Int = 1

    fun fileName(id: String): String = id.hashCode().toUInt().toString(16) + ".bin"

    fun file(filesDir: File, id: String): File = File(File(filesDir, DIR), fileName(id))

    fun playUri(filesDir: File, item: FeedItem): String? {
        val local = file(filesDir, item.id)
        if (local.isFile && local.length() > 0L) {
            return local.absolutePath
        }
        return item.enclosureUrl?.takeIf { it.isNotBlank() }
    }

    fun latest(items: List<FeedItem>, keep: Int = DEFAULT_KEEP): List<FeedItem> {
        if (keep <= 0) {
            return emptyList()
        }
        return items
            .filter { !it.enclosureUrl.isNullOrBlank() }
            .groupBy { it.sourceUrl.orEmpty() }
            .values
            .flatMap { rows -> rows.sortedByDescending { it.publishedAt }.take(keep) }
    }

    fun prefetch(filesDir: File, records: List<ArticleRecord>, subs: List<FeedSub>, allow: Boolean, keep: Int = DEFAULT_KEEP) {
        if (!allow) {
            return
        }
        val urls = subs.filter { it.kind == SubKind.PODCAST }.map { it.url }.toSet()
        val episodes = records.map { it.item }.filter { it.sourceUrl in urls }
        for (item in latest(episodes, keep)) {
            download(filesDir, item)
        }
    }

    fun download(filesDir: File, item: FeedItem) {
        val url = item.enclosureUrl?.takeIf { FeedFetcher.isHttpUrl(it) } ?: return
        val dest = file(filesDir, item.id)
        if (dest.isFile && dest.length() > 0L) {
            return
        }
        runCatching {
            dest.parentFile?.mkdirs()
            val bytes = FeedFetcher.fetchBytes(url)
            if (bytes.isNotEmpty()) {
                dest.writeBytes(bytes)
            }
        }
    }
}
