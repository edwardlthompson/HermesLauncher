package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun DockBar(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = SpacingSm,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingMd),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Filled.Apps,
                    contentDescription = stringResource(R.string.launcher_drawer_open),
                )
                Text(
                    text = stringResource(R.string.launcher_dock_label),
                    modifier = Modifier.padding(start = SpacingSm),
                )
            }
        }
    }
}
