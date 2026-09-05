package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedFilterTest {
    private val day = MixPolicy.WINDOW_MS / 30

    @Test
    fun unreadCountIgnoresReadRows() {
        val rows = listOf(rec("a", read = false), rec("b", read = true))
        assertEquals(1, FeedFilter.unreadCount(rows))
    }

    @Test
    fun unreadChipHidesRead() {
        val rows = listOf(rec("a", read = false, title = "New"), rec("b", read = true, title = "Old"))
        val out = FeedFilter.apply(rows, FeedQuery(chip = FeedChip.UNREAD))
        assertEquals(listOf("a"), out.map { it.item.id })
    }

    @Test
    fun readAndStarredChips() {
        val rows = listOf(
            rec("a", read = true, starred = false),
            rec("b", read = false, starred = true),
        )
        assertEquals(listOf("a"), FeedFilter.apply(rows, FeedQuery(chip = FeedChip.READ)).map { it.item.id })
        assertEquals(listOf("b"), FeedFilter.apply(rows, FeedQuery(chip = FeedChip.STARRED)).map { it.item.id })
    }

    @Test
    fun searchMatchesTitleAndFeed() {
        val rows = listOf(
            rec("a", title = "Pixel cameras", feed = "Android Authority"),
            rec("b", title = "Other", feed = "Elsewhere"),
        )
        val out = FeedFilter.apply(rows, FeedQuery(text = "pixel"))
        assertEquals(listOf("a"), out.map { it.item.id })
        assertEquals(1, FeedFilter.apply(rows, FeedQuery(text = "authority")).size)
    }

    @Test
    fun purgeKeepsStarredPastWindow() {
        val now = 40 * day
        val oldStar = rec("star", starred = true, published = 1L)
        val oldPlain = rec("gone", starred = false, published = 1L)
        val fresh = rec("new", published = now)
        val kept = FeedFilter.purge(listOf(oldStar, oldPlain, fresh), now)
        assertEquals(setOf("star", "new"), kept.map { it.item.id }.toSet())
    }

    @Test
    fun purgeDropsReadAfterOneDayUnlessStarred() {
        val now = 10 * MixPolicy.READ_TTL_MS
        val oldRead = rec("gone", read = true, readAt = now - MixPolicy.READ_TTL_MS - 1, published = now)
        val freshRead = rec("keep-read", read = true, readAt = now - 1_000L, published = now)
        val oldReadStar = rec("star", read = true, starred = true, readAt = 1L, published = 1L)
        val unreadFresh = rec("unread", read = false, published = now)
        val kept = FeedFilter.purge(listOf(oldRead, freshRead, oldReadStar, unreadFresh), now)
        assertEquals(setOf("keep-read", "star", "unread"), kept.map { it.item.id }.toSet())
    }

    @Test
    fun mergeKeepsStarredMissingFromFetch() {
        val now = 1_000L
        val existing = listOf(rec("keep", starred = true, title = "Saved"))
        val merged = FeedFilter.merge(existing, fetched = emptyList(), now = now)
        assertTrue(merged.any { it.item.id == "keep" && it.starred })
    }

    @Test
    fun adjacentWalksTrail() {
        val ids = listOf("a", "b", "c")
        assertEquals(null to "b", FeedFilter.adjacent(ids, "a"))
        assertEquals("a" to "c", FeedFilter.adjacent(ids, "b"))
        assertEquals("b" to null, FeedFilter.adjacent(ids, "c"))
        assertEquals(null to null, FeedFilter.adjacent(ids, "z"))
    }

    @Test
    fun sourceUrlIsolatesOneFeed() {
        val rows = listOf(
            rec("a", feed = "AA", source = "https://aa.example/feed"),
            rec("b", feed = "FD", source = "https://fd.example/feed", published = 20L),
        )
        val isolated = FeedFilter.apply(rows, FeedQuery(sourceUrl = "https://fd.example/feed"))
        assertEquals(listOf("b"), isolated.map { it.item.id })
        val mixed = FeedFilter.apply(rows, FeedQuery())
        assertEquals(listOf("b", "a"), mixed.map { it.item.id })
    }

    @Test
    fun savedOnlyAndsWithStarredChip() {
        val rows = listOf(
            rec("a", starred = true),
            rec("b", starred = false),
            rec("c", starred = true, read = true),
        )
        val saved = FeedFilter.apply(rows, FeedQuery(savedOnly = true))
        assertEquals(setOf("a", "c"), saved.map { it.item.id }.toSet())
        val both = FeedFilter.apply(rows, FeedQuery(chip = FeedChip.STARRED, savedOnly = true))
        assertEquals(setOf("a", "c"), both.map { it.item.id }.toSet())
    }

    @Test
    fun drawerRowsHidesZeroUnreadKeepsAllSaved() {
        val rows = listOf(
            rec("a", feed = "Zebra", source = "https://z.example/feed", read = true),
            rec("b", feed = "Alpha", source = "https://a.example/feed", read = false),
        )
        val out = FeedFilter.drawerRows(rows)
        assertEquals(DrawerKind.ALL, out[0].kind)
        assertEquals(DrawerKind.SAVED, out[1].kind)
        assertEquals(listOf("Alpha"), out.filter { it.kind == DrawerKind.FEED }.map { it.title })
        val searched = FeedFilter.drawerRows(rows, search = "zeb")
        assertTrue(searched.any { it.title == "Zebra" })
    }

    @Test
    fun drawerRowsSearchTagKeepsTaggedFeeds() {
        val rows = listOf(
            rec("a", feed = "Zebra", source = "https://z.example/feed", read = true),
        )
        val out = FeedFilter.drawerRows(
            rows,
            search = "foss",
            tags = mapOf("https://z.example/feed" to "foss"),
        )
        assertTrue(out.any { it.kind == DrawerKind.TAG && it.tag == "foss" })
        assertTrue(out.any { it.kind == DrawerKind.FEED && it.title == "Zebra" })
    }

    @Test
    fun blockListHidesMatchingTitle() {
        val rows = listOf(rec("a", title = "Ads inside"), rec("b", title = "News"))
        val out = FeedFilter.apply(rows, FeedQuery(blocked = "ads"))
        assertEquals(listOf("b"), out.map { it.item.id })
    }

    private fun rec(
        id: String,
        title: String = id,
        feed: String = "Feed",
        read: Boolean = false,
        starred: Boolean = false,
        published: Long = 10L,
        readAt: Long = 0L,
        source: String? = null,
    ): ArticleRecord {
        return ArticleRecord(
            item = FeedItem(id = id, feedTitle = feed, title = title, publishedAt = published, sourceUrl = source),
            starred = starred,
            read = read,
            firstSeen = published,
            readAt = readAt,
        )
    }
}
