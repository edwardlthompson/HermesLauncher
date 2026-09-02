package org.hermeslauncher.app.widgets

data class WidgetGridSpec(
    val columns: Int = DEFAULT_COLUMNS,
    val rows: Int = DEFAULT_ROWS,
) {
    fun clamped(): WidgetGridSpec = WidgetGridSpec(
        columns.coerceIn(MIN_AXIS, MAX_AXIS),
        rows.coerceIn(MIN_AXIS, MAX_AXIS),
    )

    fun encoded(): String = "${clamped().columns}x${clamped().rows}"

    companion object {
        const val MIN_AXIS: Int = 3
        const val MAX_AXIS: Int = 12
        const val DEFAULT_COLUMNS: Int = 4
        const val DEFAULT_ROWS: Int = 5
        val DEFAULT: WidgetGridSpec = WidgetGridSpec()
        val PRESETS: List<WidgetGridSpec> = listOf(
            WidgetGridSpec(4, 5),
            WidgetGridSpec(5, 5),
            WidgetGridSpec(6, 6),
            WidgetGridSpec(8, 8),
        )

        fun parse(raw: String): WidgetGridSpec {
            val match = AXIS.find(raw.trim()) ?: return DEFAULT
            val cols = match.groupValues[1].toIntOrNull() ?: DEFAULT_COLUMNS
            val rows = match.groupValues[2].toIntOrNull() ?: DEFAULT_ROWS
            return WidgetGridSpec(cols, rows).clamped()
        }

        private val AXIS = Regex("""(\d+)\s*[x×]\s*(\d+)""")
    }
}
