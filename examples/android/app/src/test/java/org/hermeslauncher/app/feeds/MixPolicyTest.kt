package org.hermeslauncher.app.feeds

import org.hermeslauncher.app.vault.VaultItem
import org.hermeslauncher.app.vault.VaultItemType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MixPolicyTest {
    @Test
    fun newestFirstAcrossVaultAndFeeds() {
        val vault = VaultItem(
            id = "n1",
            sbnKey = "n1",
            packageName = "com.example.chat",
            channelId = null,
            postedAt = 100,
            type = VaultItemType.MESSAGE,
            priority = 0,
            title = "chat",
            text = "hi",
            extrasJson = null,
            conversationTitle = null,
            contentStored = true,
            imagesStored = false,
        )
        val feed = FeedItem(id = "f1", feedTitle = "Show", title = "Ep", publishedAt = 200)
        val mixed = MixPolicy.merge(listOf(vault), listOf(feed))
        assertEquals(2, mixed.size)
        assertTrue(mixed[0] is MixedEntry.Feed)
        assertTrue(mixed[1] is MixedEntry.Vault)
    }

    @Test
    fun oldestFirstPutsVaultAheadOfNewerFeed() {
        val vault = VaultItem(
            id = "n1",
            sbnKey = "n1",
            packageName = "com.example.chat",
            channelId = null,
            postedAt = 100,
            type = VaultItemType.MESSAGE,
            priority = 0,
            title = "chat",
            text = "hi",
            extrasJson = null,
            conversationTitle = null,
            contentStored = true,
            imagesStored = false,
        )
        val feed = FeedItem(id = "f1", feedTitle = "Show", title = "Ep", publishedAt = 200)
        val mixed = MixPolicy.merge(listOf(vault), listOf(feed), newestFirst = false)
        assertTrue(mixed[0] is MixedEntry.Vault)
        assertTrue(mixed[1] is MixedEntry.Feed)
    }

    @Test
    fun withinWindowKeepsLastThirtyDays() {
        val now = 1_000_000_000_000L
        val fresh = FeedItem(id = "f1", feedTitle = "S", title = "New", publishedAt = now - 1_000)
        val old = FeedItem(id = "f2", feedTitle = "S", title = "Old", publishedAt = now - MixPolicy.WINDOW_MS - 1)
        val unknown = FeedItem(id = "f3", feedTitle = "S", title = "?", publishedAt = 0)
        val kept = MixPolicy.withinWindow(listOf(fresh, old, unknown), now)
        assertEquals(listOf("f1", "f3"), kept.map { it.id })
    }
}
