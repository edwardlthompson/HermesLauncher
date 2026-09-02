package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DockLayoutTest {
    @Test
    fun assignsWithinSlotRange() {
        val app = LaunchableApp("com.mail", "Inbox", "Mail")
        val layout = DockLayout().withApp(0, app)
        assertEquals(app, layout.slot(0))
        assertNull(layout.slot(1))
    }

    @Test
    fun ignoresOutOfRangeIndex() {
        val app = LaunchableApp("com.mail", "Inbox", "Mail")
        val layout = DockLayout(slotCount = 2).withApp(5, app)
        assertNull(layout.slot(0))
    }

    @Test
    fun withAppSwitchesToCustom() {
        val app = LaunchableApp("com.mail", "Inbox", "Mail")
        val layout = DockLayout().withApp(0, app)
        assertEquals(DockMode.CUSTOM, layout.mode)
        assertEquals(app, layout.slot(0))
    }
}
