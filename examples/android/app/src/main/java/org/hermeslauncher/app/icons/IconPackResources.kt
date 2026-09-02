package org.hermeslauncher.app.icons

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

object IconPackResources {
    fun installedPacks(pm: PackageManager): List<IconPackId> {
        val query = Intent("org.adw.launcher.THEMES")
        return runCatching {
            pm.queryIntentActivities(query, 0).map { resolve ->
                IconPackId(resolve.activityInfo.packageName)
            }
        }.getOrDefault(emptyList())
    }

    fun drawable(context: Context, pack: IconPackId, app: LaunchableApp): Drawable? {
        if (pack.isSystem) {
            return null
        }
        val packPkg = pack.packageName ?: return null
        return runCatching {
            val res = context.packageManager.getResourcesForApplication(packPkg)
            val name = drawableName(app)
            val id = res.getIdentifier(name, "drawable", packPkg)
            if (id == 0) {
                null
            } else {
                androidx.core.content.res.ResourcesCompat.getDrawable(res, id, null)
            }
        }.getOrNull()
    }

    fun drawableName(app: LaunchableApp): String {
        return "${app.packageName}_${app.activityName.substringAfterLast('.')}"
            .replace('.', '_')
            .lowercase()
    }
}
