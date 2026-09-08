package org.hermeslauncher.app.workspace

import android.content.Context
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_DESKTOP
import com.android.launcher3.LauncherState
import com.android.launcher3.Workspace
import org.hermeslauncher.app.R

/** Drag/drop page math so reserved Podcasts/News/Inbox stay empty. */
object HermesDragPages {
    fun ids(ws: Workspace<*>): List<Int> =
        (0 until ws.pageCount).map { ws.getScreenIdForPageIndex(it) }

    fun droppable(locked: Boolean, screenIds: List<Int>, page: Int, extra: Int): Int {
        if (!locked && page in screenIds.indices) return page
        return EmptyPagePolicy.nearestDroppable(screenIds, page, extra)
    }

    fun droppable(locked: Boolean, ws: Workspace<*>, page: Int): Int {
        return droppable(locked, ids(ws), page, EmptyPagePolicy.EXTRA_EMPTY_SCREEN_ID)
    }

    fun stayPage(ws: Workspace<*>, page: Int): Int {
        return EmptyPagePolicy.stayOnDroppable(ids(ws), page)
    }

    fun step(ws: Workspace<*>, delta: Int): Int {
        return EmptyPagePolicy.step(ids(ws), ws.nextPage, delta, wrap = false)
    }

    fun refuseDesktop(container: Int, screenId: Int): Boolean {
        return container == CONTAINER_DESKTOP && HermesScreens.isReserved(screenId)
    }

    fun pageLock(dragLock: Boolean, launcher: Launcher): Boolean {
        if (dragLock) return true
        return launcher.isInState(LauncherState.SPRING_LOADED) ||
            AbstractFloatingView.hasOpenView(launcher, AbstractFloatingView.TYPE_WIDGET_RESIZE_FRAME)
    }

    fun homeIndex(ws: Workspace<*>): Int {
        val inbox = ws.getPageIndexForScreenId(HermesScreens.INBOX)
        return if (inbox >= 0) inbox else HermesScreens.homePageIndex(ws.pageCount)
    }

    fun pageDescription(ctx: Context, screenId: Int, fallback: String): String {
        val res = when (screenId) {
            HermesScreens.PODCASTS -> R.string.launcher_page_podcasts
            HermesScreens.NEWS -> R.string.launcher_page_news
            HermesScreens.INBOX -> R.string.launcher_page_feed
            EmptyPagePolicy.EXTRA_EMPTY_SCREEN_ID -> R.string.launcher_page_new
            else -> 0
        }
        if (res != 0) return ctx.getString(res)
        return fallback.takeIf { it.isNotBlank() && it != "null" }
            ?: ctx.getString(R.string.launcher_page_new)
    }
}
