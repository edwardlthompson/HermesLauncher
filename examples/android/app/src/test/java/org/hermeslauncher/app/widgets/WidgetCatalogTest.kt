package org.hermeslauncher.app.widgets

import android.content.ComponentName
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetCatalogTest {
    @Test
    fun sortsByAppNameThenWidgetLabel() {
        val zoom = WidgetChoice(ComponentName("z.app", "Meet"), "Zoom", "Meet")
        val day = WidgetChoice(ComponentName("c.app", "Day"), "Calendar", "Day")
        val agenda = WidgetChoice(ComponentName("c.app", "Agenda"), "Calendar", "Agenda")
        val sorted = WidgetCatalog.sorted(listOf(zoom, day, agenda))
        assertEquals(
            listOf("Calendar/Agenda", "Calendar/Day", "Zoom/Meet"),
            sorted.map { "${it.appLabel}/${it.widgetLabel}" },
        )
    }

    @Test
    fun groupedKeepsAppHeaders() {
        val zoom = WidgetChoice(ComponentName("z.app", "Meet"), "Zoom", "Meet")
        val day = WidgetChoice(ComponentName("c.app", "Day"), "Calendar", "Day")
        val agenda = WidgetChoice(ComponentName("c.app", "Agenda"), "Calendar", "Agenda")
        val groups = WidgetCatalog.grouped(listOf(zoom, day, agenda))
        assertEquals(listOf("Calendar", "Zoom"), groups.map { it.first })
        assertEquals(2, groups[0].second.size)
    }

    @Test
    fun filterMatchesAppOrWidgetLabel() {
        val zoom = WidgetChoice(ComponentName("z.app", "Meet"), "Zoom", "Meet")
        val day = WidgetChoice(ComponentName("c.app", "Day"), "Calendar", "Day")
        val hits = WidgetCatalog.filter(listOf(zoom, day), "cal")
        assertEquals(listOf("Calendar"), hits.map { it.appLabel })
        assertEquals(2, WidgetCatalog.filter(listOf(zoom, day), "").size)
    }
}
