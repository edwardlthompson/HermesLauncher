package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxDisplayTest {
    @Test
    fun truncateLeavesShortText() {
        assertEquals("hello", InboxDisplay.truncate("hello", 120))
        assertEquals("hello", InboxDisplay.truncate("hello", 0))
    }

    @Test
    fun truncateCapsLongText() {
        assertEquals("abcd…", InboxDisplay.truncate("abcdefgh", 4))
    }

    @Test
    fun keepImageSkipsAvatarsWhenEnabled() {
        assertTrue(InboxDisplay.keepImage(800, 600, fromLargeIcon = false, hideSmall = false))
        assertFalse(InboxDisplay.keepImage(96, 96, fromLargeIcon = true, hideSmall = true))
        assertFalse(InboxDisplay.keepImage(120, 120, fromLargeIcon = false, hideSmall = true))
        assertTrue(InboxDisplay.keepImage(800, 600, fromLargeIcon = false, hideSmall = true))
    }

    @Test
    fun clampChars() {
        assertEquals(40, InboxDisplay.clampChars(1))
        assertEquals(240, InboxDisplay.clampChars(999))
        assertEquals(120, InboxDisplay.clampChars(120))
        assertEquals(0, InboxDisplay.clampChars(0))
        assertEquals(0, InboxDisplay.clampChars(-8))
    }

    @Test
    fun bodyLinesGrowWithCap() {
        assertEquals(Int.MAX_VALUE, InboxDisplay.bodyLines(0))
        assertEquals(3, InboxDisplay.bodyLines(80))
        assertEquals(6, InboxDisplay.bodyLines(240))
        assertTrue(InboxDisplay.bodyLines(240) > InboxDisplay.bodyLines(80))
    }

    @Test
    fun titleBoldOnlyWhenUnread() {
        assertTrue(InboxDisplay.titleBold(true))
        assertFalse(InboxDisplay.titleBold(false))
    }

    @Test
    fun showPinIconOnlyWhenPinned() {
        assertTrue(InboxDisplay.showPinIcon(true))
        assertFalse(InboxDisplay.showPinIcon(false))
    }

    @Test
    fun groupBoldWhenAnyUnread() {
        val unread = filterSample("u", VaultItemType.MESSAGE, unread = true, pinned = false, title = "U", text = "a")
        val read = filterSample("r", VaultItemType.OTHER, unread = false, pinned = false, title = "R", text = "b")
        assertTrue(InboxDisplay.groupBold(listOf(read, unread)))
        assertFalse(InboxDisplay.groupBold(listOf(read)))
        assertFalse(InboxDisplay.groupBold(emptyList()))
    }
}
