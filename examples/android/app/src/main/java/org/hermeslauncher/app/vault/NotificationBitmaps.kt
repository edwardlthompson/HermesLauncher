package org.hermeslauncher.app.vault

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayOutputStream

data class NotificationJpeg(
    val bytes: ByteArray = byteArrayOf(),
    val width: Int = 0,
    val height: Int = 0,
    val fromLargeIcon: Boolean = false,
)

object NotificationBitmaps {
    fun jpeg(context: Context, notification: Notification): NotificationJpeg {
        val extras = notification.extras
        bitmapOf(extras, Notification.EXTRA_PICTURE)?.let { return encode(it, false) }
        if (Build.VERSION.SDK_INT >= 31) {
            bitmapOf(extras, Notification.EXTRA_PICTURE_ICON)?.let { return encode(it, false) }
        }
        bitmapOf(extras, Notification.EXTRA_LARGE_ICON)?.let { return encode(it, true) }
        drawableBitmap(notification.getLargeIcon()?.loadDrawable(context))
            ?.let { return encode(it, true) }
        return messageUriJpeg(context, notification)
    }

    fun messageUriBytes(context: Context, notification: Notification): ByteArray {
        return messageUriJpeg(context, notification).bytes
    }

    private fun messageUriJpeg(context: Context, notification: Notification): NotificationJpeg {
        val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
            notification,
        )
        val uri = style?.messages.orEmpty().mapNotNull { it.dataUri }.lastOrNull()
            ?: return NotificationJpeg()
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                if (bytes.size > ImageLimits.ORIGINAL_MAX_BYTES) {
                    NotificationJpeg()
                } else {
                    NotificationJpeg(bytes = bytes)
                }
            } ?: NotificationJpeg()
        }.onFailure { err ->
            Log.w(
                VaultImageStore.TAG,
                "photo uri denied $uri (notification access, not storage)",
                err,
            )
        }.getOrDefault(NotificationJpeg())
    }

    private fun bitmapOf(extras: Bundle, key: String): Bitmap? {
        return extras.get(key) as? Bitmap
    }

    private fun drawableBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        return runCatching { drawable.toBitmap() }.getOrNull()
            ?: runCatching {
                val w = drawable.intrinsicWidth.coerceAtLeast(1)
                val h = drawable.intrinsicHeight.coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, w, h)
                drawable.draw(canvas)
                bitmap
            }.getOrNull()
    }

    private fun encode(bitmap: Bitmap, fromLargeIcon: Boolean): NotificationJpeg {
        val out = ByteArrayOutputStream()
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)) {
            return NotificationJpeg()
        }
        val bytes = out.toByteArray()
        if (bytes.size > ImageLimits.ORIGINAL_MAX_BYTES) {
            return NotificationJpeg()
        }
        return NotificationJpeg(bytes, bitmap.width, bitmap.height, fromLargeIcon)
    }
}
