package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.DockLayout
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.ui.theme.RadiusLg
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun DockBar(
    layout: DockLayout,
    pack: IconPackId = IconPackId(),
    custom: Boolean = false,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onAssign: (Int) -> Unit,
    modifier: Modifier = Modifier,
    unreadByPackage: Map<String, Int> = emptyMap(),
    showDots: Boolean = true,
) {
    var popup by remember { mutableStateOf<LaunchableApp?>(null) }
    val dockPager = rememberPagerState(pageCount = { layout.pageCount })
    LaunchedEffect(layout.pageCount) {
        val max = (layout.pageCount - 1).coerceAtLeast(0)
        if (dockPager.currentPage > max) {
            dockPager.scrollToPage(max)
        }
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -24f) {
                        onOpenDrawer()
                    }
                }
            },
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        shape = RoundedCornerShape(RadiusLg),
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMd, vertical = SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalPager(
                    state = dockPager,
                    modifier = Modifier.weight(1f),
                ) { page ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        for (index in 0 until layout.slotCount) {
                            val app = if (page == 0) layout.slot(index) else null
                            DockSlot(
                                app = app,
                                pack = pack,
                                unread = if (showDots && app != null) unreadByPackage[app.packageName] ?: 0 else 0,
                                onLaunch = { app?.let(onLaunch) },
                                onAssign = { if (custom && page == 0) onAssign(index) },
                                onShortcuts = { chosen -> popup = chosen },
                            )
                        }
                    }
                }
                ContrastDockIcon(
                    imageVector = Icons.Filled.Apps,
                    contentDescription = stringResource(R.string.launcher_drawer_open),
                    onClick = onOpenDrawer,
                )
                ContrastDockIcon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings_open),
                    onClick = onOpenSettings,
                )
            }
            ShortcutPopup(app = popup, onDismiss = { popup = null })
        }
    }
}

@Composable
private fun ContrastDockIcon(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.inverseSurface,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DockSlot(
    app: LaunchableApp?,
    pack: IconPackId,
    unread: Int,
    onLaunch: () -> Unit,
    onAssign: () -> Unit,
    onShortcuts: (LaunchableApp) -> Unit,
) {
    val label = app?.label ?: stringResource(R.string.dock_empty_slot)
    Box(
        modifier = Modifier
            .size(48.dp)
            .semantics { contentDescription = label }
            .combinedClickable(
                onClick = if (app == null) onAssign else onLaunch,
                onLongClick = { if (app != null) onShortcuts(app) else onAssign() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (app != null) {
            AppIconImage(
                app = app,
                pack = pack,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
            UnreadDot(
                count = unread,
                description = stringResource(R.string.dock_unread_count, unread),
                modifier = Modifier.align(Alignment.TopEnd),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Apps,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
