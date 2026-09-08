package org.hermeslauncher.app.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import org.hermeslauncher.app.ui.scroll.OverflowScrubBar
import org.hermeslauncher.app.ui.scroll.scrubGutter
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
    embedded: Boolean = false,
) {
    val uriHandler = LocalUriHandler.current
    val navMode = LocalNavigationMode.current
    val insetDp = navigationBarInsetBottomDp()
    val scroll = rememberScrollState()
    val body: @Composable ColumnScope.() -> Unit = {
        if (!embedded) {
            Text(text = stringResource(R.string.about_title), style = MaterialTheme.typography.headlineSmall)
        }
        Text(text = stringResource(R.string.about_app_blurb))
        Text(text = stringResource(R.string.about_version, version))
        Text(text = stringResource(R.string.about_format, installedFormat))
        Text(text = updateStatus)
        if (!embedded) {
            Text(
                text = stringResource(
                    R.string.about_debug_navigation_mode,
                    stringResource(navigationModeLabelRes(navMode)),
                    insetDp,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (canApplyUpdate) {
            Button(onClick = onApplyUpdate) { Text(stringResource(R.string.about_update_apply)) }
        }
        if (donations.enabled && donations.links.isNotEmpty()) {
            Text(text = stringResource(R.string.about_donations_heading), style = MaterialTheme.typography.titleMedium)
            Text(text = donations.message.ifBlank { stringResource(R.string.about_donations_message) })
            donations.links.forEach { link ->
                Button(onClick = { uriHandler.openUri(link.url) }) {
                    Text(link.label.ifBlank { stringResource(R.string.about_donate) })
                }
            }
        }
        Button(onClick = onReportBug) { Text(stringResource(R.string.feedback_bug_title)) }
        Button(onClick = onRequestFeature) { Text(stringResource(R.string.feedback_feature_title)) }
        if (!embedded) {
            Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
                Text(stringResource(R.string.about_close))
            }
        }
    }
    if (embedded) {
        Column(modifier = modifier.padding(SpacingMd), verticalArrangement = Arrangement.spacedBy(SpacingMd), content = body)
        return
    }
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .highRefreshScroll()
                .verticalScroll(scroll)
                .padding(SpacingMd)
                .scrubGutter(),
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
            content = body,
        )
        OverflowScrubBar(scroll = scroll, modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight())
    }
}
