package org.hermeslauncher.app.launcher

/** How far a vertical swipe must travel before the app drawer or mapped action fires. */
enum class SwipeSensitivity {
    LOW,
    MEDIUM,
    HIGH,
    ;

    fun slopMultiplier(): Float = when (this) {
        LOW -> 4.5f
        MEDIUM -> 2.5f
        HIGH -> 1f
    }

    fun emptySpacePx(): Float = when (this) {
        LOW -> 160f
        MEDIUM -> 96f
        HIGH -> 48f
    }

    companion object {
        val DEFAULT: SwipeSensitivity = MEDIUM

        fun parse(raw: String?): SwipeSensitivity {
            return entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: DEFAULT
        }
    }
}
