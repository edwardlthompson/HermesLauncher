package org.hermeslauncher.app.ui.workspace

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import org.hermeslauncher.app.widgets.WidgetGridSpec
import org.hermeslauncher.app.workspace.DesktopItem

@Composable
fun CellLayoutGrid(
    spec: WidgetGridSpec,
    items: List<DesktopItem>,
    modifier: Modifier = Modifier,
    content: @Composable (DesktopItem, Modifier) -> Unit,
) {
    val grid = spec.clamped()
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val cellW = maxWidth / grid.columns
        val cellH = maxHeight / grid.rows
        items.forEach { item ->
            val w = cellW * item.spanX.coerceAtLeast(1)
            val h = cellH * item.spanY.coerceAtLeast(1)
            val xPx = with(density) { (cellW * item.cellX).roundToPx() }
            val yPx = with(density) { (cellH * item.cellY).roundToPx() }
            content(
                item,
                Modifier
                    .offset { IntOffset(xPx, yPx) }
                    .size(w, h),
            )
        }
    }
}
