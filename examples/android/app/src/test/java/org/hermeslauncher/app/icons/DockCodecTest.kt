package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Test

class DockCodecTest {
    @Test
    fun roundTripPreservesAssignments() {
        val app = LaunchableApp("com.mail", "Inbox", "Mail")
        val layout = DockLayout().withApp(0, app).withApp(2, app)
        val restored = DockCodec.decode(DockCodec.encode(layout))
        assertEquals(layout.slotCount, restored.slotCount)
        assertEquals("com.mail", restored.slot(0)?.packageName)
        assertEquals("Inbox", restored.slot(2)?.activityName)
    }

    @Test
    fun corruptInputYieldsDefaults() {
        val restored = DockCodec.decode("nope")
        assertEquals(DockLayout.DEFAULT_SLOTS, restored.slotCount)
        assertEquals(0, restored.assigned.size)
        assertEquals(DockMode.USAGE, restored.mode)
    }

    @Test
    fun v1AssignedBecomesCustom() {
        val restored = DockCodec.decode("v1|5|0:com.mail:Inbox")
        assertEquals(DockMode.CUSTOM, restored.mode)
        assertEquals("com.mail", restored.slot(0)?.packageName)
    }

    @Test
    fun v1EmptyBecomesUsage() {
        val restored = DockCodec.decode("v1|5|")
        assertEquals(DockMode.USAGE, restored.mode)
    }

    @Test
    fun v2RoundTripKeepsUsage() {
        val layout = DockLayout(mode = DockMode.USAGE).withApp(0, LaunchableApp("com.a", "A", "A"))
            .copy(mode = DockMode.USAGE)
        val restored = DockCodec.decode(DockCodec.encode(layout))
        assertEquals(DockMode.USAGE, restored.mode)
        assertEquals("com.a", restored.slot(0)?.packageName)
    }
}
