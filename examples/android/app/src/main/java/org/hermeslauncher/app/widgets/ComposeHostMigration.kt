package org.hermeslauncher.app.widgets

import android.appwidget.AppWidgetHost
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.first

/** One-time move of Compose widget host off Launcher3's 1024 id. */
object ComposeHostMigration {
    private const val PREFS = "hermes_compose_host"
    private const val KEY_DONE = "host_2048_migrated"
    private const val TAG = "HermesComposeHost"

    fun needsMigration(context: Context): Boolean {
        return !context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DONE, false)
    }

    suspend fun runIfNeeded(context: Context, store: WidgetHostStore) {
        if (!needsMigration(context)) {
            return
        }
        val state = store.state.first()
        val hasBindings = state.pages.any { it.bindings.isNotEmpty() }
        if (hasBindings) {
            runCatching {
                AppWidgetHost(context.applicationContext, WidgetHostIds.LEGACY_COMPOSE_HOST_ID)
                    .deleteHost()
            }.onFailure { Log.w(TAG, "legacy compose host delete failed", it) }
            store.save(state.copy(pages = listOf(WidgetPageState(1))))
            Log.i(TAG, "cleared Compose-only widget bindings after host id split")
        }
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DONE, true)
            .commit()
    }
}
