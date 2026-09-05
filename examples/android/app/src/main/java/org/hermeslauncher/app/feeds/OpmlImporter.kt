package org.hermeslauncher.app.feeds

import java.io.InputStream

object OpmlImporter {
    fun rewriteHttps(url: String): String {
        val trimmed = url.trim()
        if (!trimmed.startsWith("http://", ignoreCase = true)) {
            return trimmed
        }
        val https = "https://" + trimmed.substring(7)
        return if (FeedFetcher.isHttpUrl(https)) https else trimmed
    }

    fun read(stream: InputStream): List<OpmlOutline> {
        val xml = runCatching { stream.bufferedReader().use { it.readText() } }.getOrDefault("")
        return OpmlParser.parse(xml)
            .map { outline -> outline.copy(xmlUrl = rewriteHttps(outline.xmlUrl)) }
            .filter { FeedFetcher.isHttpUrl(it.xmlUrl) }
    }
}
