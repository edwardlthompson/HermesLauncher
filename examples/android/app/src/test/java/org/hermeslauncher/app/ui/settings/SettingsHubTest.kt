package org.hermeslauncher.app.ui.settings

import org.hermeslauncher.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsHubTest {
    @Test
    fun hubListsFourGroups() {
        assertEquals(4, SettingsGroup.entries.size)
        assertEquals(13, SettingsSection.entries.size)
        assertEquals(R.string.settings_group_home, SettingsGroup.HOME.titleRes())
        assertEquals(R.string.settings_section_subs, SettingsSection.FEEDS_SUBS.titleRes())
        assertEquals(
            listOf(SettingsSection.FEEDS, SettingsSection.FEEDS_SUBS),
            SettingsGroup.FEEDS.sections(),
        )
        assertEquals(SettingsSection.INBOX, SettingsGroup.INBOX.directSection())
        assertEquals(null, SettingsGroup.HOME.directSection())
        assertEquals(SettingsGroup.FEEDS, SettingsGroup.of(SettingsSection.FEEDS_SUBS))
        assertEquals(R.color.settings_hub_about, SettingsSection.ABOUT.accentRes())
    }

    @Test
    fun parseSectionName() {
        assertEquals(SettingsSection.PERMISSIONS, SettingsSection.parse("permissions"))
        assertEquals(SettingsSection.ABOUT, SettingsSection.parse("about"))
        assertEquals(SettingsSection.BACKUP, SettingsSection.parse(" BACKUP "))
        assertEquals(SettingsSection.FEEDS_SUBS, SettingsSection.parse("feeds_subs"))
        assertEquals(null, SettingsSection.parse(null))
        assertEquals(null, SettingsSection.parse("nope"))
    }
}
