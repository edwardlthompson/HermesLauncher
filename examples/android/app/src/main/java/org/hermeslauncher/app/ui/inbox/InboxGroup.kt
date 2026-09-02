package org.hermeslauncher.app.ui.inbox

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm
import org.hermeslauncher.app.vault.InboxAppGroup
import java.io.File

@Composable
fun InboxGroup(
    group: InboxAppGroup,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismissGroup: () -> Unit,
    onDismissItem: (String) -> Unit,
    onOpenItem: (String) -> Unit,
    onAction: (String, Int) -> Unit,
    onPin: (String) -> Unit,
    imageDir: File,
    showDismiss: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val pm = LocalContext.current.packageManager
    val unknown = stringResource(R.string.inbox_unknown_app)
    val raw = group.displayLabel
    val resolved = raw?.let { categoryLabel(it) }
    val label = remember(group.packageName, resolved, unknown) {
        resolved ?: if (group.packageName.isBlank()) unknown else appLabel(pm, group.packageName)
    }
    val bitmap = remember(group.packageName) {
        if (group.packageName.isBlank()) {
            null
        } else {
            runCatching {
                pm.getApplicationIcon(group.packageName).toBitmap(width = 96, height = 96).asImageBitmap()
            }.getOrNull()
        }
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        ),
    ) {
        Column(modifier = Modifier.padding(SpacingSm)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onToggle,
                        onClickLabel = stringResource(R.string.inbox_group_expand, label),
                    )
                    .padding(SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = label,
                        modifier = Modifier.size(40.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Apps,
                        contentDescription = label,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = "$label (${group.items.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = SpacingMd),
                )
                if (showDismiss) {
                    IconButton(onClick = onDismissGroup) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.inbox_group_dismiss, label),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            if (expanded) {
                group.items.forEach { item ->
                    VaultItemCard(
                        item = item,
                        imageDir = imageDir,
                        showDismiss = showDismiss,
                        onDismiss = { onDismissItem(item.id) },
                        onPin = { onPin(item.id) },
                        onOpen = { onOpenItem(item.id) },
                        onAction = { index -> onAction(item.id, index) },
                        modifier = Modifier.padding(bottom = SpacingSm),
                    )
                }
            }
        }
    }
}

private fun appLabel(pm: PackageManager, packageName: String): String {
    if (packageName.isBlank()) {
        return ""
    }
    return runCatching {
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName.substringAfterLast('.'))
}
