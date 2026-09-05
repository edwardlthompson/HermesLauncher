package org.hermeslauncher.app.feeds

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

enum class ArticleTarget {
    LAUNCHER,
    BROWSER,
    CUSTOM_TAB,
    ;

    companion object {
        fun parse(raw: String?): ArticleTarget =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: LAUNCHER
    }
}

enum class ReaderMode {
    READING,
    FULL,
    WEB,
}

object ArticleOpen {
    fun browserIntent(url: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun openBrowser(context: Context, url: String): Boolean {
        if (!FeedFetcher.isHttpUrl(url)) {
            return false
        }
        val custom = runCatching {
            CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
            true
        }.getOrDefault(false)
        if (custom) {
            return true
        }
        return runCatching {
            context.startActivity(browserIntent(url))
            true
        }.getOrDefault(false)
    }
}
