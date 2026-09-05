package org.hermeslauncher.app.workspace

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import org.hermeslauncher.app.widgets.HermesAppWidgetHost

/** Allocates a fresh host id for [providerFlattened]; null if provider missing or bind denied. */
object WidgetIdRemap {
    fun allocate(context: Context, host: HermesAppWidgetHost, providerFlattened: String): Int? {
        val component = ComponentName.unflattenFromString(providerFlattened) ?: return null
        val manager = AppWidgetManager.getInstance(context)
        val installed = manager.installedProviders.any {
            it.provider.packageName == component.packageName &&
                it.provider.className == component.className
        }
        if (!installed) return null
        val id = host.allocateAppWidgetId()
        val bound = runCatching { manager.bindAppWidgetIdIfAllowed(id, component) }.getOrDefault(false)
        if (!bound) {
            runCatching { host.deleteAppWidgetId(id) }
            return null
        }
        return id
    }
}
