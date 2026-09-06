package org.hermeslauncher.app.feeds

object FeedSubPolicy {
    fun allNotify(subs: List<FeedSub>): Boolean =
        subs.isNotEmpty() && subs.all { it.notify }

    fun allPrefetch(subs: List<FeedSub>): Boolean =
        subs.isNotEmpty() && subs.all { it.prefetch }

    fun setAllNotify(subs: List<FeedSub>, on: Boolean): List<FeedSub> =
        subs.map { it.copy(notify = on) }

    fun setAllPrefetch(subs: List<FeedSub>, on: Boolean): List<FeedSub> =
        subs.map { it.copy(prefetch = on) }

    fun ofKind(subs: List<FeedSub>, kind: SubKind): List<FeedSub> =
        subs.filter { it.kind == kind }.sortedBy { it.title.ifBlank { it.url }.lowercase() }

    fun withoutUrl(subs: List<FeedSub>, url: String): List<FeedSub> {
        if (url.isBlank()) {
            return subs
        }
        val canon = FeedDiscover.canonicalize(url)
        return subs.filter { sub ->
            sub.url != url && FeedDiscover.canonicalize(sub.url) != canon
        }
    }
}
