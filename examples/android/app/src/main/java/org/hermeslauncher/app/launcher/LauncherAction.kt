package org.hermeslauncher.app.launcher

enum class LauncherAction {
    NONE,
    DRAWER,
    SEARCH,
    LOCK,
    FLASHLIGHT,
    SHADE,
}

enum class GestureSlot {
    SWIPE_UP,
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    TWO_FINGER,
    PINCH,
}

object GestureMap {
    fun defaults(): Map<GestureSlot, LauncherAction> {
        return mapOf(
            GestureSlot.SWIPE_UP to LauncherAction.DRAWER,
            GestureSlot.SWIPE_DOWN to LauncherAction.SEARCH,
            GestureSlot.SWIPE_LEFT to LauncherAction.NONE,
            GestureSlot.SWIPE_RIGHT to LauncherAction.NONE,
            GestureSlot.TWO_FINGER to LauncherAction.NONE,
            GestureSlot.PINCH to LauncherAction.DRAWER,
        )
    }

    fun parse(raw: String?): LauncherAction {
        return LauncherAction.entries.firstOrNull { it.name == raw?.uppercase() } ?: LauncherAction.NONE
    }

    fun encode(action: LauncherAction): String = action.name

    fun action(slot: GestureSlot, overrides: Map<GestureSlot, LauncherAction> = emptyMap()): LauncherAction {
        return overrides[slot] ?: defaults().getValue(slot)
    }
}
