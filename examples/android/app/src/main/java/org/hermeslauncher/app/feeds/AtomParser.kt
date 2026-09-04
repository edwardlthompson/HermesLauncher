package org.hermeslauncher.app.feeds

import org.w3c.dom.Element
import java.time.Instant

object AtomParser {
    fun parse(feed: Element): List<FeedItem> {
        val feedTitle = XmlDocuments.childText(feed, "title").orEmpty()
        return XmlDocuments.elementsNamed(feed, "entry").mapIndexed { index, entry ->
            val title = XmlDocuments.childText(entry, "title").orEmpty()
            val id = XmlDocuments.childText(entry, "id")
            val link = href(entry, "alternate") ?: href(entry, "")
            FeedItem(
                id = id ?: link ?: "$feedTitle:$index",
                feedTitle = feedTitle,
                title = title,
                link = link,
                publishedAt = parseDate(
                    XmlDocuments.childText(entry, "published") ?: XmlDocuments.childText(entry, "updated"),
                ),
                enclosureUrl = href(entry, "enclosure"),
                enclosureMime = mime(entry, "enclosure"),
            )
        }
    }

    private fun href(entry: Element, rel: String): String? {
        val nodes = entry.getElementsByTagName("link")
        for (i in 0 until nodes.length) {
            val node = nodes.item(i) as? Element ?: continue
            if (node.parentNode != entry) {
                continue
            }
            val got = node.getAttribute("rel").ifBlank { "alternate" }
            if (rel.isNotEmpty() && got != rel) {
                continue
            }
            val href = node.getAttribute("href").trim()
            if (href.isNotEmpty()) {
                return href
            }
        }
        return null
    }

    private fun mime(entry: Element, rel: String): String? {
        val nodes = entry.getElementsByTagName("link")
        for (i in 0 until nodes.length) {
            val node = nodes.item(i) as? Element ?: continue
            if (node.getAttribute("rel") != rel) {
                continue
            }
            return node.getAttribute("type").takeIf { it.isNotBlank() }
        }
        return null
    }

    private fun parseDate(raw: String?): Long {
        if (raw.isNullOrBlank()) {
            return 0L
        }
        return runCatching { Instant.parse(raw.trim()).toEpochMilli() }.getOrDefault(0L)
    }
}
