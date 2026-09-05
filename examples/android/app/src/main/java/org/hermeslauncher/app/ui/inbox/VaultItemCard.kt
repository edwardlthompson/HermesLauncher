package org.hermeslauncher.app.ui.inbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toBitmap
import org.hermeslauncher.app.R
import org.hermeslauncher.app.vault.ShadeBridge
import org.hermeslauncher.app.vault.VaultImageStore
import org.hermeslauncher.app.vault.VaultItem
import org.hermeslauncher.app.vault.VaultPreview
import java.io.File

@Composable
fun VaultItemCard(
    item: VaultItem,
    imageDir: File,
    onDismiss: () -> Unit,
    onPin: () -> Unit,
    onOpen: () -> Unit,
    onAction: (Int) -> Unit,
    showDismiss: Boolean = true,
    showSource: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val pm = LocalContext.current.packageManager
    val unknown = stringResource(R.string.inbox_unknown_app)
    val sourceName = remember(item.packageName, showSource, unknown) {
        if (!showSource) {
            ""
        } else if (item.packageName.isBlank()) {
            unknown
        } else {
            inboxAppLabel(pm, item.packageName).ifBlank { unknown }
        }
    }
    val sourceIcon = remember(item.packageName, showSource) {
        if (!showSource || item.packageName.isBlank()) {
            null
        } else {
            runCatching {
                pm.getApplicationIcon(item.packageName).toBitmap(width = 72, height = 72).asImageBitmap()
            }.getOrNull()
        }
    }
    val preview = VaultPreview.parse(item.extrasJson)
    InboxCard(
        title = item.title.orEmpty(),
        body = preview.body(item.text),
        caption = preview.caption(),
        sourceName = sourceName,
        sourceIcon = sourceIcon,
        imageFile = VaultImageStore.file(imageDir, preview.imageRef),
        pinned = item.pinned,
        actions = ShadeBridge.actions(item.sbnKey),
        onDismiss = onDismiss,
        onPin = onPin,
        onOpen = onOpen,
        onAction = onAction,
        showDismiss = showDismiss,
        modifier = modifier,
    )
}
