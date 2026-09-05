package org.hermeslauncher.app.launcher

object GestureCodec {
    fun encodeMap(map: Map<GestureSlot, LauncherAction>): String {
        return GestureSlot.entries.joinToString(";") { slot ->
            "${slot.name}=${GestureMap.encode(map[slot] ?: GestureMap.defaults().getValue(slot))}"
        }
    }

    fun decodeMap(raw: String?): Map<GestureSlot, LauncherAction> {
        val base = GestureMap.defaults().toMutableMap()
        if (raw.isNullOrBlank()) {
            return base
        }
        raw.split(";").forEach { chunk ->
            val eq = chunk.indexOf('=')
            if (eq <= 0) {
                return@forEach
            }
            val slot = GestureSlot.entries.firstOrNull { it.name == chunk.substring(0, eq).uppercase() }
                ?: return@forEach
            base[slot] = GestureMap.parse(chunk.substring(eq + 1))
        }
        return base
    }
}
