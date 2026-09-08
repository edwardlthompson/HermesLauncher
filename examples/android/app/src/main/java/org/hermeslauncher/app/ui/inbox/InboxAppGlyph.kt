package org.hermeslauncher.app.ui.inbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.IconPackResources
import org.hermeslauncher.app.icons.IconPlate

@Composable
fun InboxAppGlyph(
    packageName: String,
    size: Dp,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val pack by app.iconPackStore.pack.collectAsStateWithLifecycle(IconPackId())
    val px = with(LocalDensity.current) { size.roundToPx().coerceAtLeast(1) }
    val bitmap = remember(packageName, pack.packageName, IconPlate.color, px) {
        if (packageName.isBlank()) {
            null
        } else {
            runCatching {
                val drawable = IconPackResources.visibleIcon(context, pack, packageName)
                    ?: context.packageManager.getApplicationIcon(packageName)
                drawable.toBitmap(width = px, height = px).asImageBitmap()
            }.getOrNull()
        }
    }
    val sized = modifier.size(size)
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = contentDescription, modifier = sized)
    } else {
        Icon(
            imageVector = Icons.Filled.Apps,
            contentDescription = contentDescription,
            modifier = sized,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
