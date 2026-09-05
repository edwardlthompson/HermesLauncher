package org.hermeslauncher.app.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.RadiusMd
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm
import org.hermeslauncher.app.widgets.WidgetCatalog
import org.hermeslauncher.app.widgets.WidgetChoice
import org.hermeslauncher.app.widgets.WidgetPreview

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetPicker(
    choices: List<WidgetChoice>,
    collapsed: Boolean,
    onCancel: () -> Unit,
    onPick: (WidgetChoice) -> Unit,
    onDragStart: (WidgetChoice, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val groups = remember(choices, query) { WidgetCatalog.grouped(WidgetCatalog.filter(choices, query)) }
    fun closeSearch() {
        searchOpen = false
        query = ""
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (collapsed) 0f else 1f },
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(SpacingMd).height(420.dp)) {
            Text(
                text = stringResource(R.string.widget_picker_title),
                style = MaterialTheme.typography.titleLarge,
            )
            if (searchOpen) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.widget_picker_search)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { closeSearch() }),
                    trailingIcon = {
                        IconButton(onClick = { closeSearch() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.filter_close),
                            )
                        }
                    },
                )
            } else {
                TextButton(onClick = { searchOpen = true }) {
                    Icon(Icons.Filled.Search, contentDescription = null)
                    Text(stringResource(R.string.widget_picker_search))
                }
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                groups.forEach { (app, widgets) ->
                    stickyHeader(key = "h:$app") {
                        Surface(color = MaterialTheme.colorScheme.surface) {
                            Text(
                                text = app,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = SpacingSm),
                            )
                        }
                    }
                    items(widgets.chunked(2), key = { row ->
                        row.joinToString { it.provider.flattenToString() }
                    }) { row ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            row.forEach { choice ->
                                PreviewCard(
                                    choice = choice,
                                    onPick = onPick,
                                    onDragStart = onDragStart,
                                    onDrag = onDrag,
                                    onDragEnd = onDragEnd,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.surface,
                                ) {}
                            }
                        }
                    }
                }
            }
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.widget_picker_cancel))
            }
        }
    }
}

@Composable
private fun PreviewCard(
    choice: WidgetChoice,
    onPick: (WidgetChoice) -> Unit,
    onDragStart: (WidgetChoice, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var coords by remember(choice.provider) { mutableStateOf<LayoutCoordinates?>(null) }
    val preview = remember(choice.provider) {
        WidgetPreview.bitmap(context, choice.provider, 256)?.asImageBitmap()
    }
    Column(
        modifier = modifier
            .onGloballyPositioned { coords = it }
            .pointerInput(choice.provider) {
                detectTapGestures(onTap = { onPick(choice) })
            }
            .pointerInput(choice.provider) {
                var window = Offset.Zero
                detectDragGesturesAfterLongPress(
                    onDragStart = { start ->
                        window = coords?.localToWindow(start) ?: Offset.Zero
                        onDragStart(choice, window)
                    },
                    onDrag = { change, dragAmount ->
                        window += dragAmount
                        onDrag(window)
                        change.consume()
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd,
                )
            }
            .padding(SpacingSm),
    ) {
        Surface(
            shape = RoundedCornerShape(RadiusMd),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
        ) {
            if (preview != null) {
                Image(
                    bitmap = preview,
                    contentDescription = choice.widgetLabel,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(96.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Apps,
                    contentDescription = choice.widgetLabel,
                    modifier = Modifier.padding(SpacingMd),
                )
            }
        }
        Text(
            text = choice.widgetLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = SpacingSm),
        )
    }
}
