package org.hermeslauncher.app.display

/** Physical display mode used to pick the fastest same-resolution option. */
data class DisplayModeChoice(
    val modeId: Int,
    val widthPx: Int,
    val heightPx: Int,
    val refreshHz: Float,
)

object DisplayModeSelector {
    /**
     * Highest refresh rate among modes that match [current] pixel size.
     * Returns null when [modes] is empty so callers leave the window unchanged.
     */
    fun fastestSameResolution(
        modes: List<DisplayModeChoice>,
        current: DisplayModeChoice,
    ): DisplayModeChoice? {
        val sameSize = modes.filter { it.widthPx == current.widthPx && it.heightPx == current.heightPx }
        return sameSize.maxByOrNull { it.refreshHz }
    }
}
