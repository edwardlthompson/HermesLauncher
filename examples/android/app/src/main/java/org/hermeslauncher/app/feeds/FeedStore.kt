package org.hermeslauncher.app.feeds

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.feedDataStore by preferencesDataStore(name = "feed_urls")
private val KEY = stringSetPreferencesKey("urls")

class FeedStore(private val context: Context) {
    val urls: Flow<List<String>> = context.feedDataStore.data.map { prefs ->
        prefs[KEY].orEmpty().sorted()
    }

    suspend fun addAll(urls: Collection<String>) {
        val valid = urls.filter { FeedFetcher.isHttpUrl(it) }.toSet()
        if (valid.isEmpty()) {
            return
        }
        context.feedDataStore.edit { prefs ->
            prefs[KEY] = prefs[KEY].orEmpty() + valid
        }
    }
}
