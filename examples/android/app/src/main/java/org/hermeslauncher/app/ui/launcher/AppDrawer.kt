package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun AppDrawer(
    apps: List<LaunchableApp>,
    predicted: List<LaunchableApp>,
    assignMode: Boolean,
    pack: IconPackId,
    onApp: (LaunchableApp) -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier,
    unreadByPackage: Map<String, Int> = emptyMap(),
    showDots: Boolean = true,
    onOpenSearch: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onOpenSearch) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(R.string.drawer_search),
                    )
                }
                Text(
                    text = stringResource(
                        if (assignMode) R.string.dock_assign_title else R.string.launcher_drawer_title,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onAbout) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = stringResource(R.string.about_open),
                    )
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AllAppsGrid(
                    apps = apps,
                    predicted = predicted,
                    pack = pack,
                    onApp = onApp,
                    unreadByPackage = unreadByPackage,
                    showDots = showDots,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
