package org.hermeslauncher.app.icons

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import org.hermeslauncher.app.BuildConfig

object AppCatalog {
    fun launchables(pm: PackageManager): List<LaunchableApp> {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(query, 0).mapNotNull { resolve ->
            val info = resolve.activityInfo ?: return@mapNotNull null
            if (info.packageName == BuildConfig.APPLICATION_ID) {
                return@mapNotNull null
            }
            LaunchableApp(
                packageName = info.packageName,
                activityName = info.name,
                label = resolve.loadLabel(pm).toString(),
            )
        }.sortedBy { it.label.lowercase() }
    }

    fun withLabels(pm: PackageManager, layout: DockLayout): DockLayout {
        val apps = launchables(pm).associateBy { "${it.packageName}/${it.activityName}" }
        val labeled = layout.assigned.mapValues { (_, app) ->
            apps["${app.packageName}/${app.activityName}"] ?: app
        }
        return layout.copy(assigned = labeled)
    }

    fun displayed(
        stored: DockLayout,
        usageApps: List<LaunchableApp>,
        usageOk: Boolean,
        launchables: List<LaunchableApp>,
    ): DockLayout {
        if (launchables.isEmpty()) {
            return stored
        }
        val byKey = launchables.associateBy { "${it.packageName}/${it.activityName}" }
        val labeled = stored.copy(
            assigned = stored.assigned.mapValues { (_, app) ->
                byKey["${app.packageName}/${app.activityName}"] ?: app
            },
        )
        if (labeled.mode != DockMode.USAGE) {
            return labeled
        }
        val fill = if (usageOk && usageApps.isNotEmpty()) {
            usageApps
        } else {
            launchables.take(labeled.slotCount)
        }
        return labeled.fillSlots(fill)
    }

    fun seeded(pm: PackageManager): DockLayout {
        val apps = launchables(pm).take(DockLayout.DEFAULT_SLOTS)
        val assigned = apps.mapIndexed { index, app -> index to app }.toMap()
        return DockLayout(assigned = assigned)
    }

    fun launch(context: Context, app: LaunchableApp) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }
}
