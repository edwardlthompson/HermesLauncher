package org.hermeslauncher.app.widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetPreviewTest {
    @Test
    fun emptyPreviewImageUsesIcon() {
        assertEquals(WidgetPreviewKind.ICON, WidgetPreview.kind(0, hasIcon = true))
    }

    @Test
    fun previewResourceWins() {
        assertEquals(WidgetPreviewKind.IMAGE, WidgetPreview.kind(7, hasIcon = true))
    }

    @Test
    fun missingPreviewAndIconIsNone() {
        assertEquals(WidgetPreviewKind.NONE, WidgetPreview.kind(0, hasIcon = false))
    }

    @Test
    fun rasterNullDrawable() {
        assertNull(WidgetPreview.raster(null, 64))
    }
}
