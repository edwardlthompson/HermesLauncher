package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.workspace.LabsFlags
import org.hermeslauncher.app.workspace.PageSnapSpeed
import org.hermeslauncher.app.workspace.ScrollMode

@Composable
fun DesktopSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val showLabels by app.homePrefs.showLabels.collectAsStateWithLifecycle(true)
    val labs by app.pagedPrefs.labs.collectAsStateWithLifecycle(LabsFlags())
    val scrollMode by app.pagedPrefs.scrollMode.collectAsStateWithLifecycle(ScrollMode.ADJACENT)
    val snapSpeed by app.pagedPrefs.snapSpeed.collectAsStateWithLifecycle(PageSnapSpeed.DEFAULT)
    val wrapLabel = stringResource(R.string.labs_wrap)
    val overlapLabel = stringResource(R.string.labs_overlap)
    val inverseLabel = stringResource(R.string.paged_inverse)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.desktop_icon_grid), style = MaterialTheme.typography.bodySmall)
        WidgetGridSettings()
        SettingsSwitchRow(
            title = R.string.desktop_show_labels,
            body = R.string.desktop_show_labels_body,
            checked = showLabels,
            onCheckedChange = { on -> scope.launch { app.homePrefs.setShowLabels(on) } },
        )
        SettingsExpander(title = stringResource(R.string.settings_expand_labs)) {
            SettingsSwitchRow(
                title = R.string.labs_wrap,
                body = R.string.labs_wrap_body,
                checked = labs.wrap,
                onCheckedChange = { on -> scope.launch { app.pagedPrefs.setWrap(on) } },
                modifier = Modifier.semantics { contentDescription = wrapLabel },
            )
            SettingsSwitchRow(
                title = R.string.labs_overlap,
                body = R.string.labs_overlap_body,
                checked = labs.overlap,
                onCheckedChange = { on -> scope.launch { app.pagedPrefs.setOverlap(on) } },
                modifier = Modifier.semantics { contentDescription = overlapLabel },
            )
            SettingsSwitchRow(
                title = R.string.paged_inverse,
                body = R.string.paged_inverse_body,
                checked = scrollMode == ScrollMode.INVERSE,
                onCheckedChange = { on ->
                    scope.launch {
                        app.pagedPrefs.setScrollMode(if (on) ScrollMode.INVERSE else ScrollMode.ADJACENT)
                    }
                },
                modifier = Modifier.semantics { contentDescription = inverseLabel },
            )
            SettingsDropdown(
                title = stringResource(R.string.settings_page_speed),
                options = PageSnapSpeed.entries,
                selected = snapSpeed,
                labelOf = { speed ->
                    stringResource(
                        when (speed) {
                            PageSnapSpeed.FAST -> R.string.settings_page_speed_fast
                            PageSnapSpeed.NORMAL -> R.string.settings_page_speed_normal
                            PageSnapSpeed.SLOW -> R.string.settings_page_speed_slow
                        },
                    )
                },
                onSelect = { speed -> scope.launch { app.pagedPrefs.setSnapSpeed(speed) } },
            )
        }
    }
}
