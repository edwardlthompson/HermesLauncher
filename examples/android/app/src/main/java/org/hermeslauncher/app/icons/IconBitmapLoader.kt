package org.hermeslauncher.app.icons

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IconBitmapLoader<T : Any>(
    private val maxEntries: Int = DEFAULT_MAX,
) {
    private val cache = object : LinkedHashMap<String, T>(maxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, T>?): Boolean {
            return size > maxEntries
        }
    }

    suspend fun load(key: String, decode: suspend () -> T?): T? {
        if (key.isBlank()) {
            return null
        }
        synchronized(cache) { cache[key] }?.let { return it }
        val decoded = withContext(Dispatchers.IO) { decode() } ?: return null
        synchronized(cache) { cache.put(key, decoded) }
        return decoded
    }

    fun peek(key: String): T? {
        return synchronized(cache) { cache[key] }
    }

    fun clear(packPrefix: String? = null) {
        synchronized(cache) {
            if (packPrefix == null) {
                cache.clear()
            } else {
                cache.keys.filter { it.startsWith(packPrefix) }.forEach { cache.remove(it) }
            }
        }
    }

    companion object {
        const val DEFAULT_MAX: Int = 64

        fun key(pack: IconPackId, app: LaunchableApp): String {
            return "${pack.packageName.orEmpty()}|${app.packageName}|${app.activityName}"
        }
    }
}
