package org.hermeslauncher.app.feeds

object FeedOpml {
    fun titleFor(kind: SubKind): String {
        return if (kind == SubKind.PODCAST) "Hermes podcasts" else "Hermes feeds"
    }

    fun outlines(subs: List<FeedSub>, kind: SubKind): List<OpmlOutline> {
        return subs.filter { it.kind == kind }.map { sub ->
            OpmlOutline(
                title = sub.title.ifBlank { sub.url },
                xmlUrl = sub.url,
                tag = sub.tag,
            )
        }
    }

    fun imported(
        existing: List<FeedSub>,
        outlines: List<OpmlOutline>,
        importKind: SubKind,
    ): List<FeedSub> {
        val byUrl = existing.associateBy { it.url.lowercase() }
        return outlines.mapNotNull { outline ->
            val url = OpmlImporter.rewriteHttps(outline.xmlUrl)
            if (!FeedFetcher.isHttpUrl(url)) {
                return@mapNotNull null
            }
            val prev = byUrl[url.lowercase()]
            val alreadyNews = prev?.kind == SubKind.NEWS
            val kind = if (importKind == SubKind.PODCAST && alreadyNews) SubKind.NEWS else importKind
            FeedSub(
                url = url,
                title = outline.title.ifBlank { prev?.title.orEmpty() },
                tag = outline.tag.ifBlank { prev?.tag.orEmpty() },
                kind = kind,
                notify = prev?.notify ?: false,
                prefetch = kind == SubKind.NEWS && (prev?.prefetch ?: true),
                lastError = prev?.lastError,
            )
        }
    }
}
