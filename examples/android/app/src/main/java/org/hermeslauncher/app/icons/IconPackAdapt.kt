package org.hermeslauncher.app.icons

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build

/** Nova-style wrap: keep the app glyph, put it on pack chrome with a themed plate. */
object IconPackAdapt {
    fun wrap(source: Drawable, layers: IconPackLayers, plate: Int, size: Int): Drawable {
        val px = size.coerceAtLeast(48)
        val fg = if (source is AdaptiveIconDrawable) source.foreground ?: source else source
        if (layers.mask == null && layers.upon == null) {
            return AdaptiveIconDrawable(ColorDrawable(plate), fg)
        }
        return AdaptiveIconDrawable(
            ColorDrawable(plate),
            asBitmap(chrome(raster(source, px), layers, px)),
        )
    }

    fun replate(source: Drawable, plate: Int, size: Int = 192): Drawable {
        return wrap(source, IconPackLayers(), plate, size)
    }

    internal fun inset(size: Int, scale: Float): Int {
        val s = scale.coerceIn(IconPackChrome.MIN_SCALE, IconPackChrome.MAX_SCALE)
        return ((1f - s) / 2f * size).toInt().coerceAtLeast(0)
    }

    internal fun raster(source: Drawable, size: Int): Bitmap {
        val software = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        drawOnto(Canvas(software), source, size)
        if (hasInk(software)) {
            return software
        }
        return rasterHw(source, size) ?: software
    }

    private fun drawOnto(canvas: Canvas, source: Drawable, size: Int) {
        source.setBounds(0, 0, size, size)
        source.draw(canvas)
        if (source is AdaptiveIconDrawable) {
            source.foreground?.let { layer ->
                layer.setBounds(0, 0, size, size)
                layer.draw(canvas)
            }
        }
    }

    private fun rasterHw(source: Drawable, size: Int): Bitmap? {
        if (Build.VERSION.SDK_INT < 26) {
            return null
        }
        return runCatching {
            val hw = Bitmap.createBitmap(size, size, Bitmap.Config.HARDWARE)
            drawOnto(Canvas(hw), source, size)
            hw.copy(Bitmap.Config.ARGB_8888, false)
        }.getOrNull()
    }

    private fun hasInk(bmp: Bitmap): Boolean {
        val step = (bmp.width / 4).coerceAtLeast(1)
        var y = step
        while (y < bmp.height) {
            var x = step
            while (x < bmp.width) {
                if (Color.alpha(bmp.getPixel(x, y)) > 16) {
                    return true
                }
                x += step
            }
            y += step
        }
        return false
    }

    @Suppress("DEPRECATION")
    private fun asBitmap(bmp: Bitmap): Drawable = BitmapDrawable(bmp)

    private fun chrome(glyph: Bitmap, layers: IconPackLayers, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val pad = inset(size, layers.scale)
        val inner = (size - 2 * pad).coerceAtLeast(1)
        val scaled = if (glyph.width == inner && glyph.height == inner) {
            glyph
        } else {
            Bitmap.createScaledBitmap(glyph, inner, inner, true)
        }
        val pixels = IntArray(inner * inner)
        scaled.getPixels(pixels, 0, inner, 0, 0, inner, inner)
        bmp.setPixels(pixels, 0, inner, pad, pad, inner, inner)
        val canvas = Canvas(bmp)
        layers.mask?.let { applyMask(canvas, it, size) }
        layers.upon?.let { drawFit(canvas, it, size) }
        return bmp
    }

    private fun drawFit(canvas: Canvas, drawable: Drawable, size: Int) {
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
    }

    private fun applyMask(canvas: Canvas, mask: Drawable, size: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        val maskBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        drawFit(Canvas(maskBmp), mask, size)
        canvas.drawBitmap(maskBmp, 0f, 0f, paint)
    }
}
