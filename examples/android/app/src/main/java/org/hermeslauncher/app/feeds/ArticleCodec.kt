package org.hermeslauncher.app.feeds

import org.json.JSONArray
import org.json.JSONObject

object ArticleCodec {
    fun encode(records: List<ArticleRecord>): String {
        val arr = JSONArray()
        for (rec in records) {
            val item = rec.item
            arr.put(
                JSONObject()
                    .put("id", item.id)
                    .put("feedTitle", item.feedTitle)
                    .put("title", item.title)
                    .put("link", item.link ?: JSONObject.NULL)
                    .put("publishedAt", item.publishedAt)
                    .put("enclosureUrl", item.enclosureUrl ?: JSONObject.NULL)
                    .put("enclosureMime", item.enclosureMime ?: JSONObject.NULL)
                    .put("html", item.html?.take(20_000) ?: JSONObject.NULL)
                    .put("imageUrl", item.imageUrl ?: JSONObject.NULL)
                    .put("sourceUrl", item.sourceUrl ?: JSONObject.NULL)
                    .put("starred", rec.starred)
                    .put("read", rec.read)
                    .put("firstSeen", rec.firstSeen)
                    .put("readAt", rec.readAt),
            )
        }
        return arr.toString()
    }

    fun decode(raw: String?): List<ArticleRecord> {
        if (raw.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        ArticleRecord(
                            item = FeedItem(
                                id = obj.optString("id"),
                                feedTitle = obj.optString("feedTitle"),
                                title = obj.optString("title"),
                                link = obj.stringOrNull("link"),
                                publishedAt = obj.optLong("publishedAt"),
                                enclosureUrl = obj.stringOrNull("enclosureUrl"),
                                enclosureMime = obj.stringOrNull("enclosureMime"),
                                html = obj.stringOrNull("html"),
                                imageUrl = obj.stringOrNull("imageUrl"),
                                sourceUrl = obj.stringOrNull("sourceUrl"),
                            ),
                            starred = obj.optBoolean("starred"),
                            read = obj.optBoolean("read"),
                            firstSeen = obj.optLong("firstSeen"),
                            readAt = obj.optLong("readAt"),
                        ),
                    )
                }
            }.filter { it.item.id.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun JSONObject.stringOrNull(key: String): String? {
        if (!has(key) || isNull(key)) {
            return null
        }
        return optString(key).takeIf { it.isNotBlank() }
    }
}
