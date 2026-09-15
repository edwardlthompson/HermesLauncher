package org.hermeslauncher.app.l3

import com.android.launcher3.BubbleTextView
import com.android.launcher3.icons.DotRenderer

object L3Badge {
    fun paintsDesktopIcons(): Boolean = true

    fun hideDot(showDots: Boolean): Boolean = !showDots

    fun applyColor(bubble: BubbleTextView, color: Int?) {
        if (color == null) {
            return
        }
        runCatching {
            val field = BubbleTextView::class.java.getDeclaredField("mDotParams")
            field.isAccessible = true
            val params = field.get(bubble) as? DotRenderer.DrawParams ?: return
            params.dotColor = color
            bubble.invalidate()
        }
    }
}
