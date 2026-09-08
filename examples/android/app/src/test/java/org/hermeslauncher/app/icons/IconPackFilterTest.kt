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
    fun mapsIconTagAndDrawablePath() {
        val maps = IconPackFilter.parseXml(
            """
            <resources>
              <icon component="ComponentInfo{com.mail/.Inbox}" drawable="@drawable/com_mail"/>
            </resources>
            """.trimIndent(),
        )
        assertEquals("com_mail", maps.byComponent["com.mail/com.mail.Inbox"])
        assertEquals("chrome", IconPackFilter.drawableName("@drawable/Chrome.PNG"))
    }

    @Test
    fun relativeClassExpandsToPackage() {
        assertEquals(
            "com.mail/com.mail.Inbox",
            IconPackFilter.normalizeComponent("ComponentInfo{com.mail/.Inbox}"),
        )
    }

    @Test
    fun parsesPackChrome() {
        val maps = IconPackFilter.parseXml(
            """
            <resources>
              <iconback img1="back_plate" img2="back_alt"/>
              <iconmask img1="mask_squircle"/>
              <iconupon img1="upon_gloss"/>
              <scale factor="0.75"/>
              <item component="ComponentInfo{com.app/.Main}" drawable="app"/>
            </resources>
            """.trimIndent(),
        )
        assertEquals(listOf("back_plate", "back_alt"), maps.chrome.backs)
        assertEquals(listOf("mask_squircle"), maps.chrome.masks)
        assertEquals(listOf("upon_gloss"), maps.chrome.upons)
        assertEquals(0.75f, maps.chrome.scale, 0.001f)
        assertEquals("app", maps.byComponent["com.app/com.app.Main"])
    }
}
