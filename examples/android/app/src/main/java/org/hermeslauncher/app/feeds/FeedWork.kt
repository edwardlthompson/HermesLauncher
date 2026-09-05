package org.hermeslauncher.app.feeds

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.hermeslauncher.app.HermesApplication
import java.util.concurrent.TimeUnit

object FeedWork {
    const val UNIQUE: String = "hermes-feed-sync"
    @Volatile
    var registered: Boolean = false

    fun enqueue(context: Context, minutes: Int) {
        registered = true
        val mgr = WorkManager.getInstance(context.applicationContext)
        if (minutes <= 0) {
            mgr.cancelUniqueWork(UNIQUE)
            return
        }
        val request = PeriodicWorkRequestBuilder<FeedSyncWorker>(minutes.toLong().coerceAtLeast(15), TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        mgr.enqueueUniquePeriodicWork(UNIQUE, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun loopShouldRefresh(scanMinutes: Int, workOn: Boolean): Boolean {
        return scanMinutes > 0 && !workOn
    }
}

class FeedSyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val app = applicationContext as? HermesApplication ?: return Result.success()
        return runCatching {
            kotlinx.coroutines.runBlocking {
                val prefs = app.readerPrefs.settingsFirst()
                app.feeds.expire()
                if (FeedSync.allowAuto(app, prefs)) {
                    app.feeds.refresh()
                }
            }
            Result.success()
        }.getOrDefault(Result.retry())
    }
}
