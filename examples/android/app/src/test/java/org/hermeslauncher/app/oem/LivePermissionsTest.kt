package org.hermeslauncher.app.oem

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class LivePermissionsTest {
    @Test
    fun snapshotMapsListenerBatteryAndHome() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val snap = LivePermissions.snapshot(context)
        assertNotNull(snap.notificationListenerEnabled)
        assertNotNull(snap.batteryUnrestricted)
        assertNotNull(snap.homeRoleHeld)
        assertNotNull(snap.mediaGranted)
    }

    @Test
    fun usageSettingsIntent() {
        assertEquals(
            android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS,
            LivePermissions.usageSettings().action,
        )
    }

    @Test
    fun requestMediaNoopsWithoutActivity() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        assertNull(LivePermissions.unwrapActivity(context))
        LivePermissions.requestMedia(context)
    }
}
