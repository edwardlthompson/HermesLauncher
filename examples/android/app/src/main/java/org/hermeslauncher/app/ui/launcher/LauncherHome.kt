package org.hermeslauncher.app.ui.launcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.hermeslauncher.app.launcher.DrawerState
import org.hermeslauncher.app.launcher.HomePagerState

@Composable
fun LauncherHome(modifier: Modifier = Modifier) {
    val pagerModel = remember { HomePagerState() }
    val pagerState = rememberPagerState(pageCount = { pagerModel.pageCount })
    var drawer by remember { mutableStateOf(DrawerState()) }

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            if (page == 0) {
                FeedPage()
            } else {
                WidgetPlaceholderPage(pageNumber = page)
            }
        }
        DockBar(onOpenDrawer = { drawer = drawer.opened() })
        if (drawer.open) {
            AppDrawerStub(onClose = { drawer = drawer.closed() })
        }
    }
}
