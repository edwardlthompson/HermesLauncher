package org.hermeslauncher.app.ui.launcher

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class WallpaperIntentsTest {
    @Test
    fun pickerUsesSystemActionWithoutPackage() {
        val intent = WallpaperIntents.picker()
        assertEquals(Intent.ACTION_SET_WALLPAPER, intent.action)
        assertNull(intent.`package`)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun pickerDoesNotPinGoogle() {
        assertTrue(!WallpaperIntents.pinsGoogle(WallpaperIntents.picker()))
    }

    @Test
    fun hermesLiveUsesCallerApplicationId() {
        val names = WallpaperIntents.hermesLive("org.hermeslauncher.app")
        assertEquals(2, names.size)
        assertTrue(names.all { it.packageName == "org.hermeslauncher.app" })
        assertTrue(names.any { it.className.endsWith("GradientLiveWallpaper") })
        assertTrue(names.any { it.className.endsWith("ClockLiveWallpaper") })
    }
}
