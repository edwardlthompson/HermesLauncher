package org.hermeslauncher.app.workspace

import org.hermeslauncher.app.icons.LaunchableApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderLidTest {
    private val mail = LaunchableApp("com.mail", "Inbox", "Mail")
    private val maps = LaunchableApp("com.maps", "Map", "Maps")
    private val blank = LaunchableApp("", "X", "Gone")
    private val noAct = LaunchableApp("com.x", "", "X")

    @Test
    fun emptyFolderPreviewIsEmpty() {
        assertTrue(FolderLid.preview(emptyList()).isEmpty())
        assertEquals(0, FolderLid.badge(mapOf("com.mail" to 3), emptyList()))
    }

    @Test
    fun previewDropsMissingActivityAndCaps() {
        val apps = listOf(mail, blank, noAct, maps, LaunchableApp("com.z", "Z", "Z"))
        assertEquals(listOf(mail, maps), FolderLid.preview(apps, limit = 2))
    }

    @Test
    fun badgeSumsUnreadForContentsOnly() {
        val unread = mapOf("com.mail" to 2, "com.maps" to 1, "com.other" to 9)
        assertEquals(3, FolderLid.badge(unread, listOf(mail, maps)))
        assertEquals(0, FolderLid.badge(unread, listOf(LaunchableApp("com.none", "N", "None"))))
    }
}
