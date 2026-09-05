package org.hermeslauncher.app.feeds

object InoreaderSeed {
    const val SONY: String =
        "https://sony.mediaroom.com/index.php?s=2429&pagetemplate=rss"

    fun subs(xml: String): List<FeedSub> {
        val seen = mutableSetOf<String>()
        return OpmlParser.parse(xml).mapNotNull { outline ->
            val url = OpmlImporter.rewriteHttps(outline.xmlUrl)
            if (!FeedFetcher.isHttpUrl(url)) {
                return@mapNotNull null
            }
            val key = url.lowercase()
            if (!seen.add(key)) {
                return@mapNotNull null
            }
            val folder = outline.tag
            val sony = key.contains("sony.mediaroom.com")
            val podcastHint = folder.equals("Podcasts", ignoreCase = true)
            val kind = if (podcastHint && !sony) SubKind.PODCAST else SubKind.NEWS
            FeedSub(
                url = url,
                title = outline.title,
                tag = folder,
                kind = kind,
                prefetch = kind == SubKind.NEWS,
            )
        }
    }
}
