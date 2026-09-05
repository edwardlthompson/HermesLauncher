package org.hermeslauncher.app.l3

import android.view.View
import android.view.ViewGroup
import com.android.launcher3.BubbleTextView
import com.android.launcher3.Launcher
import org.hermeslauncher.app.HermesWorkspace
import org.hermeslauncher.app.workspace.LabsFlags
import org.hermeslauncher.app.workspace.ScrollMode

object L3Chrome {
    fun apply(
        launcher: Launcher,
        showLabels: Boolean,
        showDots: Boolean,
        labs: LabsFlags,
        scrollMode: ScrollMode,
        labelShadow: Boolean,
    ) {
        val workspace = launcher.workspace as? HermesWorkspace
        workspace?.applyMotion(labs.wrap, labs.overlap, scrollMode == ScrollMode.INVERSE)
        walk(launcher.workspace) { view ->
            val bubble = view as? BubbleTextView ?: return@walk
            bubble.setTextVisibility(showLabels)
            bubble.setForceHideDot(!showDots)
            if (labelShadow) {
                bubble.setShadowLayer(bubble.textSize / 12f, 0f, 1f, 0x80000000.toInt())
            } else {
                bubble.setShadowLayer(0f, 0f, 0f, 0)
            }
        }
        walk(launcher.hotseat) { view ->
            val bubble = view as? BubbleTextView ?: return@walk
            bubble.setTextVisibility(showLabels)
            bubble.setForceHideDot(!showDots)
        }
    }

    private fun walk(root: View?, onView: (View) -> Unit) {
        if (root == null) {
            return
        }
        onView(root)
        val group = root as? ViewGroup ?: return
        for (i in 0 until group.childCount) {
            walk(group.getChildAt(i), onView)
        }
    }
}
