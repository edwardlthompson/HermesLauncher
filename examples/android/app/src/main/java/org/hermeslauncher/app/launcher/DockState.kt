package org.hermeslauncher.app.launcher

/** Locked Sprint 1 dock model. Slots stay empty until icon-pack work. */
data class DockState(
    val slotCount: Int = DEFAULT_SLOT_COUNT,
    val visible: Boolean = true,
) {
    init {
        require(slotCount >= 1) { "slotCount must be at least 1" }
    }

    companion object {
        const val DEFAULT_SLOT_COUNT: Int = 5
    }
}
