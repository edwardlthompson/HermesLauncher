package org.hermeslauncher.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.about.AppUpdates
import org.hermeslauncher.app.about.DonationsConfig
import org.hermeslauncher.app.ui.about.AboutScreen
import org.hermeslauncher.app.ui.about.LaunchPromptDialogs
import org.hermeslauncher.app.ui.components.HermesScaffold
import org.hermeslauncher.app.ui.components.ThemeToggle
import org.hermeslauncher.app.ui.feedback.FeedbackScreen
import org.hermeslauncher.app.ui.launcher.LauncherHome
import org.hermeslauncher.app.ui.settings.SettingsScreen
import org.hermeslauncher.app.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HermesScreen(
    snackbarHostState: SnackbarHostState,
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
    HermesScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title)) },
                actions = {
                    if (donations.enabled && donations.links.isNotEmpty()) {
                        TextButton(onClick = onDonate) {
                            Text(stringResource(R.string.about_donate))
                        }
                    }
                    IconButton(onClick = onSettingsOpen) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_open),
                        )
                    }
                    IconButton(onClick = onAboutOpen) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.about_open),
                        )
                    }
                    ThemeToggle(themeMode = themeMode, onToggle = onThemeToggle)
                },
            )
        },
    ) { innerPadding ->
        if (launchPrompt != null) {
            LaunchPromptDialogs(
                prompt = launchPrompt,
                onDonate = onDonatePrompt,
                onUpdate = onUpdatePrompt,
            )
        }
        when {
            showFeedback != null -> FeedbackScreen(
                kind = showFeedback,
                releaseRepo = releaseRepo,
                stack = pendingStack,
                onBack = onFeedbackClose,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            showSettings -> SettingsScreen(
                themeMode = themeMode,
                onThemeModeSelect = onThemeModeSelect,
                saveCrashes = saveCrashes,
                onSaveCrashes = onSaveCrashes,
                onBack = onSettingsClose,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            showAbout -> AboutScreen(
                version = appVersion,
                installedFormat = installedFormat,
                updateStatus = updateStatus,
                donations = donations,
                canApplyUpdate = canApplyUpdate,
                onApplyUpdate = onApplyUpdate,
                onReportBug = onReportBug,
                onRequestFeature = onRequestFeature,
                onBack = onAboutClose,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            else -> LauncherHome(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}
