package org.hermeslauncher.app.icons

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.launchRecencyStore by preferencesDataStore(name = "launch_recency")
private val TIMES = stringPreferencesKey("package_times")

object LaunchRecencyCodec {
    fun encode(times: Map<String, Long>): String {
        val json = JSONObject()
        times.forEach { (pkg, at) ->
            if (pkg.isNotBlank() && at > 0L) {
                json.put(pkg, at)
            }
        }
        return json.toString()
    }

    fun decode(raw: String?): Map<String, Long> {
        if (raw.isNullOrBlank()) {
            return emptyMap()
        }
        return runCatching {
            val json = JSONObject(raw)
            buildMap {
                json.keys().forEach { key ->
                    val at = json.optLong(key, 0L)
                    if (key.isNotBlank() && at > 0L) {
                        put(key, at)
                    }
                }
            }
        }.getOrDefault(emptyMap())
    }
}

class LaunchRecencyStore(private val context: Context) {
    suspend fun load(): Map<String, Long> {
        return context.launchRecencyStore.data.map { prefs ->
            LaunchRecencyCodec.decode(prefs[TIMES])
        }.first()
    }

    suspend fun write(packageName: String, at: Long) {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || at <= 0L) {
            return
        }
        context.launchRecencyStore.edit { prefs ->
            val next = LaunchRecencyCodec.decode(prefs[TIMES]).toMutableMap()
            next[pkg] = at
            prefs[TIMES] = LaunchRecencyCodec.encode(next)
        }
    }
}
