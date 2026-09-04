package org.hermeslauncher.app.workspace

import org.hermeslauncher.app.widgets.WidgetGridSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopItemTest {
    @Test
    fun skipUnknownDropsBlankAndZeroFolder() {
        val kept = DesktopItems.skipUnknown(
            listOf(
                DesktopItem.Shortcut(1, "org.example", ".Main"),
                DesktopItem.Shortcut(0, "org.example", ".Main"),
                DesktopItem.Folder(2, folderId = 9),
                DesktopItem.Folder(3, folderId = 99),
                DesktopItem.Widget(4, appWidgetId = 8, cellX = 0, cellY = 0, spanX = 2, spanY = 2),
                DesktopItem.Widget(5, appWidgetId = 0, cellX = 0, cellY = 0, spanX = 1, spanY = 1),
            ),
            folderIds = setOf(9L),
        )
        assertEquals(3, kept.size)
        assertTrue(kept[0] is DesktopItem.Shortcut)
        assertTrue(kept[1] is DesktopItem.Folder)
        assertTrue(kept[2] is DesktopItem.Widget)
    }
}

class GridSpanTest {
    @Test
    fun fourByFiveWidgetKeepsOriginSpan() {
        val spec = WidgetGridSpec.DEFAULT
        assertEquals(2 to 2, GridSpan.mapSize(2, 2, spec, spec))
    }

    @Test
    fun emptyInputsMapToZero() {
        assertEquals(0, GridSpan.map(2, 0, 4))
        assertEquals(0, GridSpan.map(0, 4, 4))
    }
}
