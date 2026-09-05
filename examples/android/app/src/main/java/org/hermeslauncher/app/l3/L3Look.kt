package org.hermeslauncher.app.l3

import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherPrefs
import com.android.launcher3.graphics.IconShape
import com.android.launcher3.icons.IconCache
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.IconPackResources
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.ui.theme.IconShape as HermesShape
import org.hermeslauncher.app.widgets.WidgetGridSpec

object L3Look {
    fun applyGrid(launcher: Launcher, spec: WidgetGridSpec) {
        val idp = InvariantDeviceProfile.INSTANCE.get(launcher)
        val options = idp.parseAllGridOptions(launcher).map { option ->
            GridChoice(option.name, option.numColumns, option.numRows)
        }
        val pick = L3Grid.pick(spec.columns, spec.rows, options) ?: return
        val current = InvariantDeviceProfile.getCurrentGridName(launcher)
        if (pick.name != current) {
            idp.setCurrentGrid(launcher, pick.name)
        }
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
        IconCache.sIconOverride = IconCache.IconOverride { info ->
            if (pack.isSystem) {
                null
            } else {
                val cn = info.componentName
                if (cn == null) {
                    null
                } else {
                    IconPackResources.drawable(
                        launcher,
                        pack,
                        LaunchableApp(cn.packageName, cn.className, info.label?.toString() ?: cn.packageName),
                    )
                }
            }
        }
    }

    fun applyThemedIcons(launcher: Launcher, wallpaperPalette: Boolean) {
        LauncherPrefs.get(launcher).put(LauncherPrefs.THEMED_ICONS, wallpaperPalette)
    }
}
