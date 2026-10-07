package org.hermeslauncher.app.feeds

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.hermeslauncher.app.HermesLauncherActivity
import org.hermeslauncher.app.R

object FeedNotify {
    const val CHANNEL: String = "hermes_feeds"
    const val EXTRA_ARTICLE_ID: String = "extra_article_id"
    const val EXTRA_SUB_KIND: String = "extra_sub_kind"

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 33) {
            return true
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val mgr = context.getSystemService(NotificationManager::class.java) ?: return
        if (mgr.getNotificationChannel(CHANNEL) != null) {
            return
        }
        mgr.createNotificationChannel(
            NotificationChannel(CHANNEL, context.getString(R.string.feed_notify_channel), NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    fun kindFor(rec: ArticleRecord, subs: List<FeedSub>): SubKind {
        val fromSub = subs.firstOrNull { it.url == rec.item.sourceUrl }?.kind
        if (fromSub != null) {
            return fromSub
        }
        return if (PodcastDetect.audioItem(rec.item)) SubKind.PODCAST else SubKind.NEWS
    }

    fun inboxLabelRes(kind: SubKind): Int {
        return if (kind == SubKind.PODCAST) {
            R.string.launcher_page_podcasts
        } else {
            R.string.launcher_page_news
        }
    }

    fun parseKind(raw: String?): SubKind {
        return runCatching { SubKind.valueOf(raw.orEmpty()) }.getOrDefault(SubKind.NEWS)
    }

    fun newUnread(before: List<ArticleRecord>, after: List<ArticleRecord>, notifyUrls: Set<String>): List<ArticleRecord> {
        val oldIds = before.map { it.item.id }.toSet()
        return after.filter { rec ->
            !rec.read && rec.item.id !in oldIds &&
                (notifyUrls.isEmpty() || rec.item.sourceUrl in notifyUrls)
        }.take(5)
    }

    fun post(context: Context, rows: List<ArticleRecord>, subs: List<FeedSub>): Boolean {
        if (rows.isEmpty() || !canPost(context)) {
            return false
        }
        return runCatching {
            ensureChannel(context)
            val mgr = NotificationManagerCompat.from(context)
            rows.forEachIndexed { index, rec ->
                val kind = kindFor(rec, subs)
                val open = Intent(context, HermesLauncherActivity::class.java)
                    .putExtra(EXTRA_ARTICLE_ID, rec.item.id)
                    .putExtra(EXTRA_SUB_KIND, kind.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                val pending = PendingIntent.getActivity(
                    context,
                    rec.item.id.hashCode(),
                    open,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                val note = NotificationCompat.Builder(context, CHANNEL)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(rec.item.title)
                    .setContentText(rec.item.feedTitle.ifBlank { context.getString(R.string.app_name) })
                    .setSubText(context.getString(inboxLabelRes(kind)))
                    .setContentIntent(pending)
                    .setAutoCancel(true)
                    .build()
                mgr.notify(7000 + index, note)
            }
            true
        }.getOrDefault(false)
    }
}
