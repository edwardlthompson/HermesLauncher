package org.hermeslauncher.app.l3

import com.android.launcher3.AppFilter
import com.android.launcher3.Launcher
import com.android.launcher3.allapps.AlphabeticalAppsList
import com.android.launcher3.folder.ClippedFolderIconLayoutRule
import com.android.launcher3.folder.Folder
import org.hermeslauncher.app.icons.DrawerPolicy
import org.hermeslauncher.app.icons.DrawerSnapshot
import org.hermeslauncher.app.workspace.FolderLid
import org.hermeslauncher.app.workspace.FolderSnapshot

object L3Drawer {
    fun apply(launcher: Launcher, snapshot: DrawerSnapshot, appRowCap: Boolean, folder: FolderSnapshot) {
        val hidden = snapshot.hidden
        AppFilter.sHiddenPackage = { pkg -> pkg in hidden }
        val columns = DrawerPolicy.chunkSize(snapshot.listMode, snapshot.columns)
        launcher.appsView.setHermesAppsPerRow(columns)
        AlphabeticalAppsList.sSearchAppCap = L3Grid.previewCap(appRowCap, columns)
        launcher.appsView.setHermesRailVisible(snapshot.showRail)
        Folder.sFullscreenFolders = folder.fullscreen
        ClippedFolderIconLayoutRule.MAX_NUM_ITEMS_IN_PREVIEW = FolderLid.previewCap(folder.preview)
    }
}
