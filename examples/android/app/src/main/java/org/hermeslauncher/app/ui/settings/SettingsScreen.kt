package org.hermeslauncher.app.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.about.ReleaseTagFetcher
import org.hermeslauncher.app.crashcapture.PendingCrashStore
import org.hermeslauncher.app.display.highRefreshScroll
import org.hermeslauncher.app.ui.feedback.FeedbackScreen
import org.hermeslauncher.app.ui.inbox.InboxHistoryScreen
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeSelect: (ThemeMode) -> Unit,
    saveCrashes: Boolean,
    onSaveCrashes: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialSection: SettingsSection? = null,
) {
    var section by remember { mutableStateOf(initialSection) }
    var historyOpen by remember { mutableStateOf(false) }
    var feedbackKind by remember { mutableStateOf<String?>(null) }
    val scroll = rememberScrollState()
    LaunchedEffect(section, historyOpen, feedbackKind) { scroll.scrollTo(0) }
    BackHandler {
        when {
            feedbackKind != null -> feedbackKind = null
            historyOpen -> historyOpen = false
            section != null -> section = null
            else -> onBack()
        }
    }
    val inset = modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
    if (historyOpen) {
        InboxHistoryScreen(modifier = inset)
        return
    }
    val feedback = feedbackKind
    if (feedback != null) {
        val context = LocalContext.current
        FeedbackScreen(
            kind = feedback,
            releaseRepo = ReleaseTagFetcher.loadReleaseRepo(context).orEmpty(),
            stack = PendingCrashStore(context).read()?.stack,
            onBack = { feedbackKind = null },
            modifier = inset,
        )
        return
    }
    Column(
        modifier = inset
            .highRefreshScroll()
            .verticalScroll(scroll)
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        val open = section
        if (open == null) {
            SettingsHub(onOpen = { section = it })
        } else {
            Text(text = stringResource(open.titleRes()), style = MaterialTheme.typography.headlineSmall)
            when (open) {
                SettingsSection.PERMISSIONS -> SettingsPermissionsPane()
                SettingsSection.DESKTOP -> SettingsDesktopPane()
                SettingsSection.DOCK -> DockSettings()
                SettingsSection.LOOK -> SettingsLookPane(themeMode, onThemeModeSelect)
                SettingsSection.GESTURES -> GestureSettings()
                SettingsSection.INBOX -> SettingsInboxPane(
                    saveCrashes = saveCrashes,
                    onSaveCrashes = onSaveCrashes,
                    onHistory = { historyOpen = true },
                )
                SettingsSection.FEEDS -> SettingsFeedsPane()
                SettingsSection.DRAWER -> DrawerSettings()
                SettingsSection.FOLDERS -> FolderSettings()
                SettingsSection.SEARCH -> SearchSettings()
                SettingsSection.BACKUP -> BackupSettings()
                SettingsSection.ABOUT -> SettingsAboutPane(
                    onReportBug = { feedbackKind = "bug" },
                    onRequestFeature = { feedbackKind = "feature" },
                )
            }
        }
    }
}
