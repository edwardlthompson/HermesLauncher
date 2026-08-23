package dev.foss.goldenpath.feedback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackPreviewTest {
    @Test
    fun previewUsesComposeTextContract() {
        val text = FeedbackPreview.text("bug", "<script>alert(1)</script> ghp_abcdefghijklmnopqrstuvwxyz012345", null)
        assertFalse(text.contains("ghp_"))
        assertTrue(FeedbackPreview.canSubmit("steps", null))
        assertFalse(FeedbackPreview.canSubmit("  ", null))
    }
}
