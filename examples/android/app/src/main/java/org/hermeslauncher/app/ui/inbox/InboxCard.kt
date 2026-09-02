package org.hermeslauncher.app.ui.inbox

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.RadiusMd
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm
import org.hermeslauncher.app.vault.ShadeAction
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InboxCard(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    onPin: () -> Unit = {},
    onOpen: () -> Unit = {},
    actions: List<ShadeAction> = emptyList(),
    onAction: (Int) -> Unit = {},
    pinned: Boolean = false,
    caption: String = "",
    imageFile: File? = null,
    showDismiss: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(imageFile?.path, imageFile?.length()) {
        imageFile?.takeIf { it.isFile }?.let { BitmapFactory.decodeFile(it.absolutePath) }
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        ),
    ) {
        Column(modifier = Modifier.padding(SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            onClick = onOpen,
                            onClickLabel = stringResource(R.string.inbox_open),
                        )
                        .padding(end = SpacingSm),
                ) {
                    Text(
                        text = title.ifBlank { stringResource(R.string.inbox_untitled) },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (caption.isNotBlank()) {
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (body.isNotBlank()) {
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onPin, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = stringResource(R.string.inbox_pin),
                        tint = if (pinned) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                if (showDismiss) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.inbox_dismiss),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            if (actions.isNotEmpty()) {
                FlowRow(modifier = Modifier.padding(top = SpacingSm)) {
                    actions.forEach { action ->
                        TextButton(onClick = { onAction(action.index) }) {
                            Text(action.title)
                        }
                    }
                }
            }
            if (bitmap != null) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SpacingSm)
                        .clip(RoundedCornerShape(RadiusMd)),
                ) {
                    val maxH = 480.dp
                    val ratio = bitmap.width.toFloat() / bitmap.height.coerceAtLeast(1).toFloat()
                    val fitted = maxWidth / ratio
                    val height = if (fitted > maxH) maxH else fitted
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.inbox_preview),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}
