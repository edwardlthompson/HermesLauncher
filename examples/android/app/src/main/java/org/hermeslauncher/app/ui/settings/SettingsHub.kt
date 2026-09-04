package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd

enum class SettingsSection {
    DESKTOP,
    DOCK,
    DRAWER,
    FOLDERS,
    SEARCH,
    LOOK,
    GESTURES,
    INBOX,
    FEEDS,
    BACKUP,
}

fun SettingsSection.titleRes(): Int = when (this) {
    SettingsSection.DESKTOP -> R.string.settings_section_home
    SettingsSection.DOCK -> R.string.settings_dock_mode
    SettingsSection.DRAWER -> R.string.settings_section_drawer
    SettingsSection.FOLDERS -> R.string.settings_section_folders
    SettingsSection.SEARCH -> R.string.settings_section_search
    SettingsSection.LOOK -> R.string.settings_section_look
    SettingsSection.GESTURES -> R.string.settings_section_gestures
    SettingsSection.INBOX -> R.string.settings_section_inbox
    SettingsSection.FEEDS -> R.string.settings_section_feeds
    SettingsSection.BACKUP -> R.string.settings_section_backup
}

fun SettingsSection.bodyRes(): Int = when (this) {
    SettingsSection.DESKTOP -> R.string.settings_section_home_body
    SettingsSection.DOCK -> R.string.settings_section_dock_body
    SettingsSection.DRAWER -> R.string.settings_section_drawer_body
    SettingsSection.FOLDERS -> R.string.settings_section_folders_body
    SettingsSection.SEARCH -> R.string.settings_section_search_body
    SettingsSection.LOOK -> R.string.settings_section_look_body
    SettingsSection.GESTURES -> R.string.settings_section_gestures_body
    SettingsSection.INBOX -> R.string.settings_section_inbox_body
    SettingsSection.FEEDS -> R.string.settings_section_feeds_body
    SettingsSection.BACKUP -> R.string.settings_section_backup_body
}

@Composable
fun SettingsHub(
    onOpen: (SettingsSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
        SettingsSection.entries.forEach { section ->
            val title = stringResource(section.titleRes())
            val body = stringResource(section.bodyRes())
            ListItem(
                headlineContent = { Text(title) },
                supportingContent = { Text(body) },
                modifier = Modifier
                    .clickable { onOpen(section) }
                    .semantics { contentDescription = "$title. $body" },
            )
        }
    }
}
