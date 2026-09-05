package org.hermeslauncher.app.l3

import org.hermeslauncher.app.icons.DockLayout
import org.hermeslauncher.app.icons.DrawerSnapshot
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.launcher.DoubleTapAction
import org.hermeslauncher.app.launcher.GestureMap
import org.hermeslauncher.app.launcher.GestureSlot
import org.hermeslauncher.app.launcher.LauncherAction
import org.hermeslauncher.app.launcher.SwipeSensitivity
import org.hermeslauncher.app.ui.theme.BadgeStyle
import org.hermeslauncher.app.ui.theme.IconShape
import org.hermeslauncher.app.ui.theme.NightSchedule
import org.hermeslauncher.app.ui.theme.ThemeMode
import org.hermeslauncher.app.widgets.WidgetGridSpec
import org.hermeslauncher.app.workspace.FolderSnapshot
import org.hermeslauncher.app.workspace.LabsFlags
import org.hermeslauncher.app.workspace.ScrollMode

/** Latest settings snapshot for touch controllers (updated off the UI thread). */
object L3Caches {
    @Volatile var sensitivity: SwipeSensitivity = SwipeSensitivity.DEFAULT
    @Volatile var gestureMap: Map<GestureSlot, LauncherAction> = GestureMap.defaults()
    @Volatile var doubleTap: DoubleTapAction = DoubleTapAction.OFF
    @Volatile var showLabels: Boolean = true
    @Volatile var showDots: Boolean = true
    @Volatile var labs: LabsFlags = LabsFlags()
    @Volatile var scrollMode: ScrollMode = ScrollMode.ADJACENT
    @Volatile var drawer: DrawerSnapshot = DrawerSnapshot()
    @Volatile var dock: DockLayout = DockLayout()
    @Volatile var folder: FolderSnapshot = FolderSnapshot()
    @Volatile var appRowCap: Boolean = true
    @Volatile var grid: WidgetGridSpec = WidgetGridSpec.DEFAULT
    @Volatile var iconPack: IconPackId = IconPackId()
    @Volatile var iconShape: IconShape = IconShape.SYSTEM
    @Volatile var night: NightSchedule = NightSchedule.OFF
    @Volatile var themeMode: ThemeMode = ThemeMode.System
    @Volatile var badgeStyle: BadgeStyle = BadgeStyle.COUNTS
    @Volatile var badgeColor: Int? = null
    @Volatile var labelShadow: Boolean = true
    @Volatile var wallpaperPalette: Boolean = false
    @Volatile var pendingSearchFocus: Boolean = false

    val actionUp: LauncherAction
        get() = GestureMap.action(GestureSlot.SWIPE_UP, gestureMap)

    val actionDown: LauncherAction
        get() = GestureMap.action(GestureSlot.SWIPE_DOWN, gestureMap)
}
