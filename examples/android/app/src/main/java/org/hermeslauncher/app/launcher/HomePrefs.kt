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
private val USAGE_BANNER_DISMISSED = booleanPreferencesKey("usage_banner_dismissed")
private val DOUBLE_TAP = stringPreferencesKey("double_tap")

class HomePrefs(private val context: Context) {
    val showDots: Flow<Boolean> = context.homeDataStore.data.map { prefs ->
        prefs[SHOW_DOTS] ?: true
    }

    val usageBannerDismissed: Flow<Boolean> = context.homeDataStore.data.map { prefs ->
        prefs[USAGE_BANNER_DISMISSED] ?: false
    }

    val doubleTap: Flow<DoubleTapAction> = context.homeDataStore.data.map { prefs ->
        DoubleTapCodec.parse(prefs[DOUBLE_TAP])
    }

    suspend fun setShowDots(value: Boolean) {
        context.homeDataStore.edit { prefs -> prefs[SHOW_DOTS] = value }
    }

    suspend fun setUsageBannerDismissed(value: Boolean) {
        context.homeDataStore.edit { prefs -> prefs[USAGE_BANNER_DISMISSED] = value }
    }

    suspend fun setDoubleTap(value: DoubleTapAction) {
        context.homeDataStore.edit { prefs -> prefs[DOUBLE_TAP] = DoubleTapCodec.encode(value) }
    }
}
