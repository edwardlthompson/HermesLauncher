package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class IconPackFilterTest {
    @Test
    fun mapsComponentInfoAndSkipsCategories() {
        val maps = IconPackFilter.parseXml(
            """
            <resources>
              <item component="ComponentInfo{com.android.chrome/com.google.android.apps.chrome.Main}" drawable="chrome"/>
              <item component=":BROWSER" drawable="browser"/>
            </resources>
            """.trimIndent(),
        )
        assertEquals(
            "chrome",
            maps.byComponent["com.android.chrome/com.google.android.apps.chrome.Main"],
        )
        assertEquals("chrome", maps.byPackage["com.android.chrome"])
        assertNull(maps.byComponent[":BROWSER"])
    }

    @Test
    fun relativeClassExpandsToPackage() {
        assertEquals(
            "com.mail/com.mail.Inbox",
            IconPackFilter.normalizeComponent("ComponentInfo{com.mail/.Inbox}"),
        )
    }
}
