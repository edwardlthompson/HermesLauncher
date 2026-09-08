package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Test

class IconPlateTest {
    @Test
    fun darkThemeUsesDarkPlate() {
        assertEquals(IconPlate.DARK, IconPlate.colorFor(true))
        assertEquals(IconPlate.LIGHT, IconPlate.colorFor(false))
    }
}
