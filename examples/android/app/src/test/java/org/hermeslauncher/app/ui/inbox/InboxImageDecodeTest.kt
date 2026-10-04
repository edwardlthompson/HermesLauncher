package org.hermeslauncher.app.ui.inbox

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxImageDecodeTest {
    @Test
    fun sampleSizeDownscalesLargeDims() {
        val sample = InboxImageDecode.sampleSize(4000, 3000, targetWidth = 1000)
        assertTrue(sample >= 2)
        assertEquals(4, sample)
    }

    @Test
    fun sampleSizeStaysOneForSmall() {
        assertEquals(1, InboxImageDecode.sampleSize(800, 600, targetWidth = 1080))
    }
}
