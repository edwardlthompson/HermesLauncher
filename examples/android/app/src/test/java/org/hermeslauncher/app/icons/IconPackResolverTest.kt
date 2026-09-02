package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Test

class IconPackResolverTest {
    private val app = LaunchableApp("com.mail", "Inbox", "Mail")

    @Test
    fun systemPackKey() {
        assertEquals("system/com.mail/Inbox", IconPackResolver.componentKey(IconPackId(), app))
    }

    @Test
    fun namedPackKey() {
        val pack = IconPackId("org.example.icons")
        assertEquals("org.example.icons/com.mail/Inbox", IconPackResolver.componentKey(pack, app))
    }
}
