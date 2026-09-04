package org.hermeslauncher.app.workspace

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.desktopDataStore by preferencesDataStore(name = "desktop_items")
private val KEY = stringPreferencesKey("layout")

class DesktopStore(private val context: Context) {
    val layout: Flow<DesktopLayout> = context.desktopDataStore.data.map { prefs ->
        DesktopCodec.decode(prefs[KEY].orEmpty())
    }

    suspend fun save(layout: DesktopLayout) {
        context.desktopDataStore.edit { prefs ->
            prefs[KEY] = DesktopCodec.encode(layout)
        }
    }
}
