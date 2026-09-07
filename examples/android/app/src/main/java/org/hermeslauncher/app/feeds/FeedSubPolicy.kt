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

    fun folderNames(subs: List<FeedSub>): List<String> =
        subs.map { it.tag.trim() }.filter { it.isNotEmpty() }.distinct().sortedBy { it.lowercase() }

    fun folderUrls(subs: List<FeedSub>, tag: String): Set<String> =
        subs.filter { it.tag == tag }.map { it.url }.toSet()

    fun setTag(subs: List<FeedSub>, url: String, tag: String): List<FeedSub> {
        if (url.isBlank()) {
            return subs
        }
        val canon = FeedDiscover.canonicalize(url)
        val next = tag.trim()
        return subs.map { sub ->
            if (sub.url == url || FeedDiscover.canonicalize(sub.url) == canon) {
                sub.copy(tag = next)
            } else {
                sub
            }
        }
    }

    fun renameTag(subs: List<FeedSub>, from: String, to: String): List<FeedSub> {
        val src = from.trim()
        if (src.isEmpty()) {
            return subs
        }
        val dest = to.trim()
        return subs.map { sub -> if (sub.tag == src) sub.copy(tag = dest) else sub }
    }
}
