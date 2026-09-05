package org.hermeslauncher.app.ui.player

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReaderWeb(url: String, find: String = "", modifier: Modifier = Modifier) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        update = { view ->
            if (view.url != url) {
                view.loadUrl(url)
            }
            if (find.isBlank()) {
                view.clearMatches()
            } else {
                view.findAllAsync(find)
            }
        },
        onRelease = { it.destroy() },
        modifier = modifier,
    )
}
