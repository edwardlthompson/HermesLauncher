package org.hermeslauncher.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.oem.LivePermissions
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun SettingsInboxPane(
    saveCrashes: Boolean,
    onSaveCrashes: (Boolean) -> Unit,
    onHistory: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val ignoreOngoing by app.inboxPrefs.ignoreOngoing.collectAsStateWithLifecycle(true)
    val storePhotos by app.inboxPrefs.storePhotos.collectAsStateWithLifecycle(true)
    var mediaOk by remember { mutableStateOf(LivePermissions.mediaGranted(context)) }
    var listenerOk by remember { mutableStateOf(LivePermissions.snapshot(context).notificationListenerEnabled) }
    val mediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { mediaOk = LivePermissions.mediaGranted(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                listenerOk = LivePermissions.snapshot(context).notificationListenerEnabled
                mediaOk = LivePermissions.mediaGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.settings_listener_body), style = MaterialTheme.typography.bodySmall)
        if (!listenerOk) {
            Button(onClick = { LivePermissions.startSafe(context, LivePermissions.listenerSettings()) }) {
                Text(stringResource(R.string.settings_listener_open))
            }
        }
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
        InboxRetentionSettings(onHistory = onHistory)
        Text(text = stringResource(R.string.settings_feedback_save_crashes))
        Switch(checked = saveCrashes, onCheckedChange = onSaveCrashes)
    }
}
