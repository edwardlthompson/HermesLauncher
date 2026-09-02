package org.hermeslauncher.app.ui

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
import org.hermeslauncher.app.BuildConfig
import org.hermeslauncher.app.R
import org.hermeslauncher.app.crashcapture.PendingCrashStore
import org.hermeslauncher.app.feedback.FeedbackPrefs
import org.hermeslauncher.app.about.ReleaseTagFetcher
import org.hermeslauncher.app.about.AppUpdatePreferences
import org.hermeslauncher.app.about.AppUpdates
import org.hermeslauncher.app.about.DonationsLoader
import org.hermeslauncher.app.about.UpdateLaunchPrefs
import org.hermeslauncher.app.network.NetworkStatusMonitor
import org.hermeslauncher.app.ui.insets.NavigationModeProvider
import org.hermeslauncher.app.ui.theme.ThemeMode
import org.hermeslauncher.app.ui.theme.ThemePreferences
import org.hermeslauncher.app.ui.theme.next
import kotlinx.coroutines.CoroutineScope
import org.hermeslauncher.app.ui.theme.HermesTheme
import org.hermeslauncher.app.widgets.WidgetHostController
import kotlinx.coroutines.launch

@Composable
fun HermesApp(
    context: Context,
    scope: CoroutineScope,
    themePreferences: ThemePreferences,
    appUpdatePreferences: AppUpdatePreferences,
    networkStatusMonitor: NetworkStatusMonitor,
    widgetController: WidgetHostController,
) {
    val themeMode by themePreferences.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.System)
    val isOnline by networkStatusMonitor.isOnline.collectAsStateWithLifecycle(initialValue = true)
    val installedFormat by appUpdatePreferences.installedFormat.collectAsStateWithLifecycle(initialValue = "apk")
    val pendingRestart by appUpdatePreferences.pendingRestart.collectAsStateWithLifecycle(initialValue = false)
    var showAbout by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val feedbackPrefs = remember { FeedbackPrefs(context) }
    var saveCrashes by remember { mutableStateOf(feedbackPrefs.saveCrashes()) }
    var showFeedback by remember { mutableStateOf<String?>(if (PendingCrashStore(context).read() != null) "bug" else null) }
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

    HermesTheme(themeMode = themeMode) {
        NavigationModeProvider {
            HermesScreen(
                snackbarHostState = snackbarHostState,
                widgetController = widgetController,
                themeMode = themeMode,
                isOnline = isOnline,
                showAbout = showAbout,
                showSettings = showSettings,
                showFeedback = showFeedback,
                saveCrashes = saveCrashes,
                releaseRepo = ReleaseTagFetcher.loadReleaseRepo(context).orEmpty(),
                pendingStack = PendingCrashStore(context).read()?.stack,
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
                onSaveCrashes = { on -> feedbackPrefs.setSaveCrashes(on); saveCrashes = on },
                onReportBug = { showAbout = false; showFeedback = "bug" },
                onRequestFeature = { showAbout = false; showFeedback = "feature" },
                onFeedbackClose = { showFeedback = null; PendingCrashStore(context).clear() },
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
