package org.hermeslauncher.app.ui.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.about.DonationsConfig
import org.hermeslauncher.app.display.highRefreshScroll
import org.hermeslauncher.app.ui.insets.LocalNavigationMode
import org.hermeslauncher.app.ui.insets.bottomInsetPadding
import org.hermeslauncher.app.ui.insets.navigationBarInsetBottomDp
import org.hermeslauncher.app.ui.insets.navigationModeLabelRes
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun AboutScreen(
    version: String,
    installedFormat: String,
    updateStatus: String,
    donations: DonationsConfig,
    canApplyUpdate: Boolean,
    onApplyUpdate: () -> Unit,
    onReportBug: () -> Unit,
    onRequestFeature: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val navMode = LocalNavigationMode.current
    val insetDp = navigationBarInsetBottomDp()
    Column(
        modifier = modifier
            .highRefreshScroll()
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(text = stringResource(R.string.about_version, version))
        Text(text = stringResource(R.string.about_format, installedFormat))
        Text(text = updateStatus)
        Text(
            text = stringResource(
                R.string.about_debug_navigation_mode,
                stringResource(navigationModeLabelRes(navMode)),
                insetDp,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (canApplyUpdate) {
            Button(onClick = onApplyUpdate) {
                Text(stringResource(R.string.about_update_apply))
            }
        }
        if (donations.enabled && donations.links.isNotEmpty()) {
            Text(text = donations.message)
            donations.links.forEach { link ->
                Text(
                    text = link.label,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { uriHandler.openUri(link.url) },
                )
            }
        }
        Button(onClick = onReportBug) {
            Text(stringResource(R.string.feedback_bug_title))
        }
        Button(onClick = onRequestFeature) {
            Text(stringResource(R.string.feedback_feature_title))
        }
        Button(
            onClick = onBack,
            modifier = Modifier.bottomInsetPadding(),
        ) {
            Text(stringResource(R.string.about_close))
        }
    }
}
