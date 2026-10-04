package org.hermeslauncher.app.ui.inbox

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object InboxImageDecode {
    const val DEFAULT_TARGET_WIDTH: Int = 1080

    fun sampleSize(width: Int, height: Int, targetWidth: Int = DEFAULT_TARGET_WIDTH): Int {
        if (width <= 0 || height <= 0 || targetWidth <= 0) {
            return 1
        }
        var sample = 1
        while (width / (sample * 2) >= targetWidth && height / (sample * 2) >= 1) {
            sample *= 2
        }
        return sample.coerceAtLeast(1)
    }

    fun decodeFile(path: String, targetWidth: Int = DEFAULT_TARGET_WIDTH): Bitmap? {
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, targetWidth)
            }
            BitmapFactory.decodeFile(path, opts)
        }.getOrNull()
    }

    suspend fun decodeAsync(file: File?, targetWidth: Int = DEFAULT_TARGET_WIDTH): Bitmap? {
        val path = file?.takeIf { it.isFile }?.absolutePath ?: return null
        return withContext(Dispatchers.IO) { decodeFile(path, targetWidth) }
    }

    fun preferDisplayFile(original: File?, thumb: File?): File? {
        return thumb?.takeIf { it.isFile } ?: original?.takeIf { it.isFile }
    }
}
