package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultFeedsTest {
    @Test
    fun seedZeroAddsAndroidAuthorityAndFdroid() {
        val urls = DefaultFeeds.urlsForSeed(0)
        assertEquals(listOf(DefaultFeeds.ANDROID_AUTHORITY, DefaultFeeds.FDROID), urls)
        assertTrue(FeedFetcher.isHttpUrl(DefaultFeeds.ANDROID_AUTHORITY))
        assertTrue(FeedFetcher.isHttpUrl(DefaultFeeds.FDROID))
    }

    @Test
    fun seedOneAddsOnlyFdroid() {
        assertEquals(listOf(DefaultFeeds.FDROID), DefaultFeeds.urlsForSeed(1))
    }

    @Test
    fun laterSeedAddsNothing() {
        assertTrue(DefaultFeeds.urlsForSeed(2).isEmpty())
        assertTrue(DefaultFeeds.urlsForSeed(DefaultFeeds.SEED).isEmpty())
    }
}
