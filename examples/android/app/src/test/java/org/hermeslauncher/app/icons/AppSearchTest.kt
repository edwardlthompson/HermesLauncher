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

    @Test
    fun filtersByPackageName() {
        assertEquals(listOf(apps[0]), AppSearch.filter(apps, "com.mail"))
    }

    @Test
    fun ranksByRecencyThenLabel() {
        val lastUsed = mapOf("com.phone" to 9L, "com.maps" to 3L)
        assertEquals(
            listOf(apps[2], apps[1], apps[0]),
            AppSearch.filter(apps, "", lastUsed),
        )
        assertEquals(
            listOf("Phone"),
            AppSearch.filter(apps, "p", lastUsed).map { it.label },
        )
    }
}
