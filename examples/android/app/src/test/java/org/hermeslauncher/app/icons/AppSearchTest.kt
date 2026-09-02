package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSearchTest {
    private val apps = listOf(
        LaunchableApp("com.mail", "Inbox", "Mail"),
        LaunchableApp("com.maps", "Map", "Maps"),
        LaunchableApp("com.phone", "Dialer", "Phone"),
    )

    @Test
    fun emptyQueryReturnsAll() {
        assertEquals(apps, AppSearch.filter(apps, "  "))
    }

    @Test
    fun filtersCaseInsensitive() {
        assertEquals(listOf(apps[0]), AppSearch.filter(apps, "mail"))
    }
}
