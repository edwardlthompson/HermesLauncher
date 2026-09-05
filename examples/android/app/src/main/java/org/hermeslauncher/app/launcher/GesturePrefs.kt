package org.hermeslauncher.app.launcher

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.gestureDataStore by preferencesDataStore(name = "gesture_prefs")
private val MAP = stringPreferencesKey("map")
private val SENSITIVITY = stringPreferencesKey("sensitivity")

class GesturePrefs(private val context: Context) {
    val map: Flow<Map<GestureSlot, LauncherAction>> = context.gestureDataStore.data.map { prefs ->
        GestureCodec.decodeMap(prefs[MAP])
    }

    val sensitivity: Flow<SwipeSensitivity> = context.gestureDataStore.data.map { prefs ->
        SwipeSensitivity.parse(prefs[SENSITIVITY])
    }

    suspend fun setAction(slot: GestureSlot, action: LauncherAction) {
        context.gestureDataStore.edit { prefs ->
            val current = GestureCodec.decodeMap(prefs[MAP]).toMutableMap()
            current[slot] = action
            prefs[MAP] = GestureCodec.encodeMap(current)
        }
    }

    suspend fun setSensitivity(value: SwipeSensitivity) {
        context.gestureDataStore.edit { prefs ->
            prefs[SENSITIVITY] = value.name
        }
    }
}
