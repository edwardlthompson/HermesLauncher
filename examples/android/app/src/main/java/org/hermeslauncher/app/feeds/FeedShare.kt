package org.hermeslauncher.app.feeds

import android.content.Intent

object FeedShare {
    fun intent(title: String, url: String): Intent? {
        if (!FeedFetcher.isHttpUrl(url)) {
            return null
        }
        val body = listOf(title.trim(), url.trim()).filter { it.isNotBlank() }.joinToString("\n")
        if (body.isBlank()) {
            return null
        }
        return Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, title.trim())
            .putExtra(Intent.EXTRA_TEXT, body)
    }
}
