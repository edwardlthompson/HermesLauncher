package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.LaunchableApp
import kotlin.math.roundToInt

@Composable
fun HomeIconDrag(
    app: LaunchableApp?,
    pack: IconPackId,
    dragWindow: Offset,
    rootCoords: LayoutCoordinates?,
) {
    if (app == null) {
        return
    }
    val local = rootCoords?.windowToLocal(dragWindow) ?: Offset.Zero
    AppIconImage(
        app = app,
        pack = pack,
        contentDescription = stringResource(R.string.drawer_drop_icon, app.label),
        modifier = Modifier
            .offset {
                IntOffset(
                    (local.x - 24.dp.toPx()).roundToInt(),
                    (local.y - 24.dp.toPx()).roundToInt(),
                )
            }
            .size(48.dp),
    )
}
