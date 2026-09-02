package org.hermeslauncher.app.vault

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.inboxDataStore by preferencesDataStore(name = "inbox_prefs")

private val IGNORE_ONGOING = booleanPreferencesKey("ignore_ongoing")
private val STORE_PHOTOS = booleanPreferencesKey("store_photos")
private val MAX_ITEMS = intPreferencesKey("max_items")
private val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_days")
private val AUTO_DELETE = booleanPreferencesKey("auto_delete")

class InboxPrefs(private val context: Context) {
    val ignoreOngoing: Flow<Boolean> = context.inboxDataStore.data.map { prefs ->
        prefs[IGNORE_ONGOING] ?: true
    }

    val storePhotos: Flow<Boolean> = context.inboxDataStore.data.map { prefs ->
        prefs[STORE_PHOTOS] ?: true
    }

    val maxItems: Flow<Int> = context.inboxDataStore.data.map { prefs ->
        prefs[MAX_ITEMS] ?: VaultPrune.DEFAULT_MAX
    }

    val autoDeleteDays: Flow<Int> = context.inboxDataStore.data.map { prefs ->
        prefs[AUTO_DELETE_DAYS] ?: VaultPrune.DEFAULT_DAYS
    }

    val autoDelete: Flow<Boolean> = context.inboxDataStore.data.map { prefs ->
        prefs[AUTO_DELETE] ?: true
    }

    suspend fun setIgnoreOngoing(value: Boolean) {
        context.inboxDataStore.edit { prefs -> prefs[IGNORE_ONGOING] = value }
    }

    suspend fun setStorePhotos(value: Boolean) {
        context.inboxDataStore.edit { prefs -> prefs[STORE_PHOTOS] = value }
    }

    suspend fun setMaxItems(value: Int) {
        context.inboxDataStore.edit { prefs -> prefs[MAX_ITEMS] = value.coerceIn(100, 5000) }
    }

    suspend fun setAutoDeleteDays(value: Int) {
        context.inboxDataStore.edit { prefs -> prefs[AUTO_DELETE_DAYS] = value.coerceIn(1, 365) }
    }

    suspend fun setAutoDelete(value: Boolean) {
        context.inboxDataStore.edit { prefs -> prefs[AUTO_DELETE] = value }
    }
}
