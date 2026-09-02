package org.hermeslauncher.app.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log

data class WidgetChoice(
    val provider: ComponentName,
    val appLabel: String,
    val widgetLabel: String,
)

object WidgetCatalog {
    const val TAG: String = "HermesWidget"

    fun sorted(choices: List<WidgetChoice>): List<WidgetChoice> {
        return choices.sortedWith(
            compareBy<WidgetChoice> { it.appLabel.lowercase() }
                .thenBy { it.widgetLabel.lowercase() },
        )
    }

    fun grouped(choices: List<WidgetChoice>): List<Pair<String, List<WidgetChoice>>> {
        return sorted(choices).groupBy { it.appLabel }.toList()
    }

    fun filter(choices: List<WidgetChoice>, query: String): List<WidgetChoice> {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) {
            return choices
        }
        return choices.filter { choice ->
            choice.appLabel.lowercase().contains(needle) ||
                choice.widgetLabel.lowercase().contains(needle)
        }
    }

    fun from(context: Context): List<WidgetChoice> {
        val pm = context.packageManager
        val manager = AppWidgetManager.getInstance(context)
        val choices = manager.installedProviders.map { info ->
            val appLabel = runCatching {
                pm.getApplicationLabel(pm.getApplicationInfo(info.provider.packageName, 0)).toString()
            }.getOrDefault(info.provider.packageName)
            val widgetLabel = info.loadLabel(pm).toString().ifBlank {
                info.provider.shortClassName
            }
            WidgetChoice(info.provider, appLabel, widgetLabel)
        }
        val sorted = sorted(choices)
        Log.i(TAG, "catalog size=${sorted.size}")
        return sorted
    }
}
