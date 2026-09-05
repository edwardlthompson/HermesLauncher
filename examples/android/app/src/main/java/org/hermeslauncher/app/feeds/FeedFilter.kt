package org.hermeslauncher.app.feeds

enum class FeedChip {
    ALL,
    UNREAD,
    READ,
    STARRED,
}

data class FeedQuery(
    val chip: FeedChip = FeedChip.ALL,
    val text: String = "",
    val newestFirst: Boolean = true,
    val sourceUrl: String? = null,
    val savedOnly: Boolean = false,
    val blocked: String = "",
)

data class ArticleRecord(
    val item: FeedItem,
    val starred: Boolean = false,
    val read: Boolean = false,
    val firstSeen: Long = 0L,
    val readAt: Long = 0L,
)

object FeedFilter {
    fun unreadCount(records: List<ArticleRecord>): Int {
        return records.count { !it.read }
    }

    fun apply(records: List<ArticleRecord>, query: FeedQuery): List<ArticleRecord> {
        val needle = query.text.trim()
        val filtered = records.filter { rec ->
            chipMatches(rec, query.chip) &&
                textMatches(rec.item, needle) &&
                FeedApply.sourceMatches(rec.item, query.sourceUrl) &&
                FeedApply.savedMatches(rec, query.savedOnly) &&
                !FeedApply.blocked(rec.item, query.blocked)
        }
        return if (query.newestFirst) {
            filtered.sortedByDescending { it.item.publishedAt }
        } else {
            filtered.sortedBy { it.item.publishedAt }
        }
    }

    fun purge(records: List<ArticleRecord>, now: Long, windowMs: Long = MixPolicy.WINDOW_MS): List<ArticleRecord> {
        return records.filter { rec ->
            when {
                rec.starred -> true
                rec.read -> {
                    val marked = rec.readAt.takeIf { it > 0L } ?: rec.firstSeen
                    marked >= now - MixPolicy.READ_TTL_MS
                }
                else -> withinWindow(rec, now, windowMs)
            }
        }
    }

    fun merge(
        existing: List<ArticleRecord>,
        fetched: List<FeedItem>,
        now: Long,
    ): List<ArticleRecord> {
        val byId = existing.associateBy { it.item.id }.toMutableMap()
        for (item in fetched) {
            val dup = item.duplicateKey()
            if (byId.values.any { it.item.id != item.id && it.item.duplicateKey() == dup }) {
                continue
            }
            val prev = byId[item.id]
            byId[item.id] = ArticleRecord(
                item = item.copy(
                    html = item.html ?: prev?.item?.html,
                    imageUrl = item.imageUrl ?: prev?.item?.imageUrl,
                    sourceUrl = item.sourceUrl ?: prev?.item?.sourceUrl,
                ),
                starred = prev?.starred == true,
                read = prev?.read == true,
                firstSeen = prev?.firstSeen?.takeIf { it > 0L } ?: now,
                readAt = prev?.readAt ?: 0L,
            )
        }
        return purge(byId.values.toList(), now)
    }

    fun drawerRows(
        records: List<ArticleRecord>,
        query: FeedQuery = FeedQuery(),
        search: String = "",
        tags: Map<String, String> = emptyMap(),
    ): List<DrawerRow> = FeedDrawerModel.rows(records, query, search, tags)

    fun droppedIds(before: List<ArticleRecord>, after: List<ArticleRecord>): Set<String> {
        return before.map { it.item.id }.toSet() - after.map { it.item.id }.toSet()
    }

    private fun withinWindow(rec: ArticleRecord, now: Long, windowMs: Long): Boolean {
        val ts = if (rec.item.publishedAt > 0L) rec.item.publishedAt else rec.firstSeen
        return ts == 0L || ts >= now - windowMs
    }

    private fun chipMatches(rec: ArticleRecord, chip: FeedChip): Boolean {
        return when (chip) {
            FeedChip.ALL -> true
            FeedChip.UNREAD -> !rec.read
            FeedChip.READ -> rec.read
            FeedChip.STARRED -> rec.starred
        }
    }

    private fun textMatches(item: FeedItem, needle: String): Boolean {
        if (needle.isEmpty()) {
            return true
        }
        val hay = listOf(item.title, item.feedTitle, item.html)
            .joinToString(" ") { it.orEmpty() }
            .lowercase()
        return hay.contains(needle.lowercase())
    }

    fun adjacent(ids: List<String>, id: String): Pair<String?, String?> {
        val index = ids.indexOf(id)
        if (index < 0) {
            return null to null
        }
        return ids.getOrNull(index - 1) to ids.getOrNull(index + 1)
    }
}
