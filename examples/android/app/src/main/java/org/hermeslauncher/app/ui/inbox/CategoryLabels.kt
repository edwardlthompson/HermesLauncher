package org.hermeslauncher.app.ui.inbox

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R

@Composable
fun categoryLabel(key: String): String {
    val res = when (key) {
        "game" -> R.string.category_game
        "audio" -> R.string.category_audio
        "video" -> R.string.category_video
        "image" -> R.string.category_image
        "social" -> R.string.category_social
        "news" -> R.string.category_news
        "maps" -> R.string.category_maps
        "productivity" -> R.string.category_productivity
        "accessibility" -> R.string.category_accessibility
        "other" -> R.string.category_other
        else -> return key
    }
    return stringResource(res)
}
