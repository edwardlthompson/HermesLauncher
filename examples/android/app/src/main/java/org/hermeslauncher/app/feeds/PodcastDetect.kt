package org.hermeslauncher.app.feeds

/** Channel-level podcast vs article. Malformed XML is not a podcast. */
object PodcastDetect {
    fun fromXml(xml: String): Boolean {
        return runCatching { detect(xml) }.getOrDefault(false)
    }

    fun audioItem(item: FeedItem): Boolean {
        val mime = item.enclosureMime.orEmpty().lowercase()
        if (mime.startsWith("audio/")) {
            return true
        }
        val url = item.enclosureUrl.orEmpty().lowercase()
        return url.contains(".mp3") || url.contains(".m4a")
    }

    private fun detect(xml: String): Boolean {
        val lower = xml.lowercase()
        if ("itunes:" in lower || "podcast:" in lower || "xmlns:itunes" in lower) {
            return true
        }
        val items = FeedFetcher.itemsFromXml(xml)
        if (items.isEmpty()) {
            return false
        }
        val audio = items.count { audioItem(it) }
        return audio * 2 >= items.size
    }
}
