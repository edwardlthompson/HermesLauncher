package org.hermeslauncher.app.vault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class VaultPreviewTest {
    @Test
    fun roundTripKeepsCaptionAndImage() {
        val preview = VaultPreview(
            subText = "Ada",
            bigText = "hello with a photo",
            infoText = "now",
            imageRef = "vault/images/a/preview.jpg",
        )
        val parsed = VaultPreview.parse(preview.encode())
        assertEquals("Ada", parsed.caption())
        assertEquals("hello with a photo", parsed.body("fallback"))
        assertEquals("vault/images/a/preview.jpg", parsed.imageRef)
    }

    @Test
    fun blankFallsBackToText() {
        val parsed = VaultPreview.parse(null)
        assertEquals("plain", parsed.body("plain"))
        assertEquals("", parsed.caption())
        assertNull(parsed.imageRef)
    }

    @Test
    fun withImagePatchesRef() {
        val encoded = VaultPreview(bigText = "x").withImage("vault/images/b/preview.jpg").encode()
        assertEquals("vault/images/b/preview.jpg", VaultPreview.parse(encoded).imageRef)
    }
}
