package org.hermeslauncher.app.icons

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.drawerDataStore by preferencesDataStore(name = "drawer_prefs")

private val HIDDEN = stringPreferencesKey("hidden_packages")
private val COLUMNS = intPreferencesKey("columns")
private val LIST_MODE = booleanPreferencesKey("list_mode")
private val SHOW_RAIL = booleanPreferencesKey("show_rail")

class DrawerPrefs(private val context: Context) {
    val snapshot: Flow<DrawerSnapshot> = context.drawerDataStore.data.map { prefs ->
        DrawerSnapshot(
            hidden = DrawerCodec.decodeHidden(prefs[HIDDEN]),
            columns = DrawerPolicy.columns(prefs[COLUMNS] ?: DrawerPolicy.COLUMNS_DEFAULT),
            listMode = prefs[LIST_MODE] ?: false,
            showRail = prefs[SHOW_RAIL] ?: true,
        )
    }

    suspend fun hide(packageName: String) {
        context.drawerDataStore.edit { prefs ->
            val next = DrawerPolicy.hide(DrawerCodec.decodeHidden(prefs[HIDDEN]), packageName)
            prefs[HIDDEN] = DrawerCodec.encodeHidden(next)
        }
    }

    suspend fun show(packageName: String) {
        context.drawerDataStore.edit { prefs ->
            val next = DrawerPolicy.show(DrawerCodec.decodeHidden(prefs[HIDDEN]), packageName)
            prefs[HIDDEN] = DrawerCodec.encodeHidden(next)
        }
    }

    suspend fun setColumns(value: Int) {
        context.drawerDataStore.edit { prefs ->
            prefs[COLUMNS] = DrawerPolicy.columns(value)
        }
    }

    suspend fun setListMode(value: Boolean) {
        context.drawerDataStore.edit { prefs -> prefs[LIST_MODE] = value }
    }

    suspend fun setShowRail(value: Boolean) {
        context.drawerDataStore.edit { prefs -> prefs[SHOW_RAIL] = value }
    }
}
