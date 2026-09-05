package org.hermeslauncher.app.ui.theme

import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import androidx.compose.ui.graphics.Color

/** AOSP [WallpaperColors] seed; miss → null (theme palette). */
object LookPalette {
    fun wallpaperSeed(context: Context): Color? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return null
        return runCatching {
            val colors = WallpaperManager.getInstance(context).getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                ?: return null
            Color(colors.primaryColor.toArgb())
        }.getOrNull()
    }
}
