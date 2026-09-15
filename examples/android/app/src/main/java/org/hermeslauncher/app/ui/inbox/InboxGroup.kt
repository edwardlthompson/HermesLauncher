package org.hermeslauncher.app.ui.inbox

import android.content.pm.PackageManager
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.MotionPrefs
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm
import org.hermeslauncher.app.vault.InboxAppGroup
import org.hermeslauncher.app.vault.InboxDisplay
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
        resolved ?: if (group.packageName.isBlank()) unknown else inboxAppLabel(pm, group.packageName)
    }
    val launch = showGroupLaunch(expanded, group.packageName)
    val reduced = MotionPrefs.reduced(LocalContext.current)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(SpacingSm)
                .then(if (MotionPrefs.animateSize(reduced)) Modifier.animateContentSize() else Modifier),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            onClick = onToggle,
                            onClickLabel = stringResource(
                                if (expanded) R.string.inbox_group_collapse else R.string.inbox_group_expand,
                                label,
                            ),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InboxAppGlyph(
                        packageName = group.packageName,
                        size = 40.dp,
                        contentDescription = null,
                    )
                    Text(
                        text = "$label (${group.items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (InboxDisplay.groupBold(group.items)) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = SpacingMd),
                    )
                }
                if (launch) {
                    IconButton(
                        onClick = { group.items.firstOrNull()?.let { onOpenItem(it.id) } },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = stringResource(R.string.inbox_group_open, label),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
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

internal fun showGroupLaunch(expanded: Boolean, packageName: String): Boolean {
    return expanded && packageName.isNotBlank()
}

internal fun inboxAppLabel(pm: PackageManager, packageName: String): String {
    if (packageName.isBlank()) {
        return ""
    }
    return runCatching {
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName.substringAfterLast('.'))
}
