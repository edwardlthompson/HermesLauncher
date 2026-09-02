package org.hermeslauncher.app.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent

object WidgetBindIntents {
    fun bind(id: Int, provider: ComponentName): Intent {
        return Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }
    }

    fun configure(id: Int, target: ComponentName): Intent {
        return Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = target
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        }
    }
}
