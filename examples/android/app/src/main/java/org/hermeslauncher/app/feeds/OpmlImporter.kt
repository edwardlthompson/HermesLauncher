package org.hermeslauncher.app.feeds

import java.io.InputStream

object OpmlImporter {
    fun read(stream: InputStream): List<OpmlOutline> {
        val xml = runCatching { stream.bufferedReader().use { it.readText() } }.getOrDefault("")
        return OpmlParser.parse(xml)
            .filter { FeedFetcher.isHttpUrl(it.xmlUrl) }
    }
}
