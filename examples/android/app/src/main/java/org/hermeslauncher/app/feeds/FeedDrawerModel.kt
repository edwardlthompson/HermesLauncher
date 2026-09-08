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
    val depth: Int = 0,
    val childCount: Int = 0,
)

internal data class DrawerNode(
    val url: String,
    val title: String,
    val unread: Int,
    val tag: String,
)

object FeedDrawerModel {
    fun rows(
        records: List<ArticleRecord>,
        query: FeedQuery = FeedQuery(),
        search: String = "",
        tags: Map<String, String> = emptyMap(),
        hideEmpty: Boolean = true,
    ): List<DrawerRow> {
        val needle = search.trim()
        val out = mutableListOf(
            DrawerRow(DrawerKind.ALL, "All feeds", records.count { !it.read }),
            DrawerRow(DrawerKind.SAVED, "Saved", records.count { it.starred && !it.read }, savedOnly = true),
        )
        val groups = records.groupBy { it.item.sourceUrl ?: it.item.feedTitle }
        val nodes = (groups.keys + tags.keys).distinct().map { key ->
            val recs = groups[key].orEmpty()
            val url = key.takeIf { FeedFetcher.isHttpUrl(it) } ?: key
            val title = recs.firstOrNull()?.item?.feedTitle?.ifBlank { key } ?: key
            val tag = tags[key].orEmpty().ifBlank { tags[url].orEmpty() }
            DrawerNode(url = url, title = title, unread = recs.count { !it.read }, tag = tag)
        }
        val byTag = nodes.groupBy { it.tag }
        for ((tag, children) in byTag.filter { it.key.isNotBlank() }.toSortedMap(String.CASE_INSENSITIVE_ORDER)) {
            val kids = folderKids(children, tag, needle, query, hideEmpty)
            if (kids.isEmpty()) {
                continue
            }
            out.add(
                DrawerRow(
                    DrawerKind.TAG,
                    tag,
                    kids.sumOf { it.unread },
                    tag = tag,
                    childCount = kids.size,
                ),
            )
            kids.forEach { node ->
                out.add(
                    DrawerRow(
                        DrawerKind.FEED,
                        node.title,
                        node.unread,
                        sourceUrl = node.url,
                        tag = tag,
                        depth = 1,
                    ),
                )
            }
        }
        byTag[""].orEmpty().sortedBy { it.title.lowercase() }.forEach { node ->
            if (includeFeed(node, needle, query, hideEmpty)) {
                out.add(DrawerRow(DrawerKind.FEED, node.title, node.unread, sourceUrl = node.url))
            }
        }
        return out
    }

    fun visible(rows: List<DrawerRow>, openTags: Set<String>, searching: Boolean): List<DrawerRow> {
        return rows.filter { row ->
            row.kind != DrawerKind.FEED || row.depth == 0 || searching || row.tag in openTags
        }
    }

    private fun folderKids(
        children: List<DrawerNode>,
        tag: String,
        needle: String,
        query: FeedQuery,
        hideEmpty: Boolean,
    ): List<DrawerNode> {
        val sorted = children.sortedBy { it.title.lowercase() }
        if (needle.isNotEmpty()) {
            val tagHit = tag.contains(needle, ignoreCase = true)
            return sorted.filter { tagHit || it.title.contains(needle, ignoreCase = true) }
        }
        return sorted.filter { includeFeed(it, needle, query, hideEmpty) }
    }

    private fun includeFeed(
        node: DrawerNode,
        needle: String,
        query: FeedQuery,
        hideEmpty: Boolean,
    ): Boolean {
        if (needle.isNotEmpty()) {
            return node.title.contains(needle, ignoreCase = true)
        }
        val pinned = query.sourceUrl != null && (query.sourceUrl == node.url || query.sourceUrl == node.title)
        return !hideEmpty || node.unread > 0 || pinned
    }
}
