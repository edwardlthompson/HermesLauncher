package org.hermeslauncher.app.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.ui.theme.SpacingSm

enum class PageIndicatorStyle {
    DOTS,
    LINE,
    NONE,
}

@Composable
fun PageIndicator(
    count: Int,
    current: Int,
    style: PageIndicatorStyle = PageIndicatorStyle.DOTS,
    modifier: Modifier = Modifier,
) {
    if (style == PageIndicatorStyle.NONE || count < 1) {
        return
    }
    val safe = current.coerceIn(0, (count - 1).coerceAtLeast(0))
    val label = stringResource(R.string.workspace_page_indicator, safe + 1, count)
    val active = MaterialTheme.colorScheme.primary
    val idle = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = label }
            .padding(vertical = SpacingSm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (style) {
            PageIndicatorStyle.DOTS -> repeat(count) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (index == safe) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (index == safe) active else idle),
                )
            }
            PageIndicatorStyle.LINE -> Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(idle),
            ) {
                val fraction = ((safe + 1f) / count.coerceAtLeast(1)).coerceIn(0.12f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(3.dp)
                        .align(Alignment.CenterStart)
                        .background(active),
                )
            }
            PageIndicatorStyle.NONE -> Unit
        }
    }
}
