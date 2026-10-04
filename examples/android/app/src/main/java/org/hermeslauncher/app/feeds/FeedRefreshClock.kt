package org.hermeslauncher.app.feeds

import android.content.Context

object FeedRefreshClock {
    private const val PREFS = "hermes_feed_refresh"
    private const val KEY_LAST = "last_refresh_at"

    fun lastRefreshAt(context: Context): Long {
        return context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST, 0L)
    }

    fun markRefreshed(context: Context, at: Long = System.currentTimeMillis()) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST, at)
            .commit()
    }
}
