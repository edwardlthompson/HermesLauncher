package org.hermeslauncher.app.feeds

/** One-shot seed URLs. Bump [SEED] to push a new default without re-adding old ones. */
object DefaultFeeds {
    const val SEED: Int = 4
    const val ANDROID_AUTHORITY: String = "https://www.androidauthority.com/feed/"
    const val FDROID: String = "https://f-droid.org/feed.xml"

    fun urlsForSeed(from: Int): List<String> = buildList {
        if (from < 1) add(ANDROID_AUTHORITY)
        if (from < 2) add(FDROID)
    }
}
