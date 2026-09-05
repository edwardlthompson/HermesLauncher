package org.hermeslauncher.app.feeds

import android.text.Html.ImageGetter
import androidx.core.text.HtmlCompat

object ReaderHtml {
    val noopImages: ImageGetter = ImageGetter { null }

    fun fromHtml(html: String): CharSequence {
        if (html.isBlank()) {
            return ""
        }
        return runCatching {
            HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT, noopImages, null)
        }.getOrDefault(html)
    }

    fun imageGetterCalled(): Boolean = false
}
