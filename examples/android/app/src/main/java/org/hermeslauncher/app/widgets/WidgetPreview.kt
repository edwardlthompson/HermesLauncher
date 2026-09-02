package org.hermeslauncher.app.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.max

enum class WidgetPreviewKind {
    IMAGE,
    ICON,
    NONE,
}

object WidgetPreview {
    const val MAX_PX: Int = 512

    fun kind(previewImage: Int, hasIcon: Boolean): WidgetPreviewKind {
        return when {
            previewImage != 0 -> WidgetPreviewKind.IMAGE
            hasIcon -> WidgetPreviewKind.ICON
            else -> WidgetPreviewKind.NONE
        }
    }

    fun bitmap(context: Context, provider: ComponentName, maxPx: Int = MAX_PX): Bitmap? {
        val info = AppWidgetManager.getInstance(context).installedProviders
            .firstOrNull { it.provider == provider }
        val preview = info?.let { runCatching { it.loadPreviewImage(context, 0) }.getOrNull() }
        val icon = runCatching {
            context.packageManager.getApplicationIcon(provider.packageName)
        }.getOrNull()
        return raster(preview ?: icon, maxPx)
    }

    internal fun raster(drawable: Drawable?, maxPx: Int): Bitmap? {
        if (drawable == null || maxPx <= 0) return null
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return scale(drawable.bitmap, maxPx)
        }
        val width = max(drawable.intrinsicWidth, 1)
        val height = max(drawable.intrinsicHeight, 1)
        val bound = maxPx.coerceAtLeast(1)
        val scale = minOf(bound.toFloat() / width, bound.toFloat() / height, 1f)
        val w = (width * scale).toInt().coerceAtLeast(1)
        val h = (height * scale).toInt().coerceAtLeast(1)
        return runCatching { drawable.toBitmap(w, h) }.getOrElse {
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bmp ->
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, w, h)
                drawable.draw(canvas)
            }
        }
    }

    private fun scale(src: Bitmap, maxPx: Int): Bitmap {
        val bound = maxPx.coerceAtLeast(1)
        if (src.width <= bound && src.height <= bound) return src
        val scale = minOf(bound.toFloat() / src.width, bound.toFloat() / src.height)
        val w = (src.width * scale).toInt().coerceAtLeast(1)
        val h = (src.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, w, h, true)
    }
}
