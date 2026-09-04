package org.hermeslauncher.app.ui.settings

import org.hermeslauncher.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsHubTest {
    @Test
    fun hubListsTenSections() {
        assertEquals(10, SettingsSection.entries.size)
        assertEquals(R.string.settings_section_home, SettingsSection.DESKTOP.titleRes())
        assertEquals(R.string.settings_section_backup, SettingsSection.BACKUP.titleRes())
    }
}
