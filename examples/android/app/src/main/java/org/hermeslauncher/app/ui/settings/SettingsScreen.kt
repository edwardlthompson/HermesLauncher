package org.hermeslauncher.app.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.display.highRefreshScroll
import org.hermeslauncher.app.ui.inbox.InboxHistoryScreen
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeSelect: (ThemeMode) -> Unit,
    saveCrashes: Boolean,
    onSaveCrashes: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var section by remember { mutableStateOf<SettingsSection?>(null) }
    var historyOpen by remember { mutableStateOf(false) }
    BackHandler {
        when {
            historyOpen -> historyOpen = false
            section != null -> section = null
            else -> onBack()
        }
    }
    if (historyOpen) {
        InboxHistoryScreen(modifier = modifier)
        return
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .highRefreshScroll()
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        val open = section
        if (open == null) {
            SettingsHub(onOpen = { section = it })
        } else {
            Text(text = stringResource(open.titleRes()), style = MaterialTheme.typography.headlineSmall)
            when (open) {
                SettingsSection.DESKTOP -> SettingsDesktopPane()
                SettingsSection.DOCK -> DockSettings()
                SettingsSection.LOOK -> SettingsLookPane(themeMode, onThemeModeSelect)
                SettingsSection.GESTURES -> GestureSettings()
                SettingsSection.INBOX -> SettingsInboxPane(
                    saveCrashes = saveCrashes,
                    onSaveCrashes = onSaveCrashes,
                    onHistory = { historyOpen = true },
                )
                SettingsSection.FEEDS -> SettingsFeedsPane()
                SettingsSection.DRAWER -> DrawerSettings()
                SettingsSection.FOLDERS -> FolderSettings()
                SettingsSection.SEARCH -> SearchSettings()
                SettingsSection.BACKUP,
                -> SettingsStubPane(open)
            }
        }
    }
}
