package org.hermeslauncher.app.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.hermeslauncher.app.HermesSettingsActivity
import org.hermeslauncher.app.oem.GrantCatalog
import org.hermeslauncher.app.oem.GrantKind
import org.hermeslauncher.app.oem.LivePermissions
import org.hermeslauncher.app.oem.OemFamily
import org.hermeslauncher.app.oem.PermissionSnapshot
import org.hermeslauncher.app.oem.RepairPolicy
import org.hermeslauncher.app.ui.settings.SettingsSection

@Composable
fun GrantChrome(
    snapshot: PermissionSnapshot,
    grantsWereGood: Boolean,
    oem: OemFamily,
    onNotification: () -> Unit,
    onBattery: () -> Unit,
    onHome: () -> Unit,
    onPhotos: () -> Unit,
    onUsage: () -> Unit,
    onPost: () -> Unit,
    onAllGrants: () -> Unit,
    onRepair: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (RepairPolicy.needsOverlay(snapshot) && !grantsWereGood) {
        FirstRunOverlay(
            snapshot = snapshot,
            onNotification = onNotification,
            onBattery = onBattery,
            onHome = onHome,
            onPhotos = onPhotos,
            onUsage = onUsage,
            onPost = onPost,
            onAllGrants = onAllGrants,
            onLater = onLater,
            modifier = modifier,
        )
    } else if (RepairPolicy.needsBanner(snapshot) && grantsWereGood) {
        RepairBanner(oem = oem, onRepair = onRepair, modifier = modifier)
    } else if (!snapshot.homeRoleHeld) {
        DefaultHomeReminder(onHome = onHome, modifier = modifier)
    }
}

@Composable
fun HomeGrantChrome(
    snapshot: PermissionSnapshot,
    grantsWereGood: Boolean,
    oem: OemFamily,
    onPhotos: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val openPerms = {
        LivePermissions.startSafe(
            context,
            HermesSettingsActivity.intent(context, SettingsSection.PERMISSIONS),
        )
    }
    GrantChrome(
        snapshot = snapshot,
        grantsWereGood = grantsWereGood,
        oem = oem,
        onNotification = { GrantCatalog.open(context, GrantKind.LISTENER) },
        onBattery = { GrantCatalog.open(context, GrantKind.BATTERY) },
        onHome = { GrantCatalog.open(context, GrantKind.HOME) },
        onPhotos = onPhotos,
        onUsage = { GrantCatalog.open(context, GrantKind.USAGE) },
        onPost = { GrantCatalog.open(context, GrantKind.POST) },
        onAllGrants = openPerms,
        onRepair = openPerms,
        onLater = onLater,
        modifier = modifier,
    )
}
