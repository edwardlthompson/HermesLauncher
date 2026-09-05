package org.hermeslauncher.app.vault

import android.app.Notification
import android.content.Context
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

object StatusBarNotificationMapper {
    fun map(context: Context, sbn: StatusBarNotification): PostedNotification {
        val notification = sbn.notification
        val extras = notification.extras
        val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
            notification,
        )
        val parts = style?.messages.orEmpty().map { message ->
            PostedMessagePart(
                sender = message.person?.name?.toString(),
                text = message.text?.toString(),
                timestamp = message.timestamp,
            )
        }
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: parts.lastOrNull()?.text
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()
        val summary = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()
        val imageBytes = NotificationBitmaps.jpeg(context, notification)
        val preview = VaultPreview(
            subText = subText,
            bigText = bigText,
            infoText = infoText,
            summaryText = summary,
        )
        return PostedNotification(
            sbnKey = sbn.key,
            packageName = sbn.packageName,
            channelId = notification.channelId,
            postedAt = sbn.postTime,
            title = title ?: style?.conversationTitle?.toString(),
            text = text,
            extrasJson = preview.encode(),
            conversationTitle = style?.conversationTitle?.toString(),
            type = when {
                parts.isNotEmpty() -> VaultItemType.MESSAGE
                imageBytes.isNotEmpty() -> VaultItemType.MEDIA
                else -> VaultItemType.OTHER
            },
            priority = notification.priority,
            imageByteSize = imageBytes.size.toLong(),
            imageBytes = imageBytes,
            messageParts = parts,
            ongoing = sbn.isOngoing,
            groupSummary = notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
        )
    }
}
