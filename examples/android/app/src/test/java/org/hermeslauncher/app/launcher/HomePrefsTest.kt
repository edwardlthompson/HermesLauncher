package org.hermeslauncher.app.launcher

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hermeslauncher.app.clearPreferenceDataStores
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class HomePrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun resetDataStore() {
        context.clearPreferenceDataStores()
    }

    @Test
    fun defaultsShowDotsAndDoubleTapOff() = runBlocking {
        val prefs = HomePrefs(context)
        assertEquals(true, prefs.showDots.first())
        assertEquals(true, prefs.showLabels.first())
        assertEquals(false, prefs.usageBannerDismissed.first())
        assertEquals(DoubleTapAction.OFF, prefs.doubleTap.first())
    }

    @Test
    fun persistsChromeToggles() = runBlocking {
        val prefs = HomePrefs(context)
        prefs.setShowDots(false)
        prefs.setShowLabels(false)
        prefs.setUsageBannerDismissed(true)
        prefs.setDoubleTap(DoubleTapAction.FLASHLIGHT)
        assertEquals(false, prefs.showDots.first())
        assertEquals(false, prefs.showLabels.first())
        assertEquals(true, prefs.usageBannerDismissed.first())
        assertEquals(DoubleTapAction.FLASHLIGHT, prefs.doubleTap.first())
    }
}
