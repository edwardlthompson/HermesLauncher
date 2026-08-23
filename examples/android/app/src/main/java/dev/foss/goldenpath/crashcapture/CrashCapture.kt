package dev.foss.goldenpath.crashcapture

import android.content.Context
import dev.foss.goldenpath.feedback.FeedbackPrefs

object CrashCapture {
    @Volatile
    private var installed = false

    fun install(context: Context) {
        if (installed) return
        installed = true
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        val app = context.applicationContext
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            if (FeedbackPrefs(app).saveCrashes()) {
                runCatching {
                    PendingCrashStore(app).write(
                        PendingCrash(
                            message = error.javaClass.simpleName + ": " + (error.message ?: ""),
                            stack = error.stackTraceToString(),
                        ),
                    )
                }
            }
            previous?.uncaughtException(thread, error)
        }
    }
}
