package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawerPolicyTest {
    private val mail = LaunchableApp("com.mail", "Inbox", "Mail")
    private val maps = LaunchableApp("com.maps", "Map", "Maps")

    @Test
    fun emptyHiddenShowsAll() {
        assertEquals(listOf(mail, maps), DrawerPolicy.visible(listOf(mail, maps), emptySet()))
    }

    @Test
    fun hidePackageFiltersList() {
        val hidden = DrawerPolicy.hide(emptySet(), "com.mail")
        assertEquals(listOf(maps), DrawerPolicy.visible(listOf(mail, maps), hidden))
    }

    @Test
    fun blankHideIsIgnored() {
        assertTrue(DrawerPolicy.hide(emptySet(), "  ").isEmpty())
        assertTrue(DrawerPolicy.hide(emptySet(), "").isEmpty())
    }

    @Test
    fun columnsClamp() {
        assertEquals(4, DrawerPolicy.columns(1))
        assertEquals(6, DrawerPolicy.columns(99))
        assertEquals(5, DrawerPolicy.columns(5))
    }

    @Test
    fun chunkSizeIsOneInListMode() {
        assertEquals(1, DrawerPolicy.chunkSize(listMode = true, columns = 5))
        assertEquals(4, DrawerPolicy.chunkSize(listMode = false, columns = 4))
    }

    @Test
    fun codecRoundTripDropsBlanks() {
        val encoded = DrawerCodec.encodeHidden(setOf(" b ", "", "a"))
        assertEquals("a\nb", encoded)
        assertEquals(setOf("a", "b"), DrawerCodec.decodeHidden(encoded))
        assertTrue(DrawerCodec.decodeHidden(null).isEmpty())
    }

    @Test
    fun picksNeedQueryAndSkipExcluded() {
        assertTrue(DrawerPolicy.picks(listOf(mail), "", emptySet()).isEmpty())
        assertEquals(listOf(mail), DrawerPolicy.picks(listOf(mail, maps), "mail", emptySet()))
        assertTrue(DrawerPolicy.picks(listOf(mail), "mail", setOf("com.mail")).isEmpty())
    }
}
