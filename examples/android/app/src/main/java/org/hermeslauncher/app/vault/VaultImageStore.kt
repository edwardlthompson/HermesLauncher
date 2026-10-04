package org.hermeslauncher.app.vault

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File

object VaultImageStore {
    const val TAG: String = "HermesVault"

    fun write(filesDir: File, itemId: String, bytes: ByteArray): String? {
        if (bytes.isEmpty() || bytes.size > ImageLimits.ORIGINAL_MAX_BYTES) {
            Log.i(TAG, "skip image item=$itemId bytes=${bytes.size}")
            return null
        }
        val safe = itemId.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val rel = "${ImageLimits.RELATIVE_DIR}/$safe/preview.jpg"
        val dest = File(filesDir, rel)
        return runCatching {
            dest.parentFile?.mkdirs()
            dest.writeBytes(bytes)
            writeThumb(dest.parentFile!!, bytes)
            Log.i(TAG, "wrote image $rel bytes=${bytes.size}")
            rel
        }.onFailure { err ->
            Log.w(TAG, "write image failed item=$itemId", err)
        }.getOrNull()
    }

    fun writeThumb(dir: File, bytes: ByteArray) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            var sample = 1
            val target = 720
            while (bounds.outWidth / (sample * 2) >= target && bounds.outHeight / (sample * 2) >= 1) {
                sample *= 2
            }
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: return
            val out = ByteArrayOutputStream()
            var quality = 85
            do {
                out.reset()
                bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
                quality -= 10
            } while (out.size() > ImageLimits.THUMB_MAX_BYTES && quality >= 40)
            val thumb = File(dir, "thumb.jpg")
            if (out.size() <= ImageLimits.THUMB_MAX_BYTES) {
                thumb.writeBytes(out.toByteArray())
            }
            bmp.recycle()
        }
    }

    fun attach(
        filesDir: File,
        item: VaultItem,
        posted: PostedNotification,
        action: PersistAction,
    ): VaultItem {
        if (action != PersistAction.PERSIST_TEXT_AND_IMAGES) {
            return item
        }
        val ref = write(filesDir, item.id, posted.imageBytes) ?: return item.copy(imagesStored = false)
        return item.copy(
            extrasJson = VaultPreview.parse(item.extrasJson)
                .withImage(ref, posted.imageWidth, posted.imageHeight, posted.imageIsLargeIcon)
                .encode(),
        )
    }

    fun delete(filesDir: File, itemId: String) {
        val safe = itemId.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val dir = File(filesDir, "${ImageLimits.RELATIVE_DIR}/$safe")
        runCatching { dir.deleteRecursively() }
    }

    fun file(filesDir: File, rel: String?): File? {
        if (rel.isNullOrBlank()) {
            return null
        }
        val dest = File(filesDir, rel)
        return dest.takeIf { it.isFile }
    }

    fun displayFile(filesDir: File, rel: String?): File? {
        val original = file(filesDir, rel) ?: return null
        val thumb = File(original.parentFile, "thumb.jpg")
        return if (thumb.isFile) thumb else original
    }
}
