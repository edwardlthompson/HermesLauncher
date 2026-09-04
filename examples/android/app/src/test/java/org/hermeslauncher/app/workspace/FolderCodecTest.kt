package org.hermeslauncher.app.workspace

import org.junit.Assert.assertEquals
import org.junit.Test

class FolderCodecTest {
    @Test
    fun previewRoundTripAndBlankDefault() {
        assertEquals("GRID", FolderCodec.encodePreview(FolderPreviewKind.GRID))
        assertEquals(FolderPreviewKind.FAN, FolderCodec.decodePreview("FAN"))
        assertEquals(FolderPreviewKind.STACK, FolderCodec.decodePreview(null))
        assertEquals(FolderPreviewKind.STACK, FolderCodec.decodePreview("nope"))
    }
}
