package org.hermeslauncher.app.launcher

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.searchDataStore by preferencesDataStore(name = "search_prefs")

private val APP_ROW_CAP = booleanPreferencesKey("app_row_cap")

class SearchPrefs(private val context: Context) {
    val appRowCap: Flow<Boolean> = context.searchDataStore.data.map { prefs ->
        prefs[APP_ROW_CAP] ?: true
    }

    suspend fun setAppRowCap(value: Boolean) {
        context.searchDataStore.edit { prefs -> prefs[APP_ROW_CAP] = value }
    }
}
