package org.hermeslauncher.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.BuildConfig
import org.hermeslauncher.app.R
import org.hermeslauncher.app.about.ArtifactFormatDetector
import org.hermeslauncher.app.about.DonationsLoader
import org.hermeslauncher.app.ui.about.AboutScreen

@Composable
fun SettingsAboutPane(
    onReportBug: () -> Unit,
    onRequestFeature: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val donations = remember { DonationsLoader.load(context) }
    AboutScreen(
        version = BuildConfig.VERSION_NAME,
        installedFormat = ArtifactFormatDetector.detectAndroidFormat(),
        updateStatus = stringResource(R.string.about_update_current),
        donations = donations,
        canApplyUpdate = false,
        onApplyUpdate = {},
        onReportBug = onReportBug,
        onRequestFeature = onRequestFeature,
        onBack = {},
        embedded = true,
        modifier = modifier,
    )
}
