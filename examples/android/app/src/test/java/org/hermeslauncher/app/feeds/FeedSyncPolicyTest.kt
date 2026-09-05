package org.hermeslauncher.app.feeds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedSyncPolicyTest {
    @Test
    fun readerSettingsDefaultChipIsAll() {
        assertEquals(FeedChip.ALL, ReaderSettings().chip)
    }

    @Test
    fun clampSnapsToKnownIntervals() {
        assertEquals(0, ScanInterval.clamp(0))
        assertEquals(15, ScanInterval.clamp(12))
        assertEquals(60, ScanInterval.clamp(60))
        assertEquals(1440, ScanInterval.clamp(2000))
    }

    @Test
    fun autoSyncAllowsUnmeteredWifi() {
        assertTrue(FeedSyncPolicy.allowAuto(true, false, false, false, false, false))
    }

    @Test
    fun autoSyncBlocksCellularWhenMobileOff() {
        assertFalse(FeedSyncPolicy.allowAuto(true, false, true, true, false, false))
    }

    @Test
    fun autoSyncAllowsCellularWhenMobileOn() {
        assertTrue(FeedSyncPolicy.allowAuto(true, true, true, true, false, false))
    }

    @Test
    fun autoSyncRespectsChargingGate() {
        assertFalse(FeedSyncPolicy.allowAuto(true, true, false, false, true, false))
        assertTrue(FeedSyncPolicy.allowAuto(true, true, false, false, true, true))
    }

    @Test
    fun autoSyncBlocksMeteredWifiWhenMobileOff() {
        assertFalse(FeedSyncPolicy.allowAuto(true, false, false, true, false, false))
    }

    @Test
    fun downloadSkipsOffline() {
        assertFalse(FeedSyncPolicy.allowDownload(false, true, false, false))
    }
}
