package org.hermeslauncher.app.ui.inbox

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier,
) {
    val preview = VaultPreview.parse(item.extrasJson)
    InboxCard(
        title = item.title.orEmpty(),
        body = preview.body(item.text),
        caption = preview.caption(),
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
