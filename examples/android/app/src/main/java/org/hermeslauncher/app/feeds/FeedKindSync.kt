package org.hermeslauncher.app.feeds

object FeedKindSync {
    fun afterFetch(sub: FeedSub, xml: String?): FeedSub {
        if (xml.isNullOrBlank()) {
            return sub
        }
        val detect = runCatching { PodcastDetect.fromXml(xml) }.getOrDefault(false)
        val next = when {
            sub.kind == SubKind.NEWS && detect -> SubKind.PODCAST
            sub.kind == SubKind.PODCAST && !detect -> SubKind.NEWS
            else -> sub.kind
        }
        if (next == sub.kind) {
            return sub
        }
        return sub.copy(kind = next, prefetch = next == SubKind.NEWS)
    }
}
