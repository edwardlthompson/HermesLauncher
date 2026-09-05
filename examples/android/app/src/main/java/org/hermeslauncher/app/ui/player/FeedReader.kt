package org.hermeslauncher.app.ui.player

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hermeslauncher.app.R
import org.hermeslauncher.app.feeds.ArticleBlock
import org.hermeslauncher.app.feeds.ArticleExtract
import org.hermeslauncher.app.feeds.ArticleOpen
import org.hermeslauncher.app.feeds.ArticleStamp
import org.hermeslauncher.app.feeds.ArticleThumb
import org.hermeslauncher.app.feeds.FeedFetcher
import org.hermeslauncher.app.feeds.FeedFull
import org.hermeslauncher.app.feeds.FeedItem
import org.hermeslauncher.app.feeds.FeedShare
import org.hermeslauncher.app.feeds.ReaderMode
import org.hermeslauncher.app.feeds.ReaderScale
import org.hermeslauncher.app.feeds.ReaderTts
import org.hermeslauncher.app.ui.theme.SpacingMd

private val Rule = 2.dp

@Composable
fun FeedReader(
    item: FeedItem,
    starred: Boolean,
    onClose: () -> Unit,
    onStar: () -> Unit,
    onUnread: () -> Unit,
    onViewed: () -> Unit,
    onPrev: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    downloadImages: Boolean = true,
    bodyScale: Float = ReaderScale.DEFAULT,
    onBodyScale: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val closeCd = stringResource(R.string.feed_reader_close)
    val openCd = stringResource(R.string.feed_reader_browser)
    val starCd = stringResource(if (starred) R.string.feed_reader_unstar else R.string.feed_reader_star)
    val unreadCd = stringResource(R.string.feed_reader_unread)
    val shareCd = stringResource(R.string.feed_reader_share)
    val prevCd = stringResource(R.string.feed_reader_previous)
    val nextCd = stringResource(R.string.feed_reader_next)
    val url = item.articleUrl()
    var modeName by rememberSaveable { mutableStateOf(ReaderMode.READING.name) }
    val mode = ReaderMode.entries.firstOrNull { it.name == modeName } ?: ReaderMode.READING
    var blocks by remember(item.id, mode) { mutableStateOf<List<ArticleBlock>>(emptyList()) }
    var loading by remember(item.id, mode) { mutableStateOf(mode != ReaderMode.WEB) }
    var findOpen by remember { mutableStateOf(false) }
    var find by remember { mutableStateOf("") }
    var speaking by remember { mutableStateOf(false) }
    val tts = remember { ReaderTts(context) }
    val scroll = rememberScrollState()
    val published = ArticleStamp.format(item.publishedAt)
    val density = LocalDensity.current
    var navH by remember { mutableIntStateOf(0) }
    BackHandler(onBack = onClose)
    DisposableEffect(item.id) {
        onDispose { tts.stop() }
    }
    DisposableEffect(Unit) {
        onDispose { tts.shutdown() }
    }
    LaunchedEffect(item.id) {
        onViewed()
        scroll.scrollTo(0)
        tts.stop()
        speaking = false
    }
    LaunchedEffect(item.id, mode, item.html, url) {
        if (mode == ReaderMode.WEB) {
            loading = false
            blocks = emptyList()
            return@LaunchedEffect
        }
        loading = true
        blocks = withContext(Dispatchers.IO) { loadBlocks(mode, item, context.filesDir) }
        loading = false
    }
    val barPad = with(density) { (if (navH > 0) navH else 160).toDp() }
    val shareIntent = url?.let { FeedShare.intent(item.title, it) }
    Surface(
        modifier = modifier.fillMaxSize().consumeWindowInsets(WindowInsets(0, 0, 0, 0)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                ReaderTopBar(
                    title = item.title,
                    published = published,
                    closeCd = closeCd,
                    starCd = starCd,
                    unreadCd = unreadCd,
                    openCd = openCd,
                    starred = starred,
                    url = url,
                    onClose = onClose,
                    onStar = onStar,
                    onUnread = onUnread,
                    shareCd = shareCd,
                    onShare = shareIntent?.let { send ->
                        {
                            context.startActivity(android.content.Intent.createChooser(send, shareCd))
                        }
                    },
                    overflow = {
                        ReaderOverflow(
                            onTts = {
                                val text = blocks.filterIsInstance<ArticleBlock.Text>().joinToString("\n") { it.value }
                                if (speaking) {
                                    tts.stop()
                                    speaking = false
                                } else {
                                    tts.speak(text)
                                    speaking = true
                                }
                            },
                            ttsPlaying = speaking,
                            onFind = { findOpen = !findOpen },
                            onScale = {
                                val next = if (bodyScale >= 1.4f) ReaderScale.MIN else bodyScale + 0.15f
                                onBodyScale(ReaderScale.clamp(next))
                            },
                            onEnclosure = item.fileEnclosure()?.let { file ->
                                { ArticleOpen.openBrowser(context, file) }
                            },
                        )
                    },
                )
                if (findOpen) {
                    ReaderFindField(query = find, onQuery = { find = it })
                }
                HorizontalDivider(thickness = Rule, color = MaterialTheme.colorScheme.inverseSurface)
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = barPad),
                ) {
                    ReaderBody(
                        mode = mode,
                        url = url,
                        loading = loading,
                        blocks = blocks,
                        itemId = item.id,
                        dir = context.filesDir,
                        download = downloadImages,
                        scroll = scroll,
                        scale = bodyScale,
                        find = find,
                    )
                }
            }
            ReaderNavBar(
                prevCd = prevCd,
                nextCd = nextCd,
                onPrev = onPrev,
                onNext = onNext,
                mode = mode,
                onMode = { modeName = it.name },
                modifier = Modifier.align(Alignment.BottomCenter).onGloballyPositioned { navH = it.size.height },
            )
        }
    }
}

@Composable
private fun ReaderBody(
    mode: ReaderMode,
    url: String?,
    loading: Boolean,
    blocks: List<ArticleBlock>,
    itemId: String,
    dir: java.io.File,
    download: Boolean,
    scroll: androidx.compose.foundation.ScrollState,
    scale: Float,
    find: String,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            mode == ReaderMode.WEB && url != null -> ReaderWeb(url = url, find = find, modifier = Modifier.fillMaxSize())
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).padding(SpacingMd))
            blocks.isEmpty() -> Text(text = stringResource(R.string.feed_reader_empty), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(SpacingMd))
            else -> Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(SpacingMd)) {
                blocks.forEachIndexed { index, block ->
                    when (block) {
                        is ArticleBlock.Text -> if (ReaderFind.matches(block.value, find)) {
                            Text(
                                text = block.value,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * scale,
                                ),
                                modifier = Modifier.padding(bottom = SpacingMd),
                            )
                        }
                        is ArticleBlock.Image -> ReaderImage(id = "$itemId-$index", url = block.url, dir = dir, download = download)
                    }
                }
            }
        }
    }
}

private fun loadBlocks(mode: ReaderMode, item: FeedItem, dir: java.io.File): List<ArticleBlock> {
    val cached = FeedFull.load(dir, item.id)
    val html = when (mode) {
        ReaderMode.READING -> item.html?.takeIf { it.length >= 40 } ?: cached
            ?: item.articleUrl()?.let { runCatching { FeedFetcher.fetchXml(it) }.getOrNull() }
        ReaderMode.FULL -> cached ?: item.articleUrl()?.let { runCatching { FeedFetcher.fetchXml(it) }.getOrNull() }
            ?: item.html
        ReaderMode.WEB -> null
    }
    return when {
        html.isNullOrBlank() -> emptyList()
        else -> ArticleExtract.blocks(html).ifEmpty {
            listOf(ArticleBlock.Text(ArticleExtract.fromRss(html, null)))
        }
    }
}

@Composable
private fun ReaderImage(id: String, url: String, dir: java.io.File, download: Boolean) {
    var bmp by remember(id, url) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(id, url, download) {
        bmp = withContext(Dispatchers.IO) { ArticleThumb.article(dir, id, url, download = download) }
    }
    val image = bmp ?: return
    Image(
        bitmap = image.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier.fillMaxWidth().padding(bottom = SpacingMd),
    )
}
