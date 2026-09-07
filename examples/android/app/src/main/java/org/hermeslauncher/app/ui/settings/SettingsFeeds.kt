package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.ArticleTarget
import org.hermeslauncher.app.feeds.DefaultFeeds
import org.hermeslauncher.app.feeds.ImagePolicy
import org.hermeslauncher.app.feeds.ReaderSettings
import org.hermeslauncher.app.feeds.ScanInterval
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun SettingsFeedsPane(onSubscriptions: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val prefs by app.readerPrefs.settings.collectAsStateWithLifecycle(ReaderSettings())
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.feed_subs_title)) },
            supportingContent = { Text(stringResource(R.string.feed_subs_open_body)) },
            modifier = Modifier.clickable(onClick = onSubscriptions),
        )
        SettingsExpander(title = stringResource(R.string.settings_expand_sync)) {
            Text(text = stringResource(R.string.feed_scan_body), style = MaterialTheme.typography.bodySmall)
            SettingsDropdown(
                title = stringResource(R.string.feed_scan_title),
                options = ScanInterval.OPTIONS,
                selected = prefs.scanMinutes,
                labelOf = { scanLabel(it) },
                onSelect = { minutes -> scope.launch { app.readerPrefs.setScanMinutes(minutes) } },
            )
            SettingsSwitchRow(
                title = R.string.feed_charging_title,
                body = R.string.feed_charging_body,
                checked = prefs.onlyWhenCharging,
                onCheckedChange = { on -> scope.launch { app.readerPrefs.setOnlyWhenCharging(on) } },
            )
            SettingsSwitchRow(
                title = R.string.feed_open_refresh_title,
                body = R.string.feed_open_refresh_body,
                checked = prefs.refreshOnOpen,
                onCheckedChange = { on -> scope.launch { app.readerPrefs.setRefreshOnOpen(on) } },
            )
        }
        SettingsExpander(title = stringResource(R.string.settings_expand_reading)) {
            Text(text = stringResource(R.string.feed_images_body), style = MaterialTheme.typography.bodySmall)
            SettingsDropdown(
                title = stringResource(R.string.feed_images_title),
                options = ImagePolicy.entries,
                selected = prefs.imagePolicy,
                labelOf = { stringResource(imageLabel(it)) },
                onSelect = { policy -> scope.launch { app.readerPrefs.setImagePolicy(policy) } },
            )
            SettingsDropdown(
                title = stringResource(R.string.feed_sort_title),
                options = listOf(true, false),
                selected = prefs.newestFirst,
                labelOf = { newest ->
                    stringResource(if (newest) R.string.feed_sort_newest else R.string.feed_sort_oldest)
                },
                onSelect = { newest -> scope.launch { app.readerPrefs.setNewestFirst(newest) } },
            )
            SettingsDropdown(
                title = stringResource(R.string.feed_opener_title),
                options = ArticleTarget.entries,
                selected = prefs.target,
                labelOf = { stringResource(openerLabel(it)) },
                onSelect = { target -> scope.launch { app.readerPrefs.setTarget(target) } },
            )
            OutlinedTextField(
                value = prefs.blocked,
                onValueChange = { scope.launch { app.readerPrefs.setBlocked(it) } },
                label = { Text(stringResource(R.string.feed_block_list)) },
                singleLine = true,
            )
        }
        SettingsExpander(title = stringResource(R.string.settings_expand_import)) {
            Button(onClick = { scope.launch { app.feeds.addFromLink(DefaultFeeds.ANDROID_AUTHORITY) } }) {
                Text(stringResource(R.string.feed_add_android_authority))
            }
            SettingsOpmlButtons()
        }
        Button(onClick = { scope.launch { app.feeds.refresh() } }) {
            Text(stringResource(R.string.feed_refresh))
        }
    }
}

@Composable
private fun scanLabel(minutes: Int): String {
    val res = when (minutes) {
        0 -> R.string.feed_scan_manual
        15 -> R.string.feed_scan_15
        30 -> R.string.feed_scan_30
        60 -> R.string.feed_scan_hour
        180 -> R.string.feed_scan_3h
        360 -> R.string.feed_scan_6h
        720 -> R.string.feed_scan_12h
        else -> R.string.feed_scan_day
    }
    return stringResource(res)
}

private fun imageLabel(policy: ImagePolicy): Int = when (policy) {
    ImagePolicy.ALWAYS -> R.string.feed_images_always
    ImagePolicy.WIFI -> R.string.feed_images_wifi
    ImagePolicy.NEVER -> R.string.feed_images_never
}

private fun openerLabel(target: ArticleTarget): Int = when (target) {
    ArticleTarget.LAUNCHER -> R.string.feed_opener_launcher
    ArticleTarget.BROWSER -> R.string.feed_opener_browser
    ArticleTarget.CUSTOM_TAB -> R.string.feed_opener_custom_tab
}
