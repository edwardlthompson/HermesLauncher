package org.hermeslauncher.app.feeds

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.feedDataStore by preferencesDataStore(name = "feed_urls")
private val KEY = stringSetPreferencesKey("urls")
private val SEED = intPreferencesKey("seed")
private val SUBS = stringPreferencesKey("subs_json")

class FeedStore(private val context: Context) {
    val subs: Flow<List<FeedSub>> = context.feedDataStore.data.map { prefs ->
        migrate(prefs[SUBS], prefs[KEY])
    }
    val urls: Flow<List<String>> = subs.map { rows -> rows.map { it.url }.sorted() }

    suspend fun snapshot(): List<FeedSub> = subs.first()

    suspend fun addAll(urls: Collection<String>) {
        val extra = FeedSubCodec.fromUrls(urls)
        if (extra.isEmpty()) {
            return
        }
        context.feedDataStore.edit { prefs ->
            val current = migrate(prefs[SUBS], prefs[KEY]).associateBy { it.url }.toMutableMap()
            extra.forEach { current.putIfAbsent(it.url, it) }
            prefs[SUBS] = FeedSubCodec.encode(current.values.toList())
        }
    }

    suspend fun replaceAll(urls: Collection<String>) {
        context.feedDataStore.edit { prefs ->
            val current = migrate(prefs[SUBS], prefs[KEY]).associateBy { it.url }
            val byCanon = current.mapKeys { FeedDiscover.canonicalize(it.key) }
            val next = urls.filter { FeedFetcher.isHttpUrl(it) }.distinct().map { url ->
                current[url] ?: byCanon[url]?.copy(url = url) ?: FeedSub(url = url)
            }
            prefs[SUBS] = FeedSubCodec.encode(next)
        }
    }

    suspend fun upsert(sub: FeedSub) {
        if (!FeedFetcher.isHttpUrl(sub.url)) {
            return
        }
        context.feedDataStore.edit { prefs ->
            val current = migrate(prefs[SUBS], prefs[KEY]).associateBy { it.url }.toMutableMap()
            current[sub.url] = sub
            prefs[SUBS] = FeedSubCodec.encode(current.values.toList())
        }
    }

    suspend fun seedIfNeeded() {
        context.feedDataStore.edit { prefs ->
            val from = prefs[SEED] ?: 0
            val extra = DefaultFeeds.urlsForSeed(from).filter { FeedFetcher.isHttpUrl(it) }
            val current = migrate(prefs[SUBS], prefs[KEY]).associateBy { it.url }.toMutableMap()
            extra.forEach { url -> current.putIfAbsent(url, FeedSub(url = url)) }
            if (from < DefaultFeeds.SEED) {
                val xml = runCatching {
                    context.assets.open("inoreader.opml").bufferedReader().use { it.readText() }
                }.getOrDefault("")
                InoreaderSeed.subs(xml).forEach { sub ->
                    val prior = current[sub.url]
                    if (prior == null) {
                        current[sub.url] = sub
                    } else if (prior.kind == SubKind.NEWS && sub.kind == SubKind.PODCAST) {
                        current[sub.url] = prior.copy(kind = SubKind.PODCAST, prefetch = false, tag = prior.tag.ifBlank { sub.tag })
                    }
                }
            }
            prefs[SUBS] = FeedSubCodec.encode(current.values.toList())
            prefs[SEED] = DefaultFeeds.SEED
        }
    }

    private fun migrate(json: String?, urls: Set<String>?): List<FeedSub> {
        val fromJson = FeedSubCodec.decode(json)
        if (fromJson.isNotEmpty()) {
            return fromJson
        }
        return FeedSubCodec.fromUrls(urls.orEmpty())
    }
}
