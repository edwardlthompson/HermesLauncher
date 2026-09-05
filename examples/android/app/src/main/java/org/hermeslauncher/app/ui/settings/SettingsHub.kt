package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd

enum class SettingsSection {
    PERMISSIONS,
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
    ABOUT,
    ;

    companion object {
        fun parse(raw: String?): SettingsSection? {
            val key = raw?.trim()?.uppercase() ?: return null
            return entries.firstOrNull { it.name == key }
        }
    }
}

fun SettingsSection.titleRes(): Int = when (this) {
    SettingsSection.PERMISSIONS -> R.string.settings_section_permissions
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
    SettingsSection.ABOUT -> R.string.settings_section_about
}

fun SettingsSection.bodyRes(): Int = when (this) {
    SettingsSection.PERMISSIONS -> R.string.settings_section_permissions_body
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
    SettingsSection.ABOUT -> R.string.settings_section_about_body
}

fun SettingsSection.accentRes(): Int = when (this) {
    SettingsSection.PERMISSIONS -> R.color.settings_hub_permissions
    SettingsSection.DESKTOP -> R.color.settings_hub_desktop
    SettingsSection.DOCK -> R.color.settings_hub_dock
    SettingsSection.DRAWER -> R.color.settings_hub_drawer
    SettingsSection.FOLDERS -> R.color.settings_hub_folders
    SettingsSection.SEARCH -> R.color.settings_hub_search
    SettingsSection.LOOK -> R.color.settings_hub_look
    SettingsSection.GESTURES -> R.color.settings_hub_gestures
    SettingsSection.INBOX -> R.color.settings_hub_inbox
    SettingsSection.FEEDS -> R.color.settings_hub_feeds
    SettingsSection.BACKUP -> R.color.settings_hub_backup
    SettingsSection.ABOUT -> R.color.settings_hub_about
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
            val accent = colorResource(section.accentRes())
            ListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .background(accent, RoundedCornerShape(2.dp)),
                    )
                },
                headlineContent = { Text(title, color = accent) },
                supportingContent = { Text(body, color = accent.copy(alpha = 0.86f)) },
                modifier = Modifier
                    .clickable { onOpen(section) }
                    .semantics { contentDescription = "Open settings section $title" },
            )
        }
    }
}
