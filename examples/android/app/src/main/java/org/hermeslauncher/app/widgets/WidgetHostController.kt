package org.hermeslauncher.app.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hermeslauncher.app.R

class WidgetHostController(
    private val activity: ComponentActivity,
    private val host: HermesAppWidgetHost,
    private val store: WidgetHostStore,
    private val scope: CoroutineScope,
) {
    private var pendingPage = 1
    private var pendingId = 0
    private var pendingX = 0
    private var pendingY = 0
    private var pendingW = WidgetBinding.PLACE_CELLS
    private var pendingH = WidgetBinding.PLACE_CELLS_H
    private var inFlight = false
    private val layout = WidgetHostLayout(host, store, scope)
    private val _picker = MutableStateFlow<List<WidgetChoice>?>(null)
    val picker: StateFlow<List<WidgetChoice>?> = _picker

    private val bind = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) fail(pendingId, "bind") else maybeConfigure(pendingId)
    }

    private val configure = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) fail(pendingId, "configure") else record(pendingId)
    }

    fun openPicker(pageIndex: Int) {
        if (inFlight) return
        pendingPage = pageIndex
        val choices = WidgetCatalog.from(activity)
        Log.i(WidgetCatalog.TAG, "picker page=$pageIndex count=${choices.size}")
        if (choices.isEmpty()) {
            fail(0, "empty catalog")
            return
        }
        _picker.value = choices
    }

    fun drop(choice: WidgetChoice, pageIndex: Int, cellX: Int, cellY: Int) {
        if (inFlight) return
        scope.launch {
            val state = store.state.first()
            val candidate = WidgetGrid.dropCandidate(
                choice.provider.flattenToString(), cellX, cellY, state.grid,
            )
            if (!WidgetGrid.canPlace(state.page(pageIndex).bindings, candidate, state.grid)) {
                Log.i(WidgetCatalog.TAG, "occupancy miss page=$pageIndex x=${candidate.cellX} y=${candidate.cellY}")
                withContext(Dispatchers.Main) { Toast.makeText(activity, R.string.widget_drop_occupied, Toast.LENGTH_SHORT).show() }
                return@launch
            }
            inFlight = true
            _picker.value = null
            pendingPage = pageIndex
            pendingX = candidate.cellX; pendingY = candidate.cellY
            pendingW = candidate.cellsW; pendingH = candidate.cellsH
            pendingId = host.allocateAppWidgetId()
            Log.i(WidgetCatalog.TAG, "place id=$pendingId page=$pageIndex ${candidate.cellX},${candidate.cellY} ${candidate.cellsW}x${candidate.cellsH}")
            withContext(Dispatchers.Main) { bindProvider(choice.provider) }
        }
    }

    fun cancelPick() {
        _picker.value = null
        if (pendingId > 0 && inFlight) fail(pendingId, "cancelled")
    }

    fun remove(pageIndex: Int, appWidgetId: Int) = layout.remove(pageIndex, appWidgetId)
    fun applySpan(pageIndex: Int, binding: WidgetBinding) = layout.applySpan(pageIndex, binding)
    fun relocate(fromPage: Int, toPage: Int, appWidgetId: Int, cellX: Int, cellY: Int) =
        layout.relocate(fromPage, toPage, appWidgetId, cellX, cellY)

    private fun bindProvider(provider: ComponentName) {
        val id = pendingId
        val manager = AppWidgetManager.getInstance(activity)
        if (manager.bindAppWidgetIdIfAllowed(id, provider)) {
            maybeConfigure(id)
            return
        }
        val intent = WidgetBindIntents.bind(id, provider)
        if (intent.resolveActivity(activity.packageManager) == null) {
            fail(id, "no bind activity")
            return
        }
        Log.i(WidgetCatalog.TAG, "request bind id=$id")
        bind.launch(intent)
    }

    private fun maybeConfigure(id: Int) {
        val info = AppWidgetManager.getInstance(activity).getAppWidgetInfo(id)
        val target = info?.configure
        if (info == null) {
            fail(id, "no info")
            return
        }
        if (target == null) {
            record(id)
            return
        }
        val intent = WidgetBindIntents.configure(id, target)
        if (intent.resolveActivity(activity.packageManager) == null) {
            record(id)
            return
        }
        Log.i(WidgetCatalog.TAG, "configure $target")
        configure.launch(intent)
    }

    private fun record(id: Int) {
        val provider = AppWidgetManager.getInstance(activity).getAppWidgetInfo(id)?.provider?.flattenToString()
        Log.i(WidgetCatalog.TAG, "record id=$id provider=$provider page=$pendingPage")
        val binding = WidgetBinding(id, provider, pendingW, pendingH, pendingX, pendingY)
        scope.launch { store.save(store.state.first().withBinding(pendingPage, binding)) }
        inFlight = false
        pendingId = 0
        _picker.value = null
    }

    private fun fail(id: Int, reason: String) {
        Log.w(WidgetCatalog.TAG, "fail id=$id reason=$reason")
        if (id > 0) runCatching { host.deleteAppWidgetId(id) }
        inFlight = false
        pendingId = 0
        if (reason == "cancelled") return
        activity.runOnUiThread {
            Toast.makeText(activity, R.string.widget_bind_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
