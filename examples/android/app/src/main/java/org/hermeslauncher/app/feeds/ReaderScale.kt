package org.hermeslauncher.app.feeds

object ReaderScale {
    const val MIN: Float = 0.85f
    const val MAX: Float = 1.6f
    const val DEFAULT: Float = 1.0f

    fun clamp(raw: Float): Float = raw.coerceIn(MIN, MAX)
}

enum class ImagePolicy {
    ALWAYS,
    WIFI,
    NEVER,
    ;

    companion object {
        fun parse(raw: String?): ImagePolicy =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: ALWAYS

        fun fromFlags(showThumbs: Boolean, mobileData: Boolean): ImagePolicy {
            return when {
                !showThumbs -> NEVER
                !mobileData -> WIFI
                else -> ALWAYS
            }
        }
    }

    fun showThumbs(): Boolean = this != NEVER

    fun mobileData(): Boolean = this == ALWAYS
}
