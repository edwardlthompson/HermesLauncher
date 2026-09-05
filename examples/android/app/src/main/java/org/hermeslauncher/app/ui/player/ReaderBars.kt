package org.hermeslauncher.app.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.ArticleOpen
import org.hermeslauncher.app.feeds.ReaderMode
import org.hermeslauncher.app.ui.theme.SpacingSm

private val Rule = 2.dp

@Composable
fun ReaderTopBar(
    title: String,
    published: String,
    closeCd: String,
    starCd: String,
    unreadCd: String,
    openCd: String,
    starred: Boolean,
    url: String?,
    onClose: () -> Unit,
    onStar: () -> Unit,
    onUnread: () -> Unit,
    shareCd: String = "",
    onShare: (() -> Unit)? = null,
    overflow: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose, modifier = Modifier.semantics { contentDescription = closeCd }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = closeCd)
        }
        Column(modifier = Modifier.weight(1f).padding(end = SpacingSm)) {
            Text(
                text = title.ifBlank { stringResource(R.string.player_untitled) },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (published.isNotBlank()) {
                Text(
                    text = published,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        IconButton(onClick = onStar, modifier = Modifier.semantics { contentDescription = starCd }) {
            Icon(
                imageVector = if (starred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = starCd,
            )
        }
        IconButton(onClick = onUnread, modifier = Modifier.semantics { contentDescription = unreadCd }) {
            Icon(Icons.Filled.MarkEmailUnread, contentDescription = unreadCd)
        }
        if (onShare != null && url != null) {
            IconButton(onClick = onShare, modifier = Modifier.semantics { contentDescription = shareCd }) {
                Icon(Icons.Filled.Share, contentDescription = shareCd)
            }
        }
        if (url != null) {
            IconButton(
                onClick = { ArticleOpen.openBrowser(context, url) },
                modifier = Modifier.semantics { contentDescription = openCd },
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = openCd)
            }
        }
        overflow()
    }
}

@Composable
fun ReaderNavBar(
    prevCd: String,
    nextCd: String,
    onPrev: (() -> Unit)?,
    onNext: (() -> Unit)?,
    mode: ReaderMode,
    onMode: (ReaderMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 3.dp,
    ) {
        Column {
            HorizontalDivider(
                thickness = Rule,
                color = MaterialTheme.colorScheme.inverseSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingSm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { onPrev?.invoke() },
                    enabled = onPrev != null,
                    modifier = Modifier.semantics { contentDescription = prevCd },
                ) {
                    Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = prevCd)
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ReaderMode.entries.forEach { entry ->
                        val label = stringResource(modeLabel(entry))
                        val cd = stringResource(modeCd(entry))
                        FilterChip(
                            selected = mode == entry,
                            onClick = { onMode(entry) },
                            label = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f).semantics { contentDescription = cd },
                        )
                    }
                }
                IconButton(
                    onClick = { onNext?.invoke() },
                    enabled = onNext != null,
                    modifier = Modifier.semantics { contentDescription = nextCd },
                ) {
                    Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = nextCd)
                }
            }
        }
    }
}

private fun modeLabel(mode: ReaderMode): Int = when (mode) {
    ReaderMode.READING -> R.string.feed_reader_mode_reading
    ReaderMode.FULL -> R.string.feed_reader_mode_full
    ReaderMode.WEB -> R.string.feed_reader_mode_web
}

private fun modeCd(mode: ReaderMode): Int = when (mode) {
    ReaderMode.READING -> R.string.feed_reader_mode_reading_cd
    ReaderMode.FULL -> R.string.feed_reader_mode_full_cd
    ReaderMode.WEB -> R.string.feed_reader_mode_web_cd
}
