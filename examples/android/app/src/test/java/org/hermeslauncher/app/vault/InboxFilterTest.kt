package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxFilterTest {
    private val items = listOf(
        sample("1", VaultItemType.MESSAGE, unread = true, pinned = false, title = "Ada", text = "hello"),
        sample("2", VaultItemType.OTHER, unread = false, pinned = true, title = "System", text = "update"),
        sample("3", VaultItemType.MESSAGE, unread = false, pinned = false, title = "Bob", text = "later", pkg = "com.other"),
        sample("4", VaultItemType.OTHER, unread = true, pinned = false, title = "Orphan", text = "x", pkg = "", postedAt = 9L),
    )

    @Test
    fun unreadKeepsOnlyUnread() {
        assertTrue(InboxQuery().newestFirst)
        assertEquals(InboxLayout.APP, InboxQuery().layout)
        val out = InboxFilter.apply(items, InboxQuery(chip = InboxChip.UNREAD))
        assertEquals(listOf("1", "4"), out.map { it.id })
    }

    @Test
    fun messagesChip() {
        val out = InboxFilter.apply(items, InboxQuery(chip = InboxChip.MESSAGES))
        assertEquals(listOf("1", "3"), out.map { it.id })
    }

    @Test
    fun searchMatchesTitleAndBody() {
        val out = InboxFilter.apply(items, InboxQuery(text = "UPD"))
        assertEquals(listOf("2"), out.map { it.id })
    }

    @Test
    fun perAppChip() {
        val out = InboxFilter.apply(items, InboxQuery(packageName = "com.other"))
        assertEquals(listOf("3"), out.map { it.id })
    }

    @Test
    fun emptyFilterYieldsEmpty() {
        val out = InboxFilter.apply(items, InboxQuery(chip = InboxChip.PINNED, text = "nope"))
        assertTrue(out.isEmpty())
    }

    @Test
    fun groupsNewestPackageFirst() {
        val mixed = listOf(
            sample("a", VaultItemType.OTHER, unread = true, pinned = false, title = "A", text = "", pkg = "com.old", postedAt = 1L),
            sample("b", VaultItemType.OTHER, unread = true, pinned = false, title = "B", text = "", pkg = "com.new", postedAt = 5L),
            sample("c", VaultItemType.OTHER, unread = true, pinned = false, title = "C", text = "", pkg = "com.old", postedAt = 3L),
        )
        val groups = InboxFilter.groups(mixed)
        assertEquals(listOf("com.new", "com.old"), groups.map { it.packageName })
        assertEquals(listOf("c", "a"), groups[1].items.map { it.id })
    }

    @Test
    fun groupsOldestFirstReversesPackageAndItems() {
        val mixed = listOf(
            sample("a", VaultItemType.OTHER, unread = true, pinned = false, title = "A", text = "", pkg = "com.old", postedAt = 1L),
            sample("b", VaultItemType.OTHER, unread = true, pinned = false, title = "B", text = "", pkg = "com.new", postedAt = 5L),
            sample("c", VaultItemType.OTHER, unread = true, pinned = false, title = "C", text = "", pkg = "com.old", postedAt = 3L),
        )
        val groups = InboxFilter.groups(mixed, newestFirst = false)
        assertEquals(listOf("com.old", "com.new"), groups.map { it.packageName })
        assertEquals(listOf("a", "c"), groups[0].items.map { it.id })
    }

    @Test
    fun groupsSingletonAndBlankPackage() {
        val out = InboxFilter.groups(items)
        assertEquals("", out.first().packageName)
        assertEquals(listOf("4"), out.first().items.map { it.id })
        assertTrue(out.any { it.packageName == "com.chat" && it.items.size == 2 })
    }

    @Test
    fun applyRunsBeforeGroups() {
        val filtered = InboxFilter.apply(items, InboxQuery(chip = InboxChip.UNREAD))
        val groups = InboxFilter.groups(filtered)
        assertEquals(setOf("", "com.chat"), groups.map { it.packageName }.toSet())
        assertTrue(groups.none { it.packageName == "com.other" })
    }

    @Test
    fun unreadCountSkipsArchivedAndCapsLabel() {
        val mixed = items + sample(
            "arch",
            VaultItemType.OTHER,
            unread = true,
            pinned = false,
            title = "Gone",
            text = "x",
            archived = true,
        )
        assertEquals(2, InboxFilter.unreadCount(mixed))
        assertEquals("99+", InboxFilter.unreadLabel(100))
        assertEquals("3", InboxFilter.unreadLabel(3))
        assertEquals(mapOf("com.chat" to 1), InboxFilter.unreadByPackage(mixed))
    }

    @Test
    fun categoryGroupsByKindNewestFirst() {
        val mixed = listOf(
            sample("a", VaultItemType.OTHER, unread = true, pinned = false, title = "A", text = "", pkg = "game.a", postedAt = 1L),
            sample("b", VaultItemType.OTHER, unread = true, pinned = false, title = "B", text = "", pkg = "chat.b", postedAt = 5L),
            sample("c", VaultItemType.OTHER, unread = true, pinned = false, title = "C", text = "", pkg = "game.c", postedAt = 3L),
        )
        val groups = InboxFilter.categoryGroups(mixed) { pkg ->
            if (pkg.startsWith("game")) "game" else "social"
        }
        assertEquals(listOf("social", "game"), groups.map { it.displayLabel })
        assertEquals(listOf("c", "a"), groups[1].items.map { it.id })
    }

    private fun sample(
        id: String,
        type: VaultItemType,
        unread: Boolean,
        pinned: Boolean,
        title: String,
        text: String,
        pkg: String = "com.chat",
        postedAt: Long = 1L,
        archived: Boolean = false,
    ): VaultItem {
        return VaultItem(
            id = id,
            sbnKey = id,
            packageName = pkg,
            channelId = null,
            postedAt = postedAt,
            type = type,
            priority = 0,
            title = title,
            text = text,
            extrasJson = null,
            conversationTitle = null,
            pinned = pinned,
            unread = unread,
            archived = archived,
            contentStored = true,
            imagesStored = false,
        )
    }
}
