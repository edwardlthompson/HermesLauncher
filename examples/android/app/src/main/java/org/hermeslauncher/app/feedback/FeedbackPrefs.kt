package org.hermeslauncher.app.feedback

import android.content.Context
import org.hermeslauncher.app.crashcapture.PendingCrashStore

class FeedbackPrefs(private val context: Context) {
    fun saveCrashes(): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, false)

    fun setSaveCrashes(on: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, on).apply()
        if (!on) PendingCrashStore(context).clear()
    }

    companion object {
        const val PREFS = "gp_feedback"
        const val KEY = "save_crashes"
    }
}
