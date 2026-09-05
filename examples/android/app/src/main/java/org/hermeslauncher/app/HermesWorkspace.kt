package org.hermeslauncher.app

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import com.android.launcher3.DropTarget.DragObject
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.Workspace
import com.android.launcher3.pageindicators.WorkspacePageIndicator
import org.hermeslauncher.app.l3.L3GestureHost
import org.hermeslauncher.app.l3.L3Pinch
import org.hermeslauncher.app.workspace.HermesPages
import org.hermeslauncher.app.workspace.HermesScreens

/** Workspace that keeps News/Inbox as real pages and snaps Home to Inbox. */
class HermesWorkspace @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int = 0,
) : Workspace<WorkspacePageIndicator>(context, attrs, defStyle) {
    private val taps = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                L3GestureHost.onDoubleTap(Launcher.getLauncher(context))
                return true
            }
        },
    )
    private val pinch = L3Pinch(context)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        taps.onTouchEvent(ev)
        if (!HermesScreens.isReserved(getScreenIdForPageIndex(nextPage))) {
            pinch.onTouch(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun applyMotion(wrap: Boolean, overlap: Boolean, inverse: Boolean) {
        setWrapPages(wrap)
        setEnableOverscroll(!wrap)
        val gap = if (overlap) -(24f * resources.displayMetrics.density).toInt() else 0
        setPageSpacing(gap)
        setInvertScroll(inverse)
    }
    override fun moveToDefaultScreen() {
        val page = homeIndex()
        val launcher = Launcher.getLauncher(context)
        if (launcher.isInState(LauncherState.NORMAL) && nextPage != page) {
            snapToPage(page)
        }
        getChildAt(page)?.requestFocus()
    }

    override fun acceptDrop(d: DragObject): Boolean {
        if (!HermesScreens.canDrop(getScreenIdForPageIndex(nextPage))) {
            return false
        }
        return super.acceptDrop(d)
    }

    override fun removeAllWorkspaceScreens() {
        super.removeAllWorkspaceScreens()
        HermesPages.ensure(this)
    }

    override fun stripEmptyScreens() {
        super.stripEmptyScreens()
        HermesPages.ensure(this)
    }

    override fun getPageDescription(page: Int): String {
        return when (getScreenIdForPageIndex(page)) {
            HermesScreens.PODCASTS -> context.getString(R.string.launcher_page_podcasts)
            HermesScreens.NEWS -> context.getString(R.string.launcher_page_news)
            HermesScreens.INBOX -> context.getString(R.string.launcher_page_feed)
            else -> super.getPageDescription(page)
        }
    }

    fun homeIndex(): Int {
        val inbox = getPageIndexForScreenId(HermesScreens.INBOX)
        return if (inbox >= 0) inbox else HermesScreens.homePageIndex(pageCount)
    }
}
