package org.hermeslauncher.app.oem

import org.junit.Assert.assertEquals
import org.junit.Test

class OemDetectorTest {
    @Test
    fun onePlusFromManufacturer() {
        assertEquals(OemFamily.ONEPLUS, OemDetector.detect("OnePlus", "CPH2583"))
    }

    @Test
    fun lineageWinsOverManufacturer() {
        assertEquals(OemFamily.LINEAGE, OemDetector.detect("OnePlus", "lineage_waffle-userdebug"))
    }

    @Test
    fun unknownIsOther() {
        assertEquals(OemFamily.OTHER, OemDetector.detect("UnknownOem", "stock"))
    }
}
