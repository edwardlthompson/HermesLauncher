package org.hermeslauncher.app.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.about.ReleaseTagFetcher
import org.hermeslauncher.app.crashcapture.PendingCrashStore
import org.hermeslauncher.app.display.highRefreshScroll
import org.hermeslauncher.app.ui.feedback.FeedbackScreen
import org.hermeslauncher.app.ui.inbox.InboxHistoryScreen
import org.hermeslauncher.app.ui.scroll.OverflowScrubBar
import org.hermeslauncher.app.ui.scroll.scrubGutter
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
    var group by remember { mutableStateOf<SettingsGroup?>(null) }
    var skipGroupOnBack by remember { mutableStateOf(initialSection != null) }
    var historyOpen by remember { mutableStateOf(false) }
    var feedbackKind by remember { mutableStateOf<String?>(null) }
    val scroll = rememberScrollState()
    LaunchedEffect(section, group, historyOpen, feedbackKind) { scroll.scrollTo(0) }
    BackHandler {
        when {
            feedbackKind != null -> feedbackKind = null
            historyOpen -> historyOpen = false
            section != null -> {
                val parent = SettingsGroup.of(section!!)
                section = null
                group = if (skipGroupOnBack) null else parent.takeIf { it.directSection() == null }
                skipGroupOnBack = false
            }
            group != null -> group = null
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
    Box(modifier = inset) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .highRefreshScroll()
            .verticalScroll(scroll)
            .padding(SpacingMd)
            .scrubGutter(),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        val open = section
        val openGroup = group
        when {
            open != null -> SettingsSectionPane(
                section = open,
                themeMode = themeMode,
                onThemeModeSelect = onThemeModeSelect,
                saveCrashes = saveCrashes,
                onSaveCrashes = onSaveCrashes,
                onHistory = { historyOpen = true },
                onReportBug = { feedbackKind = "bug" },
                onRequestFeature = { feedbackKind = "feature" },
                onOpenSection = { next -> section = next },
            )
            openGroup != null -> SettingsGroupPane(group = openGroup, onOpen = { section = it })
            else -> SettingsHub(
                onOpen = { picked ->
                    val direct = picked.directSection()
                    if (direct != null) {
                        section = direct
                    } else {
                        group = picked
                    }
                },
            )
        }
    }
    OverflowScrubBar(
        scroll = scroll,
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
    )
    }
}

@Composable
private fun SettingsSectionPane(
    section: SettingsSection,
    themeMode: ThemeMode,
    onThemeModeSelect: (ThemeMode) -> Unit,
    saveCrashes: Boolean,
    onSaveCrashes: (Boolean) -> Unit,
    onHistory: () -> Unit,
    onReportBug: () -> Unit,
    onRequestFeature: () -> Unit,
    onOpenSection: (SettingsSection) -> Unit,
) {
    Text(text = stringResource(section.titleRes()), style = MaterialTheme.typography.headlineSmall)
    when (section) {
        SettingsSection.PERMISSIONS -> SettingsPermissionsPane()
        SettingsSection.DESKTOP -> SettingsDesktopPane()
        SettingsSection.DOCK -> DockSettings()
        SettingsSection.LOOK -> SettingsLookPane(themeMode, onThemeModeSelect)
        SettingsSection.GESTURES -> GestureSettings()
        SettingsSection.INBOX -> SettingsInboxPane(
            saveCrashes = saveCrashes,
            onSaveCrashes = onSaveCrashes,
            onHistory = onHistory,
        )
        SettingsSection.FEEDS -> SettingsFeedsPane(
            onSubscriptions = { onOpenSection(SettingsSection.FEEDS_SUBS) },
        )
        SettingsSection.FEEDS_SUBS -> SettingsFeedSubs()
        SettingsSection.DRAWER -> DrawerSettings()
        SettingsSection.FOLDERS -> FolderSettings()
        SettingsSection.SEARCH -> SearchSettings()
        SettingsSection.BACKUP -> BackupSettings()
        SettingsSection.ABOUT -> SettingsAboutPane(
            onReportBug = onReportBug,
            onRequestFeature = onRequestFeature,
        )
    }
}
