package org.hermeslauncher.app.oem

import org.hermeslauncher.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GrantCatalogTest {
    @Test
    fun listenerOffIsNotGranted() {
        val snap = PermissionSnapshot(notificationListenerEnabled = false, batteryUnrestricted = true)
        assertFalse(GrantCatalog.granted(GrantKind.LISTENER, snap))
        assertTrue(GrantCatalog.granted(GrantKind.BATTERY, snap))
        assertEquals(R.string.grant_listener_title, GrantCatalog.titleRes(GrantKind.LISTENER))
    }

    @Test
    fun homeRecommendedStillHasCopy() {
        assertEquals(R.string.grant_home_body, GrantCatalog.bodyRes(GrantKind.HOME))
    }
}
