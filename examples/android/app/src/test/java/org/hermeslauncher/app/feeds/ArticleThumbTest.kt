package org.hermeslauncher.app.feeds

import android.graphics.Bitmap
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ArticleThumbTest {
    @Test
    fun sampleForSkipsWhenAlreadySmall() {
        org.junit.Assert.assertEquals(1, ArticleThumb.sampleFor(100, 80, 720))
        org.junit.Assert.assertEquals(1, ArticleThumb.sampleFor(0, 800, 720))
    }

    @Test
    fun sampleForHalvesUntilBothEdgesFit() {
        org.junit.Assert.assertEquals(2, ArticleThumb.sampleFor(2000, 2000, 720))
        org.junit.Assert.assertEquals(4, ArticleThumb.sampleFor(4000, 4000, 720))
    }

    @Test
    fun purgeLegacyThumbsDeletesFolder() {
        val dir = File.createTempFile("hermes", "dir").apply { delete(); mkdirs() }
        val thumbs = File(dir, "feed-thumbs")
        thumbs.mkdirs()
        File(thumbs, "1.jpg").writeText("x")
        ArticleThumb.purgeLegacyThumbs(dir)
        assertFalse(thumbs.exists())
    }

    @Test
    fun previewReadsOriginalWithoutThumbsDir() {
        val dir = File.createTempFile("hermes", "orig").apply { delete(); mkdirs() }
        val bmp = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
        val orig = ArticleThumb.originalFile(dir, "story")
        orig.parentFile?.mkdirs()
        orig.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val out = ArticleThumb.preview(dir, "story", null, download = false)
        assertNotNull(out)
        assertFalse(File(dir, "feed-thumbs").exists())
        assertTrue(orig.isFile)
    }
}
