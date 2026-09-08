package org.hermeslauncher.app.icons

import com.android.launcher3.icons.IconCache

/** Dark/light backplates so unmatched icons are not stuck on a white square. */
object IconPlate {
    const val LIGHT: Int = 0xFFF5F5F5.toInt()
    const val DARK: Int = 0xFF1C1B1F.toInt()

    @JvmField
    @Volatile
    var color: Int = LIGHT

    fun colorFor(dark: Boolean): Int {
        return if (dark) DARK else LIGHT
    }

    fun apply(dark: Boolean) {
        color = colorFor(dark)
        IconCache.sWrapperBackground = color
    }
}
