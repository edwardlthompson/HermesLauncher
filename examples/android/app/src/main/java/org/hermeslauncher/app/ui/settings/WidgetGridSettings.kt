package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.widgets.WidgetGridSpec
import org.hermeslauncher.app.widgets.WidgetHostState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WidgetGridSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as HermesApplication
    val widgets by app.widgetStore.state.collectAsStateWithLifecycle(WidgetHostState())
    val grid = widgets.grid.clamped()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.settings_widget_grid))
        Text(
            text = stringResource(R.string.settings_widget_grid_body),
            style = MaterialTheme.typography.bodySmall,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            WidgetGridSpec.PRESETS.forEach { preset ->
                FilterChip(
                    selected = grid == preset,
                    onClick = {
                        scope.launch { app.widgetStore.save(widgets.withGrid(preset)) }
                    },
                    label = { Text(stringResource(R.string.settings_widget_grid_preset, preset.columns, preset.rows)) },
                )
            }
        }
        AxisStepper(
            label = stringResource(R.string.settings_widget_grid_columns, grid.columns),
            onDec = {
                scope.launch {
                    app.widgetStore.save(widgets.withGrid(grid.copy(columns = grid.columns - 1)))
                }
            },
            onInc = {
                scope.launch {
                    app.widgetStore.save(widgets.withGrid(grid.copy(columns = grid.columns + 1)))
                }
            },
        )
        AxisStepper(
            label = stringResource(R.string.settings_widget_grid_rows, grid.rows),
            onDec = {
                scope.launch {
                    app.widgetStore.save(widgets.withGrid(grid.copy(rows = grid.rows - 1)))
                }
            },
            onInc = {
                scope.launch {
                    app.widgetStore.save(widgets.withGrid(grid.copy(rows = grid.rows + 1)))
                }
            },
        )
    }
}

@Composable
private fun AxisStepper(
    label: String,
    onDec: () -> Unit,
    onInc: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, modifier = Modifier.weight(1f))
        IconButton(onClick = onDec) {
            Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.settings_widget_grid_fewer))
        }
        IconButton(onClick = onInc) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.settings_widget_grid_more))
        }
    }
}
