package org.hermeslauncher.app.ui.launcher

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hermeslauncher.app.ui.theme.BadgeStyle
import org.hermeslauncher.app.ui.theme.HermesTheme
import org.hermeslauncher.app.ui.theme.IconShape
import org.hermeslauncher.app.ui.theme.LocalBadgeColorArgb
import org.hermeslauncher.app.ui.theme.LocalBadgeStyle
import org.hermeslauncher.app.ui.theme.LocalIconShape
import org.hermeslauncher.app.ui.theme.LocalLabelShadow
import org.hermeslauncher.app.ui.theme.LookPalette
import org.hermeslauncher.app.ui.theme.LookPrefs
import org.hermeslauncher.app.ui.theme.NightSchedule
import org.hermeslauncher.app.ui.theme.ThemeMode
import org.hermeslauncher.app.ui.theme.ThemePreferences

/** Look & feel prefs on reserved Workspace Compose pages. */
@Composable
fun HermesPageTheme(context: Context, content: @Composable () -> Unit) {
    val prefs = remember { ThemePreferences(context) }
    val look = remember { LookPrefs(context) }
    val themeMode by prefs.themeMode.collectAsStateWithLifecycle(ThemeMode.System)
    val night by look.nightSchedule.collectAsStateWithLifecycle(NightSchedule.OFF)
    val iconShape by look.iconShape.collectAsStateWithLifecycle(IconShape.SYSTEM)
    val badgeStyle by look.badgeStyle.collectAsStateWithLifecycle(BadgeStyle.COUNTS)
    val badgeColor by look.badgeColorArgb.collectAsStateWithLifecycle(null)
    val labelShadow by look.labelShadow.collectAsStateWithLifecycle(true)
    val wallpaperOn by look.wallpaperPalette.collectAsStateWithLifecycle(false)
    val seed = remember(wallpaperOn) {
        if (wallpaperOn) LookPalette.wallpaperSeed(context) else null
    }
    HermesTheme(themeMode, nightSchedule = night, wallpaperSeed = seed) {
        CompositionLocalProvider(
            LocalIconShape provides iconShape,
            LocalBadgeStyle provides badgeStyle,
            LocalBadgeColorArgb provides badgeColor,
            LocalLabelShadow provides labelShadow,
            content = content,
        )
    }
}
