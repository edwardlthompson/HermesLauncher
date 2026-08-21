package dev.foss.goldenpath.about

object ProductUpdate {
    const val MS_DAY = 86_400_000L
    const val DEFAULT_ASSET_PREFIX = "Golden-Path"

    data class NamedAsset(val name: String, val url: String)
    data class ProductAsset(val version: String, val url: String)

    fun shouldCheckDaily(lastCheckAt: Long?, now: Long): Boolean {
        if (lastCheckAt == null || lastCheckAt < 0L) return true
        return now - lastCheckAt >= MS_DAY
    }

    fun isNewerVersion(current: String, latest: String): Boolean {
        fun parts(v: String) = v.split('.').map { it.toIntOrNull() ?: 0 }
        val a = parts(current)
        val b = parts(latest)
        for (i in 0..2) {
            val diff = (a.getOrElse(i) { 0 }) - (b.getOrElse(i) { 0 })
            if (diff != 0) return diff < 0
        }
        return false
    }

    fun parseAssetVersion(name: String, prefix: String, apk: Boolean = true): String? {
        val src = name.trim()
        val stem = prefix.trim()
        if (src.isEmpty() || stem.isEmpty()) return null
        val quoted = Regex.escape(if (apk) stem.lowercase() else stem)
        val pattern = if (apk) {
            "$quoted-(\\d+\\.\\d+\\.\\d+)-foss\\.apk"
        } else {
            "$quoted-(\\d+\\.\\d+\\.\\d+)"
        }
        return Regex(pattern, RegexOption.IGNORE_CASE).find(src)?.groupValues?.get(1)
    }

    fun selectProductAsset(assets: List<NamedAsset>, prefix: String, apk: Boolean = true): ProductAsset? {
        for (asset in assets) {
            val version = parseAssetVersion(asset.name, prefix, apk) ?: continue
            if (asset.url.isNotBlank()) return ProductAsset(version, asset.url)
        }
        return null
    }

    fun shouldNudgeDonate(lastSeenVersion: String?, currentVersion: String): Boolean {
        if (currentVersion.isBlank()) return false
        if (lastSeenVersion.isNullOrBlank()) return false
        return lastSeenVersion.trim() != currentVersion.trim()
    }

    fun shouldPromptUpdate(
        currentVersion: String,
        latestVersion: String?,
        dismissedVersion: String?,
    ): Boolean {
        if (latestVersion.isNullOrBlank()) return false
        if (!isNewerVersion(currentVersion, latestVersion)) return false
        if (dismissedVersion == latestVersion) return false
        return true
    }
}
