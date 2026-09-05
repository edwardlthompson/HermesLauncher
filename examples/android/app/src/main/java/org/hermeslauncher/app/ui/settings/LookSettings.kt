package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.BadgeStyle
import org.hermeslauncher.app.ui.theme.IconShape
import org.hermeslauncher.app.ui.theme.LookPrefs
import org.hermeslauncher.app.ui.theme.NightSchedule
import org.hermeslauncher.app.ui.theme.SpacingMd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LookSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { LookPrefs(context) }
    val shape by prefs.iconShape.collectAsStateWithLifecycle(IconShape.SYSTEM)
    val night by prefs.nightSchedule.collectAsStateWithLifecycle(NightSchedule.OFF)
    val badge by prefs.badgeStyle.collectAsStateWithLifecycle(BadgeStyle.COUNTS)
    val badgeColor by prefs.badgeColorArgb.collectAsStateWithLifecycle(null)
    val labelShadow by prefs.labelShadow.collectAsStateWithLifecycle(true)
    val wallpaper by prefs.wallpaperPalette.collectAsStateWithLifecycle(false)
    var startText by remember(night.startMinute) { mutableStateOf(NightSchedule.formatTime(night.startMinute)) }
    var endText by remember(night.endMinute) { mutableStateOf(NightSchedule.formatTime(night.endMinute)) }
    val accentArgb = MaterialTheme.colorScheme.primary.toArgb()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(stringResource(R.string.look_shape), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            IconShape.entries.forEach { option ->
                FilterChip(
                    selected = shape == option,
                    onClick = { scope.launch { prefs.setIconShape(option) } },
                    label = { Text(shapeLabel(option)) },
                )
            }
        }
        Text(stringResource(R.string.look_night), style = MaterialTheme.typography.titleSmall)
        Text(stringResource(R.string.look_night_body), style = MaterialTheme.typography.bodySmall)
        Switch(
            checked = night.enabled,
            onCheckedChange = { on -> scope.launch { prefs.setNightSchedule(night.copy(enabled = on)) } },
        )
        OutlinedTextField(
            value = startText,
            onValueChange = { startText = it },
            label = { Text(stringResource(R.string.look_night_start)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = endText,
            onValueChange = { endText = it },
            label = { Text(stringResource(R.string.look_night_end)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        FilterChip(
            selected = false,
            onClick = {
                val start = NightSchedule.parseTime(startText) ?: night.startMinute
                val end = NightSchedule.parseTime(endText) ?: night.endMinute
                scope.launch { prefs.setNightSchedule(NightSchedule(night.enabled, start, end)) }
            },
            label = { Text(stringResource(R.string.look_night_save)) },
        )
        Text(stringResource(R.string.look_badge), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            BadgeStyle.entries.forEach { option ->
                FilterChip(
                    selected = badge == option,
                    onClick = { scope.launch { prefs.setBadgeStyle(option) } },
                    label = {
                        Text(
                            if (option == BadgeStyle.DOTS) stringResource(R.string.look_badge_dots)
                            else stringResource(R.string.look_badge_counts),
                        )
                    },
                )
            }
        }
        Text(stringResource(R.string.look_badge_color), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            FilterChip(
                selected = badgeColor == null,
                onClick = { scope.launch { prefs.setBadgeColorArgb(null) } },
                label = { Text(stringResource(R.string.look_badge_color_theme)) },
            )
            FilterChip(
                selected = badgeColor != null,
                onClick = { scope.launch { prefs.setBadgeColorArgb(accentArgb) } },
                label = { Text(stringResource(R.string.look_badge_color_accent)) },
            )
        }
        Text(stringResource(R.string.look_label_shadow))
        Switch(checked = labelShadow, onCheckedChange = { on -> scope.launch { prefs.setLabelShadow(on) } })
        Text(stringResource(R.string.look_wallpaper_palette))
        Text(stringResource(R.string.look_wallpaper_palette_body), style = MaterialTheme.typography.bodySmall)
        Switch(checked = wallpaper, onCheckedChange = { on -> scope.launch { prefs.setWallpaperPalette(on) } })
    }
}

@Composable
private fun shapeLabel(shape: IconShape): String = when (shape) {
    IconShape.SYSTEM -> stringResource(R.string.look_shape_system)
    IconShape.CIRCLE -> stringResource(R.string.look_shape_circle)
    IconShape.SQUIRCLE -> stringResource(R.string.look_shape_squircle)
    IconShape.SQUARE -> stringResource(R.string.look_shape_square)
    IconShape.TEARDROP -> stringResource(R.string.look_shape_teardrop)
}
