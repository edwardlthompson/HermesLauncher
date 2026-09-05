package org.hermeslauncher.app.ui.onboarding

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.hermeslauncher.app.HermesSettingsActivity
import org.hermeslauncher.app.oem.GrantCatalog
import org.hermeslauncher.app.oem.GrantKind
import org.hermeslauncher.app.oem.LivePermissions
import org.hermeslauncher.app.oem.OemDetector
import org.hermeslauncher.app.oem.RepairPolicy
import org.hermeslauncher.app.ui.settings.SettingsSection

/** Overlay on Inbox. HOME ComposeView has no ActivityResultRegistryOwner. */
@Composable
fun InboxGrantHost(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var live by remember { mutableStateOf(LivePermissions.snapshot(context)) }
    val snapshot = live.copy(homeRoleHeld = true)
    var grantsWereGood by remember { mutableStateOf(!RepairPolicy.needsOverlay(snapshot)) }
    val oem = remember { OemDetector.detect(Build.MANUFACTURER, Build.DISPLAY) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val next = LivePermissions.snapshot(context)
                live = next
                if (!RepairPolicy.needsOverlay(next.copy(homeRoleHeld = true))) {
                    grantsWereGood = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val openPermissions = {
        LivePermissions.startSafe(context, HermesSettingsActivity.intent(context, SettingsSection.PERMISSIONS))
    }
    GrantChrome(
        snapshot = snapshot,
        grantsWereGood = grantsWereGood,
        oem = oem,
        onNotification = { GrantCatalog.open(context, GrantKind.LISTENER) },
        onBattery = { GrantCatalog.open(context, GrantKind.BATTERY) },
        onHome = { GrantCatalog.open(context, GrantKind.HOME) },
        onPhotos = { GrantCatalog.open(context, GrantKind.MEDIA) },
        onUsage = { GrantCatalog.open(context, GrantKind.USAGE) },
        onPost = { GrantCatalog.open(context, GrantKind.POST) },
        onAllGrants = openPermissions,
        onRepair = openPermissions,
        onLater = { grantsWereGood = true },
        modifier = modifier,
    )
}
