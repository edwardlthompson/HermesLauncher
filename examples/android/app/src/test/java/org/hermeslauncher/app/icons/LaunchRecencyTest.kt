package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LaunchRecencyTest {
    @Before
    fun reset() {
        LaunchRecency.replace(emptyMap())
        LaunchRecency.persist = null
    }

    @Test
    fun touchRanksMostRecentFirst() {
        LaunchRecency.touch("com.maps", 10L)
        LaunchRecency.touch("com.mail", 50L)
        val apps = listOf(
            LaunchableApp("com.maps", "Map", "Maps"),
            LaunchableApp("com.mail", "Inbox", "Mail"),
            LaunchableApp("com.phone", "Dialer", "Phone"),
        )
        val ranked = AppSearch.filter(apps, "m", LaunchRecency.snapshot())
        assertEquals(listOf("Mail", "Maps"), ranked.map { it.label })
    }

    @Test
    fun codecRoundTrip() {
        val encoded = LaunchRecencyCodec.encode(mapOf("com.mail" to 9L, " " to 1L))
        val decoded = LaunchRecencyCodec.decode(encoded)
        assertEquals(9L, decoded["com.mail"])
        assertTrue(decoded.keys.none { it.isBlank() })
        assertTrue(LaunchRecencyCodec.decode(null).isEmpty())
    }
}
