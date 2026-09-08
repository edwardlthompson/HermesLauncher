package org.hermeslauncher.app.l3

import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherPrefs
import com.android.launcher3.graphics.IconShape
import com.android.launcher3.icons.IconCache
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.IconPackResources
import org.hermeslauncher.app.icons.IconPlate
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.ui.theme.IconShape as HermesShape
import org.hermeslauncher.app.widgets.WidgetGridSpec

object L3Look {
    fun applyGrid(launcher: Launcher, spec: WidgetGridSpec) {
        val grid = spec.clamped()
        InvariantDeviceProfile.setHermesGrid(grid.columns, grid.rows)
        val idp = InvariantDeviceProfile.INSTANCE.get(launcher)
        if (!L3Grid.shouldReapply(idp.numColumns, idp.numRows, grid)) {
            return
        }
        idp.reapplyGrid(launcher)
    }

    fun applyShape(launcher: Launcher, shape: HermesShape) {
        when (shape) {
            HermesShape.SYSTEM -> IconShape.init(launcher)
            HermesShape.CIRCLE -> IconShape.overrideShape(IconShape.Circle())
            HermesShape.SQUARE -> IconShape.overrideShape(IconShape.RoundedSquare(0.15f))
            HermesShape.SQUIRCLE -> IconShape.overrideShape(IconShape.Squircle(0.32f))
            HermesShape.TEARDROP -> IconShape.overrideShape(IconShape.TearDrop(0.45f))
        }
    }

    fun applyPack(launcher: Launcher, pack: IconPackId) {
        IconCache.sPackStamp = "${pack.packageName.orEmpty()}|${IconPlate.color}|g6"
        IconCache.sWrapperBackground = IconPlate.color
        IconCache.sIconOverride = IconCache.IconOverride { info ->
            if (pack.isSystem) {
                null
            } else {
                val cn = info.componentName ?: return@IconOverride null
                IconPackResources.drawable(
                    launcher,
                    pack,
                    LaunchableApp(cn.packageName, cn.className, info.label?.toString() ?: cn.packageName),
                )
            }
        }
        IconCache.sIconFallback = IconCache.IconFallback { info, system ->
            val cn = info.componentName
            val app = if (cn == null) {
                LaunchableApp("", "", "")
            } else {
                LaunchableApp(cn.packageName, cn.className, info.label?.toString() ?: cn.packageName)
            }
            IconPackResources.adapt(launcher, pack, app, system)
        }
        IconCache.sPackageIcon = if (pack.isSystem) {
            null
        } else {
            IconCache.PackageIcon { pkg, system ->
                IconPackResources.visibleIcon(launcher, pack, pkg) ?: system
            }
        }
    }

    fun reloadIcons(launcher: Launcher) {
        val idp = InvariantDeviceProfile.INSTANCE.get(launcher)
        LauncherAppState.getInstance(launcher).iconCache.updateIconParams(
            idp.fillResIconDpi,
            idp.iconBitmapSize,
        )
        launcher.model.forceReload()
    }

    fun applyThemedIcons(launcher: Launcher, wallpaperPalette: Boolean) {
        LauncherPrefs.get(launcher).put(LauncherPrefs.THEMED_ICONS, wallpaperPalette)
    }
}
