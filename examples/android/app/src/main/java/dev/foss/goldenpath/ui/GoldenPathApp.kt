package dev.foss.goldenpath.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.SnackbarHostState
import dev.foss.goldenpath.BuildConfig
import dev.foss.goldenpath.R
import dev.foss.goldenpath.about.AppUpdatePreferences
import dev.foss.goldenpath.about.AppUpdates
import dev.foss.goldenpath.about.DonationsLoader
import dev.foss.goldenpath.about.UpdateLaunchPrefs
import dev.foss.goldenpath.network.NetworkStatusMonitor
import dev.foss.goldenpath.ui.insets.NavigationModeProvider
import dev.foss.goldenpath.ui.theme.ThemeMode
import dev.foss.goldenpath.ui.theme.ThemePreferences
import dev.foss.goldenpath.ui.theme.next
import kotlinx.coroutines.CoroutineScope
import dev.foss.goldenpath.ui.theme.GoldenPathTheme
import kotlinx.coroutines.launch

@Composable
fun GoldenPathApp(
    context: Context,
    scope: CoroutineScope,
    themePreferences: ThemePreferences,
    appUpdatePreferences: AppUpdatePreferences,
    networkStatusMonitor: NetworkStatusMonitor,
) {
    val themeMode by themePreferences.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.System)
    val isOnline by networkStatusMonitor.isOnline.collectAsStateWithLifecycle(initialValue = true)
    val installedFormat by appUpdatePreferences.installedFormat.collectAsStateWithLifecycle(initialValue = "apk")
    val pendingRestart by appUpdatePreferences.pendingRestart.collectAsStateWithLifecycle(initialValue = false)
    var showAbout by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf(context.getString(R.string.about_update_current)) }
    var launchPrompt by remember { mutableStateOf<AppUpdates.LaunchPrompt?>(null) }
    val donations = remember { DonationsLoader.load(context) }
    val appVersion = BuildConfig.VERSION_NAME
    val snackbarHostState = remember { SnackbarHostState() }
    val launchPrefs = remember { UpdateLaunchPrefs(context) }

    LaunchedEffect(pendingRestart) {
        if (pendingRestart) {
            updateStatus = context.getString(R.string.about_update_restarting)
        }
    }

    LaunchedEffect(Unit) {
        launchPrompt = AppUpdates.onLaunch(context, appVersion)
    }

    fun openUrl(url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    GoldenPathTheme(themeMode = themeMode) {
        NavigationModeProvider {
            GoldenPathScreen(
                snackbarHostState = snackbarHostState,
                themeMode = themeMode,
                isOnline = isOnline,
                showAbout = showAbout,
                showSettings = showSettings,
                appVersion = appVersion,
                installedFormat = installedFormat ?: "apk",
                updateStatus = updateStatus,
                donations = donations,
                canApplyUpdate = false,
                launchPrompt = launchPrompt,
                onThemeToggle = { scope.launch { themePreferences.setThemeMode(themeMode.next()) } },
                onThemeModeSelect = { mode -> scope.launch { themePreferences.setThemeMode(mode) } },
                onAboutOpen = { showAbout = !showAbout; if (showAbout) showSettings = false },
                onAboutClose = { showAbout = false },
                onSettingsOpen = { showSettings = !showSettings; if (showSettings) showAbout = false },
                onSettingsClose = { showSettings = false },
                onDonate = { openUrl(DonationsLoader.primaryUrl(donations)) },
                onDonatePrompt = { donate ->
                    launchPrefs.markVersionSeen(appVersion)
                    launchPrompt = null
                    if (donate) openUrl(DonationsLoader.primaryUrl(donations))
                },
                onUpdatePrompt = { install ->
                    val prompt = launchPrompt as? AppUpdates.LaunchPrompt.Update
                    launchPrompt = null
                    if (prompt != null) {
                        launchPrefs.markChecked(System.currentTimeMillis(), prompt.version)
                        if (install) openUrl(prompt.url)
                    }
                },
                onApplyUpdate = {},
            )
        }
    }
}
