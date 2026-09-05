package org.hermeslauncher.app.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.ui.theme.RadiusMd
import org.hermeslauncher.app.widgets.DropPolicy
import org.hermeslauncher.app.widgets.WidgetChoice
import org.hermeslauncher.app.widgets.WidgetGridSpec
import org.hermeslauncher.app.widgets.WidgetPreview
import kotlin.math.roundToInt

@Composable
fun BoxScope.WidgetDragLayer(
    choices: List<WidgetChoice>?,
    dragChoice: WidgetChoice?,
    dragWindow: Offset,
    rootCoords: LayoutCoordinates?,
    onCancel: () -> Unit,
    onPick: (WidgetChoice) -> Unit,
    onDragStart: (WidgetChoice, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
) {
    if (choices != null) {
        WidgetPicker(
            choices = choices,
            collapsed = dragChoice != null,
            onCancel = onCancel,
            onPick = onPick,
            onDragStart = onDragStart,
            onDrag = onDrag,
            onDragEnd = onDragEnd,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
    if (dragChoice != null) {
        val local = rootCoords?.windowToLocal(dragWindow) ?: Offset.Zero
        DragGhost(
            choice = dragChoice,
            modifier = Modifier.offset { IntOffset(local.x.roundToInt(), local.y.roundToInt()) },
        )
    }
}

@Composable
private fun DragGhost(choice: WidgetChoice, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preview = remember(choice.provider) {
        WidgetPreview.bitmap(context, choice.provider, 256)?.asImageBitmap()
    }
    Surface(
        modifier = modifier
            .width(160.dp)
            .height(96.dp),
        shape = RoundedCornerShape(RadiusMd),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp,
    ) {
        if (preview != null) {
            Image(
                bitmap = preview,
                contentDescription = choice.widgetLabel,
                contentScale = ContentScale.Fit,
            )
        }
    }
}

fun dropCell(
    coords: LayoutCoordinates?,
    window: Offset,
    page: Int,
    spec: WidgetGridSpec,
): Triple<Int, Int, Int>? {
    if (coords == null) return null
    val local = coords.windowToLocal(window)
    return DropPolicy.cellTarget(
        page,
        local.x,
        local.y,
        coords.size.width.toFloat(),
        coords.size.height.toFloat(),
        spec,
    )
}

fun hitRemoveWell(well: LayoutCoordinates?, window: Offset): Boolean {
    if (well == null) return false
    val local = well.windowToLocal(window)
    return local.x in 0f..well.size.width.toFloat() && local.y in 0f..well.size.height.toFloat()
}

fun edgePageDelta(windowX: Float, pageWidthPx: Float, current: Int, pageCount: Int): Int {
    return when {
        windowX > pageWidthPx - 96f && current < pageCount - 1 -> 1
        windowX < 96f && current > 1 -> -1
        else -> 0
    }
}
