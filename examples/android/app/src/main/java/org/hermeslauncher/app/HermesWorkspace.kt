package org.hermeslauncher.app

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.android.launcher3.CellLayout
import com.android.launcher3.DropTarget.DragObject
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.Workspace
import com.android.launcher3.dragndrop.DragOptions
import com.android.launcher3.pageindicators.WorkspacePageIndicator
import org.hermeslauncher.app.l3.L3GestureHost
import org.hermeslauncher.app.l3.L3Pinch
import org.hermeslauncher.app.workspace.EmptyPagePolicy
import org.hermeslauncher.app.workspace.HermesDragPages
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
    private var wrapWanted = false
    private var dragLock = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        taps.onTouchEvent(ev)
        setWrapPages(wrapWanted && !pageLock())
        if (!HermesScreens.isReserved(getScreenIdForPageIndex(nextPage))) pinch.onTouch(ev)
        return super.dispatchTouchEvent(ev)
    }

    fun applyMotion(wrap: Boolean, overlap: Boolean, inverse: Boolean, snapMs: Int) {
        wrapWanted = wrap
        setWrapPages(wrap && !pageLock())
        setEnableOverscroll(!wrap)
        setPageSpacing(if (overlap) -(24f * resources.displayMetrics.density).toInt() else 0)
        setInvertScroll(inverse)
        setPageSnapAnimationDuration(snapMs)
    }

    override fun moveToDefaultScreen() {
        val page = homeIndex()
        if (Launcher.getLauncher(context).isInState(LauncherState.NORMAL) && nextPage != page) {
            setCurrentPage(page)
        }
        getChildAt(page)?.requestFocus()
    }

    override fun onDragStart(dragObject: DragObject, options: DragOptions) {
        dragLock = true
        setWrapPages(false)
        keepDroppableEmpty()
        super.onDragStart(dragObject, options)
        val extra = getPageIndexForScreenId(EXTRA_EMPTY_SCREEN_ID)
        if (extra >= 0 && !HermesScreens.canDrop(getScreenIdForPageIndex(nextPage))) snapToPage(extra)
    }

    override fun onDragEnd() {
        super.onDragEnd()
        Launcher.getLauncher(context).dragLayer.clearAnimatedView()
        postDelayed({ unlockDrag() }, 1000)
    }

    override fun setCurrentPage(currentPage: Int, overridePrevPage: Int) =
        super.setCurrentPage(droppable(currentPage), overridePrevPage)

    override fun snapToPage(whichPage: Int, duration: Int, immediate: Boolean) =
        super.snapToPage(droppable(whichPage), duration, immediate)

    override fun scrollLeft() = if (pageLock()) step(-1) else super.scrollLeft()
    override fun scrollRight() = if (pageLock()) step(1) else super.scrollRight()

    override fun addInScreen(child: View, container: Int, screenId: Int, x: Int, y: Int, spanX: Int, spanY: Int) {
        if (HermesDragPages.refuseDesktop(container, screenId)) return
        super.addInScreen(child, container, screenId, x, y, spanX, spanY)
    }

    override fun acceptDrop(d: DragObject): Boolean {
        keepDroppableEmpty()
        dropToLayout?.let { layout -> retargetDropLayout(retargetOccupiedDropLayout(layout)) }
        return super.acceptDrop(d)
    }

    override fun retargetOccupiedDropLayout(layout: CellLayout?): CellLayout? {
        if (layout == null || layout === Launcher.getLauncher(context).hotseat) return layout
        val hovered = getIdForScreen(layout)
        val land = EmptyPagePolicy.landingScreenId(hovered, getScreenIdForPageIndex(nextPage), EXTRA_EMPTY_SCREEN_ID)
        if (land == hovered) return layout
        keepDroppableEmpty()
        return getScreenWithId(land) ?: layout
    }

    override fun removeAllWorkspaceScreens() {
        super.removeAllWorkspaceScreens(); keepDroppableEmpty()
    }

    override fun stripEmptyScreens() {
        super.stripEmptyScreens(); keepDroppableEmpty()
    }

    override fun removeExtraEmptyScreenDelayed(delay: Int, stripEmptyScreens: Boolean, onComplete: Runnable?) {
        super.removeExtraEmptyScreenDelayed(delay, stripEmptyScreens) {
            keepDroppableEmpty()
            unlockDrag()
            onComplete?.run()
        }
    }

    fun keepDroppableEmpty() {
        HermesPages.ensure(this); addExtraEmptyScreens()
    }

    override fun getPageDescription(page: Int) =
        HermesDragPages.pageDescription(context, getScreenIdForPageIndex(page), super.getPageDescription(page))

    fun homeIndex() = HermesDragPages.homeIndex(this)

    private fun pageLock() = HermesDragPages.pageLock(dragLock, Launcher.getLauncher(context))

    private fun droppable(page: Int) = HermesDragPages.droppable(pageLock(), this, page)

    private fun unlockDrag() {
        dragLock = false
        setCurrentPage(HermesDragPages.stayPage(this, nextPage))
        setWrapPages(wrapWanted)
    }

    private fun step(delta: Int): Boolean {
        val next = HermesDragPages.step(this, delta)
        return next != nextPage && snapToPage(next)
    }
}
