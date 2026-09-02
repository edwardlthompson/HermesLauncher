package org.hermeslauncher.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.display.highRefreshScroll
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.IconPackResources
import org.hermeslauncher.app.oem.LivePermissions
import org.hermeslauncher.app.ui.insets.bottomInsetPadding
import org.hermeslauncher.app.ui.inbox.InboxHistoryScreen
import org.hermeslauncher.app.ui.launcher.WallpaperIntents
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeSelect: (ThemeMode) -> Unit,
    saveCrashes: Boolean,
    onSaveCrashes: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val pack by app.iconPackStore.pack.collectAsStateWithLifecycle(IconPackId())
    val packs = remember {
        listOf(IconPackId()) + IconPackResources.installedPacks(context.packageManager)
    }
    val ignoreOngoing by app.inboxPrefs.ignoreOngoing.collectAsStateWithLifecycle(true)
    val storePhotos by app.inboxPrefs.storePhotos.collectAsStateWithLifecycle(true)
    var historyOpen by remember { mutableStateOf(false) }
    var mediaOk by remember { mutableStateOf(LivePermissions.mediaGranted(context)) }
    val mediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { mediaOk = LivePermissions.mediaGranted(context) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch { app.feeds.importOpml(uri) }
        }
    }
    if (historyOpen) {
        InboxHistoryScreen(onBack = { historyOpen = false }, modifier = modifier)
        return
    }
    Column(
        modifier = modifier
            .highRefreshScroll()
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
        SectionLabel(R.string.settings_section_home)
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_wallpaper)) },
            supportingContent = { Text(stringResource(R.string.settings_wallpaper_body)) },
            modifier = Modifier.clickable { WallpaperIntents.startOrToast(context) },
        )
        WidgetGridSettings()
        DockSettings()
        HomeChromeSettings()
        HorizontalDivider()
        SectionLabel(R.string.settings_section_appearance)
        Text(text = stringResource(R.string.settings_theme_label))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = themeMode == mode,
                    onClick = { onThemeModeSelect(mode) },
                    label = {
                        Text(
                            when (mode) {
                                ThemeMode.System -> stringResource(R.string.settings_theme_mode_system)
                                ThemeMode.Light -> stringResource(R.string.settings_theme_mode_light)
                                ThemeMode.Dark -> stringResource(R.string.settings_theme_mode_dark)
                            },
                        )
                    },
                )
            }
        }
        Text(text = stringResource(R.string.chrome_icon_pack, pack.packageName ?: stringResource(R.string.chrome_icon_pack_system)))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            packs.forEach { option ->
                FilterChip(
                    selected = option.packageName == pack.packageName,
                    onClick = { scope.launch { app.iconPackStore.save(option) } },
                    label = {
                        Text(option.packageName ?: stringResource(R.string.chrome_icon_pack_system))
                    },
                )
            }
        }
        HorizontalDivider()
        SectionLabel(R.string.settings_section_inbox)
        Text(text = stringResource(R.string.settings_ignore_ongoing))
        Text(text = stringResource(R.string.settings_ignore_ongoing_body), style = MaterialTheme.typography.bodySmall)
        Switch(
            checked = ignoreOngoing,
            onCheckedChange = { on -> scope.launch { app.inboxPrefs.setIgnoreOngoing(on) } },
        )
        Text(text = stringResource(R.string.settings_store_photos))
        Text(text = stringResource(R.string.settings_store_photos_body), style = MaterialTheme.typography.bodySmall)
        Switch(
            checked = storePhotos,
            onCheckedChange = { on -> scope.launch { app.inboxPrefs.setStorePhotos(on) } },
        )
        if (!mediaOk) {
            Button(onClick = { mediaLauncher.launch(LivePermissions.mediaPermission()) }) {
                Text(stringResource(R.string.settings_grant_photos))
            }
        }
        InboxRetentionSettings(onHistory = { historyOpen = true })
        Text(text = stringResource(R.string.settings_feedback_save_crashes))
        Switch(checked = saveCrashes, onCheckedChange = onSaveCrashes)
        Button(onClick = { picker.launch(arrayOf("text/xml", "application/xml", "*/*")) }) {
            Text(stringResource(R.string.feed_import_opml))
        }
        Button(onClick = { scope.launch { app.feeds.refresh() } }) {
            Text(stringResource(R.string.feed_refresh))
        }
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.settings_close))
        }
    }
}

@Composable
private fun SectionLabel(resId: Int) {
    Text(text = stringResource(resId), style = MaterialTheme.typography.titleMedium)
}
