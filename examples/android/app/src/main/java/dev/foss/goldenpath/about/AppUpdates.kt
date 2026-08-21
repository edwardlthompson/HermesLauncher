package dev.foss.goldenpath.about

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppUpdates {
    sealed class LaunchPrompt {
        data object Donate : LaunchPrompt()
        data class Update(val version: String, val url: String) : LaunchPrompt()
    }

    fun decide(
        current: String,
        lastSeen: String?,
        lastCheckAt: Long?,
        dismissed: String?,
        now: Long,
        release: GithubRelease.Parsed?,
        prefix: String,
        fallbackUrl: String,
    ): LaunchPrompt? {
        if (ProductUpdate.shouldNudgeDonate(lastSeen, current)) return LaunchPrompt.Donate
        if (!ProductUpdate.shouldCheckDaily(lastCheckAt, now)) return null
        if (release == null) return null
        val asset = ProductUpdate.selectProductAsset(release.assets, prefix, apk = true)
        val latest = asset?.version
        if (!ProductUpdate.shouldPromptUpdate(current, latest, dismissed) || latest == null) return null
        val url = asset.url.ifBlank { null } ?: release.htmlUrl.ifBlank { fallbackUrl }
        return LaunchPrompt.Update(latest, url)
    }

    suspend fun onLaunch(
        context: Context,
        current: String,
        now: Long = System.currentTimeMillis(),
    ): LaunchPrompt? {
        val prefs = UpdateLaunchPrefs(context)
        if (ProductUpdate.shouldNudgeDonate(prefs.lastSeenVersion(), current)) {
            return LaunchPrompt.Donate
        }
        prefs.markVersionSeen(current)
        if (!ProductUpdate.shouldCheckDaily(prefs.lastCheckAt(), now)) return null
        val repo = ReleaseTagFetcher.loadReleaseRepo(context)
        if (repo == null) {
            prefs.markChecked(now)
            return null
        }
        val prefix = ReleaseTagFetcher.loadProductAssetPrefix(context)
        val release = withContext(Dispatchers.IO) {
            GithubRelease.fetchLatest(
                "https://api.github.com/repos/$repo/releases/latest",
                "GoldenPath/$current",
            )
        }
        prefs.markChecked(now)
        return decide(
            current = current,
            lastSeen = current,
            lastCheckAt = null,
            dismissed = prefs.dismissedVersion(),
            now = now,
            release = release,
            prefix = prefix,
            fallbackUrl = "https://github.com/$repo/releases/latest",
        )
    }
}
