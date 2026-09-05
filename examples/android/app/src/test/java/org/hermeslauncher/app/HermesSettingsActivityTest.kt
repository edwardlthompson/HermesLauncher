package org.hermeslauncher.app

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class HermesSettingsActivityTest {
    @Test
    fun applicationPreferencesResolvesToHermesSettings() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val intent = Intent(Intent.ACTION_APPLICATION_PREFERENCES).setPackage(context.packageName)
        val resolved = intent.resolveActivity(context.packageManager)
        assertNotNull(resolved)
        assertEquals(HermesSettingsActivity::class.java.name, resolved!!.className)
    }
}
