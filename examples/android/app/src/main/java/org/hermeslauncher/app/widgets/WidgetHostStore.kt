package org.hermeslauncher.app.widgets

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.widgetDataStore by preferencesDataStore(name = "widget_host")
private val KEY = stringPreferencesKey("host_state")

class WidgetHostStore(private val context: Context) {
    val state: Flow<WidgetHostState> = context.widgetDataStore.data.map { prefs ->
        WidgetHostCodec.decode(prefs[KEY].orEmpty())
    }

    suspend fun save(state: WidgetHostState) {
        context.widgetDataStore.edit { prefs ->
            prefs[KEY] = WidgetHostCodec.encode(state)
        }
    }
}
