package org.hermeslauncher.app.launcher

import android.content.Context
import org.hermeslauncher.app.oem.HomeActions

object GestureRunner {
    fun run(
        context: Context,
        action: LauncherAction,
        onDrawer: () -> Unit,
        onSearch: () -> Unit,
        requestCamera: () -> Unit,
    ) {
        when (action) {
            LauncherAction.NONE -> Unit
            LauncherAction.DRAWER -> onDrawer()
            LauncherAction.SEARCH -> onSearch()
            LauncherAction.LOCK -> HomeActions.lockOrPrompt(context)
            LauncherAction.FLASHLIGHT -> {
                if (!HomeActions.toggleTorch(context)) {
                    requestCamera()
                }
            }
            LauncherAction.SHADE -> expandShade(context)
        }
    }

    private fun expandShade(context: Context) {
        runCatching {
            val service = context.getSystemService(Context.STATUS_BAR_SERVICE) ?: return
            service.javaClass.getMethod("expandNotificationsPanel").invoke(service)
        }
    }
}
