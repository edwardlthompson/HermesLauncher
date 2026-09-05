package org.hermeslauncher.app.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AllAppsIndexTest {
    @Test
    fun sectionsAreAlphabeticalAndOmitEmptyLetters() {
        val apps = listOf(
            LaunchableApp("z", "Z", "Zoom"),
            LaunchableApp("m", "M", "Mail"),
            LaunchableApp("m2", "M2", "Maps"),
        )
        val sections = AllAppsIndex.sections(apps)
        assertEquals(listOf('M', 'Z'), AllAppsIndex.rail(sections))
        assertFalse(AllAppsIndex.rail(sections).contains('A'))
        assertEquals(listOf("Mail", "Maps"), sections[0].second.map { it.label })
    }

    @Test
    fun nonLetterGoesToHash() {
        val apps = listOf(LaunchableApp("n", "N", "7-Eleven"))
        assertEquals(listOf('#'), AllAppsIndex.rail(AllAppsIndex.sections(apps)))
    }

    @Test
    fun indexOfHeader() {
        val sections = AllAppsIndex.sections(
            listOf(LaunchableApp("a", "A", "Alpha"), LaunchableApp("b", "B", "Beta")),
        )
        val keys = AllAppsIndex.keys(sections, predicted = true)
        assertEquals("pred", keys.first())
        assertEquals(1, AllAppsIndex.indexOf(keys, 'A'))
    }

    @Test
    fun keysHonorColumnCount() {
        val apps = (1..5).map { index -> LaunchableApp("p$index", "A$index", "A$index") }
        val sections = AllAppsIndex.sections(apps)
        assertEquals(
            listOf("h:A", "r:A:0", "r:A:1"),
            AllAppsIndex.keys(sections, predicted = false, columns = 4),
        )
    }
}
