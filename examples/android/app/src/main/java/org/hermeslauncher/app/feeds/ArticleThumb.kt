package org.hermeslauncher.app.feeds

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.util.concurrent.Semaphore

object ArticleThumb {
    const val PREVIEW_EDGE: Int = 256
    const val DISPLAY_EDGE: Int = 4096
    const val MAX_ORIGINAL_BYTES: Int = 8_000_000
    private val gate = Semaphore(3)

    fun thumbFile(dir: File, id: String): File {
        return File(File(dir, "feed-thumbs"), "${id.hashCode() and 0x7fffffff}.jpg")
    }

    fun originalFile(dir: File, id: String, url: String? = null): File {
        val key = (url?.takeIf { it.isNotBlank() } ?: id).hashCode() and 0x7fffffff
        return File(File(dir, "feed-article"), "$key.bin")
    }

    fun purgeLegacyThumbs(dir: File) {
        val thumbs = File(dir, "feed-thumbs")
        if (!thumbs.isDirectory) {
            return
        }
        thumbs.walkBottomUp().forEach { runCatching { it.delete() } }
    }

    fun sampleFor(width: Int, height: Int, maxEdge: Int = DISPLAY_EDGE): Int {
        if (width <= 0 || height <= 0 || maxEdge <= 0) {
            return 1
        }
        var sample = 1
        val halfW = width / 2
        val halfH = height / 2
        while (halfW / sample >= maxEdge && halfH / sample >= maxEdge) {
            sample *= 2
        }
        return sample
    }

    fun preview(dir: File, id: String, url: String?, download: Boolean = true): Bitmap? {
        val orig = ensureOriginal(dir, id, url, download) ?: return null
        return decodePath(orig.absolutePath, PREVIEW_EDGE)
    }

    fun article(dir: File, id: String, url: String?, download: Boolean = true): Bitmap? {
        val orig = ensureOriginal(dir, id, url, download) ?: return null
        return decodePath(orig.absolutePath, DISPLAY_EDGE)
    }

    fun ensureOriginal(dir: File, id: String, url: String?, download: Boolean = true): File? {
        val dest = originalFile(dir, id, url)
        if (dest.isFile && dest.length() >= 400L) {
            return dest
        }
        if (!download) {
            return null
        }
        if (url.isNullOrBlank() || !FeedFetcher.isHttpUrl(url) || ArticleImages.looksTinyUrl(url)) {
            return null
        }
        gate.acquire()
        val bytes = try {
            runCatching { FeedFetcher.fetchBytes(url, MAX_ORIGINAL_BYTES) }.getOrNull()
        } finally {
            gate.release()
        } ?: return null
        if (bytes.size < 400) {
            return null
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (ArticleImages.isTiny(bounds.outWidth, bounds.outHeight)) {
            return null
        }
        runCatching {
            dest.parentFile?.mkdirs()
            dest.writeBytes(bytes)
        }.getOrElse { return null }
        return dest.takeIf { it.isFile }
    }

    private fun decodePath(path: String, maxEdge: Int): Bitmap? {
        val file = File(path)
        if (!file.isFile) {
            return null
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (ArticleImages.isTiny(bounds.outWidth, bounds.outHeight)) {
            return null
        }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleFor(bounds.outWidth, bounds.outHeight, maxEdge)
        }
        return BitmapFactory.decodeFile(path, opts)
    }
}
