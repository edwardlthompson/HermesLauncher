package org.hermeslauncher.app.widgets

object WidgetHostCodec {
    private const val V1 = "v1"
    private const val V2 = "v2"
    private const val V3 = "v3"
    private const val V4 = "v4"

    fun encode(state: WidgetHostState): String {
        val pages = state.pages.joinToString(";") { page ->
            val slots = page.bindings.joinToString(",") { binding ->
                val provider = binding.providerFlattened.orEmpty().replace("|", "/")
                "${binding.appWidgetId}:$provider:${binding.cellsW}:${binding.cellsH}:${binding.cellX}:${binding.cellY}"
            }
            "${page.pageIndex}=$slots"
        }
        return "$V4|${state.grid.encoded()}|$pages"
    }

    fun decode(raw: String): WidgetHostState {
        val trimmed = raw.trim()
        val version = when {
            trimmed.startsWith("$V4|") -> V4
            trimmed.startsWith("$V3|") -> V3
            trimmed.startsWith("$V2|") -> V2
            trimmed.startsWith("$V1|") -> V1
            else -> return WidgetHostState()
        }
        val body = trimmed.removePrefix("$version|")
        if (body.isEmpty()) {
            return WidgetHostState()
        }
        val grid: WidgetGridSpec
        val pagesBody: String
        if (version == V4) {
            val split = body.indexOf('|')
            if (split <= 0) return WidgetHostState()
            grid = WidgetGridSpec.parse(body.substring(0, split))
            pagesBody = body.substring(split + 1)
        } else {
            grid = WidgetGridSpec.DEFAULT
            pagesBody = body
        }
        if (pagesBody.isEmpty()) {
            return WidgetHostState(grid = grid)
        }
        val pages = pagesBody.split(";").mapNotNull { parsePage(it, version) }
        val state = if (pages.isEmpty()) {
            WidgetHostState(grid = grid)
        } else {
            WidgetHostState(pages.sortedBy { it.pageIndex }, grid)
        }
        return WidgetGrid.withTrailingEmpty(state)
    }

    private fun parsePage(chunk: String, version: String): WidgetPageState? {
        val eq = chunk.indexOf('=')
        if (eq <= 0) {
            return null
        }
        val index = chunk.substring(0, eq).toIntOrNull() ?: return null
        if (index < 1) {
            return null
        }
        val slotRaw = chunk.substring(eq + 1)
        if (slotRaw.isEmpty()) {
            return WidgetPageState(index)
        }
        val parsed = slotRaw.split(",").mapNotNull { slot -> parseBinding(slot) }
        val bindings = if (version == V1 || version == V2) WidgetGrid.stackedFromV2(parsed) else parsed
        return WidgetPageState(index, bindings)
    }

    private fun parseBinding(slot: String): WidgetBinding? {
        val parts = slot.split(":")
        val id = parts.getOrNull(0)?.toIntOrNull() ?: return null
        if (!WidgetBindPolicy.canRecord(id)) {
            return null
        }
        val provider = parts.getOrNull(1)?.takeIf { it.isNotEmpty() }
        val width = (parts.getOrNull(2)?.toIntOrNull() ?: WidgetBinding.PLACE_CELLS)
            .coerceIn(WidgetBinding.MIN_CELLS, WidgetGridSpec.MAX_AXIS)
        val height = (parts.getOrNull(3)?.toIntOrNull() ?: WidgetBinding.PLACE_CELLS_H)
            .coerceIn(WidgetBinding.MIN_CELLS, WidgetGridSpec.MAX_AXIS)
        val cellX = parts.getOrNull(4)?.toIntOrNull() ?: 0
        val cellY = parts.getOrNull(5)?.toIntOrNull() ?: 0
        return WidgetBinding(id, provider, width, height, cellX, cellY)
    }
}
