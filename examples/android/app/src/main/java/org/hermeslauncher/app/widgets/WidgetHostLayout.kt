package org.hermeslauncher.app.widgets

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WidgetHostLayout(
    private val host: HermesAppWidgetHost,
    private val store: WidgetHostStore,
    private val scope: CoroutineScope,
) {
    fun remove(pageIndex: Int, appWidgetId: Int) {
        runCatching { host.deleteAppWidgetId(appWidgetId) }
        scope.launch { store.save(store.state.first().withoutWidget(pageIndex, appWidgetId)) }
        Log.i(WidgetCatalog.TAG, "remove id=$appWidgetId page=$pageIndex")
    }

    fun applySpan(pageIndex: Int, next: WidgetBinding) {
        scope.launch {
            val before = store.state.first()
            val previous = before.page(pageIndex).bindings.firstOrNull { it.appWidgetId == next.appWidgetId }
            val state = before.applySpan(pageIndex, next)
            val updated = state.page(pageIndex).bindings.firstOrNull { it.appWidgetId == next.appWidgetId }
            if (previous == updated) return@launch
            store.save(state)
            Log.i(
                WidgetCatalog.TAG,
                "resize id=${next.appWidgetId} ${updated?.cellsW}x${updated?.cellsH} ${updated?.cellX},${updated?.cellY}",
            )
        }
    }

    fun relocate(fromPage: Int, toPage: Int, appWidgetId: Int, cellX: Int, cellY: Int) {
        scope.launch {
            store.save(store.state.first().relocate(fromPage, toPage, appWidgetId, cellX, cellY))
        }
        Log.i(WidgetCatalog.TAG, "relocate id=$appWidgetId to=$toPage $cellX,$cellY")
    }
}
