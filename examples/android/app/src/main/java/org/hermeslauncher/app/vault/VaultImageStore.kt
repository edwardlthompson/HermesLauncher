package org.hermeslauncher.app.vault

import android.util.Log
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
            Log.i(TAG, "wrote image $rel bytes=${bytes.size}")
            rel
        }.onFailure { err ->
            Log.w(TAG, "write image failed item=$itemId", err)
        }.getOrNull()
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
}
