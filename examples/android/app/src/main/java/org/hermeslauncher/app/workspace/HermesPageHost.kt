package org.hermeslauncher.app.workspace

import android.content.Context
import android.widget.FrameLayout

/** Full-bleed Workspace host. CellLayout otherwise pads like a centered icon. */
class HermesPageHost(context: Context) : FrameLayout(context) {
    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(0, 0, 0, 0)
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(0, 0, 0, 0)
    }
}
