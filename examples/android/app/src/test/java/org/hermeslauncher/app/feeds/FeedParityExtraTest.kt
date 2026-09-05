package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class FeedSubCodecTest {
    @Test
    fun migrateSkipsNonHttp() {
        val subs = FeedSubCodec.fromUrls(listOf("https://ok.example/feed", "javascript:no"))
        assertEquals(1, subs.size)
        assertEquals("https://ok.example/feed", subs[0].url)
    }

    @Test
    fun roundTripKeepsKnobs() {
        val raw = FeedSubCodec.encode(listOf(FeedSub("https://a.example/f", title = "A", tag = "news", kind = SubKind.PODCAST, notify = true, prefetch = false)))
        val back = FeedSubCodec.decode(raw)
        assertEquals("news", back[0].tag)
        assertEquals(SubKind.PODCAST, back[0].kind)
        assertTrue(back[0].notify)
        assertFalse(back[0].prefetch)
    }
}

class FeedFullTest {
    @Test
    fun prefetchSkipsWhenOffline() {
        val dir = File.createTempFile("full", "d").apply { delete(); mkdirs() }
        FeedFull.prefetch(dir, emptyList(), allow = false, prefetchUrls = setOf("https://a.example/f"))
        assertFalse(File(dir, "feed-full").exists())
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ReaderHtmlTest {
    @Test
    fun imageGetterNeverFetches() {
        assertNull(ReaderHtml.noopImages.getDrawable("https://cdn.example.com/h.jpg"))
        val spanned = ReaderHtml.fromHtml("<p>Hi <img src=\"https://cdn.example.com/h.jpg\"></p>")
        assertTrue(spanned.toString().contains("Hi"))
    }
}

class ReaderScaleTest {
    @Test
    fun clampBodyScale() {
        assertEquals(0.85f, ReaderScale.clamp(0.1f), 0.001f)
        assertEquals(1.6f, ReaderScale.clamp(9f), 0.001f)
        assertEquals(1.0f, ReaderScale.DEFAULT, 0.001f)
    }
}

class ImagePolicyTest {
    @Test
    fun mapsFlags() {
        assertEquals(ImagePolicy.NEVER, ImagePolicy.fromFlags(showThumbs = false, mobileData = true))
        assertEquals(ImagePolicy.WIFI, ImagePolicy.fromFlags(showThumbs = true, mobileData = false))
        assertEquals(ImagePolicy.ALWAYS, ImagePolicy.fromFlags(showThumbs = true, mobileData = true))
        assertFalse(FeedSyncPolicy.allowImages(true, ImagePolicy.NEVER, false, false))
        assertTrue(FeedSyncPolicy.allowImages(true, ImagePolicy.ALWAYS, true, true))
        assertFalse(FeedSyncPolicy.allowImages(true, ImagePolicy.WIFI, true, true))
    }
}

class FeedNotifyTest {
    @Test
    fun newUnreadOnlyThisCycle() {
        val old = listOf(item("a", read = false))
        val now = listOf(item("a", read = false), item("b", read = false, source = "https://n.example/f"))
        val fresh = FeedNotify.newUnread(old, now, setOf("https://n.example/f"))
        assertEquals(listOf("b"), fresh.map { it.item.id })
    }

    private fun item(id: String, read: Boolean, source: String? = "https://n.example/f"): ArticleRecord {
        return ArticleRecord(
            item = FeedItem(id = id, feedTitle = "F", title = id, sourceUrl = source),
            read = read,
        )
    }
}

class FeedWorkTest {
    @Test
    fun loopDoesNotRefreshWhenWorkRegistered() {
        assertFalse(FeedWork.loopShouldRefresh(scanMinutes = 60, workOn = true))
        assertTrue(FeedWork.loopShouldRefresh(scanMinutes = 60, workOn = false))
        assertFalse(FeedWork.loopShouldRefresh(scanMinutes = 0, workOn = false))
    }
}

class FeedUnreadTest {
    @Test
    fun globalUnreadCount() {
        val rows = listOf(
            ArticleRecord(FeedItem("a", "F", "T"), read = false),
            ArticleRecord(FeedItem("b", "F", "T"), read = true),
        )
        assertEquals(1, FeedUnread.count(rows))
        assertEquals("99+", FeedUnread.label(100))
    }
}
