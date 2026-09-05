package org.hermeslauncher.app.feeds

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.articleDataStore by preferencesDataStore(name = "article_records")
private val KEY = stringPreferencesKey("records")

class ArticleStore(private val context: Context) {
    val records: Flow<List<ArticleRecord>> = context.articleDataStore.data.map { prefs ->
        ArticleCodec.decode(prefs[KEY])
    }

    suspend fun snapshot(): List<ArticleRecord> = records.first()

    suspend fun replaceAll(rows: List<ArticleRecord>) {
        persist(rows)
    }

    suspend fun mergeFetched(fetched: List<FeedItem>, now: Long): List<ArticleRecord> {
        val merged = FeedFilter.merge(records.first(), fetched, now)
        persist(merged)
        return merged
    }

    suspend fun markRead(id: String) = update(id) { it.copy(read = true, readAt = System.currentTimeMillis()) }

    suspend fun markUnread(id: String) = update(id) { it.copy(read = false, readAt = 0L) }

    suspend fun toggleStar(id: String) = update(id) { it.copy(starred = !it.starred) }

    suspend fun markAllRead(ids: Set<String>? = null) {
        val now = System.currentTimeMillis()
        persist(
            records.first().map { rec ->
                if (ids == null || rec.item.id in ids) rec.copy(read = true, readAt = now) else rec
            },
        )
    }

    private suspend fun update(id: String, transform: (ArticleRecord) -> ArticleRecord) {
        val next = records.first().map { rec ->
            if (rec.item.id == id) transform(rec) else rec
        }
        persist(next)
    }

    private suspend fun persist(records: List<ArticleRecord>) {
        context.articleDataStore.edit { prefs ->
            prefs[KEY] = ArticleCodec.encode(records)
        }
    }
}
