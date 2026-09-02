package org.hermeslauncher.app.wallpaper

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.core.graphics.ColorUtils

object WallpaperTheme {
    fun primary(context: Context): Int = color(context, android.R.attr.colorPrimary)

    fun secondary(context: Context): Int = color(context, android.R.attr.colorAccent)

    fun onPrimary(context: Context): Int = color(context, android.R.attr.textColorPrimaryInverse)

    fun onBackground(context: Context): Int = color(context, android.R.attr.textColorPrimary)

    fun scrim(context: Context): Int {
        return ColorUtils.setAlphaComponent(color(context, android.R.attr.colorBackground), 230)
    }

    private fun color(context: Context, attr: Int): Int {
        val tv = TypedValue()
        val found = context.theme.resolveAttribute(attr, tv, true)
        if (!found) {
            return Color.BLACK
        }
        return if (tv.resourceId != 0) {
            context.getColor(tv.resourceId)
        } else {
            tv.data
        }
    }
}
