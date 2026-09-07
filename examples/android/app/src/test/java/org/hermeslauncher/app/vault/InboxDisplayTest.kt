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
        assertEquals(400, InboxDisplay.clampChars(999))
        assertEquals(120, InboxDisplay.clampChars(120))
    }
}
