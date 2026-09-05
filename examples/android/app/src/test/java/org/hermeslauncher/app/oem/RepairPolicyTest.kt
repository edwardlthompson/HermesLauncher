package org.hermeslauncher.app.oem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepairPolicyTest {
    @Test
    fun bannerWhenListenerOff() {
        val snap = PermissionSnapshot(notificationListenerEnabled = false, batteryUnrestricted = true)
        assertTrue(RepairPolicy.needsBanner(snap))
    }

    @Test
    fun bannerWhenHomeRoleMissing() {
        val snap = PermissionSnapshot(
            notificationListenerEnabled = true,
            batteryUnrestricted = true,
            homeRoleHeld = false,
        )
        assertFalse(RepairPolicy.needsOverlay(snap))
        assertTrue(RepairPolicy.needsBanner(snap))
    }

    @Test
    fun overlayWhenMediaMissing() {
        val snap = PermissionSnapshot(
            notificationListenerEnabled = true,
            batteryUnrestricted = true,
            mediaGranted = false,
        )
        assertTrue(RepairPolicy.needsOverlay(snap))
    }

    @Test
    fun noBannerWhenHealthy() {
        val snap = PermissionSnapshot(notificationListenerEnabled = true, batteryUnrestricted = true)
        assertFalse(RepairPolicy.needsBanner(snap))
        assertFalse(RepairPolicy.needsOverlay(snap))
    }

    @Test
    fun onePlusStepId() {
        assertEquals("listener_then_battery", RepairPolicy.primaryStep(OemFamily.ONEPLUS))
    }
}
