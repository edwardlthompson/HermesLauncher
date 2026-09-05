package org.hermeslauncher.app.launcher

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.homeDataStore by preferencesDataStore(name = "home_prefs")

private val SHOW_DOTS = booleanPreferencesKey("show_dots")
private val SHOW_LABELS = booleanPreferencesKey("show_labels")
private val USAGE_BANNER_DISMISSED = booleanPreferencesKey("usage_banner_dismissed")
private val DOUBLE_TAP = stringPreferencesKey("double_tap")
private val NOVA_IMPORT = booleanPreferencesKey("nova_import_done")

class HomePrefs(private val context: Context) {
    val showDots: Flow<Boolean> = context.homeDataStore.data.map { prefs ->
        prefs[SHOW_DOTS] ?: true
    }

    val showLabels: Flow<Boolean> = context.homeDataStore.data.map { prefs ->
        prefs[SHOW_LABELS] ?: true
    }

    val usageBannerDismissed: Flow<Boolean> = context.homeDataStore.data.map { prefs ->
        prefs[USAGE_BANNER_DISMISSED] ?: false
    }

    val doubleTap: Flow<DoubleTapAction> = context.homeDataStore.data.map { prefs ->
        DoubleTapCodec.parse(prefs[DOUBLE_TAP])
    }

    val novaImportDone: Flow<Boolean> = context.homeDataStore.data.map { prefs ->
        prefs[NOVA_IMPORT] ?: false
    }

    suspend fun setShowDots(value: Boolean) {
        context.homeDataStore.edit { prefs -> prefs[SHOW_DOTS] = value }
    }

    suspend fun setShowLabels(value: Boolean) {
        context.homeDataStore.edit { prefs -> prefs[SHOW_LABELS] = value }
    }

    suspend fun setUsageBannerDismissed(value: Boolean) {
        context.homeDataStore.edit { prefs -> prefs[USAGE_BANNER_DISMISSED] = value }
    }

    suspend fun setDoubleTap(value: DoubleTapAction) {
        context.homeDataStore.edit { prefs -> prefs[DOUBLE_TAP] = DoubleTapCodec.encode(value) }
    }

    suspend fun setNovaImportDone(value: Boolean) {
        context.homeDataStore.edit { prefs -> prefs[NOVA_IMPORT] = value }
    }
}
