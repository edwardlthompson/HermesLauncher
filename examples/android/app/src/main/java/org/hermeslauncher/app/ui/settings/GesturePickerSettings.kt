package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import org.hermeslauncher.app.launcher.GestureMap
import org.hermeslauncher.app.launcher.GestureSlot
import org.hermeslauncher.app.launcher.LauncherAction
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GesturePickerSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val map by app.gesturePrefs.map.collectAsStateWithLifecycle(GestureMap.defaults())
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.gesture_map_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.gesture_map_body),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = SpacingSm),
        )
        listOf(
            GestureSlot.SWIPE_UP to R.string.gesture_slot_swipe_up,
            GestureSlot.SWIPE_DOWN to R.string.gesture_slot_swipe_down,
            GestureSlot.PINCH to R.string.gesture_slot_pinch,
        ).forEach { (slot, title) ->
            Text(text = stringResource(title), modifier = Modifier.padding(top = SpacingSm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                LauncherAction.entries.forEach { action ->
                    FilterChip(
                        selected = map[slot] == action,
                        onClick = { scope.launch { app.gesturePrefs.setAction(slot, action) } },
                        label = { Text(stringResource(actionLabel(action))) },
                    )
                }
            }
        }
    }
}

private fun actionLabel(action: LauncherAction): Int {
    return when (action) {
        LauncherAction.NONE -> R.string.gesture_action_none
        LauncherAction.DRAWER -> R.string.gesture_action_drawer
        LauncherAction.SEARCH -> R.string.gesture_action_search
        LauncherAction.LOCK -> R.string.gesture_action_lock
        LauncherAction.FLASHLIGHT -> R.string.gesture_action_flashlight
        LauncherAction.SHADE -> R.string.gesture_action_shade
    }
}
