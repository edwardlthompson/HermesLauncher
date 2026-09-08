package org.hermeslauncher.app.vault

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.inboxDataStore by preferencesDataStore(name = "inbox_prefs")

private val IGNORE_ONGOING = booleanPreferencesKey("ignore_ongoing")
private val STORE_PHOTOS = booleanPreferencesKey("store_photos")
private val MAX_ITEMS = intPreferencesKey("max_items")
private val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_days")
private val AUTO_DELETE = booleanPreferencesKey("auto_delete")
private val TRUNCATE_BODY = booleanPreferencesKey("truncate_body")
private val BODY_MAX_CHARS = intPreferencesKey("body_max_chars")
private val HIDE_SMALL_IMAGES = booleanPreferencesKey("hide_small_images")
private val INBOX_LAYOUT = stringPreferencesKey("inbox_layout")
private val NEWEST_FIRST = booleanPreferencesKey("newest_first")

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

    val truncateBody: Flow<Boolean> = context.inboxDataStore.data.map { prefs ->
        prefs[TRUNCATE_BODY] ?: true
    }

    val bodyMaxChars: Flow<Int> = context.inboxDataStore.data.map { prefs ->
        val truncate = prefs[TRUNCATE_BODY] ?: true
        if (!truncate) {
            InboxDisplay.ALL_CHARS
        } else {
            InboxDisplay.clampChars(prefs[BODY_MAX_CHARS] ?: InboxDisplay.DEFAULT_CHARS)
        }
    }

    val layout: Flow<InboxLayout> = context.inboxDataStore.data.map { prefs ->
        InboxLayout.entries.firstOrNull { it.name == prefs[INBOX_LAYOUT] } ?: InboxLayout.APP
    }

    val newestFirst: Flow<Boolean> = context.inboxDataStore.data.map { prefs ->
        prefs[NEWEST_FIRST] ?: true
    }

    val hideSmallImages: Flow<Boolean> = context.inboxDataStore.data.map { prefs ->
        prefs[HIDE_SMALL_IMAGES] ?: true
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

    suspend fun setTruncateBody(value: Boolean) {
        context.inboxDataStore.edit { prefs -> prefs[TRUNCATE_BODY] = value }
    }

    suspend fun setBodyMaxChars(value: Int) {
        val chars = InboxDisplay.clampChars(value)
        context.inboxDataStore.edit { prefs ->
            prefs[BODY_MAX_CHARS] = chars
            prefs[TRUNCATE_BODY] = chars > InboxDisplay.ALL_CHARS
        }
    }

    suspend fun setFilter(layout: InboxLayout, newestFirst: Boolean) {
        context.inboxDataStore.edit { prefs ->
            prefs[INBOX_LAYOUT] = layout.name
            prefs[NEWEST_FIRST] = newestFirst
        }
    }

    suspend fun setHideSmallImages(value: Boolean) {
        context.inboxDataStore.edit { prefs -> prefs[HIDE_SMALL_IMAGES] = value }
    }
}
