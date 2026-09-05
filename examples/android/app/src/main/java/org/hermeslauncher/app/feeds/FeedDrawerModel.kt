package org.hermeslauncher.app.feeds

enum class DrawerKind {
    ALL,
    SAVED,
    TAG,
    FEED,
}

data class DrawerRow(
    val kind: DrawerKind,
    val title: String,
    val unread: Int,
    val sourceUrl: String? = null,
    val savedOnly: Boolean = false,
    val tag: String = "",
)

object FeedDrawerModel {
    fun rows(
        records: List<ArticleRecord>,
        query: FeedQuery = FeedQuery(),
        search: String = "",
        tags: Map<String, String> = emptyMap(),
    ): List<DrawerRow> {
        val needle = search.trim()
        val total = records.count { !it.read }
        val saved = records.count { it.starred && !it.read }
        val out = mutableListOf(
            DrawerRow(DrawerKind.ALL, "All feeds", total, sourceUrl = null, savedOnly = false),
            DrawerRow(DrawerKind.SAVED, "Saved", saved, sourceUrl = null, savedOnly = true),
        )
        val groups = records.groupBy { it.item.sourceUrl ?: it.item.feedTitle }
        val tagBuckets = groups.entries.groupBy { (key, _) -> tags[key].orEmpty() }
        for ((tag, entries) in tagBuckets.filter { it.key.isNotBlank() }.toSortedMap(String.CASE_INSENSITIVE_ORDER)) {
            val unread = entries.sumOf { it.value.count { rec -> !rec.read } }
            if (needle.isEmpty() && unread == 0) {
                continue
            }
            if (needle.isNotEmpty() && !tag.contains(needle, ignoreCase = true)) {
                continue
            }
            out.add(DrawerRow(DrawerKind.TAG, tag, unread, tag = tag))
        }
        val feeds = groups.map { (key, rows) ->
            val title = rows.first().item.feedTitle.ifBlank { key }
            val unread = rows.count { !it.read }
            Triple(key, title, unread)
        }.sortedBy { it.second.lowercase() }
        for ((key, title, unread) in feeds) {
            val pinned = query.sourceUrl != null && (query.sourceUrl == key || query.sourceUrl == title)
            val tagName = tags[key].orEmpty()
            val matches = needle.isNotEmpty() &&
                (title.contains(needle, ignoreCase = true) || tagName.contains(needle, ignoreCase = true))
            if (needle.isEmpty() && unread == 0 && !pinned) {
                continue
            }
            if (needle.isNotEmpty() && !matches) {
                continue
            }
            val url = key.takeIf { FeedFetcher.isHttpUrl(it) }
            out.add(
                DrawerRow(
                    DrawerKind.FEED,
                    title,
                    unread,
                    sourceUrl = url ?: key,
                    tag = tags[key].orEmpty(),
                ),
            )
        }
        return out
    }
}
