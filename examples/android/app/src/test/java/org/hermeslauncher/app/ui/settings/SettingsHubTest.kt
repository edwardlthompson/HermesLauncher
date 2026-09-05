package org.hermeslauncher.app.ui.settings

import org.hermeslauncher.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsHubTest {
    @Test
    fun hubListsTwelveSections() {
        assertEquals(12, SettingsSection.entries.size)
        assertEquals(R.string.settings_section_permissions, SettingsSection.PERMISSIONS.titleRes())
        assertEquals(R.string.settings_section_home, SettingsSection.DESKTOP.titleRes())
        assertEquals(R.string.settings_section_about, SettingsSection.ABOUT.titleRes())
        assertEquals(R.color.settings_hub_about, SettingsSection.ABOUT.accentRes())
        assertEquals(R.string.settings_section_backup, SettingsSection.BACKUP.titleRes())
    }

    @Test
    fun parseSectionName() {
        assertEquals(SettingsSection.PERMISSIONS, SettingsSection.parse("permissions"))
        assertEquals(SettingsSection.ABOUT, SettingsSection.parse("about"))
        assertEquals(SettingsSection.BACKUP, SettingsSection.parse(" BACKUP "))
        assertEquals(null, SettingsSection.parse(null))
        assertEquals(null, SettingsSection.parse("nope"))
    }
}
