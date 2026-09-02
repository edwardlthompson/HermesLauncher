package org.hermeslauncher.app.launcher

enum class DoubleTapAction {
    OFF,
    LOCK,
    FLASHLIGHT,
}

object DoubleTapCodec {
    fun parse(raw: String?): DoubleTapAction {
        return when (raw?.uppercase()) {
            "LOCK" -> DoubleTapAction.LOCK
            "FLASHLIGHT" -> DoubleTapAction.FLASHLIGHT
            else -> DoubleTapAction.OFF
        }
    }

    fun encode(action: DoubleTapAction): String {
        return action.name
    }
}

enum class HomePulseResult {
    SCROLL_INBOX,
    OPEN_SEARCH,
    CLOSE_SEARCH,
}

object HomePulse {
    fun isHome(action: String?, categories: Set<String>?): Boolean {
        if (action != "android.intent.action.MAIN") {
            return false
        }
        val cats = categories ?: return false
        return "android.intent.category.HOME" in cats ||
            "android.intent.category.LAUNCHER" in cats
    }

    fun next(page: Int, searchOpen: Boolean): HomePulseResult {
        return when {
            page > 0 -> HomePulseResult.SCROLL_INBOX
            searchOpen -> HomePulseResult.CLOSE_SEARCH
            else -> HomePulseResult.OPEN_SEARCH
        }
    }
}
