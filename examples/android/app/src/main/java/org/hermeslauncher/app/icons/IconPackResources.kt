package org.hermeslauncher.app.icons

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat

object IconPackResources {
    private val THEME_ACTIONS = listOf(
        "org.adw.launcher.THEMES",
        "com.novalauncher.THEME",
        "com.teslacoilsw.launcher.THEME",
        "com.anddoes.launcher.THEME",
        "com.gau.go.launcherex.theme",
        "ginlemon.smartlauncher.THEMES",
    )

    fun installedPacks(pm: PackageManager): List<IconPackId> {
        return THEME_ACTIONS.flatMap { action ->
            runCatching { pm.queryIntentActivities(Intent(action), 0) }.getOrDefault(emptyList())
        }.map { resolve -> IconPackId(resolve.activityInfo.packageName) }
            .distinctBy { it.packageName }
    }

    fun drawable(context: Context, pack: IconPackId, app: LaunchableApp): Drawable? {
        if (pack.isSystem) {
            return null
        }
        val packPkg = pack.packageName ?: return null
        return runCatching {
            val res = context.packageManager.getResourcesForApplication(packPkg)
            val name = IconPackFilter.nameFor(context, packPkg, app)
            val id = res.getIdentifier(name, "drawable", packPkg)
            if (id == 0) {
                null
            } else {
                ResourcesCompat.getDrawable(res, id, null)
            }
        }.getOrNull()
    }

    fun drawableForPackage(context: Context, pack: IconPackId, packageName: String): Drawable? {
        if (pack.isSystem || packageName.isBlank()) {
            return null
        }
        val activity = context.packageManager.getLaunchIntentForPackage(packageName)
            ?.component?.className.orEmpty()
        return drawable(context, pack, LaunchableApp(packageName, activity, packageName))
            ?: drawable(context, pack, LaunchableApp(packageName, "", packageName))
    }

    fun visibleIcon(context: Context, pack: IconPackId, packageName: String): Drawable? {
        if (packageName.isBlank()) {
            return null
        }
        val packed = drawableForPackage(context, pack, packageName)
        if (packed != null) {
            return packed
        }
        val system = runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
            ?: return null
        val app = LaunchableApp(packageName, "", packageName)
        return adapt(context, pack, app, system)
    }

    fun adapt(context: Context, pack: IconPackId, app: LaunchableApp, system: Drawable): Drawable {
        val size = system.intrinsicWidth.takeIf { it in 48..512 } ?: 192
        return IconPackAdapt.wrap(system, layers(context, pack), IconPlate.color, size)
    }

    private fun layers(context: Context, pack: IconPackId): IconPackLayers {
        val packPkg = pack.packageName ?: return IconPackLayers()
        if (pack.isSystem) {
            return IconPackLayers()
        }
        return runCatching {
            val chrome = IconPackFilter.mapsFor(context, packPkg).chrome
            val res = context.packageManager.getResourcesForApplication(packPkg)
            fun load(name: String?): Drawable? {
                if (name.isNullOrBlank()) {
                    return null
                }
                val id = res.getIdentifier(name, "drawable", packPkg)
                if (id == 0) {
                    return null
                }
                return ResourcesCompat.getDrawable(res, id, null)
            }
            IconPackLayers(
                back = load(chrome.backs.firstOrNull()),
                mask = load(chrome.masks.firstOrNull()),
                upon = load(chrome.upons.firstOrNull()),
                scale = chrome.scale,
            )
        }.getOrDefault(IconPackLayers())
    }

    fun drawableName(app: LaunchableApp): String {
        return "${app.packageName}_${app.activityName.substringAfterLast('.')}"
            .replace('.', '_')
            .lowercase()
    }
}
