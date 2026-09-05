package org.hermeslauncher.app.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NovaLayoutTest {
    @Test
    fun componentParsesIntent() {
        val parsed = NovaLayout.componentOf("#Intent;component=org.hermeslauncher.app/.HermesLauncherActivity;end")
        assertEquals("org.hermeslauncher.app" to "org.hermeslauncher.app.HermesLauncherActivity", parsed)
        assertNull(NovaLayout.componentOf(""))
    }

    @Test
    fun desktopMapsScreenZeroToPageOne() {
        val row = NovaFavorite(
            title = "Maps",
            intent = "#Intent;component=com.google.android.apps.maps/com.google.android.maps.MapsActivity;end",
            container = NovaLayout.CONTAINER_DESKTOP,
            screen = 0,
            cellX = 1,
            cellY = 2,
            itemType = NovaLayout.TYPE_APP,
        )
        val layout = NovaLayout.desktop(listOf(row))
        val item = layout.page(1).single()
        assertEquals("com.google.android.apps.maps", item.packageName)
        assertEquals(1, item.cellX)
        assertEquals(2, item.cellY)
    }

    @Test
    fun dockUsesHotseatOrder() {
        val row = NovaFavorite(
            title = "Phone",
            intent = "#Intent;component=com.android.dialer/.DialtactsActivity;end",
            container = NovaLayout.CONTAINER_HOTSEAT,
            screen = 0,
            cellX = 0,
            cellY = 0,
            itemType = NovaLayout.TYPE_APP,
        )
        val dock = NovaLayout.dock(listOf(row))
        assertEquals(org.hermeslauncher.app.icons.DockMode.CUSTOM, dock.mode)
        assertTrue(dock.assigned.containsKey(0))
    }
}
