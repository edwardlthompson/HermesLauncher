package org.hermeslauncher.app.l3

import android.content.Context
import com.android.launcher3.InvariantDeviceProfile
import org.hermeslauncher.app.widgets.WidgetGridSpec

/** Sync SharedPreferences pin so IDP grid survives process death before LoaderTask. */
object HermesGridPin {
    private const val PREFS = "hermes_grid_pin"
    private const val KEY_COLS = "hermes_grid_cols"
    private const val KEY_ROWS = "hermes_grid_rows"

    fun write(context: Context, spec: WidgetGridSpec) {
        val grid = spec.clamped()
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_COLS, grid.columns)
            .putInt(KEY_ROWS, grid.rows)
            .commit()
    }

    fun read(context: Context): WidgetGridSpec? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_COLS) || !prefs.contains(KEY_ROWS)) {
            return null
        }
        val cols = prefs.getInt(KEY_COLS, 0)
        val rows = prefs.getInt(KEY_ROWS, 0)
        if (cols <= 0 || rows <= 0) {
            return null
        }
        return WidgetGridSpec(cols, rows).clamped()
    }

    /** Call from Application.onCreate before any LoaderTask can start. */
    fun applyPinned(context: Context) {
        val grid = read(context) ?: return
        InvariantDeviceProfile.setHermesGrid(grid.columns, grid.rows)
    }
}
