package org.hermeslauncher.app.ui.player

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.ArticleImages
import org.hermeslauncher.app.feeds.ArticleStamp
import org.hermeslauncher.app.feeds.ArticleThumb
import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.feeds.FeedKind
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedCard(
    item: FeedItem,
    kind: FeedKind,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit = {},
    onStar: () -> Unit = {},
    onToggleRead: () -> Unit = {},
    onShare: () -> Unit = {},
    onPlayNext: (() -> Unit)? = null,
    read: Boolean = true,
    starred: Boolean = false,
    thumbDir: File? = null,
    showThumb: Boolean = true,
    downloadThumb: Boolean = true,
) {
    val canPlay = kind == FeedKind.EPISODE && !item.enclosureUrl.isNullOrBlank()
    val canOpen = item.articleUrl() != null || !item.html.isNullOrBlank()
    val openCd = stringResource(R.string.feed_open_article)
    val imageUrl = item.imageUrl ?: ArticleImages.firstFromHtml(item.html.orEmpty())
    var thumb by remember(item.id, imageUrl) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(item.id, imageUrl, thumbDir, showThumb, downloadThumb) {
        if (!showThumb) {
            thumb = null
            return@LaunchedEffect
        }
        val dir = thumbDir ?: return@LaunchedEffect
        thumb = withContext(Dispatchers.IO) {
            ArticleThumb.preview(dir, item.id, imageUrl, download = downloadThumb)
        }
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (canOpen) {
                    Modifier
                        .semantics { contentDescription = openCd }
                        .combinedClickable(
                            onClick = onOpen,
                            onLongClick = {
                                if (canPlay && onPlayNext != null) onPlayNext() else onShare()
                            },
                        )
                } else {
                    Modifier
                },
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        ),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val bmp = thumb
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(SpacingSm)
                        .width(88.dp)
                        .height(88.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.title.ifBlank { stringResource(R.string.player_untitled) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (read) FontWeight.Normal else FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(SpacingMd),
                    )
                    Icon(
                        imageVector = if (starred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = stringResource(if (starred) R.string.feed_reader_unstar else R.string.feed_reader_star),
                        modifier = Modifier
                            .padding(end = SpacingMd, top = SpacingMd)
                            .clickable(onClick = onStar),
                    )
                }
                val published = ArticleStamp.format(item.publishedAt)
                if (published.isNotBlank()) {
                    Text(
                        text = published,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = SpacingMd),
                    )
                }
                Text(
                    text = item.feedTitle,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = SpacingMd),
                )
                if (canPlay) {
                    TextButton(onClick = onPlay) {
                        Text(stringResource(R.string.player_play))
                    }
                }
            }
        }
    }
}
