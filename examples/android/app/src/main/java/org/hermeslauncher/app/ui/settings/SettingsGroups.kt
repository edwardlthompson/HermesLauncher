package org.hermeslauncher.app.ui.settings

import org.hermeslauncher.app.R

enum class SettingsGroup {
    HOME,
    INBOX,
    FEEDS,
    SYSTEM,
    ;

    companion object {
        fun of(section: SettingsSection): SettingsGroup =
            entries.first { section in it.sections() }
    }
}

fun SettingsGroup.sections(): List<SettingsSection> = when (this) {
    SettingsGroup.HOME -> listOf(
        SettingsSection.DESKTOP,
        SettingsSection.DOCK,
        SettingsSection.DRAWER,
        SettingsSection.FOLDERS,
        SettingsSection.SEARCH,
        SettingsSection.GESTURES,
        SettingsSection.LOOK,
    )
    SettingsGroup.INBOX -> listOf(SettingsSection.INBOX)
    SettingsGroup.FEEDS -> listOf(SettingsSection.FEEDS, SettingsSection.FEEDS_SUBS)
    SettingsGroup.SYSTEM -> listOf(
        SettingsSection.PERMISSIONS,
        SettingsSection.BACKUP,
        SettingsSection.ABOUT,
    )
}

fun SettingsGroup.titleRes(): Int = when (this) {
    SettingsGroup.HOME -> R.string.settings_group_home
    SettingsGroup.INBOX -> R.string.settings_group_inbox
    SettingsGroup.FEEDS -> R.string.settings_group_feeds
    SettingsGroup.SYSTEM -> R.string.settings_group_system
}

fun SettingsGroup.bodyRes(): Int = when (this) {
    SettingsGroup.HOME -> R.string.settings_group_home_body
    SettingsGroup.INBOX -> R.string.settings_group_inbox_body
    SettingsGroup.FEEDS -> R.string.settings_group_feeds_body
    SettingsGroup.SYSTEM -> R.string.settings_group_system_body
}

fun SettingsGroup.accentRes(): Int = when (this) {
    SettingsGroup.HOME -> R.color.settings_hub_desktop
    SettingsGroup.INBOX -> R.color.settings_hub_inbox
    SettingsGroup.FEEDS -> R.color.settings_hub_feeds
    SettingsGroup.SYSTEM -> R.color.settings_hub_permissions
}

fun SettingsGroup.directSection(): SettingsSection? = sections().singleOrNull()
