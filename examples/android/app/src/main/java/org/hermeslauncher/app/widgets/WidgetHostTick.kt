package org.hermeslauncher.app.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import android.util.Log

object WidgetHostTick {
    const val KEY: String = "org.hermeslauncher.tick"

    fun positiveIds(ids: IntArray): IntArray {
        return ids.filter { it > 0 }.toIntArray()
    }

    fun poke(context: Context, ids: IntArray, now: Long = System.currentTimeMillis()) {
        val live = positiveIds(ids)
        if (live.isEmpty()) {
            return
        }
        val mgr = AppWidgetManager.getInstance(context) ?: return
        var n = 0
        for (id in live) {
            val opts = mgr.getAppWidgetOptions(id) ?: Bundle()
            opts.putLong(KEY, now)
            if (runCatching { mgr.updateAppWidgetOptions(id, opts) }.isSuccess) {
                n += 1
            }
        }
        Log.i(WidgetCatalog.TAG, "tick n=$n")
    }
}
