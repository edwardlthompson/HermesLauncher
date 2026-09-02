package org.hermeslauncher.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hermeslauncher.app.about.AppUpdates
import org.hermeslauncher.app.about.DonationsConfig
import org.hermeslauncher.app.ui.about.AboutScreen
import org.hermeslauncher.app.ui.about.LaunchPromptDialogs
import org.hermeslauncher.app.ui.components.HermesScaffold
import org.hermeslauncher.app.ui.feedback.FeedbackScreen
import org.hermeslauncher.app.ui.launcher.LauncherHome
import org.hermeslauncher.app.ui.settings.SettingsScreen
import org.hermeslauncher.app.ui.theme.ThemeMode
import org.hermeslauncher.app.widgets.WidgetHostController

@Suppress("UNUSED_PARAMETER")
@Composable
fun HermesScreen(
    snackbarHostState: SnackbarHostState,
    widgetController: WidgetHostController,
    themeMode: ThemeMode,
    isOnline: Boolean,
    showAbout: Boolean,
    showSettings: Boolean,
    showFeedback: String?,
    saveCrashes: Boolean,
    releaseRepo: String,
    pendingStack: String?,
    appVersion: String,
    installedFormat: String,
    updateStatus: String,
    donations: DonationsConfig,
    canApplyUpdate: Boolean,
    launchPrompt: AppUpdates.LaunchPrompt?,
    onThemeToggle: () -> Unit,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onAboutOpen: () -> Unit,
    onAboutClose: () -> Unit,
    onSettingsOpen: () -> Unit,
    onSettingsClose: () -> Unit,
    onSaveCrashes: (Boolean) -> Unit,
    onReportBug: () -> Unit,
    onRequestFeature: () -> Unit,
    onFeedbackClose: () -> Unit,
    onDonate: () -> Unit,
    onDonatePrompt: (Boolean) -> Unit,
    onUpdatePrompt: (Boolean) -> Unit,
    onApplyUpdate: () -> Unit,
) {
    HermesScaffold(snackbarHostState = snackbarHostState) { innerPadding ->
        if (launchPrompt != null) {
            LaunchPromptDialogs(
                prompt = launchPrompt,
                onDonate = onDonatePrompt,
                onUpdate = onUpdatePrompt,
            )
        }
        when {
            showFeedback != null -> OverlayPane(Modifier.padding(innerPadding)) {
                FeedbackScreen(
                    kind = showFeedback,
                    releaseRepo = releaseRepo,
                    stack = pendingStack,
                    onBack = onFeedbackClose,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            showSettings -> OverlayPane(Modifier.padding(innerPadding)) {
                SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeSelect = onThemeModeSelect,
                    saveCrashes = saveCrashes,
                    onSaveCrashes = onSaveCrashes,
                    onBack = onSettingsClose,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            showAbout -> OverlayPane(Modifier.padding(innerPadding)) {
                AboutScreen(
                    version = appVersion,
                    installedFormat = installedFormat,
                    updateStatus = updateStatus,
                    donations = donations,
                    canApplyUpdate = canApplyUpdate,
                    onApplyUpdate = onApplyUpdate,
                    onReportBug = onReportBug,
                    onRequestFeature = onRequestFeature,
                    onBack = onAboutClose,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            else -> LauncherHome(
                widgetController = widgetController,
                onOpenSettings = onSettingsOpen,
                onOpenAbout = onAboutOpen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun OverlayPane(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        content = { content() },
    )
}
