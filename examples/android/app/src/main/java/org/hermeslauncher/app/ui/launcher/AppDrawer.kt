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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.DrawerSnapshot
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
    assignTitle: Int = R.string.dock_assign_title,
    snapshot: DrawerSnapshot = DrawerSnapshot(),
    draggingOut: Boolean = false,
    onIconDragStart: (LaunchableApp, Offset) -> Unit = { _, _ -> },
    onIconDrag: (Offset) -> Unit = {},
    onIconDragEnd: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = if (draggingOut) 0f else 1f
                translationY = if (draggingOut) 4000f else 0f
            },
        color = if (draggingOut) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        var tab by remember { mutableIntStateOf(0) }
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
                        if (assignMode) assignTitle else R.string.launcher_drawer_title,
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
            if (!assignMode) {
                TabRow(selectedTabIndex = tab) {
                    Tab(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        text = { Text(stringResource(R.string.launcher_drawer_title)) },
                    )
                    Tab(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        text = { Text(stringResource(R.string.drawer_predicted)) },
                    )
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AllAppsGrid(
                    apps = if (tab == 0 || assignMode) apps else emptyList(),
                    predicted = predicted,
                    pack = pack,
                    onApp = onApp,
                    unreadByPackage = unreadByPackage,
                    showDots = showDots,
                    columns = snapshot.columns,
                    listMode = snapshot.listMode,
                    showRail = snapshot.showRail && (tab == 0 || assignMode),
                    dragEnabled = !assignMode,
                    onIconDragStart = onIconDragStart,
                    onIconDrag = onIconDrag,
                    onIconDragEnd = onIconDragEnd,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
