package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalLayoutApi::class)
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
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
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
        UnreadDotSettings()
        LookSettings()
    }
}
