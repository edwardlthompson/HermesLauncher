package org.hermeslauncher.app.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopCodecTest {
    @Test
    fun roundTripKeepsPageAndCell() {
        val item = DesktopItem.Shortcut(7, "org.example", ".Main", "Example", 2, 3)
        val original = DesktopLayout().withShortcut(1, item)
        val restored = DesktopCodec.decode(DesktopCodec.encode(original))
        assertTrue(DesktopCodec.encode(original).startsWith("v1|"))
        assertEquals(item, restored.page(1).single())
        assertTrue(restored.page(2).isEmpty())
    }

    @Test
    fun corruptYieldsEmpty() {
        assertTrue(DesktopCodec.decode("nope").byPage.isEmpty())
    }
}
