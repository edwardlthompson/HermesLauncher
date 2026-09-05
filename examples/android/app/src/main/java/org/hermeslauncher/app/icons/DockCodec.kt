package org.hermeslauncher.app.icons

object DockCodec {
    private const val V1 = "v1"
    private const val V2 = "v2"
    private const val V3 = "v3"

    fun encode(layout: DockLayout): String {
        val slots = layout.assigned.entries.sortedBy { it.key }.joinToString(",") { (index, app) ->
            "$index:${app.packageName}:${app.activityName}"
        }
        val mode = if (layout.mode == DockMode.CUSTOM) "custom" else "usage"
        return "$V3|${layout.slotCount}|$mode|${layout.pageCount}|$slots"
    }

    fun decode(raw: String): DockLayout {
        val trimmed = raw.trim()
        return when {
            trimmed.startsWith("$V3|") -> decodeV3(trimmed)
            trimmed.startsWith("$V2|") -> decodeV2(trimmed)
            trimmed.startsWith("$V1|") -> decodeV1(trimmed)
            else -> DockLayout()
        }
    }

    private fun decodeV3(trimmed: String): DockLayout {
        val parts = trimmed.split("|")
        val count = parts.getOrNull(1)?.toIntOrNull() ?: DockLayout.DEFAULT_SLOTS
        val mode = if (parts.getOrNull(2) == "custom") DockMode.CUSTOM else DockMode.USAGE
        val pages = HotseatPolicy.pageCount(parts.getOrNull(3)?.toIntOrNull() ?: HotseatPolicy.MIN_PAGES)
        return DockLayout(
            slotCount = count.coerceIn(DockLayout.MIN_SLOTS, DockLayout.MAX_SLOTS),
            assigned = parseSlots(parts.getOrNull(4).orEmpty()),
            mode = mode,
            pageCount = pages,
        )
    }

    private fun decodeV2(trimmed: String): DockLayout {
        val parts = trimmed.split("|")
        val count = parts.getOrNull(1)?.toIntOrNull() ?: DockLayout.DEFAULT_SLOTS
        val mode = if (parts.getOrNull(2) == "custom") DockMode.CUSTOM else DockMode.USAGE
        return DockLayout(
            slotCount = count.coerceIn(DockLayout.MIN_SLOTS, DockLayout.MAX_SLOTS),
            assigned = parseSlots(parts.getOrNull(3).orEmpty()),
            mode = mode,
        )
    }

    private fun decodeV1(trimmed: String): DockLayout {
        val parts = trimmed.split("|")
        val count = parts.getOrNull(1)?.toIntOrNull() ?: DockLayout.DEFAULT_SLOTS
        val assigned = parseSlots(parts.getOrNull(2).orEmpty())
        val mode = if (assigned.isEmpty()) DockMode.USAGE else DockMode.CUSTOM
        return DockLayout(
            slotCount = count.coerceIn(DockLayout.MIN_SLOTS, DockLayout.MAX_SLOTS),
            assigned = assigned,
            mode = mode,
        )
    }

    private fun parseSlots(raw: String): Map<Int, LaunchableApp> {
        val assigned = mutableMapOf<Int, LaunchableApp>()
        raw.split(",").forEach { slot ->
            val bits = slot.split(":")
            if (bits.size >= 3) {
                val index = bits[0].toIntOrNull() ?: return@forEach
                assigned[index] = LaunchableApp(bits[1], bits[2], bits[1])
            }
        }
        return assigned
    }
}
