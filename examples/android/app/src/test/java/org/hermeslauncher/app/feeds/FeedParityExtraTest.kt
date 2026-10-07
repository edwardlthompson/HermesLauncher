package org.hermeslauncher.app.feeds

import org.hermeslauncher.app.R
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

    @Test
    fun jsonNullIsNotLiteralText() {
        assertNull(FeedSubCodec.visibleCopy("null"))
        assertNull(FeedSubCodec.visibleCopy(" NULL "))
        assertEquals("timeout", FeedSubCodec.visibleCopy("timeout"))
        val raw = """[{"url":"https://a.example/f","title":"null","tag":"null","kind":"NEWS","notify":false,"prefetch":true,"lastError":null}]"""
        val back = FeedSubCodec.decode(raw)
        assertEquals("", back[0].title)
        assertEquals("", back[0].tag)
        assertNull(back[0].lastError)
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

    @Test
    fun kindForUsesSubscriptionThenAudioFallback() {
        val newsUrl = "https://n.example/news"
        val podUrl = "https://n.example/pod"
        val subs = listOf(
            FeedSub(url = newsUrl, kind = SubKind.NEWS),
            FeedSub(url = podUrl, kind = SubKind.PODCAST),
        )
        assertEquals(SubKind.NEWS, FeedNotify.kindFor(item("n", source = newsUrl), subs))
        assertEquals(SubKind.PODCAST, FeedNotify.kindFor(item("p", source = podUrl), subs))
        val audio = ArticleRecord(
            item = FeedItem(
                id = "a",
                feedTitle = "F",
                title = "ep",
                sourceUrl = "https://unknown.example/x",
                enclosureUrl = "https://cdn.example/ep.mp3",
                enclosureMime = "audio/mpeg",
            ),
        )
        assertEquals(SubKind.PODCAST, FeedNotify.kindFor(audio, emptyList()))
        assertEquals(SubKind.NEWS, FeedNotify.kindFor(item("plain", source = "https://unknown.example/y"), emptyList()))
    }

    @Test
    fun parseKindDefaultsToNews() {
        assertEquals(SubKind.NEWS, FeedNotify.parseKind(null))
        assertEquals(SubKind.NEWS, FeedNotify.parseKind(""))
        assertEquals(SubKind.NEWS, FeedNotify.parseKind("nope"))
        assertEquals(SubKind.PODCAST, FeedNotify.parseKind("PODCAST"))
        assertEquals(SubKind.NEWS, FeedNotify.parseKind("NEWS"))
    }

    @Test
    fun inboxLabelResMapsKind() {
        assertEquals(R.string.launcher_page_news, FeedNotify.inboxLabelRes(SubKind.NEWS))
        assertEquals(R.string.launcher_page_podcasts, FeedNotify.inboxLabelRes(SubKind.PODCAST))
    }

    @Test
    fun newsPendingDoesNotMatchPodcastKind() {
        val open = PendingFeedOpen("ep1", SubKind.PODCAST)
        assertFalse(open.kind == SubKind.NEWS)
        assertEquals(SubKind.PODCAST, open.kind)
    }

    @Test
    fun openRoutePageIndexDefaultsWithoutWorkspace() {
        assertEquals(0, FeedOpenRoute.pageIndex(null, SubKind.PODCAST))
        assertEquals(1, FeedOpenRoute.pageIndex(null, SubKind.NEWS))
        assertEquals(0, FeedOpenRoute.pageIndex(null, null))
    }

    private fun item(
        id: String,
        read: Boolean = false,
        source: String? = "https://n.example/f",
    ): ArticleRecord {
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
