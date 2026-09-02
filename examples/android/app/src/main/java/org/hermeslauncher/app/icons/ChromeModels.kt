package org.hermeslauncher.app.icons

data class LaunchableApp(
    val packageName: String,
    val activityName: String,
    val label: String,
)

data class IconPackId(
    val packageName: String? = null,
) {
    val isSystem: Boolean
        get() = packageName.isNullOrBlank()
}

enum class DockMode {
    USAGE,
    CUSTOM,
}

data class DockLayout(
    val slotCount: Int = DEFAULT_SLOTS,
    val assigned: Map<Int, LaunchableApp> = emptyMap(),
    val mode: DockMode = DockMode.USAGE,
) {
    init {
        require(slotCount in MIN_SLOTS..MAX_SLOTS) { "slotCount must be $MIN_SLOTS..$MAX_SLOTS" }
    }

    fun slot(index: Int): LaunchableApp? {
        if (index !in 0 until slotCount) {
            return null
        }
        return assigned[index]
    }

    fun withApp(index: Int, app: LaunchableApp): DockLayout {
        if (index !in 0 until slotCount) {
            return this
        }
        return copy(assigned = assigned + (index to app), mode = DockMode.CUSTOM)
    }

    fun fillSlots(apps: List<LaunchableApp>): DockLayout {
        val next = apps.take(slotCount).mapIndexed { index, app -> index to app }.toMap()
        return copy(assigned = next)
    }

    companion object {
        const val MIN_SLOTS: Int = 1
        const val MAX_SLOTS: Int = 8
        const val DEFAULT_SLOTS: Int = 5
    }
}
