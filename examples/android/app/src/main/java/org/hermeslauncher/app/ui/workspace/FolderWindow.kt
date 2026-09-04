package org.hermeslauncher.app.ui.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.hermeslauncher.app.R
import org.hermeslauncher.app.icons.IconPackId
import org.hermeslauncher.app.icons.LaunchableApp
import org.hermeslauncher.app.ui.launcher.AppIconImage
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.workspace.FolderInfo
import org.hermeslauncher.app.workspace.FolderLid

@Composable
fun FolderWindow(
    info: FolderInfo,
    pack: IconPackId,
    fullscreen: Boolean,
    onFullscreen: (Boolean) -> Unit,
    onApp: (LaunchableApp) -> Unit,
    onDismiss: () -> Unit,
) {
    val title = info.title.ifBlank { stringResource(R.string.folder_untitled) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = !fullscreen),
    ) {
        Surface(
            modifier = if (fullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            shadowElevation = SpacingMd,
        ) {
            Column(modifier = Modifier.padding(SpacingMd), verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(R.string.folder_fullscreen))
                Switch(checked = fullscreen, onCheckedChange = onFullscreen)
                val apps = FolderLid.preview(info.contents, limit = info.contents.size)
                if (apps.isEmpty()) {
                    Text(text = stringResource(R.string.folder_empty), style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(apps, key = { "${it.packageName}/${it.activityName}" }) { app ->
                            Column(
                                modifier = Modifier
                                    .padding(SpacingMd)
                                    .clickable { onApp(app) },
                            ) {
                                AppIconImage(app = app, pack = pack, modifier = Modifier.size(48.dp))
                                Text(text = app.label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
