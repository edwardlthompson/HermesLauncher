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

    fun drawableName(app: LaunchableApp): String {
        return "${app.packageName}_${app.activityName.substringAfterLast('.')}"
            .replace('.', '_')
            .lowercase()
    }
}
