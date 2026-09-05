package org.hermeslauncher.app.ui.launcher

import android.Manifest
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import org.hermeslauncher.app.launcher.GestureMap
import org.hermeslauncher.app.launcher.GestureRunner
import org.hermeslauncher.app.launcher.GestureSlot
import org.hermeslauncher.app.launcher.LauncherAction
import org.hermeslauncher.app.workspace.PinchTarget

/** Fires a gesture-slot action; pinch+NONE+overview opens the home menu. */
fun fireHomeGesture(
    context: Context,
    slot: GestureSlot,
    map: Map<GestureSlot, LauncherAction>,
    pinch: PinchTarget,
    camera: ManagedActivityResultLauncher<String, Boolean>,
    openDrawer: () -> Unit,
    openSearch: () -> Unit,
    openOverview: () -> Unit,
) {
    val action = GestureMap.action(slot, map)
    if (slot == GestureSlot.PINCH && action == LauncherAction.NONE && pinch == PinchTarget.OVERVIEW) {
        openOverview()
        return
    }
    GestureRunner.run(
        context, action,
        onDrawer = openDrawer,
        onSearch = openSearch,
        requestCamera = { camera.launch(Manifest.permission.CAMERA) },
    )
}
