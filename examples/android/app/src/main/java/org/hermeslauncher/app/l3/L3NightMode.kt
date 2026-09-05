package org.hermeslauncher.app.l3

import android.app.UiModeManager
import org.hermeslauncher.app.ui.theme.NightSchedule
import org.hermeslauncher.app.ui.theme.ThemeMode
import java.util.Calendar

object L3NightMode {
    const val FOLLOW: Int = UiModeManager.MODE_NIGHT_AUTO
    const val NO: Int = UiModeManager.MODE_NIGHT_NO
    const val YES: Int = UiModeManager.MODE_NIGHT_YES

    fun delegateMode(themeMode: ThemeMode, schedule: NightSchedule, nowMinute: Int): Int {
        if (schedule.enabled && schedule.contains(nowMinute)) {
            return YES
        }
        return when (themeMode) {
            ThemeMode.Light -> NO
            ThemeMode.Dark -> YES
            ThemeMode.System -> FOLLOW
        }
    }

    fun nowMinute(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    fun apply(themeMode: ThemeMode, schedule: NightSchedule, uiMode: UiModeManager?) {
        val mode = delegateMode(themeMode, schedule, nowMinute())
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            runCatching { uiMode?.setApplicationNightMode(mode) }
        }
    }

    fun apply(themeMode: ThemeMode, schedule: NightSchedule) {
        apply(themeMode, schedule, null)
    }
}
