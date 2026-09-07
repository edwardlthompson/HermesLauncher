package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.IconPackResources
import org.hermeslauncher.app.ui.launcher.WallpaperIntents
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.ThemeMode

@Composable
fun SettingsDesktopPane() {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_wallpaper)) },
            supportingContent = { Text(stringResource(R.string.settings_wallpaper_body)) },
            modifier = Modifier.clickable { WallpaperIntents.startOrToast(context) },
        )
        L3HomeSettings()
        DesktopSettings()
    }
}

@Composable
fun SettingsLookPane(
    themeMode: ThemeMode,
    onThemeModeSelect: (ThemeMode) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val pack by app.iconPackStore.pack.collectAsStateWithLifecycle(IconPackId())
    val packs = remember {
        listOf(IconPackId()) + IconPackResources.installedPacks(context.packageManager)
    }
    val systemPack = stringResource(R.string.chrome_icon_pack_system)
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        SettingsDropdown(
            title = stringResource(R.string.settings_theme_label),
            options = ThemeMode.entries,
            selected = themeMode,
            labelOf = { mode ->
                stringResource(
                    when (mode) {
                        ThemeMode.System -> R.string.settings_theme_mode_system
                        ThemeMode.Light -> R.string.settings_theme_mode_light
                        ThemeMode.Dark -> R.string.settings_theme_mode_dark
                    },
                )
            },
            onSelect = onThemeModeSelect,
        )
        SettingsDropdown(
            title = stringResource(R.string.settings_icon_pack),
            options = packs,
            selected = packs.firstOrNull { it.packageName == pack.packageName } ?: IconPackId(),
            labelOf = { option -> option.packageName ?: systemPack },
            onSelect = { option -> scope.launch { app.iconPackStore.save(option) } },
        )
        UnreadDotSettings()
        LookSettings()
    }
}
