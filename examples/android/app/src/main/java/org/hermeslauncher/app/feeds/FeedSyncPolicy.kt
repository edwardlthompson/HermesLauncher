package org.hermeslauncher.app.feeds

object ScanInterval {
    val OPTIONS: List<Int> = listOf(0, 15, 30, 60, 180, 360, 720, 1440)

    fun clamp(raw: Int): Int {
        return OPTIONS.minByOrNull { option -> kotlin.math.abs(option - raw) } ?: 60
    }
}

data class ReaderSettings(
    val target: ArticleTarget = ArticleTarget.LAUNCHER,
    val mobileData: Boolean = true,
    val scanMinutes: Int = 60,
    val refreshOnOpen: Boolean = true,
    val showThumbs: Boolean = true,
    val onlyWhenCharging: Boolean = false,
    val newestFirst: Boolean = true,
    val chip: FeedChip = FeedChip.ALL,
    val bodyScale: Float = ReaderScale.DEFAULT,
    val sourceUrl: String? = null,
    val savedOnly: Boolean = false,
    val blocked: String = "",
    val imagePolicy: ImagePolicy = ImagePolicy.ALWAYS,
    val readOnScroll: Boolean = false,
    val openNextOnRead: Boolean = false,
)

object FeedSyncPolicy {
    fun allowAuto(
        online: Boolean,
        allowMobile: Boolean,
        cellular: Boolean,
        metered: Boolean,
        onlyCharging: Boolean,
        charging: Boolean,
    ): Boolean {
        if (!online) {
            return false
        }
        if (onlyCharging && !charging) {
            return false
        }
        return allowDownload(online, allowMobile, cellular, metered)
    }

    fun allowDownload(
        online: Boolean,
        allowMobile: Boolean,
        cellular: Boolean,
        metered: Boolean,
    ): Boolean {
        if (!online) {
            return false
        }
        if (!allowMobile && (cellular || metered)) {
            return false
        }
        return true
    }

    fun allowImages(
        online: Boolean,
        policy: ImagePolicy,
        cellular: Boolean,
        metered: Boolean,
    ): Boolean {
        return when (policy) {
            ImagePolicy.NEVER -> false
            ImagePolicy.ALWAYS -> online
            ImagePolicy.WIFI -> allowDownload(online, false, cellular, metered)
        }
    }
}
