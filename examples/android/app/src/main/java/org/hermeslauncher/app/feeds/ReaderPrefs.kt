package org.hermeslauncher.app.feeds

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.readerDataStore by preferencesDataStore(name = "reader_prefs")
private val TARGET = stringPreferencesKey("article_target")
private val MOBILE = booleanPreferencesKey("mobile_data")
private val SCAN = intPreferencesKey("scan_minutes")
private val OPEN_REFRESH = booleanPreferencesKey("refresh_on_open")
private val THUMBS = booleanPreferencesKey("show_thumbs")
private val CHARGING = booleanPreferencesKey("only_charging")
private val NEWEST = booleanPreferencesKey("newest_first")
private val CHIP = stringPreferencesKey("feed_chip")
private val SCALE = floatPreferencesKey("body_scale")
private val SOURCE = stringPreferencesKey("source_url")
private val SAVED = booleanPreferencesKey("saved_only")
private val BLOCKED = stringPreferencesKey("blocked")
private val POLICY = stringPreferencesKey("image_policy")
private val READ_SCROLL = booleanPreferencesKey("read_on_scroll")
private val OPEN_NEXT = booleanPreferencesKey("open_next_on_read")

class ReaderPrefs(private val context: Context) {
    val settings: Flow<ReaderSettings> = context.readerDataStore.data.map { prefs ->
        val policy = prefs[POLICY]?.let { ImagePolicy.parse(it) }
            ?: ImagePolicy.fromFlags(prefs[THUMBS] ?: true, prefs[MOBILE] ?: true)
        ReaderSettings(
            target = ArticleTarget.parse(prefs[TARGET]),
            mobileData = prefs[MOBILE] ?: policy.mobileData(),
            scanMinutes = ScanInterval.clamp(prefs[SCAN] ?: 60),
            refreshOnOpen = prefs[OPEN_REFRESH] ?: true,
            showThumbs = policy.showThumbs(),
            onlyWhenCharging = prefs[CHARGING] ?: false,
            newestFirst = prefs[NEWEST] ?: true,
            chip = FeedChip.entries.firstOrNull { it.name == prefs[CHIP] } ?: FeedChip.ALL,
            bodyScale = ReaderScale.clamp(prefs[SCALE] ?: ReaderScale.DEFAULT),
            sourceUrl = prefs[SOURCE]?.takeIf { it.isNotBlank() },
            savedOnly = prefs[SAVED] ?: false,
            blocked = prefs[BLOCKED].orEmpty(),
            imagePolicy = policy,
            readOnScroll = prefs[READ_SCROLL] ?: false,
            openNextOnRead = prefs[OPEN_NEXT] ?: false,
        )
    }
    val target: Flow<ArticleTarget> = settings.map { it.target }

    suspend fun settingsFirst(): ReaderSettings = settings.first()

    suspend fun setTarget(value: ArticleTarget) {
        context.readerDataStore.edit { prefs -> prefs[TARGET] = value.name }
    }

    suspend fun setMobileData(value: Boolean) {
        context.readerDataStore.edit { prefs -> prefs[MOBILE] = value }
    }

    suspend fun setScanMinutes(value: Int) {
        context.readerDataStore.edit { prefs -> prefs[SCAN] = ScanInterval.clamp(value) }
    }

    suspend fun setRefreshOnOpen(value: Boolean) {
        context.readerDataStore.edit { prefs -> prefs[OPEN_REFRESH] = value }
    }

    suspend fun setShowThumbs(value: Boolean) {
        context.readerDataStore.edit { prefs -> prefs[THUMBS] = value }
    }

    suspend fun setOnlyWhenCharging(value: Boolean) {
        context.readerDataStore.edit { prefs -> prefs[CHARGING] = value }
    }

    suspend fun setNewestFirst(value: Boolean) {
        context.readerDataStore.edit { prefs -> prefs[NEWEST] = value }
    }

    suspend fun setBodyScale(value: Float) {
        context.readerDataStore.edit { prefs -> prefs[SCALE] = ReaderScale.clamp(value) }
    }

    suspend fun setImagePolicy(value: ImagePolicy) {
        context.readerDataStore.edit { prefs ->
            prefs[POLICY] = value.name
            prefs[THUMBS] = value.showThumbs()
            prefs[MOBILE] = value.mobileData()
        }
    }

    suspend fun setBlocked(value: String) {
        context.readerDataStore.edit { prefs -> prefs[BLOCKED] = value }
    }

    suspend fun setListQuery(
        chip: FeedChip,
        newestFirst: Boolean,
        sourceUrl: String? = null,
        savedOnly: Boolean = false,
    ) {
        context.readerDataStore.edit { prefs ->
            prefs[CHIP] = chip.name
            prefs[NEWEST] = newestFirst
            if (sourceUrl.isNullOrBlank()) prefs.remove(SOURCE) else prefs[SOURCE] = sourceUrl
            prefs[SAVED] = savedOnly
        }
    }
}
