package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedSubPolicyTest {
    @Test
    fun allNotifyTrueOnlyWhenEverySubNotifies() {
        val mixed = listOf(sub(notify = true), sub(url = "https://b.example/f", notify = false))
        assertFalse(FeedSubPolicy.allNotify(mixed))
        assertTrue(FeedSubPolicy.allNotify(FeedSubPolicy.setAllNotify(mixed, true)))
        assertTrue(FeedSubPolicy.setAllNotify(mixed, false).none { it.notify })
        assertFalse(FeedSubPolicy.allNotify(emptyList()))
    }

    @Test
    fun allPrefetchAndKindSplit() {
        val rows = listOf(
            sub(kind = SubKind.NEWS, prefetch = true, title = "Zed"),
            sub(url = "https://p.example/rss", kind = SubKind.PODCAST, prefetch = false, title = "Alpha"),
        )
        assertFalse(FeedSubPolicy.allPrefetch(rows))
        assertEquals(listOf("Zed"), FeedSubPolicy.ofKind(rows, SubKind.NEWS).map { it.title })
        assertEquals(listOf("Alpha"), FeedSubPolicy.ofKind(rows, SubKind.PODCAST).map { it.title })
        assertTrue(FeedSubPolicy.setAllPrefetch(rows, true).all { it.prefetch })
    }

    @Test
    fun withoutUrlDropsCanonicalMatchAndIgnoresBlank() {
        val rows = listOf(sub(), sub(url = "https://gone.example/feed/"))
        assertEquals(rows, FeedSubPolicy.withoutUrl(rows, ""))
        val next = FeedSubPolicy.withoutUrl(rows, "https://gone.example/feed")
        assertEquals(listOf("https://a.example/f"), next.map { it.url })
    }

    private fun sub(
        url: String = "https://a.example/f",
        title: String = "A",
        kind: SubKind = SubKind.NEWS,
        notify: Boolean = false,
        prefetch: Boolean = true,
    ) = FeedSub(url = url, title = title, kind = kind, notify = notify, prefetch = prefetch)
}
