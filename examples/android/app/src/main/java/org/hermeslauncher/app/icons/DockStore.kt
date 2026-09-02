package org.hermeslauncher.app.icons

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dockDataStore by preferencesDataStore(name = "dock_layout")
private val KEY = stringPreferencesKey("layout")

class DockStore(private val context: Context) {
    val layout: Flow<DockLayout> = context.dockDataStore.data.map { prefs ->
        DockCodec.decode(prefs[KEY].orEmpty())
    }

    suspend fun save(layout: DockLayout) {
        context.dockDataStore.edit { prefs ->
            prefs[KEY] = DockCodec.encode(layout)
        }
    }
}
