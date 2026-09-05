package org.hermeslauncher.app.feeds

import java.time.Instant
import java.time.ZoneId

object ArticleStamp {
    fun format(epochMs: Long): String {
        if (epochMs <= 0L) {
            return ""
        }
        val local = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDate()
        return "%02d/%02d/%02d".format(local.year % 100, local.monthValue, local.dayOfMonth)
    }
}
