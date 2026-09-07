package org.hermeslauncher.app.ui.settings

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
import org.hermeslauncher.app.launcher.SwipeSensitivity

@Composable
fun UnreadDotSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val showDots by app.homePrefs.showDots.collectAsStateWithLifecycle(true)
    SettingsSwitchRow(
        title = R.string.settings_show_dots,
        body = R.string.settings_show_dots_body,
        checked = showDots,
        onCheckedChange = { on -> scope.launch { app.homePrefs.setShowDots(on) } },
        modifier = modifier,
    )
}

@Composable
fun GestureSettings() {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val doubleTap by app.homePrefs.doubleTap.collectAsStateWithLifecycle(DoubleTapAction.OFF)
    val sensitivity by app.gesturePrefs.sensitivity.collectAsStateWithLifecycle(SwipeSensitivity.DEFAULT)
    GesturePickerSettings()
    SettingsDropdown(
        title = stringResource(R.string.gesture_sensitivity),
        options = SwipeSensitivity.entries,
        selected = sensitivity,
        labelOf = { level -> stringResource(sensitivityLabel(level)) },
        onSelect = { level -> scope.launch { app.gesturePrefs.setSensitivity(level) } },
    )
    SettingsDropdown(
        title = stringResource(R.string.settings_double_tap),
        options = DoubleTapAction.entries,
        selected = doubleTap,
        labelOf = { action ->
            stringResource(
                when (action) {
                    DoubleTapAction.OFF -> R.string.settings_double_tap_off
                    DoubleTapAction.LOCK -> R.string.settings_double_tap_lock
                    DoubleTapAction.FLASHLIGHT -> R.string.settings_double_tap_flashlight
                },
            )
        },
        onSelect = { action -> scope.launch { app.homePrefs.setDoubleTap(action) } },
    )
}

private fun sensitivityLabel(level: SwipeSensitivity): Int = when (level) {
    SwipeSensitivity.LOW -> R.string.gesture_sensitivity_low
    SwipeSensitivity.MEDIUM -> R.string.gesture_sensitivity_medium
    SwipeSensitivity.HIGH -> R.string.gesture_sensitivity_high
}
