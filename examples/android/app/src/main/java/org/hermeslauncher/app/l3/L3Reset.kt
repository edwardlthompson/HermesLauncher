package org.hermeslauncher.app.l3

import android.content.Context
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings

/** Settings → Backup → Reset home layout also wipes the Launcher3 favorites DB. */
object L3Reset {
    fun homeLayout(context: Context) {
        L3Dock.forgetUsage()
        val cr = context.contentResolver
        LauncherSettings.Settings.call(cr, LauncherSettings.Settings.METHOD_CREATE_EMPTY_DB)
        LauncherSettings.Settings.call(cr, LauncherSettings.Settings.METHOD_LOAD_DEFAULT_FAVORITES)
        val launcher = Launcher.ACTIVITY_TRACKER.getCreatedActivity<Launcher>()
        launcher?.model?.forceReload()
    }
}
