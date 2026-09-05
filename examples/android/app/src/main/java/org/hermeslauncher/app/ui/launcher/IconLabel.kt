package org.hermeslauncher.app.ui.launcher

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.hermeslauncher.app.ui.theme.LocalLabelShadow

@Composable
fun IconLabel(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
) {
    val shadow = LocalLabelShadow.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            shadow = if (shadow) {
                Shadow(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f),
                    offset = Offset(0f, 1f),
                    blurRadius = 2f,
                )
            } else {
                null
            },
        ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
