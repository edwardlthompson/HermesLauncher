package org.hermeslauncher.app.ui.onboarding

import androidx.compose.runtime.Composable
import org.hermeslauncher.app.oem.OemFamily
import org.hermeslauncher.app.oem.PermissionSnapshot
import org.hermeslauncher.app.oem.RepairPolicy

@Composable
fun GrantChrome(
    snapshot: PermissionSnapshot,
    grantsWereGood: Boolean,
    oem: OemFamily,
    onNotification: () -> Unit,
    onBattery: () -> Unit,
    onHome: () -> Unit,
    onPhotos: () -> Unit,
    onRepair: () -> Unit,
) {
    if (RepairPolicy.needsOverlay(snapshot) && !grantsWereGood) {
        FirstRunOverlay(
            snapshot = snapshot,
            onNotification = onNotification,
            onBattery = onBattery,
            onHome = onHome,
            onPhotos = onPhotos,
        )
    } else if (RepairPolicy.needsBanner(snapshot) && grantsWereGood) {
        RepairBanner(oem = oem, onRepair = onRepair)
    }
}
