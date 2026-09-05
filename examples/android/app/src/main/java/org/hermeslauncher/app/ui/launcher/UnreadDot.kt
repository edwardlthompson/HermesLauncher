package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hermeslauncher.app.ui.theme.BadgeStyle
import org.hermeslauncher.app.ui.theme.LocalBadgeColorArgb
import org.hermeslauncher.app.ui.theme.LocalBadgeStyle
import org.hermeslauncher.app.ui.theme.badgeColorOrNull
import org.hermeslauncher.app.vault.InboxFilter

@Composable
fun UnreadDot(
    count: Int,
    description: String,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) return
    val style = LocalBadgeStyle.current
    val tint = badgeColorOrNull(LocalBadgeColorArgb.current) ?: MaterialTheme.colorScheme.error
    val onTint = if (tint == MaterialTheme.colorScheme.error) {
        MaterialTheme.colorScheme.onError
    } else {
        Color.White
    }
    if (style == BadgeStyle.DOTS) {
        Box(
            modifier = modifier
                .size(10.dp)
                .background(tint, CircleShape)
                .semantics { contentDescription = description },
        )
        return
    }
    Box(
        modifier = modifier
            .defaultMinSize(18.dp, 18.dp)
            .background(tint, CircleShape)
            .padding(horizontal = 4.dp, vertical = 1.dp)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = InboxFilter.unreadLabel(count),
            color = onTint,
            fontSize = 9.sp,
            lineHeight = 10.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            ),
        )
    }
}
