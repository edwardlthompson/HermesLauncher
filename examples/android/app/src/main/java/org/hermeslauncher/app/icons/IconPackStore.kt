package org.hermeslauncher.app.icons

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.iconPackDataStore by preferencesDataStore(name = "icon_pack")
private val KEY = stringPreferencesKey("pack")

class IconPackStore(private val context: Context) {
    val pack: Flow<IconPackId> = context.iconPackDataStore.data.map { prefs ->
        IconPackId(prefs[KEY]?.takeIf { it.isNotBlank() && it != IconPackResolver.SYSTEM_PACK })
    }

    suspend fun save(pack: IconPackId) {
        context.iconPackDataStore.edit { prefs ->
            prefs[KEY] = pack.packageName?.takeIf { it.isNotBlank() } ?: IconPackResolver.SYSTEM_PACK
        }
    }
}
