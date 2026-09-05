package org.hermeslauncher.app.feeds

import org.w3c.dom.Element
import java.text.ParsePosition
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

object RssParser {
    private val rfc1123 = DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US)

    fun parse(xml: String): List<FeedItem> {
        val doc = XmlDocuments.parse(xml) ?: return emptyList()
        val channels = doc.getElementsByTagName("channel")
        if (channels.length == 0) {
            val feed = doc.getElementsByTagName("feed").item(0) as? Element ?: return emptyList()
            return AtomParser.parse(feed)
        }
        val channel = channels.item(0) as? Element ?: return emptyList()
        val feedTitle = XmlDocuments.childText(channel, "title").orEmpty()
        return XmlDocuments.elementsNamed(channel, "item").mapIndexed { index, item ->
            val title = XmlDocuments.childText(item, "title").orEmpty()
            val link = XmlDocuments.childText(item, "link")
            val guid = XmlDocuments.childText(item, "guid")
            val enclosure = item.getElementsByTagName("enclosure").item(0) as? Element
            val html = XmlDocuments.firstChildText(item, "content:encoded", "encoded", "description")
            val thumb = XmlDocuments.attr(item, "media:thumbnail", "url")
                ?: XmlDocuments.attr(item, "thumbnail", "url")
                ?: XmlDocuments.attr(item, "media:content", "url")
            FeedItem(
                id = guid ?: link ?: "$feedTitle:$index",
                feedTitle = feedTitle,
                title = title,
                link = link,
                publishedAt = parseDate(XmlDocuments.childText(item, "pubDate")),
                enclosureUrl = enclosure?.getAttribute("url")?.takeIf { it.isNotBlank() },
                enclosureMime = enclosure?.getAttribute("type")?.takeIf { it.isNotBlank() },
                html = html,
                imageUrl = ArticleImages.fromRss(
                    thumb,
                    enclosure?.getAttribute("url"),
                    enclosure?.getAttribute("type"),
                    html,
                ),
            )
        }
    }

    private fun parseDate(raw: String?): Long {
        if (raw.isNullOrBlank()) {
            return 0L
        }
        return runCatching {
            Instant.from(rfc1123.parse(raw, ParsePosition(0))).toEpochMilli()
        }.getOrDefault(0L)
    }
}
