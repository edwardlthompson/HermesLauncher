package org.hermeslauncher.app.about

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class UpdateLaunchPrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun resetPrefs() {
        context.getSharedPreferences(UpdateLaunchPrefs.PREFS, Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun recordsSeenAndDismissedVersions() {
        val prefs = UpdateLaunchPrefs(context)
        assertNull(prefs.lastSeenVersion())
        prefs.markVersionSeen("1.0.0")
        assertEquals("1.0.0", prefs.lastSeenVersion())
        prefs.markChecked(1_000L, "1.1.0")
        assertEquals(1_000L, prefs.lastCheckAt())
        assertEquals("1.1.0", prefs.dismissedVersion())
    }
}
