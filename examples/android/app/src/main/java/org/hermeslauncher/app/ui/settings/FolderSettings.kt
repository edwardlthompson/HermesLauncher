package org.hermeslauncher.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.hermeslauncher.app.HermesApplication
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.workspace.FolderPreviewKind
import org.hermeslauncher.app.workspace.FolderSnapshot

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as HermesApplication
    val scope = rememberCoroutineScope()
    val snapshot by app.folderPrefs.snapshot.collectAsStateWithLifecycle(FolderSnapshot())
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.folder_preview))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            FolderPreviewKind.entries.forEach { kind ->
                FilterChip(
                    selected = snapshot.preview == kind,
                    onClick = { scope.launch { app.folderPrefs.setPreview(kind) } },
                    label = { Text(stringResource(previewLabel(kind))) },
                )
            }
        }
        ListItem(
            headlineContent = { Text(stringResource(R.string.folder_fullscreen)) },
            trailingContent = {
                Switch(
                    checked = snapshot.fullscreen,
                    onCheckedChange = { value -> scope.launch { app.folderPrefs.setFullscreen(value) } },
                )
            },
        )
    }
}

private fun previewLabel(kind: FolderPreviewKind): Int = when (kind) {
    FolderPreviewKind.STACK -> R.string.folder_preview_stack
    FolderPreviewKind.GRID -> R.string.folder_preview_grid
    FolderPreviewKind.FAN -> R.string.folder_preview_fan
}
