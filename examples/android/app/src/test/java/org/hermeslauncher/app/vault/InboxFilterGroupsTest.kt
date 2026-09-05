package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxFilterGroupsTest {
    private val items = listOf(
        filterSample("1", VaultItemType.MESSAGE, unread = true, pinned = false, title = "Ada", text = "hello"),
        filterSample("2", VaultItemType.OTHER, unread = false, pinned = true, title = "System", text = "update"),
        filterSample("3", VaultItemType.MESSAGE, unread = false, pinned = false, title = "Bob", text = "later", pkg = "com.other"),
        filterSample("4", VaultItemType.OTHER, unread = true, pinned = false, title = "Orphan", text = "x", pkg = "", postedAt = 9L),
    )

    @Test
    fun groupsNewestPackageFirst() {
        val mixed = listOf(
            filterSample("a", VaultItemType.OTHER, unread = true, pinned = false, title = "A", text = "", pkg = "com.old", postedAt = 1L),
            filterSample("b", VaultItemType.OTHER, unread = true, pinned = false, title = "B", text = "", pkg = "com.new", postedAt = 5L),
            filterSample("c", VaultItemType.OTHER, unread = true, pinned = false, title = "C", text = "", pkg = "com.old", postedAt = 3L),
        )
        val groups = InboxFilter.groups(mixed)
        assertEquals(listOf("com.new", "com.old"), groups.map { it.packageName })
        assertEquals(listOf("c", "a"), groups[1].items.map { it.id })
    }

    @Test
    fun groupsOldestFirstReversesPackageAndItems() {
        val mixed = listOf(
            filterSample("a", VaultItemType.OTHER, unread = true, pinned = false, title = "A", text = "", pkg = "com.old", postedAt = 1L),
            filterSample("b", VaultItemType.OTHER, unread = true, pinned = false, title = "B", text = "", pkg = "com.new", postedAt = 5L),
            filterSample("c", VaultItemType.OTHER, unread = true, pinned = false, title = "C", text = "", pkg = "com.old", postedAt = 3L),
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
    fun categoryGroupsByKindNewestFirst() {
        val mixed = listOf(
            filterSample("a", VaultItemType.OTHER, unread = true, pinned = false, title = "A", text = "", pkg = "game.a", postedAt = 1L),
            filterSample("b", VaultItemType.OTHER, unread = true, pinned = false, title = "B", text = "", pkg = "chat.b", postedAt = 5L),
            filterSample("c", VaultItemType.OTHER, unread = true, pinned = false, title = "C", text = "", pkg = "game.c", postedAt = 3L),
        )
        val groups = InboxFilter.categoryGroups(mixed) { pkg ->
            if (pkg.startsWith("game")) "game" else "social"
        }
        assertEquals(listOf("social", "game"), groups.map { it.displayLabel })
        assertEquals(listOf("c", "a"), groups[1].items.map { it.id })
    }
}
