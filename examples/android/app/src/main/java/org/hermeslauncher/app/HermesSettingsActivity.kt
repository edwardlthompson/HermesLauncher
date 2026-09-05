package org.hermeslauncher.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.hermeslauncher.app.feedback.FeedbackPrefs
import org.hermeslauncher.app.l3.L3NightMode
import org.hermeslauncher.app.ui.settings.SettingsScreen
import org.hermeslauncher.app.ui.settings.SettingsSection
import org.hermeslauncher.app.ui.theme.HermesTheme
import org.hermeslauncher.app.ui.theme.LookPrefs
import org.hermeslauncher.app.ui.theme.NightSchedule
import org.hermeslauncher.app.ui.theme.ThemeMode
import org.hermeslauncher.app.ui.theme.ThemePreferences

/** Nova settings hub. Long-press Settings on the desktop opens this. */
class HermesSettingsActivity : ComponentActivity() {
    companion object {
        const val EXTRA_SECTION = "extra_section"

        fun intent(context: android.content.Context, section: SettingsSection? = null): android.content.Intent {
            return android.content.Intent(context, HermesSettingsActivity::class.java).apply {
                if (section != null) {
                    putExtra(EXTRA_SECTION, section.name)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themePreferences = ThemePreferences(this)
        val feedbackPrefs = FeedbackPrefs(this)
        val lookPrefs = LookPrefs(this)
        setContent {
            val themeMode by themePreferences.themeMode.collectAsStateWithLifecycle(ThemeMode.System)
            val night by lookPrefs.nightSchedule.collectAsStateWithLifecycle(NightSchedule.OFF)
            var saveCrashes by remember { mutableStateOf(feedbackPrefs.saveCrashes()) }
            HermesTheme(themeMode, nightSchedule = night) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen(
                        themeMode = themeMode,
                        onThemeModeSelect = { mode ->
                            lifecycleScope.launch {
                                themePreferences.setThemeMode(mode)
                                L3NightMode.apply(
                                    mode,
                                    night,
                                    getSystemService(android.app.UiModeManager::class.java),
                                )
                            }
                        },
                        saveCrashes = saveCrashes,
                        onSaveCrashes = { on ->
                            saveCrashes = on
                            feedbackPrefs.setSaveCrashes(on)
                        },
                        onBack = { finish() },
                        initialSection = SettingsSection.parse(intent.getStringExtra(EXTRA_SECTION)),
                    )
                }
            }
        }
    }
}
