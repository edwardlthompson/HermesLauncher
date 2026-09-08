package org.hermeslauncher.app.icons

import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class IconPackAdaptTest {
    @Test
    fun insetShrinksUnmatchedGlyph() {
        assertEquals(24, IconPackAdapt.inset(160, 0.7f))
        assertEquals(0, IconPackAdapt.inset(100, 1f))
    }

    @Test
    fun replateUsesThemedBackgroundAndKeepsGlyph() {
        val glyph = ColorDrawable(0xFF112233.toInt())
        val wrapped = IconPackAdapt.replate(glyph, IconPlate.DARK, 96) as AdaptiveIconDrawable
        assertEquals(IconPlate.DARK, (wrapped.background as ColorDrawable).color)
        assertEquals(0xFF112233.toInt(), (wrapped.foreground as ColorDrawable).color)
    }

    @Test
    fun wrapWithoutChromeReplatesAndRastersGlyph() {
        val source = ColorDrawable(0xFF00FF00.toInt())
        val glyph = IconPackAdapt.raster(source, 96)
        var green = 0
        for (x in 0 until 96) {
            for (y in 0 until 96) {
                if (glyph.getPixel(x, y) and 0x0000FF00 > 0x00008000) {
                    green++
                }
            }
        }
        assertTrue(green > 100)
        val wrapped = IconPackAdapt.wrap(source, IconPackLayers(), IconPlate.DARK, 96)
            as AdaptiveIconDrawable
        assertEquals(IconPlate.DARK, (wrapped.background as ColorDrawable).color)
        assertEquals(0xFF00FF00.toInt(), (wrapped.foreground as ColorDrawable).color)
    }

    @Test
    fun wrapAdaptiveKeepsForegroundGlyph() {
        val source = AdaptiveIconDrawable(
            ColorDrawable(0xFF222222.toInt()),
            ColorDrawable(0xFF00FF00.toInt()),
        )
        val wrapped = IconPackAdapt.wrap(source, IconPackLayers(), IconPlate.DARK, 96)
            as AdaptiveIconDrawable
        assertEquals(IconPlate.DARK, (wrapped.background as ColorDrawable).color)
        assertEquals(0xFF00FF00.toInt(), (wrapped.foreground as ColorDrawable).color)
    }

    @Test
    fun wrapWithMaskComposesOntoPlate() {
        val mask = ColorDrawable(0xFFFFFFFF.toInt())
        val wrapped = IconPackAdapt.wrap(
            ColorDrawable(0xFF00FF00.toInt()),
            IconPackLayers(mask = mask),
            IconPlate.DARK,
            96,
        ) as AdaptiveIconDrawable
        assertEquals(IconPlate.DARK, (wrapped.background as ColorDrawable).color)
        assertTrue(wrapped.foreground is BitmapDrawable)
        val bmp = IconPackAdapt.raster(wrapped.foreground, 96)
        var green = 0
        for (x in 0 until bmp.width) {
            for (y in 0 until bmp.height) {
                if (bmp.getPixel(x, y) and 0x0000FF00 > 0x00008000) {
                    green++
                }
            }
        }
        assertTrue(green > 100)
    }
}
