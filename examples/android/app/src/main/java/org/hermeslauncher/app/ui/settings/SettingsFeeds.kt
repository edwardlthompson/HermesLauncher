package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsFeedsPane() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val prefs by app.readerPrefs.settings.collectAsStateWithLifecycle(ReaderSettings())
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.feed_scan_title))
        Text(text = stringResource(R.string.feed_scan_body), style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ScanInterval.OPTIONS.forEach { minutes ->
                FilterChip(
                    selected = prefs.scanMinutes == minutes,
                    onClick = { scope.launch { app.readerPrefs.setScanMinutes(minutes) } },
                    label = { Text(scanLabel(minutes)) },
                )
            }
        }
        Toggle(
            title = R.string.feed_charging_title,
            body = R.string.feed_charging_body,
            checked = prefs.onlyWhenCharging,
            onCheck = { on -> scope.launch { app.readerPrefs.setOnlyWhenCharging(on) } },
        )
        Toggle(
            title = R.string.feed_open_refresh_title,
            body = R.string.feed_open_refresh_body,
            checked = prefs.refreshOnOpen,
            onCheck = { on -> scope.launch { app.readerPrefs.setRefreshOnOpen(on) } },
        )
        Text(text = stringResource(R.string.feed_images_title))
        Text(text = stringResource(R.string.feed_images_body), style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ImagePolicy.entries.forEach { policy ->
                FilterChip(
                    selected = prefs.imagePolicy == policy,
                    onClick = { scope.launch { app.readerPrefs.setImagePolicy(policy) } },
                    label = { Text(stringResource(imageLabel(policy))) },
                )
            }
        }
        Text(text = stringResource(R.string.feed_sort_title))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            FilterChip(
                selected = prefs.newestFirst,
                onClick = { scope.launch { app.readerPrefs.setNewestFirst(true) } },
                label = { Text(stringResource(R.string.feed_sort_newest)) },
            )
            FilterChip(
                selected = !prefs.newestFirst,
                onClick = { scope.launch { app.readerPrefs.setNewestFirst(false) } },
                label = { Text(stringResource(R.string.feed_sort_oldest)) },
            )
        }
        Text(text = stringResource(R.string.feed_opener_title))
        Text(text = stringResource(R.string.feed_opener_body), style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            FilterChip(
                selected = prefs.target == ArticleTarget.LAUNCHER,
                onClick = { scope.launch { app.readerPrefs.setTarget(ArticleTarget.LAUNCHER) } },
                label = { Text(stringResource(R.string.feed_opener_launcher)) },
            )
            FilterChip(
                selected = prefs.target == ArticleTarget.BROWSER,
                onClick = { scope.launch { app.readerPrefs.setTarget(ArticleTarget.BROWSER) } },
                label = { Text(stringResource(R.string.feed_opener_browser)) },
            )
            FilterChip(
                selected = prefs.target == ArticleTarget.CUSTOM_TAB,
                onClick = { scope.launch { app.readerPrefs.setTarget(ArticleTarget.CUSTOM_TAB) } },
                label = { Text(stringResource(R.string.feed_opener_custom_tab)) },
            )
        }
        androidx.compose.material3.OutlinedTextField(
            value = prefs.blocked,
            onValueChange = { scope.launch { app.readerPrefs.setBlocked(it) } },
            label = { Text(stringResource(R.string.feed_block_list)) },
            singleLine = true,
        )
        SettingsFeedSubs()
        Button(onClick = { scope.launch { app.feeds.addFromLink(DefaultFeeds.ANDROID_AUTHORITY) } }) {
            Text(stringResource(R.string.feed_add_android_authority))
        }
        SettingsOpmlButtons()
        Button(onClick = { scope.launch { app.feeds.refresh() } }) {
            Text(stringResource(R.string.feed_refresh))
        }
    }
}

@Composable
private fun Toggle(title: Int, body: Int, checked: Boolean, onCheck: (Boolean) -> Unit) {
    Text(text = stringResource(title))
    Text(text = stringResource(body), style = MaterialTheme.typography.bodySmall)
    Switch(checked = checked, onCheckedChange = onCheck)
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
