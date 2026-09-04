package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import org.hermeslauncher.app.launcher.DoubleTapAction
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun UnreadDotSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val showDots by app.homePrefs.showDots.collectAsStateWithLifecycle(true)
    Text(text = stringResource(R.string.settings_show_dots), modifier = modifier)
    Text(text = stringResource(R.string.settings_show_dots_body), style = MaterialTheme.typography.bodySmall)
    Switch(checked = showDots, onCheckedChange = { on -> scope.launch { app.homePrefs.setShowDots(on) } })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GestureSettings() {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val doubleTap by app.homePrefs.doubleTap.collectAsStateWithLifecycle(DoubleTapAction.OFF)
    Text(text = stringResource(R.string.settings_double_tap))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
        DoubleTapAction.entries.forEach { action ->
            FilterChip(
                selected = doubleTap == action,
                onClick = { scope.launch { app.homePrefs.setDoubleTap(action) } },
                label = {
                    Text(
                        when (action) {
                            DoubleTapAction.OFF -> stringResource(R.string.settings_double_tap_off)
                            DoubleTapAction.LOCK -> stringResource(R.string.settings_double_tap_lock)
                            DoubleTapAction.FLASHLIGHT -> stringResource(R.string.settings_double_tap_flashlight)
                        },
                    )
                },
            )
        }
    }
}
