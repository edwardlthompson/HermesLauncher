package org.hermeslauncher.app.ui.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.ui.launcher.AppIconImage
import org.hermeslauncher.app.ui.launcher.IconLabel
import org.hermeslauncher.app.ui.launcher.UnreadDot
import org.hermeslauncher.app.workspace.FolderInfo
import org.hermeslauncher.app.workspace.FolderLid
import org.hermeslauncher.app.workspace.FolderPreviewKind

@Composable
fun FolderIcon(
    info: FolderInfo,
    pack: IconPackId,
    unreadByPackage: Map<String, Int>,
    showLabels: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preview = FolderLid.preview(info.contents)
    val badge = FolderLid.badge(unreadByPackage, info.contents)
    val title = info.title.ifBlank { stringResource(R.string.folder_untitled) }
    val announce = if (badge > 0) {
        stringResource(R.string.folder_badge, title, badge)
    } else {
        title
    }
    Column(
        modifier = modifier
            .semantics { contentDescription = announce }
            .clickable(onClick = onOpen),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            FolderLidPreview(apps = preview, pack = pack, kind = info.preview)
            UnreadDot(
                count = badge,
                description = stringResource(R.string.folder_badge, title, badge),
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
        if (showLabels) {
            IconLabel(text = title)
        }
    }
}

@Composable
internal fun FolderLidPreview(
    apps: List<LaunchableApp>,
    pack: IconPackId,
    kind: FolderPreviewKind,
    modifier: Modifier = Modifier,
) {
    if (apps.isEmpty()) {
        Box(modifier = modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.folder_empty), style = MaterialTheme.typography.labelSmall)
        }
        return
    }
    val shown = apps.take(if (kind == FolderPreviewKind.STACK) 3 else 4)
    Row(modifier = modifier) {
        shown.forEach { app ->
            AppIconImage(app = app, pack = pack, modifier = Modifier.size(16.dp).padding(1.dp))
        }
    }
}
