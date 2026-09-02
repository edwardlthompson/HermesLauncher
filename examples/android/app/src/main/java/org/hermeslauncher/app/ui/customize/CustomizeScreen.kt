package org.hermeslauncher.app.ui.customize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.DockLayout
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.IconPackResolver
import org.hermeslauncher.app.ui.theme.SpacingMd

@Composable
fun CustomizeScreen(
    pack: IconPackId = IconPackId(),
    dock: DockLayout = DockLayout(),
    modifier: Modifier = Modifier,
) {
    val packLabel = if (pack.isSystem) {
        stringResource(R.string.chrome_icon_pack_system)
    } else {
        pack.packageName.orEmpty()
    }
    Column(
        modifier = modifier.padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(
            text = stringResource(R.string.chrome_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.chrome_icon_pack, packLabel),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.chrome_dock_slots, dock.slotCount),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.chrome_pack_key_hint, IconPackResolver.SYSTEM_PACK),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
