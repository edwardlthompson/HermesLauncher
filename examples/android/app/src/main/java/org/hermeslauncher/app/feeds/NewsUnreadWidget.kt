package org.hermeslauncher.app.feeds

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.HermesLauncherActivity
import org.hermeslauncher.app.R

class NewsUnreadWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val count = runCatching {
            val app = context.applicationContext as HermesApplication
            runBlocking { FeedUnread.count(app.feeds.articleRows.first()) }
        }.getOrDefault(0)
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_news_unread)
            views.setTextViewText(R.id.widget_news_count, FeedUnread.label(count))
            val open = android.app.PendingIntent.getActivity(
                context,
                0,
                Intent(context, HermesLauncherActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_news_root, open)
            manager.updateAppWidget(id, views)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        id: Int,
        extras: android.os.Bundle?,
    ) {
        onUpdate(context, manager, intArrayOf(id))
    }
}
