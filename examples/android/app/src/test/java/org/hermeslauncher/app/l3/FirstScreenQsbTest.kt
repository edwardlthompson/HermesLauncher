package org.hermeslauncher.app.l3

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FirstScreenQsbTest {
    @Test
    fun launcher3BuildDisablesFirstScreenQsb() {
        val gradle = listOf(
            File("launcher3/build.gradle.kts"),
            File("../launcher3/build.gradle.kts"),
            File("../../launcher3/build.gradle.kts"),
        ).firstOrNull { it.isFile }
        assertTrue("launcher3/build.gradle.kts missing", gradle != null)
        val text = gradle!!.readText()
        assertTrue(text.contains("""buildConfigField("boolean", "QSB_ON_FIRST_SCREEN", "false")"""))
        assertFalse(text.contains("""buildConfigField("boolean", "QSB_ON_FIRST_SCREEN", "true")"""))
    }
}
