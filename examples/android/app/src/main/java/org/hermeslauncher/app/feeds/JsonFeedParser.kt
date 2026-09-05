package org.hermeslauncher.app.feeds

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

object JsonFeedParser {
    fun parse(json: String): List<FeedItem> {
        val obj = runCatching { JSONObject(json) }.getOrNull() ?: return emptyList()
        val version = obj.optString("version")
        if (version.isNotBlank() && "jsonfeed.org" !in version.lowercase() && !version.startsWith("https://jsonfeed.org")) {
            if (!obj.has("items")) {
                return emptyList()
            }
        }
        val feedTitle = obj.optString("title")
        val items = obj.optJSONArray("items") ?: JSONArray()
        return buildList {
            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val html = item.optString("content_html").ifBlank { item.optString("content_text") }
                val url = item.optString("url").ifBlank { item.optString("external_url") }
                val id = item.optString("id").ifBlank { url }.ifBlank { "$feedTitle:$i" }
                val image = item.optString("image").ifBlank { item.optString("banner_image") }
                add(
                    FeedItem(
                        id = id,
                        feedTitle = feedTitle,
                        title = item.optString("title"),
                        link = url.takeIf { it.isNotBlank() },
                        publishedAt = parseDate(item.optString("date_published")),
                        html = html.takeIf { it.isNotBlank() },
                        imageUrl = ArticleImages.canonicalHero(image.takeIf { it.isNotBlank() }, null, null, html),
                    ),
                )
            }
        }
    }

    private fun parseDate(raw: String): Long {
        if (raw.isBlank()) {
            return 0L
        }
        return runCatching { Instant.parse(raw.trim()).toEpochMilli() }.getOrDefault(0L)
    }
}
