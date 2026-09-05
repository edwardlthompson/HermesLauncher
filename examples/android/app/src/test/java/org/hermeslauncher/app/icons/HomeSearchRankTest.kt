package org.hermeslauncher.app.icons

import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.vault.VaultItem
import org.hermeslauncher.app.vault.VaultItemType
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeSearchRankTest {
    private val mail = LaunchableApp("com.mail", "com.mail.Main", "Mail")
    private val chat = LaunchableApp("com.chat", "com.chat.Main", "Chat")
    private val maps = LaunchableApp("com.maps", "com.maps.Main", "Maps")

    @Test
    fun emptyQueryUsesPredictedAndRecentUnread() {
        val inbox = listOf(
            vault("old", "com.mail", "Old", unread = true, postedAt = 1L),
            vault("new", "com.chat", "New", unread = true, postedAt = 9L),
            vault("read", "com.maps", "Read", unread = false, postedAt = 20L),
        )
        val out = HomeSearchRank.query(
            needle = "",
            apps = listOf(mail, chat, maps),
            usage = emptyList(),
            inbox = inbox,
            feeds = emptyList(),
            predicted = listOf(chat),
        )
        assertEquals(listOf("Chat"), out.apps.map { it.label })
        assertEquals(listOf("new", "old"), out.inbox.map { it.id })
    }

    @Test
    fun ranksMatchingAppsByUsageThenLabel() {
        val usage = listOf(
            UsageRow("com.maps", lastTimeUsed = 5L, totalTimeInForeground = 1L),
            UsageRow("com.mail", lastTimeUsed = 50L, totalTimeInForeground = 1L),
        )
        val out = HomeSearchRank.query(
            needle = "a",
            apps = listOf(mail, chat, maps),
            usage = usage,
            inbox = listOf(vault("hit", "com.chat", "Later chat", unread = true, postedAt = 3L)),
            feeds = listOf(FeedItem("f1", "News", "Alpha show", publishedAt = 2L)),
            predicted = emptyList(),
        )
        assertEquals(listOf("Mail", "Maps", "Chat"), out.apps.map { it.label })
        assertEquals(listOf("hit"), out.inbox.map { it.id })
        assertEquals(listOf("f1"), out.feeds.map { it.id })
    }

    @Test
    fun appRowCapDoesNotShrinkInboxOrFeeds() {
        val out = HomeSearchRank.query(
            needle = "a",
            apps = listOf(mail, chat, maps),
            usage = emptyList(),
            inbox = listOf(
                vault("i1", "com.chat", "alpha inbox", unread = true, postedAt = 3L),
                vault("i2", "com.mail", "later alpha", unread = true, postedAt = 2L),
            ),
            feeds = listOf(
                FeedItem("f1", "News", "Alpha show", publishedAt = 2L),
                FeedItem("f2", "News", "Alpha two", publishedAt = 1L),
            ),
            predicted = emptyList(),
            appCap = 1,
        )
        assertEquals(listOf("Chat"), out.apps.map { it.label })
        assertEquals(listOf("i1", "i2"), out.inbox.map { it.id })
        assertEquals(listOf("f1", "f2"), out.feeds.map { it.id })
    }

    @Test
    fun contactsEmptyWhenDenied() {
        assertEquals(emptyList<String>(), HomeSearchRank.contacts(false, listOf("Ada")))
        assertEquals(listOf("Ada"), HomeSearchRank.contacts(true, listOf("Ada", "  ")))
        val denied = HomeSearchRank.query(
            needle = "a",
            apps = listOf(mail),
            usage = emptyList(),
            inbox = emptyList(),
            feeds = emptyList(),
            predicted = emptyList(),
            contactsGranted = false,
            contactHits = listOf("Ada"),
        )
        assertEquals(emptyList<String>(), denied.contacts)
    }

    @Test
    fun unreadEmptyQuerySkipsBlankSubject() {
        val inbox = listOf(
            vault("kept", "com.mail", "Hello", unread = true, postedAt = 2L),
            vault("blank", "com.mail", "  ", unread = true, postedAt = 9L),
        )
        val out = HomeSearchRank.query(
            needle = "",
            apps = listOf(mail),
            usage = emptyList(),
            inbox = inbox,
            feeds = emptyList(),
            predicted = emptyList(),
        )
        assertEquals(listOf("kept"), out.inbox.map { it.id })
    }

    private fun vault(
        id: String,
        pkg: String,
        title: String,
        unread: Boolean,
        postedAt: Long,
    ): VaultItem {
        return VaultItem(
            id = id,
            sbnKey = id,
            packageName = pkg,
            channelId = null,
            postedAt = postedAt,
            type = VaultItemType.OTHER,
            priority = 0,
            title = title,
            text = title,
            extrasJson = null,
            conversationTitle = null,
            pinned = false,
            unread = unread,
            archived = false,
            contentStored = true,
            imagesStored = false,
        )
    }
}
