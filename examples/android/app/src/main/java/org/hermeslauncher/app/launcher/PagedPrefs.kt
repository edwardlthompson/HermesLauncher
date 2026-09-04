package org.hermeslauncher.app.launcher

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hermeslauncher.app.workspace.LabsFlags
import org.hermeslauncher.app.workspace.PinchTarget
import org.hermeslauncher.app.workspace.QsbPlacement
import org.hermeslauncher.app.workspace.ScrollMode

private val Context.pagedDataStore by preferencesDataStore(name = "paged_prefs")
private val WRAP = booleanPreferencesKey("labs_wrap")
private val OVERLAP = booleanPreferencesKey("labs_overlap")
private val QSB = stringPreferencesKey("qsb")
private val SCROLL = stringPreferencesKey("scroll")
private val PINCH = stringPreferencesKey("pinch")

class PagedPrefs(private val context: Context) {
    val labs: Flow<LabsFlags> = context.pagedDataStore.data.map { prefs ->
        LabsFlags(wrap = prefs[WRAP] ?: false, overlap = prefs[OVERLAP] ?: false)
    }

    val qsb: Flow<QsbPlacement> = context.pagedDataStore.data.map { prefs ->
        runCatching { QsbPlacement.valueOf(prefs[QSB] ?: QsbPlacement.NONE.name) }
            .getOrDefault(QsbPlacement.NONE)
    }

    val scrollMode: Flow<ScrollMode> = context.pagedDataStore.data.map { prefs ->
        runCatching { ScrollMode.valueOf(prefs[SCROLL] ?: ScrollMode.ADJACENT.name) }
            .getOrDefault(ScrollMode.ADJACENT)
    }

    val pinch: Flow<PinchTarget> = context.pagedDataStore.data.map { prefs ->
        runCatching { PinchTarget.valueOf(prefs[PINCH] ?: PinchTarget.ALL_APPS.name) }
            .getOrDefault(PinchTarget.ALL_APPS)
    }

    suspend fun setWrap(value: Boolean) {
        context.pagedDataStore.edit { prefs -> prefs[WRAP] = value }
    }

    suspend fun setOverlap(value: Boolean) {
        context.pagedDataStore.edit { prefs -> prefs[OVERLAP] = value }
    }

    suspend fun setQsb(value: QsbPlacement) {
        context.pagedDataStore.edit { prefs -> prefs[QSB] = value.name }
    }

    suspend fun setScrollMode(value: ScrollMode) {
        context.pagedDataStore.edit { prefs -> prefs[SCROLL] = value.name }
    }

    suspend fun setPinch(value: PinchTarget) {
        context.pagedDataStore.edit { prefs -> prefs[PINCH] = value.name }
    }
}
