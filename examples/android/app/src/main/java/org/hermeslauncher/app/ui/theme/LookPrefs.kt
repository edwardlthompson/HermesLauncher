package org.hermeslauncher.app.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.lookDataStore: DataStore<Preferences> by preferencesDataStore(name = "look_prefs")

enum class BadgeStyle {
    DOTS,
    COUNTS,
    ;

    companion object {
        fun parse(raw: String?): BadgeStyle =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: COUNTS
    }
}

class LookPrefs(private val context: Context) {
    val iconShape: Flow<IconShape> = context.lookDataStore.data.map {
        IconShape.parse(it[SHAPE_KEY])
    }

    val nightSchedule: Flow<NightSchedule> = context.lookDataStore.data.map { prefs ->
        val start = NightSchedule.parseTime(prefs[NIGHT_START_KEY]) ?: NightSchedule.DEFAULT_START
        val end = NightSchedule.parseTime(prefs[NIGHT_END_KEY]) ?: NightSchedule.DEFAULT_END
        NightSchedule(enabled = prefs[NIGHT_ON_KEY] == true, startMinute = start, endMinute = end)
    }

    val badgeStyle: Flow<BadgeStyle> = context.lookDataStore.data.map {
        BadgeStyle.parse(it[BADGE_STYLE_KEY])
    }

    val badgeColorArgb: Flow<Int?> = context.lookDataStore.data.map { it[BADGE_COLOR_KEY] }

    val labelShadow: Flow<Boolean> = context.lookDataStore.data.map {
        it[LABEL_SHADOW_KEY] != false
    }

    val wallpaperPalette: Flow<Boolean> = context.lookDataStore.data.map {
        it[WALLPAPER_PALETTE_KEY] == true
    }

    suspend fun setIconShape(shape: IconShape) {
        context.lookDataStore.edit { it[SHAPE_KEY] = shape.name }
    }

    suspend fun setNightSchedule(schedule: NightSchedule) {
        context.lookDataStore.edit { prefs ->
            prefs[NIGHT_ON_KEY] = schedule.enabled
            prefs[NIGHT_START_KEY] = NightSchedule.formatTime(schedule.startMinute)
            prefs[NIGHT_END_KEY] = NightSchedule.formatTime(schedule.endMinute)
        }
    }

    suspend fun setBadgeStyle(style: BadgeStyle) {
        context.lookDataStore.edit { it[BADGE_STYLE_KEY] = style.name }
    }

    suspend fun setBadgeColorArgb(argb: Int?) {
        context.lookDataStore.edit { prefs ->
            if (argb == null) prefs.remove(BADGE_COLOR_KEY) else prefs[BADGE_COLOR_KEY] = argb
        }
    }

    suspend fun setLabelShadow(on: Boolean) {
        context.lookDataStore.edit { it[LABEL_SHADOW_KEY] = on }
    }

    suspend fun setWallpaperPalette(on: Boolean) {
        context.lookDataStore.edit { it[WALLPAPER_PALETTE_KEY] = on }
    }

    companion object {
        private val SHAPE_KEY = stringPreferencesKey("icon_shape")
        private val NIGHT_ON_KEY = booleanPreferencesKey("night_on")
        private val NIGHT_START_KEY = stringPreferencesKey("night_start")
        private val NIGHT_END_KEY = stringPreferencesKey("night_end")
        private val BADGE_STYLE_KEY = stringPreferencesKey("badge_style")
        private val BADGE_COLOR_KEY = intPreferencesKey("badge_color")
        private val LABEL_SHADOW_KEY = booleanPreferencesKey("label_shadow")
        private val WALLPAPER_PALETTE_KEY = booleanPreferencesKey("wallpaper_palette")
    }
}
