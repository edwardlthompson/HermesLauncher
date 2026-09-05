package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxFilterTest {
    private val items = listOf(
        filterSample("1", VaultItemType.MESSAGE, unread = true, pinned = false, title = "Ada", text = "hello"),
        filterSample("2", VaultItemType.OTHER, unread = false, pinned = true, title = "System", text = "update"),
        filterSample("3", VaultItemType.MESSAGE, unread = false, pinned = false, title = "Bob", text = "later", pkg = "com.other"),
        filterSample("4", VaultItemType.OTHER, unread = true, pinned = false, title = "Orphan", text = "x", pkg = "", postedAt = 9L),
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
    fun applyHidesBlankSubject() {
        val mixed = items + filterSample(
            id = "blank",
            type = VaultItemType.OTHER,
            unread = true,
            pinned = false,
            title = "  ",
            text = "group summary",
        )
        val kept = InboxFilter.apply(mixed, InboxQuery())
        assertTrue(kept.none { it.id == "blank" })
        val named = filterSample(
            id = "named",
            type = VaultItemType.MESSAGE,
            unread = true,
            pinned = false,
            title = "",
            text = "hi",
            conversationTitle = "Ada",
        )
        assertEquals(listOf("named"), InboxFilter.apply(listOf(named), InboxQuery()).map { it.id })
        assertEquals(2, InboxFilter.unreadCount(mixed))
    }

    @Test
    fun unreadCountSkipsArchivedAndCapsLabel() {
        val mixed = items + filterSample(
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
}
